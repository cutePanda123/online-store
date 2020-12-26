package com.imooc.seckill.error;

public enum BusinessError implements CommonError {
    // general error type 1
    PARAMETER_VALIDATION_ERROR(10001, "invalid parameter"),
    UNKNOWN_ERROR(10002, "unknown error"),

    // user error type starting from 2
    USER_NOT_EXIST(20001, "user not exist"),
    USER_LOGIN_FAILED(20002, "user phone or password incorrect"),
    USER_NOT_LOGIN(20003, "user did not login"),

    // order error type starting from 3
    STOCK_NOT_ENOUGH(30001, "stock not enough"),
    MQ_SEND_FAILURE(30002, "stock change asnyc message failure"),
    ;
    private int code;
    private String message;

    private BusinessError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getErrorCode() {
        return code;
    }

    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public CommonError setErrorMessage(String message) {
        this.message = message;
        return this;
    }
}
