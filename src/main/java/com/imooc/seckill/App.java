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

    @RequestMapping("/")
    public String home() {
        UserInfo info = userInfoMapper.selectByPrimaryKey(1);
        if (info != null)
            return info.getName();
        else
            return "Spring Config Error";
    }

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class, args);
    }
}
