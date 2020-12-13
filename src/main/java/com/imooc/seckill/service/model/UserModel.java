package com.imooc.seckill.service.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class UserModel implements Serializable {
    private Integer id;

    @NotBlank(message = "user name cannot be empty")
    private String name;

    @NotNull(message = "gender cannot be null")
    private Byte gender;

    @NotNull(message = "age cannot be null")
    @Min(value = 0, message = "age cannot be negative")
    @Max(value = 150, message = "age cannot be too big")
    private Byte age;

    @NotBlank(message = "phone cannot be empty")
    private String phone;

    private String registrationMode;
    private String thirdPartyUserId;

    @NotBlank(message = "password cannot be empty")
    private String encryptPassword;

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", age=" + age +
                ", phone='" + phone + '\'' +
                ", registrationMode='" + registrationMode + '\'' +
                ", thirdPartyUserId='" + thirdPartyUserId + '\'' +
                ", encryptPassword='" + encryptPassword + '\'' +
                '}';
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public Byte getAge() {
        return age;
    }

    public void setAge(Byte age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRegistrationMode() {
        return registrationMode;
    }

    public void setRegistrationMode(String registrationMode) {
        this.registrationMode = registrationMode;
    }

    public String getThirdPartyUserId() {
        return thirdPartyUserId;
    }

    public void setThirdPartyUserId(String thirdPartyUserId) {
        this.thirdPartyUserId = thirdPartyUserId;
    }

    public String getEncryptPassword() {
        return encryptPassword;
    }

    public void setEncryptPassword(String encryptPassword) {
        this.encryptPassword = encryptPassword;
    }
}
