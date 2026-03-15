package org.example.deliver_system.service;

import org.example.deliver_system.entity.Dish;
import org.example.deliver_system.mapper.DishMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class DishService {

    private static final Logger logger = LoggerFactory.getLogger(DishService.class);

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void addDish(Dish dish) {
        dishMapper.insert(dish);
        clearCache(dish.getCategoryId(), dish.getId());
    }

    public void updateDish(Dish dish) {
        dishMapper.update(dish);
        clearCache(dish.getCategoryId(), dish.getId());
    }

    public void deleteDish(Long id) {
        Dish dish = dishMapper.getById(id);
        dishMapper.delete(id);
        if (dish != null) {
            clearCache(dish.getCategoryId(), id);
        }
    }

    /**
     * 清理菜品相关缓存
     */
    public void clearCache(Long categoryId, Long dishId) {
        try {
            if (dishId != null) {
                redisTemplate.delete("dish:detail:" + dishId);
            }
            // 简单处理：直接清理所有商家的菜品列表缓存
            Set<String> keys = redisTemplate.keys("dish:list:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            logger.error("Redis cache clear failed: {}", e.getMessage());
        }
    }

    public Dish getDishById(Long id) {
        String key = "dish:detail:" + id;
        Dish dish = null;
        try {
            dish = (Dish) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Redis get failed: {}", e.getMessage());
        }
        
        if (dish == null) {
            dish = dishMapper.getById(id);
            if (dish != null) {
                try {
                    redisTemplate.opsForValue().set(key, dish, 30, TimeUnit.MINUTES);
                } catch (Exception e) {
                    logger.error("Redis set failed: {}", e.getMessage());
                }
            }
        }
        return dish;
    }

    public List<Dish> getDishesByMerchant(Long merchantId) {
        String key = "dish:list:" + merchantId;
        List<Dish> dishes = null;
        try {
            dishes = (List<Dish>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Redis get failed: {}", e.getMessage());
        }

        if (dishes == null) {
            dishes = dishMapper.getByMerchantId(merchantId);
            if (dishes != null) {
                try {
                    redisTemplate.opsForValue().set(key, dishes, 30, TimeUnit.MINUTES);
                } catch (Exception e) {
                    logger.error("Redis set failed: {}", e.getMessage());
                }
            }
        }
        return dishes;
    }

    public void updateStatus(Long id, Integer status) {
        dishMapper.updateStatus(id, status);
        clearCache(null, id);
    }
}
