package com.imooc.seckill.response;

public class CommonResponseType {
    private String status;
    private Object data;

    public static CommonResponseType newInstance(Object result) {
        return CommonResponseType.newInstance(result, "success");
    }

    public static CommonResponseType newInstance(Object result, String status) {
        CommonResponseType crt = new CommonResponseType();
        crt.setData(result);
        crt.setStatus(status);
        return crt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
