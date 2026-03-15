package org.example.deliver_system.dto;

import java.math.BigDecimal;

public class CartItemDTO {
    private Long dishId;
    private Integer number;
    
    // 冗余字段方便计算，实际应查库，但为了演示方便
    // 实际项目中不能信赖前端传的价格
    
    // Getters and Setters
    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }
    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }
}
