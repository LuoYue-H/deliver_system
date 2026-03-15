package org.example.deliver_system.mapper;

import org.apache.ibatis.annotations.*;
import org.example.deliver_system.entity.AddressBook;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    @Select("SELECT * FROM address_book WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<AddressBook> findByUserId(Long userId);

    @Insert("INSERT INTO address_book(user_id, address, contact_name, contact_phone, is_default, create_time, update_time) " +
            "VALUES(#{userId}, #{address}, #{contactName}, #{contactPhone}, #{isDefault}, NOW(), NOW())")
    void insert(AddressBook addressBook);

    @Update("UPDATE address_book SET address = #{address}, contact_name = #{contactName}, contact_phone = #{contactPhone}, is_default = #{isDefault}, update_time = NOW() WHERE id = #{id}")
    void update(AddressBook addressBook);

    @Delete("DELETE FROM address_book WHERE id = #{id}")
    void deleteById(Long id);

    @Update("UPDATE address_book SET is_default = 0 WHERE user_id = #{userId}")
    void clearDefaultByUserId(Long userId);
    
    @Select("SELECT * FROM address_book WHERE id = #{id}")
    AddressBook findById(Long id);
}
