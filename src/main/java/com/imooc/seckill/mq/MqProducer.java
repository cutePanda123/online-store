package com.imooc.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.imooc.seckill.dao.TransactionHistoryMapper;
import com.imooc.seckill.entity.TransactionHistory;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.service.OrderService;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class MqProducer {
    private DefaultMQProducer producer;
    private TransactionMQProducer transactionMQProducer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TransactionHistoryMapper transactionHistoryMapper;

    @PostConstruct
    public void init() throws MQClientException {
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        //producer.setSendMsgTimeout(60000);
        producer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object args) {
                Integer userId = (Integer) ((Map)args).get("userId");
                Integer eventId = (Integer) ((Map)args).get("eventId");
                Integer goodId = (Integer) ((Map)args).get("goodId");
                Integer amount = (Integer) ((Map)args).get("amount");
                String transactionLogId = (String) ((Map)args).get("transactionLogId");
                try {
                    orderService.createOrder(userId, goodId, eventId, amount, transactionLogId);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    TransactionHistory history = transactionHistoryMapper.selectByPrimaryKey(transactionLogId);
                    history.setState(3);
                    transactionHistoryMapper.updateByPrimaryKeySelective(history);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                String jsonStr = new String(messageExt.getBody());
                System.out.printf("received message: %s%n", jsonStr);
                Map<String, Object> payload = JSON.parseObject(jsonStr, Map.class);
                Integer goodId = (Integer) payload.get("goodId");
                Integer amount = (Integer) payload.get("amount");
                String transactionLogId = (String) payload.get("transactionLogId");
                TransactionHistory history = transactionHistoryMapper.selectByPrimaryKey(transactionLogId);
                if (history == null || history.getState() == 1) {
                    return LocalTransactionState.UNKNOW;
                }
                if (history.getState() == 2) {
                    return  LocalTransactionState.COMMIT_MESSAGE;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });
    }

    public boolean transactionAsyncReduceStock(Integer userId, Integer goodId, Integer eventId, Integer amount, String transactionLogId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("goodId", goodId);
        payload.put("amount", amount);
        payload.put("transactionLogId", transactionLogId);
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("goodId", goodId);
        argsMap.put("amount", amount);
        argsMap.put("userId", userId);
        argsMap.put("eventId", eventId);
        argsMap.put("transactionLogId", transactionLogId);

        Message message = new Message(
                topicName,
                "increase",
                JSON.toJSON(payload).toString().getBytes(StandardCharsets.UTF_8));
        TransactionSendResult sendResult = null;
        try {
            sendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);
            System.out.printf("%s%n", sendResult);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE) {
            return true;
        }
        return false;
    }

    public boolean asyncReduceStock(Integer goodId, Integer amount) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("goodId", goodId);
        payload.put("amount", amount);
        Message message = new Message(
                topicName,
                "increase",
                JSON.toJSON(payload).toString().getBytes(StandardCharsets.UTF_8));
        try {
            SendResult sendResult = producer.send(message);
            System.out.printf("%s%n", sendResult);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
