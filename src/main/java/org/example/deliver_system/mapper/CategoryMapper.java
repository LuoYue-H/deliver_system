package org.example.deliver_system.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.example.deliver_system.entity.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Insert("INSERT INTO category(name, sort, merchant_id, create_time, update_time) " +
            "VALUES(#{name}, #{sort}, #{merchantId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Category category);

    @Select("SELECT * FROM category WHERE merchant_id = #{merchantId} ORDER BY sort ASC")
    List<Category> getByMerchantId(Long merchantId);
    
    @Select("SELECT * FROM category WHERE id = #{id}")
    Category getById(Long id);

    @Update("UPDATE category SET name = #{name}, sort = #{sort}, update_time = NOW() WHERE id = #{id}")
    void update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    void deleteById(Long id);
}
