<template>
  <div class="delivery-container">
    <div class="delivery-header">
      <h2 class="page-title">我的配送</h2>
      <el-button type="primary" :loading="loading" @click="fetchActiveOrders" round>
        <el-icon class="el-icon--left"><Refresh /></el-icon>刷新任务
      </el-button>
    </div>
    
    <div v-loading="loading">
      <el-empty v-if="orders.length === 0" description="暂无配送任务，去抢单大厅看看吧">
        <template #extra>
          <el-button type="primary" @click="router.push('/rider/hall')">前往抢单</el-button>
        </template>
      </el-empty>

      <div v-else class="task-list">
        <el-card v-for="order in orders" :key="order.id" class="task-card" shadow="hover">
          <div class="task-main">
            <div class="task-status">
              <el-steps :active="getStepActive(order.status)" finish-status="success" align-center>
                <el-step title="待取货" />
                <el-step title="配送中" />
                <el-step title="已送达" />
              </el-steps>
            </div>
            
            <el-divider content-position="left">订单 #{{ order.id }}</el-divider>
            
            <el-row :gutter="40">
              <!-- 商家信息 (取货) -->
              <el-col :span="12">
                <div class="info-section">
                  <div class="section-title">
                    <el-icon color="#ff9900"><Shop /></el-icon>
                    <span>商家取货</span>
                  </div>
                  <div class="section-content">
                    <p class="name">{{ order.merchantName }}</p>
                    <p class="phone" @click="makeCall(order.merchantPhone)">
                      <el-icon><Phone /></el-icon> {{ order.merchantPhone }}
                    </p>
                    <p class="address">商家已在备餐，请及时前往</p>
                  </div>
                </div>
              </el-col>
              
              <!-- 客户信息 (送货) -->
              <el-col :span="12">
                <div class="info-section">
                  <div class="section-title">
                    <el-icon color="#409eff"><User /></el-icon>
                    <span>客户送达</span>
                  </div>
                  <div class="section-content">
                    <p class="name">{{ order.contactName }}</p>
                    <p class="phone" @click="makeCall(order.contactPhone)">
                      <el-icon><Phone /></el-icon> {{ order.contactPhone }}
                    </p>
                    <p class="address">{{ order.address }}</p>
                  </div>
                </div>
              </el-col>
            </el-row>
            
            <div class="task-actions">
              <div class="time-info">
                <el-icon><Timer /></el-icon>
                <span>下单时间: {{ formatTime(order.createTime) }}</span>
              </div>
              <div class="btn-group">
                <el-button 
                  v-if="order.status === 2 || order.status === 3" 
                  type="primary" 
                  size="large"
                  @click="takeMeal(order.id)"
                  :disabled="order.status === 2"
                >
                  {{ order.status === 2 ? '商家备餐中...' : '确认取货' }}
                </el-button>
                <el-button 
                  v-if="order.status === 4" 
                  type="success" 
                  size="large"
                  @click="finishOrder(order.id)"
                >
                  确认送达
                </el-button>
                <el-button type="info" plain size="large" @click="router.push('/rider/map')">
                  查看路线
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Shop, User, Phone, Timer } from '@element-plus/icons-vue'
import request from '../../utils/request'

const router = useRouter()
const orders = ref<any[]>([])
const loading = ref(false)

const fetchActiveOrders = async () => {
    loading.value = true
    try {
        const res: any = await request.get('/order/list/rider/active')
        orders.value = res.data
    } catch(e) {
    } finally {
        setTimeout(() => { loading.value = false }, 500)
    }
}

const takeMeal = async (id: string) => {
    try {
        await request.put(`/order/rider/takeMeal/${id}`)
        ElMessage.success('取货成功，请注意配送安全')
        fetchActiveOrders()
    } catch(e) {}
}

const finishOrder = (id: string) => {
    ElMessageBox.confirm('确认商品已安全送达用户手中?', '配送确认', {
        confirmButtonText: '确认送达',
        cancelButtonText: '取消',
        type: 'success',
        roundButton: true
    }).then(async () => {
        try {
            await request.put(`/order/finish/${id}`)
            ElMessage({
                message: '订单已完成，配送费已结算',
                type: 'success'
            })
            fetchActiveOrders()
        } catch(e) {}
    })
}

const makeCall = (phone: string) => {
    if (!phone) return
    ElMessage.info(`正在拨打: ${phone} (模拟)`)
}

const getStepActive = (status: number) => {
    if (status === 2 || status === 3) return 1
    if (status === 4) return 2
    if (status === 5) return 3
    return 0
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
    fetchActiveOrders()
})
</script>

<style scoped>
.delivery-container {
  max-width: 1000px;
  margin: 0 auto;
}

.delivery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.task-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
}

.task-status {
  padding: 20px 0 30px;
}

.info-section {
  padding: 16px;
  background-color: #f8f9fb;
  border-radius: 8px;
  height: 100%;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #303133;
}

.section-content p {
  margin: 6px 0;
  font-size: 14px;
  color: #606266;
}

.section-content .name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.section-content .phone {
  color: #409eff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.task-actions {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.btn-group {
  display: flex;
  gap: 12px;
}

.btn-group .el-button {
  min-width: 120px;
}
</style>
