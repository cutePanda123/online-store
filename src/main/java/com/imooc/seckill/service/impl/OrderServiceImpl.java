package com.imooc.seckill.service.impl;

import com.imooc.seckill.dao.OrderMapper;
import com.imooc.seckill.dao.SequenceMapper;
import com.imooc.seckill.dao.TransactionHistoryMapper;
import com.imooc.seckill.entity.Good;
import com.imooc.seckill.entity.Order;
import com.imooc.seckill.entity.Sequence;
import com.imooc.seckill.entity.TransactionHistory;
import com.imooc.seckill.error.BusinessError;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.mq.MqProducer;
import com.imooc.seckill.service.GoodService;
import com.imooc.seckill.service.OrderService;
import com.imooc.seckill.service.UserService;
import com.imooc.seckill.service.model.GoodModel;
import com.imooc.seckill.service.model.OrderModel;
import com.imooc.seckill.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private GoodService goodService;

    @Autowired
    private UserService userService;

    @Autowired
    private SequenceMapper sequenceMapper;

    @Autowired
    private TransactionHistoryMapper transactionHistoryMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer goodId, Integer eventId, Integer amount, String transactionLogId) throws BusinessException {
        // validate input
        GoodModel good = goodService.getGoodByIdFromCache(goodId);
        if (good == null) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "good does not exist");
        }
        UserModel userModel = userService.getUserByIdFromCache(userId);
        if (userModel == null) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "user does not exist");
        }

        if (amount < 0 || amount > 100) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "invalid amount");
        }

        // verify event information
        if (eventId != null) {
            if (eventId != good.getEventModel().getId()) {
                throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "event does not have this good");
            }
            if (good.getEventModel().getStatus() != 2) {
                throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "not started event");
            }
        }

        // reduce good stock and increase sales in stock and good table before insert order to order table
        if (!goodService.reduceStock(goodId, amount)) {
            throw new BusinessException(BusinessError.STOCK_NOT_ENOUGH);
        }

        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setGoodId(goodId);
        orderModel.setAmount(amount);
        if (eventId != null) {
            orderModel.setGoodPrice(good.getEventModel().getDealPrice());
        } else {
            orderModel.setGoodPrice(good.getPrice());
        }
        orderModel.setEventId(eventId);
        orderModel.setOrderPrice(orderModel.getGoodPrice().multiply(new BigDecimal(amount)));
        orderModel.setId(generateOrderId());

        // insert order to order table
        Order order = covertFromOrderModel(orderModel);
        orderMapper.insertSelective(order);

        // increase sales
        goodService.increaseSales(goodId, amount);

        // set transaction history log state to COMMIT
        TransactionHistory history = transactionHistoryMapper.selectByPrimaryKey(transactionLogId);
        if (history == null) {
            throw new BusinessException(BusinessError.UNKNOWN_ERROR);
        }
        history.setState(2);
        transactionHistoryMapper.updateByPrimaryKeySelective(history);

        return orderModel;
    }

    private Order covertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        Order order = new Order();
        BeanUtils.copyProperties(orderModel, order);
        return order;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String generateOrderId() {
        // order id : date + auto increment sequence + partition key
        StringBuffer buffer = new StringBuffer();
        LocalDateTime time = LocalDateTime.now();
        String date = time.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        buffer.append(date);

        Sequence sequence = sequenceMapper.getSequenceByName("order_info");
        int sequenceVal = sequence.getCurrentVal();
        for (int i = 0; i < 6 - String.valueOf(sequenceVal).length(); ++i) {
            buffer.append("0");
        }
        buffer.append(sequenceVal);

        // when the sequence reaches the maximal value, it should start over from 0
        sequence.setCurrentVal(sequenceVal + sequence.getStep());
        sequenceMapper.updateByPrimaryKeySelective(sequence);

        // last two bits are reserved for table partition in the future
        String partitionKey = "00";
        buffer.append(partitionKey);
        return buffer.toString();
    }
}
