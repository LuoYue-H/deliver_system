<template>
  <el-card shadow="hover" class="order-card-item">
    <div class="card-header-row">
      <div class="order-no">
        <el-icon><Ticket /></el-icon>
        <span>订单号: {{ order.id }}</span>
      </div>
      <el-tag :type="getStatusType(order.status)" size="small" effect="dark" round>
        {{ getStatusText(order.status) }}
      </el-tag>
    </div>

    <div class="card-body">
      <div class="info-row">
        <el-icon class="info-icon"><Location /></el-icon>
        <span class="info-content">{{ order.address }}</span>
      </div>
      <div class="info-row">
        <el-icon class="info-icon"><User /></el-icon>
        <span class="info-content">{{ order.contactName }} ({{ order.contactPhone }})</span>
      </div>
      <div class="info-row time-row">
        <el-icon class="info-icon"><Timer /></el-icon>
        <span class="info-content">{{ formatFullTime(order.createTime) }}</span>
      </div>

      <el-collapse class="details-collapse">
        <el-collapse-item>
          <template #title>
            <div class="details-title">
              <span>共 {{ totalItems }} 件商品</span>
              <span class="total-price">¥{{ order.totalAmount.toFixed(2) }}</span>
            </div>
          </template>
          <div class="dish-list">
            <div v-for="item in order.details" :key="item.id" class="dish-item">
              <span class="dish-name">{{ item.dishName }}</span>
              <div class="dish-right">
                <span class="dish-num">x{{ item.number }}</span>
                <span class="dish-price">¥{{ (item.dishPrice * item.number).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <div class="card-footer">
      <div class="footer-left">
        <el-button 
          v-if="order.status === 1 || order.status === 2" 
          link 
          type="danger" 
          size="small" 
          @click="$emit('cancel', order.id)"
        >取消订单</el-button>
      </div>
      <div class="footer-right">
        <el-button 
          v-if="order.status === 1" 
          type="primary" 
          size="default" 
          round
          @click="$emit('accept', order.id, 2)"
        >确认接单</el-button>
        <el-button 
          v-if="order.status === 2" 
          type="success" 
          size="default" 
          round
          @click="$emit('finish', order.id, 3)"
        >备餐完毕</el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { defineProps, defineEmits, computed } from 'vue';
import { Location, User, Timer, Ticket } from '@element-plus/icons-vue';

const props = defineProps({
  order: { type: Object, required: true }
});

defineEmits(['accept', 'finish', 'cancel']);

const totalItems = computed(() => {
  return props.order.details?.reduce((sum: number, item: any) => sum + item.number, 0) || 0;
});

const getStatusText = (status: number) => {
  const map: any = {
    1: '待接单',
    2: '备餐中',
    3: '待配送',
    4: '配送中',
    5: '已完成',
    6: '已取消'
  };
  return map[status] || '未知';
};

const getStatusType = (status: number) => {
  const map: any = {
    1: 'warning',
    2: 'primary',
    3: 'info',
    4: 'success',
    5: 'success',
    6: 'danger'
  };
  return map[status] || 'info';
};

const formatFullTime = (timeStr: string) => {
  if (!timeStr) return 'N/A';
  const date = new Date(timeStr);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const seconds = date.getSeconds().toString().padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};
</script>

<style scoped>
.order-card-item {
  margin-bottom: 20px;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
}

.order-card-item:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  border-color: #409eff;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
}

.order-no {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.card-body {
  padding: 5px 0;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.info-icon {
  margin-top: 3px;
  font-size: 16px;
  color: #909399;
}

.info-content {
  line-height: 1.4;
  word-break: break-all;
}

.time-row {
  color: #909399;
  font-size: 13px;
}

.details-collapse {
  margin-top: 15px;
  border: none;
}

:deep(.el-collapse-item__header) {
  height: 40px;
  background-color: #f8f9fa;
  padding: 0 12px;
  border-radius: 6px;
  border: none;
}

:deep(.el-collapse-item__wrap) {
  border: none;
}

.details-title {
  display: flex;
  justify-content: space-between;
  width: 100%;
  padding-right: 15px;
  font-size: 13px;
  font-weight: 500;
}

.total-price {
  color: #f56c6c;
  font-weight: 700;
}

.dish-list {
  padding: 10px 12px;
  background-color: #fafafa;
  border-radius: 0 0 6px 6px;
}

.dish-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
  color: #606266;
}

.dish-item:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.dish-right {
  display: flex;
  gap: 15px;
  align-items: center;
}

.dish-num {
  color: #909399;
}

.dish-price {
  font-weight: 500;
  color: #303133;
  min-width: 60px;
  text-align: right;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f2f5;
}

.footer-right {
  display: flex;
  gap: 10px;
}
</style>
