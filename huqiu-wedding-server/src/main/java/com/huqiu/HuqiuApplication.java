package com.huqiu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.huqiu.module.**.mapper")
public class HuqiuApplication {
    public static void main(String[] args) {
        SpringApplication.run(HuqiuApplication.class, args);
    }
}