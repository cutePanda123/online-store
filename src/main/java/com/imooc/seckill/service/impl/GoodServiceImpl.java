package com.imooc.seckill.service.impl;

import com.imooc.seckill.dao.GoodMapper;
import com.imooc.seckill.dao.StockMapper;
import com.imooc.seckill.dao.TransactionHistoryMapper;
import com.imooc.seckill.entity.Good;
import com.imooc.seckill.entity.Stock;
import com.imooc.seckill.entity.TransactionHistory;
import com.imooc.seckill.error.BusinessError;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.mq.MqProducer;
import com.imooc.seckill.service.EventService;
import com.imooc.seckill.service.GoodService;
import com.imooc.seckill.service.model.EventModel;
import com.imooc.seckill.service.model.GoodModel;
import com.imooc.seckill.validator.ValidationResult;
import com.imooc.seckill.validator.ValidatorImpl;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GoodServiceImpl implements GoodService {
    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private GoodMapper goodMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private EventService eventService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private TransactionHistoryMapper transactionHistoryMapper;

    @Override
    @Transactional
    public GoodModel createGood(GoodModel goodModel) throws BusinessException {
        ValidationResult validationResult = validator.validate(goodModel);
        if (validationResult.isHasErrors()) {
            throw  new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, validationResult.getErrorMsg());
        }
        Good good = covertGoodFromGoodModel(goodModel);
        goodMapper.insertSelective(good);
        goodModel.setId(good.getId());
        Stock stock = convertStockFromGoodModel(goodModel);
        stockMapper.insertSelective(stock);
        return getGoodById(goodModel.getId());
    }

    @Override
    public List<GoodModel> listGoods() {
        List<Good> goods = goodMapper.listAll();
        List<GoodModel> goodModels = goods.stream().map(good -> {
            Stock stock = stockMapper.selectByGoodId(good.getId());
            return convertModelFromDataEntity(good, stock);
        }).collect(Collectors.toList());
        return goodModels;
    }

    @Override
    public GoodModel getGoodById(Integer id) {
        Good good = goodMapper.selectByPrimaryKey(id);
        if (good == null) {
            return null;
        }
        Stock stock = stockMapper.selectByGoodId(good.getId());
        GoodModel goodModel = convertModelFromDataEntity(good, stock);

        // get good's event information if it has
        EventModel eventModel = eventService.getEventByGoodId(id);
        if (eventModel != null && eventModel.getStatus() != 3) {
            goodModel.setEventModel(eventModel);
        }

        return goodModel;
    }

    @Override
    @Transactional
    public boolean reduceStock(Integer id, Integer amount) throws BusinessException {
        String redisStockKey = "event_good_stock_" + id;
        long remainingStockNum = redisTemplate.opsForValue().increment(redisStockKey, amount.intValue() * -1);
        if (remainingStockNum >= 0) {
            return  true;
        } else {
            increaseStock(id, amount.intValue());
            return false;
        }
    }

    @Override
    public boolean asyncReduceStock(Integer id, Integer amount) throws BusinessException {
        return mqProducer.asyncReduceStock(id, amount);
    }

    @Override
    public boolean increaseStock(Integer id, Integer amount) throws BusinessException {
        String redisStockKey = "event_good_stock_" + id;
        redisTemplate.opsForValue().increment(redisStockKey, amount);
        return true;
    }

    @Override
    public void increaseSales(Integer id, Integer amount) throws BusinessException {
        goodMapper.increaseSales(id, amount);
    }

    @Override
    public GoodModel getGoodByIdFromCache(Integer id) {
        String goodValidationKey = "good_validation_" + id;
        GoodModel goodModel = (GoodModel) redisTemplate.opsForValue().get(goodValidationKey);
        if (goodModel == null) {
            goodModel = this.getGoodById(id);
            redisTemplate.opsForValue().set(goodValidationKey, goodModel);
            redisTemplate.expire(goodValidationKey, 10, TimeUnit.MINUTES);
        }
        return goodModel;
    }

    @Override
    @Transactional
    public String initTransactionHistoryLog(Integer id, Integer amount) {
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setGoodId(id);
        transactionHistory.setAmount(amount);
        transactionHistory.setId(UUID.randomUUID().toString().replace("-", ""));
        transactionHistory.setState(1);

        transactionHistoryMapper.insertSelective(transactionHistory);
        return transactionHistory.getId();
    }

    private Good covertGoodFromGoodModel(GoodModel goodModel) {
        if (goodModel == null) {
            return null;
        }
        Good good = new Good();
        BeanUtils.copyProperties(goodModel, good);
        return  good;
    }

    private Stock convertStockFromGoodModel(GoodModel goodModel) {
        if (goodModel == null) {
            return null;
        }
        Stock stock = new Stock();
        stock.setStock(goodModel.getStock());
        stock.setGoodId(goodModel.getId());
        return stock;
    }

    private GoodModel convertModelFromDataEntity(Good good, Stock stock) {
        GoodModel goodModel = new GoodModel();
        BeanUtils.copyProperties(good, goodModel);
        goodModel.setStock(stock.getStock());
        return goodModel;
    }

}
