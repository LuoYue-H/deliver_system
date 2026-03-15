package org.example.deliver_system.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.deliver_system.entity.OrderDetail;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {

    @Insert("<script>" +
            "INSERT INTO order_detail(order_id, dish_id, dish_name, dish_price, dish_image, number) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.orderId}, #{item.dishId}, #{item.dishName}, #{item.dishPrice}, #{item.dishImage}, #{item.number})" +
            "</foreach>" +
            "</script>")
    void batchInsert(List<OrderDetail> orderDetailList);

    @Select("SELECT * FROM order_detail WHERE order_id = #{orderId}")
    List<OrderDetail> findByOrderId(String orderId);

    @Select("SELECT od.dish_name, SUM(od.number) as total_sold FROM order_detail od " +
            "JOIN orders o ON od.order_id = o.id " +
            "WHERE o.merchant_id = #{merchantId} AND o.status = 5 " +
            "GROUP BY od.dish_name ORDER BY total_sold DESC LIMIT 10")
    List<Map<String, Object>> getTop10SellingDishes(Long merchantId);

    @Select("<script>" +
            "SELECT * FROM order_detail WHERE order_id IN " +
            "<foreach item='item' collection='orderIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<OrderDetail> findByOrderIds(@Param("orderIds") List<String> orderIds);
}
