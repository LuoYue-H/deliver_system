package org.example.deliver_system.controller;

import org.example.deliver_system.common.Result;
import org.example.deliver_system.dto.SubmitOrderDTO;
import org.example.deliver_system.entity.Orders;
import org.example.deliver_system.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public Result<String> submit(@RequestBody SubmitOrderDTO submitOrderDTO, @RequestAttribute("userId") Long userId) {
        try {
            orderService.submitOrder(submitOrderDTO, userId);
            return Result.success("下单成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage()); 
        }
    }

    @PutMapping("/user/cancel/{id}")
    public Result<String> userCancelOrder(@PathVariable String id, @RequestAttribute("userId") Long userId) {
        try {
            orderService.userCancelOrder(id, userId);
            return Result.success("订单已取消");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/status/{id}/{status}")
    public Result<String> updateStatus(@PathVariable String id, @PathVariable Integer status) {
        try {
            orderService.updateStatus(id, status);
            return Result.success("状态更新成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/list/merchant")
    public Result<List<Orders>> listMerchantOrders(@RequestAttribute("userId") Long merchantId) {
        return Result.success(orderService.getMerchantOrders(merchantId));
    }

    @GetMapping("/list/merchant/history")
    public Result<List<Orders>> listMerchantHistoryOrders(@RequestAttribute("userId") Long merchantId) {
        return Result.success(orderService.getMerchantHistoryOrders(merchantId));
    }

    @GetMapping("/list/user")
    public Result<List<Orders>> listUserOrders(@RequestAttribute("userId") Long userId) {
        return Result.success(orderService.getUserOrders(userId));
    }
    
    @GetMapping("/pending")
    public Result<List<Orders>> listPendingOrders() {
        return Result.success(orderService.getPendingOrders());
    }
    
    @PutMapping("/take/{id}")
    public Result<String> takeOrder(@PathVariable String id, @RequestAttribute("userId") Long riderId) {
        try {
            orderService.takeOrder(id, riderId);
            return Result.success("抢单成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/rider/takeMeal/{id}")
    public Result<String> riderTakeMeal(@PathVariable String id, @RequestAttribute("userId") Long riderId) {
        try {
            orderService.riderTakeMeal(id, riderId);
            return Result.success("取货成功，开始配送");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/stats/daily")
    public Result<List<Map<String, Object>>> getDailyStats(@RequestAttribute("userId") Long merchantId) {
        return Result.success(orderService.getDailyStats(merchantId));
    }

    @GetMapping("/stats/top-dishes")
    public Result<List<Map<String, Object>>> getTopDishes(@RequestAttribute("userId") Long merchantId) {
        return Result.success(orderService.getTopDishes(merchantId));
    }
    
    @GetMapping("/list/rider/active")
    public Result<List<Orders>> listRiderActiveOrders(@RequestAttribute("userId") Long riderId) {
        return Result.success(orderService.getRiderActiveOrders(riderId));
    }
    
    @GetMapping("/list/rider/history")
    public Result<List<Orders>> listRiderHistoryOrders(@RequestAttribute("userId") Long riderId) {
        return Result.success(orderService.getRiderHistoryOrders(riderId));
    }
    
    @GetMapping("/stats/rider/income")
    public Result<Map<String, Object>> getRiderIncomeStats(@RequestAttribute("userId") Long riderId) {
        return Result.success(orderService.getRiderIncomeStats(riderId));
    }

    @GetMapping("/stats/rider/daily")
    public Result<List<Map<String, Object>>> getRiderDailyStats(@RequestAttribute("userId") Long riderId) {
        return Result.success(orderService.getRiderDailyStats(riderId));
    }
    
    @PutMapping("/finish/{id}")
    public Result<String> finishOrder(@PathVariable String id) {
        try {
            orderService.finishOrder(id);
            return Result.success("订单已送达");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/cancel/{id}")
    public Result<String> cancelOrder(@PathVariable String id) {
        try {
            orderService.cancelOrder(id);
            return Result.success("订单已取消");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/merchant/cancel/{id}")
    public Result<String> merchantCancelOrder(@PathVariable String id) {
        try {
            orderService.merchantCancelOrder(id);
            return Result.success("订单已由商家取消");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/stats/merchant/income")
    public Result<Map<String, Object>> getMerchantIncomeStats(@RequestAttribute("userId") Long merchantId) {
        return Result.success(orderService.getMerchantIncomeStats(merchantId));
    }
}
