package com.anhun;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisTest {

    @Autowired
    public RedisTemplate<String, String> redisTemplate;

    @Test
    public void setString() {
        redisTemplate.opsForValue().append("user01", "ofja");
    }

    @Test
    public void getString() {
        System.out.println(redisTemplate.opsForValue().get("user01"));
    }

}
