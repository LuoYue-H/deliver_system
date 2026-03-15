<template>
  <div class="dashboard-container">
    <div v-if="userStore.merchantStatus === 2" class="dashboard-content">
      <!-- 顶部标题与工具栏 -->
      <div class="header-toolbar">
        <div class="welcome-text">
          <h2>今日营业概览</h2>
          <p>实时监控您的店铺动态</p>
        </div>
        <div class="toolbar-actions">
          <el-switch
            v-model="enableVoice"
            active-text="语音提醒"
            inactive-text="静音"
            class="voice-switch"
          />
          <el-button :icon="Refresh" circle @click="fetchOrders" :loading="loading" />
        </div>
      </div>

      <!-- 顶部数据概览 -->
      <el-row :gutter="24" class="stats-cards">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-icon new-order"><el-icon><Bell /></el-icon></div>
              <div class="stat-text">
                <div class="stat-title">待接订单</div>
                <div class="stat-value">{{ pendingOrders.length }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-icon cooking"><el-icon><Dish /></el-icon></div>
              <div class="stat-text">
                <div class="stat-title">备餐中</div>
                <div class="stat-value">{{ cookingOrders.length }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-icon completed"><el-icon><CircleCheck /></el-icon></div>
              <div class="stat-text">
                <div class="stat-title">今日已完成</div>
                <div class="stat-value">{{ todayCompletedOrders.length }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card highlight">
            <div class="stat-item">
              <div class="stat-icon turnover"><el-icon><Money /></el-icon></div>
              <div class="stat-text">
                <div class="stat-title">今日预估收入</div>
                <div class="stat-value">¥{{ todayAmount }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 订单看板 -->
      <el-row :gutter="24" class="kanban-board">
        <!-- 新订单泳道 -->
        <el-col :span="12">
          <div class="kanban-lane-wrap">
            <div class="lane-header">
              <div class="lane-title">
                <span class="dot warning"></span>
                待处理新订单
                <el-badge :value="pendingOrders.length" :hidden="pendingOrders.length === 0" />
              </div>
            </div>
            <div class="lane-content">
              <transition-group name="order-list" tag="div" class="order-list-inner">
                <OrderCard 
                  v-for="order in pendingOrders" 
                  :key="order.id" 
                  :order="order" 
                  @accept="handleOrder" 
                  @cancel="handleCancel" 
                />
              </transition-group>
              <el-empty v-if="pendingOrders.length === 0" description="暂无新订单" :image-size="120" />
            </div>
          </div>
        </el-col>

        <!-- 备餐中泳道 -->
        <el-col :span="12">
          <div class="kanban-lane-wrap">
            <div class="lane-header">
              <div class="lane-title">
                <span class="dot primary"></span>
                正在备餐中
                <el-badge :value="cookingOrders.length" :hidden="cookingOrders.length === 0" />
              </div>
            </div>
            <div class="lane-content">
              <transition-group name="order-list" tag="div" class="order-list-inner">
                <OrderCard 
                  v-for="order in cookingOrders" 
                  :key="order.id" 
                  :order="order" 
                  @finish="handleOrder" 
                  @cancel="handleCancel" 
                />
              </transition-group>
              <el-empty v-if="cookingOrders.length === 0" description="暂无备餐中订单" :image-size="120" />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <FeatureLock v-else />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { ElNotification } from 'element-plus';
import { useUserStore } from '../../store/user';
import request from '../../utils/request';
import OrderCard from '../../components/OrderCard.vue';
import FeatureLock from '../../components/merchant/FeatureLock.vue';
import { Bell, Dish, Money, Refresh, CircleCheck } from '@element-plus/icons-vue';

const userStore = useUserStore();
const allOrders = ref<any[]>([]);
const loading = ref(false);
const enableVoice = ref(true);
let socket: WebSocket | null = null;

const pendingOrders = computed(() => allOrders.value.filter(o => o.status === 1).sort((a, b) => new Date(a.createTime).getTime() - new Date(b.createTime).getTime()));
const cookingOrders = computed(() => allOrders.value.filter(o => o.status === 2).sort((a, b) => new Date(a.createTime).getTime() - new Date(b.createTime).getTime()));
const todayCompletedOrders = computed(() => {
  const today = new Date().toDateString();
  return allOrders.value.filter(o => o.status === 5 && new Date(o.finishTime || o.createTime).toDateString() === today);
});

const todayAmount = computed(() => {
  return todayCompletedOrders.value.reduce((sum, o) => sum + o.totalAmount, 0).toFixed(2);
});

const fetchOrders = async () => {
  loading.value = true;
  try {
    const res: any = await request.get(`/order/list/merchant`);
    allOrders.value = res.data;
  } catch (e) {
    console.error('获取订单失败', e);
  } finally {
    loading.value = false;
  }
};

const initWebSocket = () => {
  const userId = userStore.userId;
  if (!userId) return;
  
  // 生产环境下建议使用相对路径或从配置中读取
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  socket = new WebSocket(`${protocol}//localhost:8088/ws/${userId}`);
  
  socket.onmessage = (event) => {
    let messageContent = event.data;
    try {
      const data = JSON.parse(event.data);
      if (data.type === 'NEW_ORDER') {
        messageContent = '您有新的外卖订单，请及时处理';
        if (enableVoice.value) speak(messageContent);
      } else {
        messageContent = data.message || messageContent;
      }
    } catch (e) {}
    
    ElNotification({ 
      title: '新订单提醒', 
      message: messageContent, 
      type: 'success',
      duration: 5000,
      position: 'bottom-right'
    });
    fetchOrders();
  };
};

const speak = (text: string) => {
  if (!window.speechSynthesis) return;
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = 'zh-CN';
  utterance.rate = 1.1;
  window.speechSynthesis.speak(utterance);
};

const handleOrder = async (id: string, status: number) => {
  try {
    await request.put(`/order/status/${id}/${status}`);
    ElNotification.success({
      title: '操作成功',
      message: status === 2 ? '已成功接单' : '已通知骑手取餐',
      duration: 2000
    });
    fetchOrders();
  } catch (e) {}
};

const handleCancel = async (id: string) => {
  try {
    await request.put(`/order/merchant/cancel/${id}`);
    ElNotification.success('订单已取消');
    fetchOrders();
  } catch (e) {}
};

onMounted(() => {
  initWebSocket();
  fetchOrders();
});

onUnmounted(() => {
  if (socket) socket.close();
});
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}

.dashboard-content {
  max-width: 1400px;
  margin: 0 auto;
}

.header-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.welcome-text h2 {
  margin: 0;
  font-size: 24px;
  color: #1e293b;
  font-weight: 700;
}

.welcome-text p {
  margin: 4px 0 0 0;
  color: #64748b;
  font-size: 14px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stats-cards {
  margin-bottom: 30px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card.highlight {
  background: linear-gradient(135deg, #ffffff 0%, #f0f9ff 100%);
  border: 1px solid #bae6fd;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.new-order { background: linear-gradient(135deg, #f87171 0%, #ef4444 100%); box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2); }
.cooking { background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%); box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2); }
.completed { background: linear-gradient(135deg, #34d399 0%, #10b981 100%); box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2); }
.turnover { background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%); box-shadow: 0 4px 12px rgba(245, 158, 11, 0.2); }

.stat-text .stat-title {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-text .stat-value {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
}

.kanban-board {
  height: calc(100vh - 320px);
}

.kanban-lane-wrap {
  background-color: #f8fafc;
  border-radius: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #e2e8f0;
}

.lane-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
  background-color: #fff;
  border-radius: 16px 16px 0 0;
}

.lane-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot.warning { background-color: #f59e0b; box-shadow: 0 0 0 4px rgba(245, 158, 11, 0.1); }
.dot.primary { background-color: #3b82f6; box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1); }

.lane-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.order-list-inner {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 列表过渡动画 */
.order-list-enter-active,
.order-list-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.order-list-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.order-list-leave-to {
  opacity: 0;
  transform: translateX(100px);
}

.order-list-move {
  transition: transform 0.5s ease;
}

/* 隐藏滚动条 */
.lane-content::-webkit-scrollbar {
  width: 6px;
}
.lane-content::-webkit-scrollbar-thumb {
  background-color: #e2e8f0;
  border-radius: 3px;
}
</style>
