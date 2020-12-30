package com.imooc.seckill.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.imooc.seckill.controller.viewmodel.OrderViewModel;
import com.imooc.seckill.error.BusinessError;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.mq.MqProducer;
import com.imooc.seckill.response.CommonResponseType;
import com.imooc.seckill.service.EventService;
import com.imooc.seckill.service.GoodService;
import com.imooc.seckill.service.OrderService;
import com.imooc.seckill.service.model.OrderModel;
import com.imooc.seckill.service.model.UserModel;
import com.imooc.seckill.utils.VerificationCodeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class OrderController extends BaseController{
    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private GoodService goodService;

    @Autowired
    private EventService eventService;

    private ExecutorService executorService;

    private RateLimiter createOrderRateLimiter;
    private int createOrderTpsThreshold = 200;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(20);

        createOrderRateLimiter = RateLimiter.create(createOrderTpsThreshold);
    }

    @RequestMapping(value = "/verificationcode", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public void generateVerificationCode(HttpServletResponse response) throws BusinessException, IOException {
        // check user login token
        String clientToken = request.getParameterMap().get("token").length > 0 ? request.getParameterMap().get("token")[0] : "";
        if (StringUtils.isEmpty(clientToken)) {
            throw new BusinessException(BusinessError.USER_NOT_LOGIN, "user does not login");
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(clientToken);
        if (userModel == null) {
            throw new BusinessException(BusinessError.USER_NOT_LOGIN, "user does not login and cannot generate verification code");
        }
        Map<String, Object> verificationCodeMap = VerificationCodeUtil.generateQrCode();
        System.out.println("generated verification code: " + verificationCodeMap.get("code"));
        String verificationCodeRedisKey = "verification_code_" + userModel.getId();
        redisTemplate.opsForValue().set(verificationCodeRedisKey, verificationCodeMap.get("code"));
        redisTemplate.expire(verificationCodeRedisKey, 10, TimeUnit.MINUTES);
        ImageIO.write((RenderedImage) verificationCodeMap.get("picture"), "jpeg", response.getOutputStream());
    }

    @RequestMapping(value = "/token", method = { RequestMethod.POST }, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonResponseType generateEventAccessToken(
        @RequestParam(name = "goodId", required = true) Integer goodId,
        @RequestParam(name = "eventId", required = true) Integer eventId,
        @RequestParam(name = "verificationCode", required = true) String verificationCode
    ) throws BusinessException {
        // check user login token
        String clientToken = request.getParameterMap().get("token").length > 0 ? request.getParameterMap().get("token")[0] : "";
        if (StringUtils.isEmpty(clientToken)) {
            throw new BusinessException(BusinessError.USER_NOT_LOGIN, "user does not login");
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(clientToken);
        if (userModel == null) {
            throw new BusinessException(BusinessError.USER_NOT_LOGIN, "user does not login");
        }

        // verify verification code
        String verificationCodeRedisKey = "verification_code_" + userModel.getId();
        String cachedVerificationCode = (String) redisTemplate.opsForValue().get(verificationCodeRedisKey);
        if (StringUtils.isEmpty(cachedVerificationCode)) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "invalid operation");
        }
        if (!cachedVerificationCode.equals(verificationCode)) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "wrong verification code");
        }

        // generate event access token
        String eventToken = eventService.generateEventAccessToken(eventId, goodId, userModel.getId());
        if (eventToken == null) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "event access token creation failure");
        }
        return CommonResponseType.newInstance(eventToken);
    }

    @RequestMapping(value = "/post", method = { RequestMethod.POST }, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonResponseType createOrder(
            @RequestParam(name = "goodId") Integer goodId,
            @RequestParam(name = "amount") Integer amount,
            @RequestParam(name = "eventId", required = false) Integer eventId,
            @RequestParam(name = "eventToken", required = false) String eventToken
    ) throws BusinessException {
        // check TPS limit threshold
        if (!createOrderRateLimiter.tryAcquire()) {
            throw new BusinessException(BusinessError.RATE_LIMIT);
        }

        String clientToken = request.getParameterMap().get("token").length > 0 ? request.getParameterMap().get("token")[0] : "";
        if (StringUtils.isEmpty(clientToken)) {
            throw new BusinessException(BusinessError.USER_NOT_LOGIN, "user does not login");
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(clientToken);
        if (userModel == null) {
            throw new BusinessException(BusinessError.USER_NOT_LOGIN, "user does not login");
        }

        // check event access token if applicable
        if (eventId != null) {
            String eventAccessTokenRedisKey =
                    "event_token_" + eventId.toString() + ",userId_" + userModel.getId().toString() + "_goodId_" + goodId.toString();
            String cachedToken = (String) redisTemplate.opsForValue().get(eventAccessTokenRedisKey);
            if (cachedToken == null || !org.apache.commons.lang3.StringUtils.equals(cachedToken, eventToken)) {
                throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "invalid event access token");
            }
        }
        
        // call submit method from thread pool in a sync way
        // which implements a congestion window with size 20
        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                // added transaction history init status into DB
                String logId = goodService.initTransactionHistoryLog(goodId, amount);

                // start order creation async operation
                if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), goodId, eventId, amount, logId)) {
                    throw new BusinessException(BusinessError.UNKNOWN_ERROR, "create order failed");
                }
                return null;
            }
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new BusinessException(BusinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new BusinessException(BusinessError.UNKNOWN_ERROR);
        }

        return CommonResponseType.newInstance(null);
    }

    private OrderViewModel convertViewModelFromDataModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderViewModel orderViewModel = new OrderViewModel();
        BeanUtils.copyProperties(orderModel, orderViewModel);
        return orderViewModel;
    }
}
