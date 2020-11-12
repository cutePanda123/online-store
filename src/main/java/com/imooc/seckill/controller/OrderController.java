package com.imooc.seckill.controller;

import com.imooc.seckill.controller.viewmodel.GoodViewModel;
import com.imooc.seckill.controller.viewmodel.OrderViewModel;
import com.imooc.seckill.error.BusinessError;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.response.CommonResponseType;
import com.imooc.seckill.service.OrderService;
import com.imooc.seckill.service.model.GoodModel;
import com.imooc.seckill.service.model.OrderModel;
import com.imooc.seckill.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class OrderController extends BaseController{
    @Autowired
    private OrderService orderService;

    @Autowired
    HttpServletRequest request;

    @RequestMapping(value = "/postorder", method = RequestMethod.POST)
    public CommonResponseType createOrder(@RequestParam(name = "itemId") Integer itemId, @RequestParam(name = "amount") Integer amount) throws BusinessException {
        Boolean isLoggedIn = (Boolean)request.getSession().getAttribute("IS_LOGIN");
        if (isLoggedIn == null || !isLoggedIn.booleanValue()) {
            throw new BusinessException(BusinessError.USER_NOT_LOGIN, "user does not login");
        }
        UserModel userModel = (UserModel)request.getSession().getAttribute("LOGIN_USER");
        OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, amount);
        OrderViewModel orderViewModel = convertViewModelFromDataModel(orderModel);
        return CommonResponseType.newInstance(orderViewModel);
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
