package org.example.deliver_system.service;

import org.example.deliver_system.dto.CartItemDTO;
import org.example.deliver_system.dto.SubmitOrderDTO;
import org.example.deliver_system.entity.Dish;
import org.example.deliver_system.entity.OrderDetail;
import org.example.deliver_system.entity.Orders;
import org.example.deliver_system.entity.User;
import org.example.deliver_system.mapper.DishMapper;
import org.example.deliver_system.mapper.OrderDetailMapper;
import org.example.deliver_system.mapper.OrderMapper;
import org.example.deliver_system.server.WebSocketServer;
import org.example.deliver_system.utils.OrderStateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishService dishService;

    @Autowired
    private org.example.deliver_system.mapper.UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 恢复库存的私有助手方法
     */
    private void restoreStock(String orderId) {
        List<OrderDetail> details = orderDetailMapper.findByOrderId(orderId);
        if (details != null && !details.isEmpty()) {
            for (OrderDetail detail : details) {
                dishMapper.addStock(detail.getDishId(), detail.getNumber());
                dishService.clearCache(null, detail.getDishId());
            }
        }
    }

    /**
     * 用户下单
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitOrder(SubmitOrderDTO submitOrderDTO, Long userId) {
        if (submitOrderDTO == null || submitOrderDTO.getItems() == null || submitOrderDTO.getItems().isEmpty()) {
            throw new RuntimeException("订单信息不完整，购物车为空");
        }
        // 1. 处理购物车数据（校验库存、计算总价）
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (CartItemDTO item : submitOrderDTO.getItems()) {
            if (item.getDishId() == null || item.getNumber() == null || item.getNumber() <= 0) {
                continue; // 跳过无效数据
            }
            Dish dish = dishMapper.getById(item.getDishId());
            if (dish == null) {
                throw new RuntimeException("菜品不存在: " + item.getDishId());
            }
            if (dish.getStatus() == 0) {
                throw new RuntimeException("菜品已停售: " + dish.getName());
            }
            
            // 扣减库存
            int rows = dishMapper.deductStock(item.getDishId(), item.getNumber());
            if (rows <= 0) {
                throw new RuntimeException("库存不足: " + dish.getName());
            }
            
            // 清理菜品缓存，确保商家端看到最新库存
            dishService.clearCache(dish.getCategoryId(), dish.getId());

            // 计算价格
            BigDecimal itemTotal = dish.getPrice().multiply(new BigDecimal(item.getNumber()));
            totalAmount = totalAmount.add(itemTotal);

            // 构建订单明细
            OrderDetail detail = new OrderDetail();
            detail.setDishId(dish.getId());
            detail.setDishName(dish.getName());
            detail.setDishImage(dish.getImage());
            detail.setDishPrice(dish.getPrice());
            detail.setNumber(item.getNumber());
            orderDetails.add(detail);
        }

        // 2. 创建订单
        Orders order = new Orders();
        // 生成8位16进制的唯一ID
        String orderId = UUID.randomUUID().toString().substring(0, 8);
        order.setId(orderId);
        order.setUserId(userId);
        order.setMerchantId(submitOrderDTO.getMerchantId());
        order.setStatus(OrderStateMachine.STATUS_WAITING_ACCEPT); 
        order.setTotalAmount(totalAmount);
        order.setAddress(submitOrderDTO.getAddress());
        order.setContactName(submitOrderDTO.getContactName());
        order.setContactPhone(submitOrderDTO.getContactPhone());
        
        orderMapper.insert(order);

        // 3. 批量插入订单明细
        for (OrderDetail detail : orderDetails) {
            detail.setOrderId(order.getId());
        }
        orderDetailMapper.batchInsert(orderDetails);

        // 5. 推送结构化消息给商家
        String message = String.format("{\"type\": \"NEW_ORDER\", \"message\": \"您有新的外卖订单！订单号：%s\"}", order.getId());
        webSocketServer.sendToUser(order.getMerchantId().toString(), message);
    }

    /**
     * 订单状态流转 (商家/骑手/用户调用)
     */
    public void updateStatus(String orderId, Integer newStatus) {
        Orders order = orderMapper.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 校验取消权限
        if (newStatus == OrderStateMachine.STATUS_CANCELLED_BY_USER) {
            // 学生：在订单送达前都可以取消
            if (order.getStatus() >= OrderStateMachine.STATUS_COMPLETED) {
                throw new RuntimeException("订单已送达，无法取消");
            }
        } else if (newStatus == OrderStateMachine.STATUS_CANCELLED_BY_MERCHANT) {
            // 商家：在骑手配送前可以取消
            if (order.getStatus() >= OrderStateMachine.STATUS_DELIVERING) {
                throw new RuntimeException("骑手已开始配送，无法取消");
            }
        }

        orderMapper.updateStatus(orderId, newStatus);

        // 如果是取消订单，恢复库存
        if (newStatus == OrderStateMachine.STATUS_CANCELLED_BY_USER || newStatus == OrderStateMachine.STATUS_CANCELLED_BY_MERCHANT) {
            restoreStock(orderId);
        }

        if (newStatus == OrderStateMachine.STATUS_CANCELLED_BY_USER || newStatus == OrderStateMachine.STATUS_CANCELLED_BY_MERCHANT || newStatus == OrderStateMachine.STATUS_COMPLETED) {
            orderMapper.updateFinishTime(orderId);
        }

        // 推送消息给用户
        if (newStatus == OrderStateMachine.STATUS_DELIVERING) {
            webSocketServer.sendToUser(order.getUserId().toString(), "骑手已经取货，正在火速配送中");
        } else if (newStatus == OrderStateMachine.STATUS_COOKING) {
            webSocketServer.sendToUser(order.getUserId().toString(), "商家已接单，正在制作中！");
        } else if (newStatus == OrderStateMachine.STATUS_WAITING_DELIVERY) {
            webSocketServer.sendToUser(order.getUserId().toString(), "商家已出餐，等待骑手取货！");
        } else if (newStatus == OrderStateMachine.STATUS_COMPLETED) {
            webSocketServer.sendToUser(order.getUserId().toString(), "您的外卖已送达，祝您用餐愉快！");
        } else if (newStatus == OrderStateMachine.STATUS_CANCELLED_BY_USER || newStatus == OrderStateMachine.STATUS_CANCELLED_BY_MERCHANT) {
            webSocketServer.sendToUser(order.getUserId().toString(), "您的订单已被取消。");
            // 也要通知商家，以便商家端刷新库存
            String cancelMsg = String.format("{\"type\": \"ORDER_CANCELLED\", \"message\": \"订单 %s 已取消，库存已恢复\"}", order.getId());
            webSocketServer.sendToUser(order.getMerchantId().toString(), cancelMsg);
        }
    }
    
    public List<Orders> getMerchantOrders(Long merchantId) {
        List<Orders> orders = orderMapper.getByMerchantId(merchantId);
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }
        // 统一装配订单详情
        return assembleOrderDetails(orders);
    }

    public List<Orders> getMerchantHistoryOrders(Long merchantId) {
        List<Orders> orders = orderMapper.getAllByMerchantId(merchantId);
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }
        // 统一装配订单详情
        return assembleOrderDetails(orders);
    }
    
    public List<Orders> getUserOrders(Long userId) {
        List<Orders> orders = orderMapper.getByUserId(userId);
        // 为每个订单装配其详情和商家名称数据
        for (Orders order : orders) {
            List<OrderDetail> details = orderDetailMapper.findByOrderId(order.getId());
            order.setDetails(details);

            User merchant = userMapper.getById(order.getMerchantId());
            if (merchant != null) {
                order.setMerchantName(merchant.getUsername());
            }
        }
        return orders;
    }

    // 抽取公共方法，用于批量装配订单详情，避免 N+1 查询
    private List<Orders> assembleOrderDetails(List<Orders> orders) {
        // 1. 提取所有订单ID
        List<String> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());

        // 2. 一次性查询所有相关的订单详情
        List<OrderDetail> allDetails = orderDetailMapper.findByOrderIds(orderIds);

        // 3. 将订单详情按 orderId 分组
        Map<String, List<OrderDetail>> detailsMap = allDetails.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        // 4. 装配数据
        for (Orders order : orders) {
            order.setDetails(detailsMap.get(order.getId()));
        }

        return orders;
    }

    @Transactional(rollbackFor = Exception.class)
    public void userCancelOrder(String orderId, Long userId) {
        int affectedRows = orderMapper.userCancelOrder(orderId, userId);
        if (affectedRows == 0) {
            throw new RuntimeException("订单无法取消，可能已被处理或不存在");
        }
        // 恢复库存
        restoreStock(orderId);
    }
    
    public List<Orders> getPendingOrders() {
        return orderMapper.getPendingOrders();
    }
    
    public void takeOrder(String orderId, Long riderId) {
        // 骑手抢单：仅更新 rider_id
        int rows = orderMapper.takeOrder(orderId, riderId);
        if (rows == 0) {
            throw new RuntimeException("抢单失败，订单可能已被抢或状态变更");
        }
        
        // 抢单成功，通知商家和用户
        Orders order = orderMapper.getById(orderId);
        webSocketServer.sendToUser(order.getUserId().toString(), "骑手已接单，正火速赶往商家！");
    }
    
    public void riderTakeMeal(String orderId, Long riderId) {
        int rows = orderMapper.riderTakeMeal(orderId, riderId);
        if (rows == 0) {
            throw new RuntimeException("操作失败，订单状态异常");
        }
        // 通知用户配送中
        Orders order = orderMapper.getById(orderId);
        webSocketServer.sendToUser(order.getUserId().toString(), "骑手已经取货，正在火速配送中");
    }
    
    public List<Map<String, Object>> getDailyStats(Long merchantId) {
        return orderMapper.getDailyStats(merchantId);
    }

    public List<Map<String, Object>> getTopDishes(Long merchantId) {
        return orderMapper.getTopDishes(merchantId);
    }
    
    public List<Orders> getRiderActiveOrders(Long riderId) {
        return orderMapper.getRiderActiveOrders(riderId);
    }
    
    public void finishOrder(String orderId) {
        updateStatus(orderId, OrderStateMachine.STATUS_COMPLETED);
    }
    
    public void cancelOrder(String orderId) {
        updateStatus(orderId, OrderStateMachine.STATUS_CANCELLED_BY_USER);
    }

    public void merchantCancelOrder(String orderId) {
        updateStatus(orderId, OrderStateMachine.STATUS_CANCELLED_BY_MERCHANT);
    }
    
    // 骑手相关新功能
    public List<Orders> getRiderHistoryOrders(Long riderId) {
        return orderMapper.getRiderHistoryOrders(riderId);
    }
    
    public List<Map<String, Object>> getRiderDailyStats(Long riderId) {
        return orderMapper.getRiderDailyStats(riderId);
    }
    
    public Map<String, Object> getRiderIncomeStats(Long riderId) {
        Map<String, Object> map = new HashMap<>();
        map.put("today", orderMapper.getRiderDayCount(riderId) * 5);
        map.put("week", orderMapper.getRiderWeekCount(riderId) * 5);
        map.put("month", orderMapper.getRiderMonthCount(riderId) * 5);
        map.put("total", orderMapper.getRiderTotalCount(riderId) * 5);
        return map;
    }
    
    // 商家相关新功能
    public Map<String, Object> getMerchantIncomeStats(Long merchantId) {
        BigDecimal today = orderMapper.getMerchantDayIncome(merchantId);
        BigDecimal week = orderMapper.getMerchantWeekIncome(merchantId);
        BigDecimal month = orderMapper.getMerchantMonthIncome(merchantId);
        BigDecimal total = orderMapper.getMerchantTotalIncome(merchantId);
        
        // 获取昨日收入用于计算趋势
        java.time.LocalDate yesterdayDate = java.time.LocalDate.now().minusDays(1);
        BigDecimal yesterday = orderMapper.getMerchantDailyIncome(merchantId, yesterdayDate.toString());
        
        // 获取上周和上月收入
        BigDecimal lastWeek = orderMapper.getMerchantLastWeekIncome(merchantId);
        BigDecimal lastMonth = orderMapper.getMerchantLastMonthIncome(merchantId);
        
        // 计算趋势百分比
        double dayTrend = calculateTrend(today, yesterday);
        double weekTrend = calculateTrend(week, lastWeek);
        double monthTrend = calculateTrend(month, lastMonth);

        Map<String, Object> map = new HashMap<>();
        map.put("today", today);
        map.put("week", week);
        map.put("month", month);
        map.put("total", total);
        map.put("dayTrend", dayTrend);
        map.put("weekTrend", weekTrend);
        map.put("monthTrend", monthTrend);
        return map;
    }

    private double calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous != null && previous.compareTo(BigDecimal.ZERO) > 0) {
            return current.subtract(previous)
                    .divide(previous, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .doubleValue();
        } else if (current != null && current.compareTo(BigDecimal.ZERO) > 0) {
            return 100.0;
        }
        return 0.0;
    }
}
