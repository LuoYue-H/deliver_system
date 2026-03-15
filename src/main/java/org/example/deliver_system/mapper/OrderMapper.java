package org.example.deliver_system.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.deliver_system.entity.Orders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders(id, user_id, merchant_id, status, total_amount, address, contact_name, contact_phone, create_time) " +
            "VALUES(#{id}, #{userId}, #{merchantId}, #{status}, #{totalAmount}, #{address}, #{contactName}, #{contactPhone}, NOW())")
    void insert(Orders order);

    @Select("SELECT * FROM orders WHERE id = #{id}")
    Orders getById(String id);

    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") Integer status);

    @Update("UPDATE orders SET finish_time = NOW() WHERE id = #{id}")
    void updateFinishTime(@Param("id") String id);
    
    @Select("SELECT * FROM orders WHERE merchant_id = #{merchantId} AND status IN (1, 2, 3, 4) ORDER BY create_time DESC")
    List<Orders> getByMerchantId(Long merchantId);

    @Select("SELECT * FROM orders WHERE merchant_id = #{merchantId} AND status IN (5, 6, 7) ORDER BY create_time DESC")
    List<Orders> getAllByMerchantId(Long merchantId);
    
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Orders> getByUserId(Long userId);
    
    // 查询所有待抢单(status=2制作中 或 status=3待取货 且 rider_id 为空)
    // 只要没进入配送中(4)，骑手都可以抢单
    @Select("SELECT o.*, u.username as merchantName FROM orders o JOIN sys_user u ON o.merchant_id = u.id " +
            "WHERE (o.status = 2 OR o.status = 3) AND o.rider_id IS NULL")
    List<Orders> getPendingOrders();

    // 骑手抢单：只更新 rider_id，状态保持不变(如果是2还是2，如果是3还是3)，或者根据业务需求
    // 用户反馈：骑手接单后不应直接变为配送中。
    // 逻辑修正：骑手抢单 -> 绑定 rider_id。状态是否变？
    // 如果商家还在制作中(2)，骑手抢单后，状态仍为2，但有了 rider_id。
    // 如果商家已出餐(3)，骑手抢单后，状态仍为3，但有了 rider_id。
    // 只有骑手“确认取货”后，状态才变为 4(配送中)。
    // 这里简化：抢单动作仅绑定 rider_id，不改变 status (或者根据当前status判断)。
    // 为了简化前端交互，我们假设抢单后，如果商家已出餐，则骑手可以直接点“取货”变配送中。
    // 这里仅 update rider_id
    @Update("UPDATE orders SET rider_id = #{riderId} WHERE id = #{id} AND rider_id IS NULL AND (status = 2 OR status = 3)")
    int takeOrder(@Param("id") String id, @Param("riderId") Long riderId);
    
    // 骑手确认取货 (状态 3 -> 4)，必须是商家出餐后
    @Update("UPDATE orders SET status = 4, delivery_time = NOW() WHERE id = #{id} AND rider_id = #{riderId} AND status = 3")
    int riderTakeMeal(@Param("id") String id, @Param("riderId") Long riderId);

    // 统计商家每日概览 (营业额、完成单数、取消单数)
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') as date, " +
            "SUM(CASE WHEN status = 5 THEN total_amount ELSE 0 END) as amount, " +
            "COUNT(CASE WHEN status = 5 THEN 1 END) as completed, " +
            "COUNT(CASE WHEN status IN (6, 7) THEN 1 END) as cancelled " +
            "FROM orders " +
            "WHERE merchant_id = #{merchantId} " + 
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') " +
            "ORDER BY date DESC LIMIT 30")
    List<Map<String, Object>> getDailyStats(Long merchantId);

    // 统计商家销量最高的菜品 Top 10
    @Select("SELECT od.dish_name as name, SUM(od.number) as value " +
            "FROM order_detail od " +
            "JOIN orders o ON od.order_id = o.id " +
            "WHERE o.merchant_id = #{merchantId} AND o.status = 5 " +
            "GROUP BY od.dish_name " +
            "ORDER BY value DESC LIMIT 10")
    List<Map<String, Object>> getTopDishes(Long merchantId);
    
    // 查询骑手当前配送中的订单 (status=4 配送中, 或者是已接单但未取货的 2/3 也算活跃订单?)
    // 骑手端“我的配送”应该包含：已接单(2/3) 和 配送中(4)
    @Select("SELECT o.*, u.username as merchantName, u.phone as merchantPhone " +
            "FROM orders o " +
            "JOIN sys_user u ON o.merchant_id = u.id " +
            "WHERE o.rider_id = #{riderId} AND o.status IN (2, 3, 4)")
    List<Orders> getRiderActiveOrders(Long riderId);
    
    // 骑手历史订单 (status=5 已完成)
    @Select("SELECT o.*, u.username as merchantName, u.phone as merchantPhone " +
            "FROM orders o " +
            "JOIN sys_user u ON o.merchant_id = u.id " +
            "WHERE o.rider_id = #{riderId} AND o.status IN (5, 6, 7) ORDER BY o.create_time DESC")
    List<Orders> getRiderHistoryOrders(Long riderId);
    
    @Select("SELECT COUNT(*) FROM orders WHERE rider_id = #{riderId} AND status = 5 AND DATE(finish_time) = CURDATE()")
    Integer getRiderDayCount(Long riderId);
    
    @Select("SELECT COUNT(*) FROM orders WHERE rider_id = #{riderId} AND status = 5 AND YEARWEEK(finish_time, 1) = YEARWEEK(CURDATE(), 1)")
    Integer getRiderWeekCount(Long riderId);
    
    @Select("SELECT COUNT(*) FROM orders WHERE rider_id = #{riderId} AND status = 5 AND DATE_FORMAT(finish_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m')")
    Integer getRiderMonthCount(Long riderId);
    
    @Select("SELECT COUNT(*) FROM orders WHERE rider_id = #{riderId} AND status = 5")
    Integer getRiderTotalCount(Long riderId);
    
    // 统计骑手每日完成单数 (status=5)
    @Select("SELECT DATE_FORMAT(finish_time, '%Y-%m-%d') as date, COUNT(*) as count " +
            "FROM orders " +
            "WHERE rider_id = #{riderId} AND status = 5 " +
            "GROUP BY DATE_FORMAT(finish_time, '%Y-%m-%d') " +
            "ORDER BY date DESC LIMIT 7")
    List<Map<String, Object>> getRiderDailyStats(Long riderId);
    
    // 商家收入统计 (status=5)
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE merchant_id = #{merchantId} AND status = 5 AND DATE(finish_time) = CURDATE()")
    BigDecimal getMerchantDayIncome(Long merchantId);
    
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE merchant_id = #{merchantId} AND status = 5 AND YEARWEEK(finish_time, 1) = YEARWEEK(CURDATE(), 1)")
    BigDecimal getMerchantWeekIncome(Long merchantId);
    
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE merchant_id = #{merchantId} AND status = 5 AND DATE_FORMAT(finish_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m')")
    BigDecimal getMerchantMonthIncome(Long merchantId);
    
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE merchant_id = #{merchantId} AND status = 5")
    BigDecimal getMerchantTotalIncome(Long merchantId);

    // 统计商家上周营业额
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE merchant_id = #{merchantId} AND status = 5 AND YEARWEEK(finish_time, 1) = YEARWEEK(DATE_SUB(CURDATE(), INTERVAL 1 WEEK), 1)")
    BigDecimal getMerchantLastWeekIncome(Long merchantId);

    // 统计商家上月营业额
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE merchant_id = #{merchantId} AND status = 5 AND DATE_FORMAT(finish_time, '%Y%m') = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m')")
    BigDecimal getMerchantLastMonthIncome(Long merchantId);

    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE merchant_id = #{merchantId} AND status = 5 AND DATE(finish_time) = #{date}")
    BigDecimal getMerchantDailyIncome(@Param("merchantId") Long merchantId, @Param("date") String date);

    @Select("SELECT COUNT(*) FROM orders WHERE merchant_id = #{merchantId} AND status = #{status}")
    long countByStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    @Update("UPDATE orders SET status = 6, finish_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND status < 5")
    int userCancelOrder(@Param("id") String id, @Param("userId") Long userId);
}
