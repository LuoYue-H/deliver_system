package org.example.deliver_system.service;

import org.example.deliver_system.entity.Category;
import org.example.deliver_system.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public void addCategory(Category category) {
        categoryMapper.insert(category);
    }

    public List<Category> getCategoriesByMerchant(Long merchantId) {
        return categoryMapper.getByMerchantId(merchantId);
    }

    public void updateCategory(Category category) {
        categoryMapper.update(category);
    }

    public void deleteCategory(Long id, Long merchantId) {
        // 可加入逻辑：检查该分类是否属于该商家
        categoryMapper.deleteById(id);
    }
}
