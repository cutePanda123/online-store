package com.imooc.seckill.service.impl;

import com.imooc.seckill.dao.GoodMapper;
import com.imooc.seckill.dao.StockMapper;
import com.imooc.seckill.entity.Good;
import com.imooc.seckill.entity.Stock;
import com.imooc.seckill.error.BusinessError;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.service.GoodService;
import com.imooc.seckill.service.model.GoodModel;
import com.imooc.seckill.validator.ValidationResult;
import com.imooc.seckill.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodServiceImpl implements GoodService {
    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private GoodMapper goodMapper;

    @Autowired
    private StockMapper stockMapper;

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
        GoodModel goodModel1 = convertModelFromDataEntity(good, stock);
        return goodModel1;
    }

    @Override
    @Transactional
    public boolean reduceStock(Integer id, Integer amount) throws BusinessException {
        if (stockMapper.reduceStock(id, amount) > 0) {
            return true;
        }
        return false;
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
