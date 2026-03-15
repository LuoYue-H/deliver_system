package org.example.deliver_system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.deliver_system.entity.User;

import java.util.List;

@Mapper
public interface MerchantMapper {
    @Select("SELECT * FROM sys_user WHERE role = 'MERCHANT'")
    List<User> getMerchantList();
}
