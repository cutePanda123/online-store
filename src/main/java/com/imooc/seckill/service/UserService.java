package com.imooc.seckill.service;

import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.service.model.UserModel;

public interface UserService {
    public UserModel getUserById(Integer id);
    public void register(UserModel userModel) throws BusinessException;
    public UserModel authenticate(String phone, String password) throws BusinessException;
}
