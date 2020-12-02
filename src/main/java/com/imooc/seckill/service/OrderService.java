package com.imooc.seckill.service;

import com.imooc.seckill.entity.Order;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.service.model.OrderModel;

public interface OrderService {
    public OrderModel createOrder(Integer userId, Integer goodId, Integer eventId, Integer amount) throws BusinessException;
}
