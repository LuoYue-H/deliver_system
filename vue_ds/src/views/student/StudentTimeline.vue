<template>
  <div class="timeline-container">
    <!-- 顶部状态切换 -->
    <div class="header-section">
      <div class="title-area">
        <h2 class="page-title">我的订单</h2>
        <p class="page-subtitle">随时查看您的美食配送进度</p>
      </div>
      <el-segmented v-model="activeTab" :options="tabOptions" size="large" class="status-segmented" />
    </div>

    <!-- 加载骨架屏 -->
    <div v-if="loading" class="skeleton-list">
      <el-card v-for="n in 3" :key="n" class="skeleton-card">
        <el-skeleton animated>
          <template #template>
            <div class="skeleton-header">
              <el-skeleton-item variant="text" style="width: 30%" />
              <el-skeleton-item variant="text" style="width: 15%" />
            </div>
            <div class="skeleton-body">
              <el-skeleton-item variant="rect" style="width: 100%; height: 80px; margin: 15px 0;" />
              <el-skeleton-item variant="text" style="width: 60%" />
            </div>
          </template>
        </el-skeleton>
      </el-card>
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredOrders.length === 0" class="empty-state">
      <el-empty :description="activeTab === 'ongoing' ? '暂无进行中的订单' : '暂无历史订单'">
        <el-button type="primary" round @click="router.push('/student/home')">去逛逛</el-button>
      </el-empty>
    </div>

    <!-- 订单列表 -->
    <div v-else class="orders-list">
      <transition-group name="list">
        <el-card 
          v-for="order in filteredOrders" 
          :key="order.id" 
          class="order-card"
          :class="{ 'ongoing-border': order.status < 5 }"
        >
          <!-- 订单头部：商家信息与状态 -->
          <div class="order-header">
            <div class="merchant-info" @click="reOrder(order.merchantId)">
              <el-avatar :size="40" :src="order.merchantAvatar || defaultMerchantAvatar" class="merchant-avatar" />
              <div class="merchant-detail">
                <span class="merchant-name">{{ order.merchantName || '未知商家' }}</span>
                <span class="order-time">{{ formatTime(order.createTime) }}</span>
              </div>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
            <div class="status-badge">
              <el-tag :type="getStatusType(order.status)" effect="dark" round>
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </div>

          <!-- 订单主体：商品简述 -->
          <div class="order-content">
            <div class="dish-preview">
              <div class="dish-names">
                <span v-for="(item, index) in order.details?.slice(0, 3)" :key="index">
                  {{ item.dishName }}{{ index < Math.min(order.details.length, 3) - 1 ? '、' : '' }}
                </span>
                <span v-if="order.details?.length > 3" class="more-items">等 {{ order.details.length }} 件商品</span>
              </div>
              <div class="order-price">
                <span class="currency">¥</span>
                <span class="amount">{{ order.totalAmount }}</span>
              </div>
            </div>

            <!-- 进度条 (仅进行中显示) -->
            <div v-if="order.status < 5 && order.status > 0" class="order-progress">
              <el-steps :active="getStatusStep(order.status)" finish-status="success" align-center simple>
                <el-step title="已接单" :icon="DocumentChecked" />
                <el-step title="制作中" :icon="Dish" />
                <el-step title="配送中" :icon="Van" />
              </el-steps>
            </div>

            <!-- 详情折叠 -->
            <el-collapse class="details-collapse">
              <el-collapse-item title="查看订单详情">
                <div class="detail-items">
                  <div v-for="item in order.details" :key="item.id" class="item-row">
                    <div class="item-left">
                      <el-image :src="item.dishImage || defaultDishImage" class="item-img" fit="cover" />
                      <span class="item-name">{{ item.dishName }}</span>
                    </div>
                    <div class="item-right">
                      <span class="item-count">x{{ item.number }}</span>
                      <span class="item-price">¥{{ item.dishPrice }}</span>
                    </div>
                  </div>
                </div>
                <div class="order-info-footer">
                  <div class="info-row"><span>订单编号：</span>{{ order.id }}</div>
                  <div class="info-row"><span>配送地址：</span>{{ order.address || '校内地址' }}</div>
                  <div class="info-row" v-if="order.finishTime"><span>完成时间：</span>{{ formatTime(order.finishTime) }}</div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>

          <!-- 订单操作 -->
          <div class="order-actions">
            <el-button v-if="order.status === 0" type="primary" round @click="payOrder(order)">立即支付</el-button>
            <el-button v-if="order.status < 2" plain round @click="cancelOrder(order.id)">取消订单</el-button>
            <el-button v-if="order.status === 4" type="primary" round @click="confirmReceipt(order.id)">确认收货</el-button>
            <el-button v-if="order.status >= 5" round @click="reOrder(order.merchantId)">再来一单</el-button>
          </div>
        </el-card>
      </transition-group>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import request from '../../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Check, Close, Van, MoreFilled, ArrowRight, 
  DocumentChecked, Dish 
} from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const allOrders = ref<any[]>([]);
const loading = ref(true);
const activeTab = ref('ongoing');

const defaultMerchantAvatar = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png';
const defaultDishImage = 'https://img.freepik.com/free-photo/top-view-circular-food-frame_23-2148723434.jpg';

const tabOptions = [
  { label: '进行中', value: 'ongoing' },
  { label: '历史订单', value: 'history' },
];

const filteredOrders = computed(() => {
  if (activeTab.value === 'ongoing') {
    return allOrders.value.filter(o => o.status < 5);
  } else {
    return allOrders.value.filter(o => o.status >= 5);
  }
});

const fetchOrders = async () => {
  loading.value = true;
  try {
    const res: any = await request.get('/order/list/user');
    allOrders.value = res.data.sort((a: any, b: any) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime());
  } catch (e) {
    console.error("获取订单失败", e);
  } finally {
    loading.value = false;
  }
};

const cancelOrder = (id: string) => {
  ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
    roundButton: true
  }).then(async () => {
    await request.put(`/order/user/cancel/${id}`);
    ElMessage.success('订单已成功取消');
    fetchOrders();
  });
};

const confirmReceipt = async (id: string) => {
  await request.put(`/order/finish/${id}`);
  ElMessage.success('感谢您的评价，祝您用餐愉快！');
  fetchOrders();
};

const reOrder = (merchantId: number) => {
  router.push(`/student/order?merchantId=${merchantId}`);
};

const payOrder = (order: any) => {
  ElMessage.info('正在对接支付系统...');
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return 'N/A';
  const date = new Date(timeStr);
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};

const getStatusStep = (status: number) => {
  if (status <= 1) return 1;
  if (status === 2) return 2;
  if (status >= 3) return 3;
  return 0;
};

const statusMap: { [key: number]: { text: string; type: 'success' | 'primary' | 'warning' | 'info'; icon: any } } = {
  0: { text: '待支付', type: 'warning', icon: MoreFilled },
  1: { text: '待接单', type: 'warning', icon: MoreFilled },
  2: { text: '制作中', type: 'primary', icon: Dish },
  3: { text: '待取货', type: 'primary', icon: Van },
  4: { text: '配送中', type: 'primary', icon: Van },
  5: { text: '已完成', type: 'success', icon: Check },
  6: { text: '已取消', type: 'info', icon: Close },
  7: { text: '商家取消', type: 'info', icon: Close }
};

const getStatusText = (status: number) => statusMap[status]?.text || '未知';
const getStatusType = (status: number) => statusMap[status]?.type || 'info';

onMounted(fetchOrders);
</script>

<style scoped>
.timeline-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.status-segmented {
  background-color: #f1f5f9;
  padding: 4px;
  border-radius: 12px;
}

/* 订单卡片样式 */
.order-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.ongoing-border {
  border-left: 4px solid #409eff;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.merchant-detail {
  display: flex;
  flex-direction: column;
}

.merchant-name {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.order-time {
  font-size: 12px;
  color: #94a3b8;
}

.arrow-icon {
  font-size: 14px;
  color: #cbd5e1;
}

.order-content {
  background-color: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.dish-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.dish-names {
  font-size: 15px;
  color: #334155;
  font-weight: 500;
  flex: 1;
}

.more-items {
  color: #94a3b8;
  font-size: 13px;
  margin-left: 8px;
}

.order-price {
  display: flex;
  align-items: baseline;
  gap: 2px;
  color: #ef4444;
}

.currency {
  font-size: 14px;
  font-weight: 700;
}

.amount {
  font-size: 20px;
  font-weight: 800;
}

.order-progress {
  margin: 20px 0;
  background: white;
  padding: 12px;
  border-radius: 8px;
}

:deep(.el-step.is-simple .el-step__title) {
  font-size: 12px;
}

.details-collapse {
  border: none;
  background: transparent;
}

:deep(.el-collapse-item__header) {
  background-color: transparent;
  height: 36px;
  border: none;
  color: #64748b;
  font-size: 13px;
}

:deep(.el-collapse-item__content) {
  padding-bottom: 10px;
}

.detail-items {
  padding: 10px 0;
}

.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-img {
  width: 40px;
  height: 40px;
  border-radius: 6px;
}

.item-name {
  font-size: 14px;
  color: #475569;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.item-count {
  font-size: 13px;
  color: #94a3b8;
}

.item-price {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
  width: 60px;
  text-align: right;
}

.order-info-footer {
  border-top: 1px dashed #e2e8f0;
  padding-top: 12px;
  margin-top: 10px;
}

.info-row {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 6px;
}

.info-row span {
  color: #64748b;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 列表动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.skeleton-card {
  margin-bottom: 20px;
  border-radius: 16px;
}

.skeleton-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

@media (max-width: 640px) {
  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .status-segmented {
    width: 100%;
  }
}
</style>
