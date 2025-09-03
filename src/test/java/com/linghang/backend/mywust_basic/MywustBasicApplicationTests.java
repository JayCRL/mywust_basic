package com.linghang.backend.mywust_basic;

import com.linghang.backend.mywust_basic.Service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@SpringBootTest
class MywustBasicApplicationTests {
//    @Autowired
//    private StringRedisTemplate redisTemplate;
    @Autowired
    private TokenService tokenService;
    @Test
    void contextLoads() {

    }
    @Test
    void testRedis(){
       System.out.println( tokenService.getName("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyMDIzMTMyMDEwMjUiLCJpYXQiOjE3NTMwODg1MjUsImV4cCI6MTc1MzE3NDkyNX0.J6YZgtd7_lgO9Ky-sRjjLJt6S-dUAMGaGW0_AnxKajY"));
        System.out.println(tokenService.getUid("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyMDIzMTMyMDEwMjUiLCJpYXQiOjE3NTMwODg1MjUsImV4cCI6MTc1MzE3NDkyNX0.J6YZgtd7_lgO9Ky-sRjjLJt6S-dUAMGaGW0_AnxKajY"));
//        // 写入 Redis，设置5分钟过期
//        redisTemplate.opsForValue().set("testKey", "helloRedis", Duration.ofMinutes(5));
//
//        // 读取 Redis
//        String value = redisTemplate.opsForValue().get("testKey");
//
//        System.out.println("Redis testKey value: " + value);
//
//        // 断言
//        assert "helloRedis".equals(value);
    }

}
