package org.example.deliver_system.mapper;

import org.apache.ibatis.annotations.*;
import org.example.deliver_system.dto.UserProfileDTO;
import org.example.deliver_system.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select * from sys_user where username = #{username}")
    User getByUsername(String username);

    @Update("UPDATE sys_user SET username = #{username}, phone = #{phone}, avatar = #{avatar} WHERE id = #{id}")
    void updateProfile(User user);

    @Insert("INSERT INTO sys_user(username, password, role) VALUES(#{username}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User getById(Long id);

    @Select("SELECT u.*, md.status AS md_status, md.license_url AS md_license, md.id_card_front_url AS md_id_front, md.id_card_back_url AS md_id_back, md.reject_reason AS md_reason, md.submit_time AS md_submit, md.category AS md_category " +
            "FROM sys_user u JOIN merchant_details md ON u.id = md.user_id WHERE u.role = 'MERCHANT' AND md.status = #{status}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "merchantDetails.userId", column = "user_id"),
            @Result(property = "merchantDetails.status", column = "md_status"),
            @Result(property = "merchantDetails.licenseUrl", column = "md_license"),
            @Result(property = "merchantDetails.idCardFrontUrl", column = "md_id_front"),
            @Result(property = "merchantDetails.idCardBackUrl", column = "md_id_back"),
            @Result(property = "merchantDetails.rejectReason", column = "md_reason"),
            @Result(property = "merchantDetails.submitTime", column = "md_submit"),
            @Result(property = "merchantDetails.category", column = "md_category")
    })
    List<UserProfileDTO> findMerchantsByStatus(Integer status);

    @Select("SELECT u.*, md.status, md.category FROM sys_user u JOIN merchant_details md ON u.id = md.user_id WHERE u.role = 'MERCHANT' AND md.status = 2")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "merchantDetails.status", column = "status"),
            @Result(property = "merchantDetails.category", column = "category")
    })
    List<UserProfileDTO> findApprovedMerchants();

    @Select("SELECT u.*, md.status AS md_status, md.license_url AS md_license, md.id_card_front_url AS md_id_front, md.id_card_back_url AS md_id_back, md.reject_reason AS md_reason, md.submit_time AS md_submit, md.category AS md_category " +
            "FROM sys_user u LEFT JOIN merchant_details md ON u.id = md.user_id ORDER BY u.create_time DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "merchantDetails.userId", column = "user_id"),
            @Result(property = "merchantDetails.status", column = "md_status"),
            @Result(property = "merchantDetails.licenseUrl", column = "md_license"),
            @Result(property = "merchantDetails.idCardFrontUrl", column = "md_id_front"),
            @Result(property = "merchantDetails.idCardBackUrl", column = "md_id_back"),
            @Result(property = "merchantDetails.rejectReason", column = "md_reason"),
            @Result(property = "merchantDetails.submitTime", column = "md_submit"),
            @Result(property = "merchantDetails.category", column = "md_category")
    })
    List<UserProfileDTO> findAllUsersWithDetails();

    @Select("SELECT * FROM sys_user WHERE role = #{role}")
    List<User> findUsersByRole(String role);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    void deleteById(Long id);

    @Update("<script>"
            + "UPDATE sys_user "
            + "<set>"
            + "<if test='username != null and username != \"\"'>username = #{username},</if>"
            + "<if test='password != null and password != \"\"'>password = #{password},</if>"
            + "</set>"
            + "WHERE id = #{id}"
            + "</script>")
    void updateUser(User user);


}
