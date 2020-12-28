package com.imooc.seckill.service;

import com.imooc.seckill.service.model.EventModel;

public interface EventService {
    public EventModel getEventByGoodId(Integer id);

    public void publishEvent(Integer id);

    public String generateEventAccessToken(Integer id, Integer goodId, Integer userId);
}
