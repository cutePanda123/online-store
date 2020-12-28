package com.imooc.seckill.service.impl;

import com.imooc.seckill.dao.EventMapper;
import com.imooc.seckill.entity.Event;
import com.imooc.seckill.error.BusinessError;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.service.EventService;
import com.imooc.seckill.service.GoodService;
import com.imooc.seckill.service.UserService;
import com.imooc.seckill.service.model.EventModel;
import com.imooc.seckill.service.model.GoodModel;
import com.imooc.seckill.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class EventServiceImpl implements EventService {
    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private GoodService goodService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Override
    public void publishEvent(Integer id) {
        Event event = eventMapper.selectByGoodId(id);
        if (event.getGoodId() == null || event.getGoodId().intValue() == 0) {
            return;
        }

        GoodModel goodModel = goodService.getGoodById(event.getGoodId());
        String redisStockKey = "event_good_stock_" + goodModel.getId();
        redisTemplate.opsForValue().set(redisStockKey, goodModel.getStock());
    }

    @Override
    public EventModel getEventByGoodId(Integer id) {
        Event event = eventMapper.selectByGoodId(id);
        EventModel eventModel = covertEventModelFromEventEntity(event);
        if (eventModel == null) {
            return null;
        }
        if (eventModel.getStartDate().isAfterNow()) {
            eventModel.setStatus(1);
        } else if (eventModel.getEndDate().isBeforeNow()) {
            eventModel.setStatus(3);
        } else {
            eventModel.setStatus(2);
        }
        return eventModel;
    }

    @Override
    public String generateEventAccessToken(Integer id, Integer goodId, Integer userId) {
        GoodModel good = goodService.getGoodByIdFromCache(goodId);
        if (good == null) {
            return null;
        }
        UserModel userModel = userService.getUserByIdFromCache(userId);
        if (userModel == null) {
            return null;
        }

        Event event = eventMapper.selectByPrimaryKey(id);
        EventModel eventModel = covertEventModelFromEventEntity(event);
        if (eventModel == null) {
            return null;
        }
        if (eventModel.getStartDate().isAfterNow()) {
            eventModel.setStatus(1);
        } else if (eventModel.getEndDate().isBeforeNow()) {
            eventModel.setStatus(3);
        } else {
            eventModel.setStatus(2);
        }
        if (eventModel.getStatus().intValue() != 2) {
            System.out.println("Promotion event(" + id.toString() + ") is not in-progress");
            return null;
        }

        if (eventModel.getGoodId() != goodId) {
            System.out.println("Promotion event(" + id.toString() + ") does not have the good(" + goodId.toString() + ")");
            return null;
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        String eventAccessTokenRedisKey =
                "event_token_" + id.toString() + ",userId_" + userId.toString() + "_goodId_" + goodId.toString();
        redisTemplate.opsForValue().set(eventAccessTokenRedisKey, token);
        redisTemplate.expire(eventAccessTokenRedisKey, 5, TimeUnit.MINUTES);

        return token;
    }

    private  EventModel covertEventModelFromEventEntity(Event event) {
        if (event == null) {
            return null;
        }
        EventModel eventModel = new EventModel();
        BeanUtils.copyProperties(event, eventModel);
        eventModel.setStartDate(new DateTime(event.getStartDate()));
        eventModel.setEndDate(new DateTime(event.getEndDate()));
        return eventModel;
    }
}
