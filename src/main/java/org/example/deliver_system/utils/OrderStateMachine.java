package org.example.deliver_system.utils;

public class OrderStateMachine {

    // 状态定义
    public static final int STATUS_PENDING_PAYMENT = 0; // 待支付
    public static final int STATUS_WAITING_ACCEPT = 1;  // 待接单
    public static final int STATUS_COOKING = 2;         // 商家已接单/制作中 (此时骑手可抢单)
    public static final int STATUS_WAITING_DELIVERY = 3;// 商家已出餐/待取货 (骑手也可抢单，抢单后变为4)
    public static final int STATUS_DELIVERING = 4;      // 骑手已取货/配送中
    public static final int STATUS_COMPLETED = 5;       // 已完成
    public static final int STATUS_CANCELLED_BY_USER = 6; // 用户取消
    public static final int STATUS_CANCELLED_BY_MERCHANT = 7; // 商家取消

    /**
     * 检查状态流转是否合法
     * @param currentStatus 当前状态
     * @param nextStatus 目标状态
     * @return true 合法, false 非法
     */
    public static boolean canTransition(Integer currentStatus, Integer nextStatus) {
        if (currentStatus == null || nextStatus == null) return false;

        // 待支付 -> 待接单
        if (currentStatus == STATUS_PENDING_PAYMENT && nextStatus == STATUS_WAITING_ACCEPT) return true;
        
        // 待接单 -> 制作中 (商家接单)
        if (currentStatus == STATUS_WAITING_ACCEPT && nextStatus == STATUS_COOKING) return true;

        // 制作中 -> 待取货 (商家出餐)
        if (currentStatus == STATUS_COOKING && nextStatus == STATUS_WAITING_DELIVERY) return true;
        
        // 制作中/待取货 -> 配送中 (骑手取货确认，前提是骑手已接单)
        // 注意：这里需要配合 rider_id 判断，状态机仅判断状态值
        if ((currentStatus == STATUS_COOKING || currentStatus == STATUS_WAITING_DELIVERY) 
                && nextStatus == STATUS_DELIVERING) return true;

        // 配送中 -> 已完成
        if (currentStatus == STATUS_DELIVERING && nextStatus == STATUS_COMPLETED) return true;

        // 取消逻辑
        if ((currentStatus == STATUS_PENDING_PAYMENT || currentStatus == STATUS_WAITING_ACCEPT) 
                && (nextStatus == STATUS_CANCELLED_BY_USER || nextStatus == STATUS_CANCELLED_BY_MERCHANT)) return true;

        return false;
    }
}
