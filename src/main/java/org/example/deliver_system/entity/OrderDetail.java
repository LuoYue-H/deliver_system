package org.example.deliver_system.entity;

import java.math.BigDecimal;

public class OrderDetail {
    private Long id;
    private String orderId;
    private Long dishId;
    private String dishName;
    private BigDecimal dishPrice;
    private String dishImage;
    private Integer number;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }
    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }
    public BigDecimal getDishPrice() { return dishPrice; }
    public void setDishPrice(BigDecimal dishPrice) { this.dishPrice = dishPrice; }
    public String getDishImage() { return dishImage; }
    public void setDishImage(String dishImage) { this.dishImage = dishImage; }
    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }
}
