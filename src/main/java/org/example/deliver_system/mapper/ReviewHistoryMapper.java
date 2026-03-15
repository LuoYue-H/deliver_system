package org.example.deliver_system.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.deliver_system.entity.ReviewHistory;

import java.util.List;

@Mapper
public interface ReviewHistoryMapper {

    @Insert("INSERT INTO review_history (merchant_id, operator_id, action, comment, create_time) VALUES (#{merchantId}, #{operatorId}, #{action}, #{comment}, NOW())")
    void insert(ReviewHistory history);

    @Select("SELECT * FROM review_history WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<ReviewHistory> findByMerchantId(Long merchantId);
}
