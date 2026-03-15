package org.example.deliver_system.controller;

import org.example.deliver_system.common.Result;
import org.example.deliver_system.entity.Category;
import org.example.deliver_system.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Result<String> add(@RequestBody Category category, @RequestAttribute("userId") Long userId) {
        category.setMerchantId(userId);
        categoryService.addCategory(category);
        return Result.success("分类添加成功");
    }

    @GetMapping("/list")
    public Result<List<Category>> list(@RequestAttribute("userId") Long userId) {
        return Result.success(categoryService.getCategoriesByMerchant(userId));
    }

    @GetMapping("/list/{merchantId}")
    public Result<List<Category>> listByMerchant(@PathVariable Long merchantId) {
        return Result.success(categoryService.getCategoriesByMerchant(merchantId));
    }

    @PutMapping
    public Result<String> update(@RequestBody Category category, @RequestAttribute("userId") Long userId) {
        // 安全校验，确保商家只能修改自己的分类
        category.setMerchantId(userId);
        categoryService.updateCategory(category);
        return Result.success("分类更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        categoryService.deleteCategory(id, userId);
        return Result.success("分类删除成功");
    }
}
