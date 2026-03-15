package org.example.deliver_system.service;

import org.example.deliver_system.mapper.OrderDetailMapper;
import org.example.deliver_system.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    public Map<String, Object> getMerchantDashboardStats(Long merchantId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 营业额趋势 (近7日)
        Map<String, Object> turnoverStats = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(formatter));
            BigDecimal dailyIncome = orderMapper.getMerchantDailyIncome(merchantId, date.toString());
            amounts.add(dailyIncome != null ? dailyIncome : BigDecimal.ZERO);
        }
        turnoverStats.put("dates", dates);
        turnoverStats.put("amounts", amounts);
        result.put("turnover", turnoverStats);

        // 2. 订单状态分布
        long completedCount = orderMapper.countByStatus(merchantId, 5);
        long cancelledCount = orderMapper.countByStatus(merchantId, 6) + orderMapper.countByStatus(merchantId, 7);
        List<Map<String, Object>> statusDistribution = new ArrayList<>();
        statusDistribution.add(Map.of("name", "已完成", "value", completedCount));
        statusDistribution.add(Map.of("name", "已取消", "value", cancelledCount));
        result.put("statusDistribution", statusDistribution);

        // 3. 菜品销量 Top 10
        List<Map<String, Object>> topDishesData = orderDetailMapper.getTop10SellingDishes(merchantId);
        Map<String, Object> topDishesStats = new HashMap<>();
        List<String> dishNames = new ArrayList<>();
        List<Long> dishCounts = new ArrayList<>();
        for (Map<String, Object> dish : topDishesData) {
            dishNames.add((String) dish.get("dish_name"));
            Object totalSold = dish.get("total_sold");
            if (totalSold instanceof Number) {
                dishCounts.add(((Number) totalSold).longValue());
            } else {
                dishCounts.add(0L);
            }
        }
        topDishesStats.put("names", dishNames);
        topDishesStats.put("counts", dishCounts);
        result.put("topDishes", topDishesStats);

        return result;
    }
}
