package com.imooc.seckill.service;

import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.service.model.GoodModel;

import java.util.List;

public interface GoodService {
    public GoodModel createGood(GoodModel goodModel) throws BusinessException;
    public List<GoodModel> listGoods();
    public GoodModel getGoodById(Integer id);
    public boolean reduceStock(Integer id, Integer amount) throws BusinessException;
    public boolean increaseStock(Integer id, Integer amount) throws BusinessException;
    public boolean asyncReduceStock(Integer id, Integer amount) throws BusinessException;
    public GoodModel getGoodByIdFromCache(Integer id);
    public void increaseSales(Integer id, Integer amount) throws BusinessException;
    public String initTransactionHistoryLog(Integer id, Integer amount);
}
