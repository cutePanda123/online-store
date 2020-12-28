package com.imooc.seckill.controller;

import com.imooc.seckill.controller.viewmodel.GoodViewModel;
import com.imooc.seckill.controller.viewmodel.OrderViewModel;
import com.imooc.seckill.error.BusinessError;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.mq.MqProducer;
import com.imooc.seckill.response.CommonResponseType;
import com.imooc.seckill.service.EventService;
import com.imooc.seckill.service.GoodService;
import com.imooc.seckill.service.OrderService;
import com.imooc.seckill.service.model.GoodModel;
import com.imooc.seckill.service.model.OrderModel;
import com.imooc.seckill.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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

    @RequestMapping(value = "/token", method = { RequestMethod.POST }, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonResponseType generateEventAccessToken(
        @RequestParam(name = "goodId", required = true) Integer goodId,
        @RequestParam(name = "eventId", required = true) Integer eventId
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

        // added transaction history init status into DB
        String logId = goodService.initTransactionHistoryLog(goodId, amount);

        // start order creation async operation
        if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), goodId, eventId, amount, logId)) {
            throw new BusinessException(BusinessError.UNKNOWN_ERROR, "create order failed");
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
