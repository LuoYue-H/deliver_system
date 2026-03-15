<template>
  <div class="hall-container">
    <div class="hall-header">
      <div class="header-left">
        <h2 class="page-title">抢单大厅</h2>
        <span class="order-count">当前有 {{ orders.length }} 个待抢订单</span>
      </div>
      <el-button 
        type="primary" 
        :loading="loading" 
        @click="fetchOrders" 
        class="refresh-btn"
        round
      >
        <el-icon class="el-icon--left"><Refresh /></el-icon>刷新列表
      </el-button>
    </div>
    
    <div v-loading="loading">
      <el-row :gutter="20" v-if="orders.length > 0">
        <el-col :xs="24" :sm="12" :md="8" :lg="8" v-for="order in orders" :key="order.id">
          <el-card class="order-card" shadow="hover">
            <div class="card-tag">
              <el-tag type="warning" effect="dark" size="small">待抢单</el-tag>
              <span class="distance">距离您 {{ (Math.random() * 2 + 0.1).toFixed(1) }} km</span>
            </div>
            
            <div class="merchant-info">
              <h3 class="merchant-name">{{ order.merchantName }}</h3>
              <p class="order-time">{{ getTimeAgo(order.createTime) }} 前发布</p>
            </div>
            
            <div class="delivery-info">
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <span class="address-text">{{ order.address }}</span>
              </div>
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span>{{ order.contactName }}</span>
              </div>
            </div>
            
            <div class="order-footer">
              <div class="income-box">
                <span class="income-label">预计收入</span>
                <span class="income-value">¥5.00</span>
              </div>
              <el-button 
                type="warning" 
                class="grab-btn" 
                @click="takeOrder(order.id)"
              >
                立即抢单
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <div v-else class="empty-state">
        <el-empty description="当前暂无待抢订单">
          <template #image>
            <el-icon :size="80" color="#909399"><Coffee /></el-icon>
          </template>
          <p class="empty-tip">休息一下吧，新订单会通过通知提醒您</p>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Location, User, Coffee } from '@element-plus/icons-vue'
import request from '../../utils/request'

const orders = ref<any[]>([])
const loading = ref(false)

const fetchOrders = async () => {
    loading.value = true
    try {
        const res: any = await request.get('/order/pending')
        orders.value = res.data
    } catch(e) {
    } finally {
        setTimeout(() => { loading.value = false }, 500)
    }
}

const takeOrder = async (id: string) => {
    try {
        await request.put(`/order/take/${id}`)
        ElMessage({
            message: '抢单成功！请尽快前往商家取货',
            type: 'success',
            duration: 3000
        })
        fetchOrders()
    } catch (e: any) {
    }
}

const getTimeAgo = (time: any) => {
    if (!time) return ''
    const date = new Date(time)
    const now = new Date()
    const diff = Math.floor((now.getTime() - date.getTime()) / 1000)
    
    if (diff < 60) return '刚刚'
    if (diff < 3600) return `${Math.floor(diff / 60)}分钟`
    return `${Math.floor(diff / 3600)}小时`
}

const formatTime = (time: any) => {
    if (!time) return ''
    return new Date(time).toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    })
}

onMounted(() => {
    fetchOrders()
})
</script>

<style scoped>
.hall-container {
  max-width: 1200px;
  margin: 0 auto;
}

.hall-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.order-count {
  font-size: 14px;
  color: #909399;
}

.order-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
  transition: transform 0.3s;
}

.order-card:hover {
  transform: translateY(-5px);
}

.card-tag {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.distance {
  font-size: 12px;
  color: #ff9900;
  font-weight: bold;
}

.merchant-info {
  margin-bottom: 20px;
}

.merchant-name {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #303133;
}

.order-time {
  font-size: 12px;
  color: #909399;
}

.delivery-info {
  background-color: #f8f9fb;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .el-icon {
  margin-top: 3px;
  color: #909399;
}

.address-text {
  line-height: 1.4;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px dashed #ebeef5;
  padding-top: 16px;
}

.income-box {
  display: flex;
  flex-direction: column;
}

.income-label {
  font-size: 12px;
  color: #909399;
}

.income-value {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.grab-btn {
  padding: 10px 24px;
  font-weight: bold;
  border-radius: 8px;
}

.empty-state {
  padding: 60px 0;
}

.empty-tip {
  color: #909399;
  font-size: 14px;
}
</style>
