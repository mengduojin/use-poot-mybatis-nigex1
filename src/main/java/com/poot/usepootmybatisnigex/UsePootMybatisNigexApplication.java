package com.poot.usepootmybatisnigex;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.controller","com.util","com.service"})
@MapperScan(basePackages ={"com.mapper"})
public class UsePootMybatisNigexApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsePootMybatisNigexApplication.class, args);
    }

}
