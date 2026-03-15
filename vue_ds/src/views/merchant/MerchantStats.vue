<template>
  <div class="stats-page">
    <div v-if="userStore.merchantStatus === 2" class="stats-content">
      <!-- 顶部汇总指标 -->
      <el-row :gutter="24" class="summary-cards">
        <el-col :xs="24" :sm="12" :md="6" v-for="item in summaryData" :key="item.label">
          <el-card shadow="never" class="summary-card">
            <div class="summary-inner">
              <div class="summary-info">
                <span class="label">{{ item.label }}</span>
                <h3 class="value">{{ item.prefix }}{{ item.value }}</h3>
                <div v-if="item.showTrend" class="trend" :class="item.trend >= 0 ? 'up' : 'down'">
                  <el-icon><CaretTop v-if="item.trend >= 0" /><CaretBottom v-else /></el-icon>
                  {{ Math.abs(item.trend).toFixed(1) }}% <span>{{ item.trendText }}</span>
                </div>
              </div>
              <div class="summary-icon" :style="{ backgroundColor: item.color + '15', color: item.color }">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="24">
        <!-- 营业额趋势图 -->
        <el-col :span="16">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="chart-header">
                <div class="header-left">
                  <span class="title">营业额趋势</span>
                  <el-tag size="small" type="primary" effect="plain">近7日</el-tag>
                </div>
                <div class="header-right">
                  <el-radio-group v-model="trendTimeRange" size="small">
                    <el-radio-button label="7d">7天</el-radio-button>
                    <el-radio-button label="30d">30天</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
            </template>
            <div ref="turnoverChart" class="chart-box"></div>
          </el-card>
        </el-col>

        <!-- 订单分布 -->
        <el-col :span="8">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="chart-header">
                <span class="title">订单状态分布</span>
              </div>
            </template>
            <div ref="statusChart" class="chart-box"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="24" style="margin-top: 24px;">
        <!-- 菜品销量排行 -->
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="chart-header">
                <span class="title">菜品销量 Top 10</span>
                <el-button link type="primary">查看更多</el-button>
              </div>
            </template>
            <div ref="topDishesChart" class="chart-box" style="height: 400px;"></div>
          </el-card>
        </el-col>

        <!-- 数据明细表格 -->
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="chart-header">
                <span class="title">近期数据概览</span>
                <el-tag size="small">按日期倒序</el-tag>
              </div>
            </template>
            <el-table :data="dailyTableData" style="width: 100%" height="400">
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column prop="amount" label="营业额">
                <template #default="scope">¥{{ scope.row.amount.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column prop="completed" label="完成数" width="80" align="center">
                <template #default="scope">
                  <el-tag size="small" type="success">{{ scope.row.completed }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="cancelled" label="取消数" width="80" align="center">
                <template #default="scope">
                  <el-tag size="small" type="danger">{{ scope.row.cancelled }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <FeatureLock v-else />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import request from '../../utils/request';
import { useUserStore } from '../../store/user';
import FeatureLock from '../../components/merchant/FeatureLock.vue';
import { 
  Money, ShoppingCart, Histogram, TrendCharts, 
  CaretTop, CaretBottom 
} from '@element-plus/icons-vue';

const userStore = useUserStore();
const trendTimeRange = ref('7d');
const turnoverChart = ref<HTMLElement | null>(null);
const statusChart = ref<HTMLElement | null>(null);
const topDishesChart = ref<HTMLElement | null>(null);
const dailyTableData = ref<any[]>([]);

let turnoverChartInstance: echarts.ECharts | null = null;
let statusChartInstance: echarts.ECharts | null = null;
let topDishesChartInstance: echarts.ECharts | null = null;

const summaryData = ref([
  { label: '今日营业额', value: '0.00', prefix: '¥', icon: Money, color: '#4f46e5', trend: 0, key: 'today', showTrend: true, trendText: '较昨日' },
  { label: '本周总收入', value: '0.00', prefix: '¥', icon: ShoppingCart, color: '#3b82f6', trend: 0, key: 'week', showTrend: true, trendText: '较上周' },
  { label: '本月总收入', value: '0.00', prefix: '¥', icon: TrendCharts, color: '#10b981', trend: 0, key: 'month', showTrend: true, trendText: '较上月' },
  { label: '累计总收入', value: '0.00', prefix: '¥', icon: Histogram, color: '#f59e0b', trend: 0, key: 'total', showTrend: false },
]);

const initCharts = (data: any) => {
  // 营业额趋势图 (需要日期从小到大排列)
  if (turnoverChart.value) {
    const dates = [...(data.turnover?.dates || [])].reverse();
    const amounts = [...(data.turnover?.amounts || [])].reverse();
    
    turnoverChartInstance = echarts.init(turnoverChart.value);
    turnoverChartInstance.setOption({
      tooltip: { 
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderWidth: 0,
        textStyle: { color: '#1e293b' },
        extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.1); border-radius: 8px;'
      },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { 
        type: 'category', 
        data: dates,
        axisLine: { lineStyle: { color: '#e2e8f0' } },
        axisLabel: { color: '#64748b' }
      },
      yAxis: { 
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } },
        axisLabel: { color: '#64748b' }
      },
      series: [{ 
        data: amounts, 
        type: 'line', 
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#4f46e5' },
        lineStyle: { width: 4, color: '#4f46e5' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(79, 70, 229, 0.2)' },
            { offset: 1, color: 'rgba(79, 70, 229, 0)' }
          ])
        }
      }]
    });
  }

  // 订单状态分布图
  if (statusChart.value) {
    statusChartInstance = echarts.init(statusChart.value);
    statusChartInstance.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0', icon: 'circle', textStyle: { color: '#64748b' } },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: '16', fontWeight: 'bold' } },
        data: data.statusDistribution && data.statusDistribution.length > 0 ? data.statusDistribution : [
          { value: 0, name: '已完成' },
          { value: 0, name: '已取消' }
        ],
        color: ['#10b981', '#ef4444', '#3b82f6', '#f59e0b']
      }]
    });
  }

  // 菜品销量Top 10
  if (topDishesChart.value) {
    topDishesChartInstance = echarts.init(topDishesChart.value);
    const names = data.topDishes?.names || [];
    const counts = data.topDishes?.counts || [];
    
    topDishesChartInstance.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
      xAxis: { 
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } }
      },
      yAxis: { 
        type: 'category', 
        data: names.reverse(), // 柱状图为了显示Top 1在上面，需要翻转
        axisLine: { lineStyle: { color: '#e2e8f0' } },
        axisLabel: { color: '#1e293b', fontSize: 12 }
      },
      series: [{ 
        data: counts.reverse(), 
        type: 'bar',
        barWidth: '50%',
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: '#3b82f6' },
            { offset: 1, color: '#60a5fa' }
          ])
        },
        label: {
          show: true,
          position: 'right',
          color: '#64748b'
        }
      }]
    });
  }
};

const fetchDataForStats = async () => {
  try {
    // 获取基础统计汇总
    const incomeRes: any = await request.get('/order/stats/merchant/income');
    if (incomeRes.data) {
      summaryData.value.forEach(item => {
        if (item.key && incomeRes.data[item.key] !== undefined) {
          item.value = Number(incomeRes.data[item.key]).toFixed(2);
        }
        // 设置各维度的趋势
        if (item.key === 'today' && incomeRes.data.dayTrend !== undefined) {
          item.trend = Number(incomeRes.data.dayTrend);
        } else if (item.key === 'week' && incomeRes.data.weekTrend !== undefined) {
          item.trend = Number(incomeRes.data.weekTrend);
        } else if (item.key === 'month' && incomeRes.data.monthTrend !== undefined) {
          item.trend = Number(incomeRes.data.monthTrend);
        }
      });
    }

    // 获取图表详细数据
    const statsRes: any = await request.get('/order/stats/daily');
    if (statsRes.data) {
      dailyTableData.value = statsRes.data; // 后端已排序，由新到旧
      const dates = statsRes.data.map((i: any) => i.date);
      const amounts = statsRes.data.map((i: any) => i.amount);
      
      const totalCompleted = statsRes.data.reduce((s: any, i: any) => s + (i.completed || 0), 0);
      const totalCancelled = statsRes.data.reduce((s: any, i: any) => s + (i.cancelled || 0), 0);

      // 获取菜品排行
      const topDishesRes: any = await request.get('/order/stats/top-dishes');
      const topNames = topDishesRes.data?.map((i: any) => i.name) || [];
      const topCounts = topDishesRes.data?.map((i: any) => i.value) || [];
      
      initCharts({
        turnover: { dates, amounts },
        statusDistribution: [
          { value: totalCompleted, name: '已完成' },
          { value: totalCancelled, name: '已取消' }
        ],
        topDishes: { names: topNames, counts: topCounts }
      });
    }
  } catch (e) {
    console.error("获取统计数据失败", e);
    initCharts({});
  }
};

const resizeCharts = () => {
  turnoverChartInstance?.resize();
  statusChartInstance?.resize();
  topDishesChartInstance?.resize();
};

onMounted(() => {
  nextTick(() => {
    fetchDataForStats();
    window.addEventListener('resize', resizeCharts);
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts);
});
</script>

<style scoped>
.stats-page {
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 60px);
}

.stats-content {
  max-width: 1400px;
  margin: 0 auto;
}

.summary-cards {
  margin-bottom: 24px;
}

.summary-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.summary-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-info .label {
  font-size: 14px;
  color: #64748b;
  display: block;
  margin-bottom: 8px;
}

.summary-info .value {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.trend.up { color: #10b981; }
.trend.down { color: #ef4444; }
.trend span { color: #94a3b8; }

.summary-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.chart-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header .title {
  font-weight: 700;
  color: #1e293b;
  font-size: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-box {
  height: 380px;
  width: 100%;
}
</style>
