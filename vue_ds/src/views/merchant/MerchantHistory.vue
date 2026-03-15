<template>
  <div class="history-container">
    <div v-if="userStore.merchantStatus === 2">
      <el-card>
        <!-- 筛选区域 -->
        <el-form :inline="true" :model="filters" class="filter-form">
          <el-form-item label="订单日期">
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="filters.status" placeholder="所有状态" clearable>
              <el-option label="已完成" :value="5" />
              <el-option label="学生取消" :value="6" />
              <el-option label="商家取消" :value="7" />
            </el-select>
          </el-form-item>
          <el-form-item label="订单号">
            <el-input v-model="filters.orderId" placeholder="输入订单号搜索" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
          </el-form-item>
        </el-form>

        <!-- 表格区域 -->
        <el-table 
          :data="pagedData" 
          style="width: 100%" 
          v-loading="loading"
          :default-sort="{ prop: 'createTime', order: 'descending' }"
          @sort-change="handleSortChange"
        >
          <el-table-column type="expand">
            <template #default="props">
              <div class="order-detail-expand">
                <h4>菜品详情</h4>
                <ul>
                  <li v-for="item in props.row.details" :key="item.id">
                    <span>{{ item.dishName }}</span>
                    <span>x {{ item.number }}</span>
                    <span>¥{{ item.dishPrice }}</span>
                  </li>
                </ul>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="id" label="订单号" />
          <el-table-column prop="contactName" label="联系人" />
          <el-table-column prop="createTime" label="开始时间" sortable="custom">
            <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column prop="finishTime" label="结束时间">
            <template #default="scope">{{ formatTime(scope.row.finishTime) }}</template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="订单金额" />
          <el-table-column label="状态">
            <template #default="scope">
              <el-tag :type="scope.row.status === 5 ? 'success' : (scope.row.status === 6 ? 'warning' : 'info')">
                {{ scope.row.status === 5 ? '已完成' : (scope.row.status === 6 ? '学生取消' : '商家取消') }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页与合计 -->
        <div class="footer-summary">
          <div class="summary-text">
            <span>总订单数: <strong>{{ filteredData.length }}</strong></span>
            <span>总收入: <strong class="total-price">¥{{ totalIncome.toFixed(2) }}</strong></span>
          </div>
          <el-pagination
            background
            layout="prev, pager, next"
            :total="filteredData.length"
            :page-size="pageSize"
            v-model:current-page="currentPage"
          />
        </div>
      </el-card>
    </div>
    
    <FeatureLock v-else />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import request from '../../utils/request';
import { useUserStore } from '../../store/user';
import FeatureLock from '../../components/merchant/FeatureLock.vue';

const userStore = useUserStore();
const allOrders = ref<any[]>([]);
const loading = ref(true);
const filters = ref({
  dateRange: null,
  status: null,
  orderId: ''
});

const currentPage = ref(1);
const pageSize = ref(10);

const filteredData = computed(() => {
  return allOrders.value.filter(order => {
    const isStatusMatch = !filters.value.status || order.status === filters.value.status;
    const isIdMatch = !filters.value.orderId || order.id.includes(filters.value.orderId);
    const isDateMatch = (() => {
      if (!filters.value.dateRange) return true;
      const orderDate = new Date(order.finishTime);
      return orderDate >= filters.value.dateRange[0] && orderDate <= filters.v-alue.dateRange[1];
    })();
    return isStatusMatch && isIdMatch && isDateMatch;
  });
});

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredData.value.slice(start, end);
});

const totalIncome = computed(() => {
  return filteredData.value
    .filter(order => order.status === 5)
    .reduce((sum, order) => sum + order.totalAmount, 0);
});

const fetchHistoryOrders = async () => {
  loading.value = true;
  try {
    const res: any = await request.get('/order/list/merchant/history');
    allOrders.value = res.data;
  } catch (e) {
    console.error("获取历史订单失败", e);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1; // 查询后返回第一页
};

const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  if (prop === 'createTime') {
    allOrders.value.sort((a, b) => {
      const timeA = new Date(a.createTime).getTime();
      const timeB = new Date(b.createTime).getTime();
      return order === 'ascending' ? timeA - timeB : timeB - timeA;
    });
  }
};

const formatTime = (timeStr: string) => {
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

onMounted(fetchHistoryOrders);
</script>

<style scoped>
.history-container { padding: 20px; }
.filter-form { margin-bottom: 20px; }
.order-detail-expand { padding: 15px; background-color: #f9f9f9; }
.order-detail-expand h4 { margin: 0 0 10px; }
.order-detail-expand ul { list-style: none; padding: 0; }
.order-detail-expand li { display: flex; justify-content: space-between; }
.footer-summary { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; }
.summary-text { font-size: 14px; color: #606266; }
.summary-text strong { margin: 0 5px; }
.summary-text .total-price { color: #f56c6c; font-size: 16px; }
</style>
