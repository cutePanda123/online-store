package com.imooc.seckill.error;


// wrapper design pattern
public class BusinessException extends Exception implements CommonError {
    private CommonError commonError;

    public BusinessException(CommonError commonError) {
        super();
        this.commonError = commonError;
    }

    public BusinessException(CommonError error, String message) {
        super();
        error.setErrorMessage(message);
        this.commonError = error;
    }

    @Override
    public int getErrorCode() {
        return commonError.getErrorCode();
    }

    @Override
    public String getErrorMessage() {
        return commonError.getErrorMessage();
    }

    @Override
    public CommonError setErrorMessage(String message) {
        this.commonError.setErrorMessage(message);
        return this;
    }
}
