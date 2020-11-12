package com.imooc.seckill.controller;

import com.imooc.seckill.error.BusinessError;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.response.CommonResponseType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {
    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception ex) {
        Map<String, Object> data = new HashMap<>();
        if (ex instanceof BusinessException) {
            data.put("errCode", BusinessError.UNKNOWN_ERROR.getErrorCode());
            data.put("errMsg", BusinessError.UNKNOWN_ERROR.getErrorMessage());
        } else {
            BusinessException be = (BusinessException) ex;
            data.put("errCode", be.getErrorCode());
            data.put("errMsg", be.getErrorMessage());
        }
        return CommonResponseType.newInstance(data, "fail");
    }
}
