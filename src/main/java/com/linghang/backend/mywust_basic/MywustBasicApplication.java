package com.linghang.backend.mywust_basic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.linghang.backend.mywust_basic.Mapper")
public class MywustBasicApplication {
    public static void main(String[] args) {
        SpringApplication.run(MywustBasicApplication.class, args);
    }

}
