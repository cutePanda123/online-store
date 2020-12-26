package com.imooc.seckill.mq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class MqProducer {
    private DefaultMQProducer producer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @PostConstruct
    public void init() throws MQClientException {
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        //producer.setSendMsgTimeout(60000);
        producer.start();
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
