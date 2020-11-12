package com.imooc.seckill.service.impl;

import com.imooc.seckill.dao.UserAccountMapper;
import com.imooc.seckill.dao.UserInfoMapper;
import com.imooc.seckill.entity.UserAccount;
import com.imooc.seckill.entity.UserInfo;
import com.imooc.seckill.error.BusinessError;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.service.UserService;
import com.imooc.seckill.service.model.UserModel;
import com.imooc.seckill.validator.ValidationResult;
import com.imooc.seckill.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(id);
        if (userInfo == null) {
            return null;
        }
        UserAccount userAccount = userAccountMapper.selectByUserId(userInfo.getId());
        return convertFromDataEntity(userInfo, userAccount);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "user model is empty");
        }

        ValidationResult validationResult = validator.validate(userModel);
        if (validationResult.isHasErrors()) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, validationResult.getErrorMsg());
        }

        UserInfo userInfo = covertFromModel(userModel);
        try {
            userInfoMapper.insertSelective(userInfo);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "duplicate phone number");
        }
        UserAccount userAccount = convertFromModel(userModel);
        userAccount.setUserId(userInfo.getId());
        userAccountMapper.insertSelective(userAccount);
    }

    @Override
    public UserModel authenticate(String phone, String password) throws BusinessException {
        UserInfo userInfo = userInfoMapper.selectByPhone(phone);
        if (userInfo == null) {
            throw new BusinessException(BusinessError.USER_LOGIN_FAILED);
        }
        UserAccount userAccount = userAccountMapper.selectByUserId(userInfo.getId());
        UserModel userModel = convertFromDataEntity(userInfo, userAccount);
        if (!StringUtils.equals(password, userModel.getEncryptPassword())) {
            throw new BusinessException(BusinessError.USER_LOGIN_FAILED);
        }
        return userModel;
    }

    private UserAccount convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserAccount userAccount = new UserAccount();
        userAccount.setEncryptPassword(userModel.getEncryptPassword());
        userAccount.setUserId(userModel.getId());

        return userAccount;
    }

    private UserInfo covertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userModel, userInfo);
        return userInfo;
    }

    private UserModel convertFromDataEntity(UserInfo userInfo, UserAccount userAccount) {
        if (userInfo == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userInfo, userModel);
        if (userAccount == null) {
            return userModel;
        }
        userModel.setEncryptPassword(userAccount.getEncryptPassword());
        return userModel;
    }
}
