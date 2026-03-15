package org.example.deliver_system.controller;

import org.example.deliver_system.common.Result;
import org.example.deliver_system.entity.Dish;
import org.example.deliver_system.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping
    public Result<String> add(@RequestBody Dish dish) {
        dishService.addDish(dish);
        return Result.success("添加成功");
    }

    @PutMapping
    public Result<String> update(@RequestBody Dish dish) {
        dishService.updateDish(dish);
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        dishService.deleteDish(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    public Result<Dish> get(@PathVariable Long id) {
        return Result.success(dishService.getDishById(id));
    }

    @GetMapping("/list/{merchantId}")
    public Result<List<Dish>> list(@PathVariable Long merchantId) {
        return Result.success(dishService.getDishesByMerchant(merchantId));
    }

    @PutMapping("/status/{id}/{status}")
    public Result<String> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        dishService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}
