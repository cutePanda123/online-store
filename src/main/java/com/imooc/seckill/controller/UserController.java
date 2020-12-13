package com.imooc.seckill.controller;

import com.imooc.seckill.controller.viewmodel.UserViewModel;
import com.imooc.seckill.error.BusinessError;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.response.CommonResponseType;
import com.imooc.seckill.service.UserService;
import com.imooc.seckill.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    HttpServletRequest request;

    @RequestMapping(value = "/register", method = { RequestMethod.POST }, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonResponseType register(@RequestParam(name = "phone")String phone,
                                       @RequestParam(name = "otpCode")String otpCode,
                                       @RequestParam(name = "name")String name,
                                       @RequestParam(name = "age")String age,
                                       @RequestParam(name = "gender")String gender,
                                       @RequestParam(name = "password")String password) throws BusinessException, NoSuchAlgorithmException {
        String inSessionOtpCode = (String)this.request.getSession().getAttribute(phone);
        if (!com.alibaba.druid.util.StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "otp code is invalid");
        }
        UserModel userModel = new UserModel();
        userModel.setAge(Byte.valueOf(age));
        userModel.setName(name);
        userModel.setPhone(phone);
        userModel.setRegistrationMode("byphone");
        userModel.setGender(Byte.valueOf(gender));
        userModel.setEncryptPassword(encodeByMd5(password));
        userService.register(userModel);

        return CommonResponseType.newInstance(null);
    }
    @RequestMapping(value = "/login", method = { RequestMethod.POST }, consumes = { CONTENT_TYPE_FORMED })
    @ResponseBody
    public CommonResponseType login(
            @RequestParam(name = "phone")String phone,
            @RequestParam(name = "password")String password) throws BusinessException, NoSuchAlgorithmException {
        if (com.alibaba.druid.util.StringUtils.isEmpty(phone) ||
            com.alibaba.druid.util.StringUtils.isEmpty(password)) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR);
        }
        UserModel userModel = userService.authenticate(phone, encodeByMd5(password));

        request.getSession().setAttribute("IS_LOGIN", true);
        request.getSession().setAttribute("LOGIN_USER", userModel);

        return CommonResponseType.newInstance(null);
    }

    @RequestMapping(value = "/otp/{phone}", method = { RequestMethod.POST }, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonResponseType getOtp(@PathVariable(name = "phone") String phone) {
        Random random = new Random();
        int randomNum = 10000 + random.nextInt(99999);
        String otpCode = String.valueOf(randomNum);
        request.getSession().setAttribute(phone, otpCode);
        System.out.println("User phone :" + phone + ", User otp :" + otpCode);

        return CommonResponseType.newInstance(null);
    }

    //example http://localhost:8090/user/1
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResponseType getUser(@PathVariable(name = "id") Integer id) throws BusinessException {
        UserModel userModel = userService.getUserById(id);
        if (userModel == null) {
            throw new BusinessException(BusinessError.USER_NOT_EXIST);
        }

        UserViewModel uvm = convertFromUserModel(userModel);
        CommonResponseType response = CommonResponseType.newInstance(uvm);

        return response;
    }

    private UserViewModel convertFromUserModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserViewModel userViewModel = new UserViewModel();
        BeanUtils.copyProperties(userModel, userViewModel);
        return userViewModel;
    }

    private String encodeByMd5(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        Base64.Encoder encoder = Base64.getEncoder();
        String encodedStr = encoder.encodeToString(md.digest(str.getBytes()));
        return encodedStr;
    }
}
