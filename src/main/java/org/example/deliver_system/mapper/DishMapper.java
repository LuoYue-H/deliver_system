package org.example.deliver_system.mapper;

import org.apache.ibatis.annotations.*;
import org.example.deliver_system.entity.Dish;

import java.util.List;

@Mapper
public interface DishMapper {

    @Select("SELECT * FROM dish WHERE id = #{id}")
    Dish getById(Long id);

    @Select("SELECT d.* FROM dish d " +
            "JOIN category c ON d.category_id = c.id " +
            "WHERE c.merchant_id = #{merchantId}")
    List<Dish> getByMerchantId(Long merchantId);

    @Insert("INSERT INTO dish(name, category_id, price, image, description, status, stock, create_time, update_time) " +
            "VALUES(#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{stock}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Dish dish);

    @Update("UPDATE dish SET name=#{name}, price=#{price}, image=#{image}, description=#{description}, " +
            "status=#{status}, stock=#{stock}, update_time=NOW() WHERE id=#{id}")
    void update(Dish dish);

    @Delete("DELETE FROM dish WHERE id = #{id}")
    void delete(Long id);
    
    @Update("UPDATE dish SET stock = stock - #{number} WHERE id = #{id} AND stock >= #{number}")
    int deductStock(@Param("id") Long id, @Param("number") Integer number);

    @Update("UPDATE dish SET stock = stock + #{number} WHERE id = #{id}")
    void addStock(@Param("id") Long id, @Param("number") Integer number);

    @Update("UPDATE dish SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
