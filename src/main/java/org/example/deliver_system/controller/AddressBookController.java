package org.example.deliver_system.controller;

import org.example.deliver_system.common.Result;
import org.example.deliver_system.entity.AddressBook;
import org.example.deliver_system.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @GetMapping("/list")
    public Result<List<AddressBook>> list(@RequestAttribute("userId") Long userId) {
        return Result.success(addressBookService.findByUserId(userId));
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody AddressBook addressBook, @RequestAttribute("userId") Long userId) {
        addressBook.setUserId(userId);
        addressBookService.save(addressBook);
        return Result.success("地址保存成功");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody AddressBook addressBook, @RequestAttribute("userId") Long userId) {
        addressBook.setUserId(userId); // 确保不会误改别人的地址
        addressBookService.update(addressBook);
        return Result.success("地址更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        addressBookService.deleteById(id);
        return Result.success("地址删除成功");
    }

    @PutMapping("/default/{id}")
    public Result<String> setDefault(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        addressBookService.setDefault(id, userId);
        return Result.success("默认地址设置成功");
    }
    
    @GetMapping("/{id}")
    public Result<AddressBook> getById(@PathVariable Long id) {
        return Result.success(addressBookService.findById(id));
    }
}
