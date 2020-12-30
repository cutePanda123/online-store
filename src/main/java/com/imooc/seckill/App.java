package com.imooc.seckill;

import com.imooc.seckill.dao.UserInfoMapper;
import com.imooc.seckill.entity.UserInfo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
//@EnableAutoConfiguration
@SpringBootApplication(scanBasePackages = {"com.imooc.seckill"})
@RestController
@MapperScan("com.imooc.seckill.dao")
public class App 
{
    @Autowired
    private UserInfoMapper userInfoMapper;

    @RequestMapping("/healthcheck")
    public String home() {
        return "Health check passed.";
    }

    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }
}
