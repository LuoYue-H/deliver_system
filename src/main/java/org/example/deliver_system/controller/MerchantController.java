package org.example.deliver_system.controller;

import org.example.deliver_system.common.Result;
import org.example.deliver_system.entity.User;
import org.example.deliver_system.mapper.MerchantMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/merchant")
public class MerchantController {

    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/list")
    public Result<List<User>> list() {
        String key = "merchant:list";
        List<User> merchants = null;
        
        try {
            merchants = (List<User>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Redis connection failed: {}", e.getMessage());
            // Redis 挂了不应该影响主业务，继续查库
        }
        
        if (merchants == null) {
            merchants = merchantMapper.getMerchantList();
            if (merchants != null) {
                try {
                    redisTemplate.opsForValue().set(key, merchants, 30, TimeUnit.MINUTES);
                } catch (Exception e) {
                    logger.error("Redis set failed: {}", e.getMessage());
                }
            }
        }
        return Result.success(merchants);
    }
}
