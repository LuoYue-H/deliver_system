package org.example.deliver_system.mapper;

import org.apache.ibatis.annotations.*;
import org.example.deliver_system.entity.MerchantDetails;

@Mapper
public interface MerchantDetailsMapper {

    @Insert("INSERT INTO merchant_details (user_id, status) VALUES (#{userId}, #{status})")
    void insert(MerchantDetails details);

    @Select("SELECT * FROM merchant_details WHERE user_id = #{userId}")
    MerchantDetails findByUserId(Long userId);

    @Update("UPDATE merchant_details SET license_url = #{licenseUrl}, id_card_front_url = #{idCardFrontUrl}, id_card_back_url = #{idCardBackUrl}, status = #{status}, category = #{category}, submit_time = #{submitTime} WHERE user_id = #{userId}")
    void updateReviewInfo(MerchantDetails details);

    @Update("UPDATE merchant_details SET category = #{category} WHERE user_id = #{userId}")
    void updateCategory(@Param("userId") Long userId, @Param("category") String category);

    @Update("UPDATE merchant_details SET status = #{status} WHERE user_id = #{userId}")
    void updateStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Update("UPDATE merchant_details SET status = #{status}, reject_reason = #{reason} WHERE user_id = #{userId}")
    void updateStatusAndReason(@Param("userId") Long userId, @Param("status") Integer status, @Param("reason") String reason);
}
