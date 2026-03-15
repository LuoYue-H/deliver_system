<template>
  <div class="history-container">
    <div class="history-header">
      <h2 class="page-title">收入统计</h2>
      <el-radio-group v-model="timeRange" size="small" @change="fetchIncome">
        <el-radio-button label="today">今日</el-radio-button>
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
      </el-radio-group>
    </div>

    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card income">
            <div class="stat-label">今日收入</div>
            <div class="stat-value">¥{{ income.today }}</div>
            <div class="stat-sub">完成 {{ income.today / 5 }} 单</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card week">
            <div class="stat-label">本周收入</div>
            <div class="stat-value">¥{{ income.week }}</div>
            <div class="stat-sub">完成 {{ income.week / 5 }} 单</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card month">
            <div class="stat-label">本月收入</div>
            <div class="stat-value">¥{{ income.month }}</div>
            <div class="stat-sub">完成 {{ income.month / 5 }} 单</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card total">
            <div class="stat-label">累计总收入</div>
            <div class="stat-value">¥{{ income.total }}</div>
            <div class="stat-sub">累计完成 {{ income.total / 5 }} 单</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="chart-section mt-6">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>收入趋势</span>
          </div>
        </template>
        <div ref="chartRef" class="income-chart"></div>
      </el-card>
    </div>

    <h2 class="page-title mt-6">历史订单记录</h2>
    <el-card shadow="never" class="table-card">
      <el-table 
        :data="historyOrders" 
        style="width: 100%" 
        stripe 
        v-loading="loading"
        :header-cell-style="{ textAlign: 'center' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column prop="id" label="订单号" width="120" align="center" />
        <el-table-column prop="merchantName" label="商家名称" min-width="150" align="center" />
        <el-table-column prop="createTime" label="接单时间" width="160" align="center">
            <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="finishTime" label="送达时间" width="160" align="center">
            <template #default="scope">
                <span v-if="scope.row.status === 5" class="finish-time">{{ formatTime(scope.row.finishTime) }}</span>
                <span v-else class="cancel-text">已取消</span>
            </template>
        </el-table-column>
        <el-table-column prop="address" label="送达地址" min-width="200" show-overflow-tooltip align="center" />
        <el-table-column label="配送费" width="100" align="center">
            <template #default><span class="fee-text">¥5.00</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
            </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="historyOrders.length"
          :page-size="10"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import request from '../../utils/request'
import * as echarts from 'echarts'

const historyOrders = ref([])
const loading = ref(false)
const timeRange = ref('week')
const income = ref({
    today: 0,
    week: 0,
    month: 0,
    total: 0
})

const chartRef = ref<HTMLElement | null>(null)
let myChart: echarts.ECharts | null = null

const fetchHistory = async () => {
    loading.value = true
    try {
        const res: any = await request.get('/order/list/rider/history')
        historyOrders.value = res.data
    } catch(e) {
    } finally {
        loading.value = false
    }
}

const fetchIncome = async () => {
    try {
        const [incomeRes, dailyRes]: any = await Promise.all([
            request.get('/order/stats/rider/income'),
            request.get('/order/stats/rider/daily')
        ])
        income.value = incomeRes.data
        initChart(dailyRes.data)
    } catch(e) {
        console.error('获取收入统计失败', e)
    }
}

const initChart = (dailyData: any[] = []) => {
    if (!chartRef.value) return
    if (!myChart) {
        myChart = echarts.init(chartRef.value)
    }
    
    let dates = []
    let values = []

    if (dailyData && dailyData.length > 0) {
        // 后端返回的是 DESC 排序，我们需要 ASC 来展示趋势
        const sortedData = [...dailyData].reverse()
        dates = sortedData.map(item => {
            const d = new Date(item.date)
            return `${d.getMonth() + 1}/${d.getDate()}`
        })
        // 骑手收入 = 单数 * 5
        values = sortedData.map(item => item.count * 5)
    } else {
        // 兜底显示近7天（全为0）
        for (let i = 6; i >= 0; i--) {
            const d = new Date()
            d.setDate(d.getDate() - i)
            dates.push(`${d.getMonth() + 1}/${d.getDate()}`)
            values.push(0)
        }
    }

    const option = {
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: dates },
        yAxis: { type: 'value', name: '元' },
        series: [{
            name: '收入',
            type: 'line',
            smooth: true,
            data: values,
            areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: 'rgba(255, 153, 0, 0.3)' },
                    { offset: 1, color: 'rgba(255, 153, 0, 0)' }
                ])
            },
            itemStyle: { color: '#ff9900' }
        }]
    }
    myChart.setOption(option)
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

const getStatusType = (status: number) => {
    if (status === 5) return 'success'
    if (status === 6 || status === 7) return 'info'
    return ''
}

const getStatusText = (status: number) => {
    const map: any = {
        5: '已完成',
        6: '用户取消',
        7: '商家取消'
    }
    return map[status] || '未知'
}

const handleResize = () => {
    myChart?.resize()
}

onMounted(() => {
    fetchHistory()
    fetchIncome()
    window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.history-container {
  max-width: 1200px;
  margin: 0 auto;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.mt-6 { margin-top: 24px; }

.stats-overview {
  margin-bottom: 24px;
}

.stat-card {
  padding: 20px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-card.income { background: linear-gradient(135deg, #ff9900 0%, #ffcc00 100%); }
.stat-card.week { background: linear-gradient(135deg, #409eff 0%, #64b5f6 100%); }
.stat-card.month { background: linear-gradient(135deg, #67c23a 0%, #95e1d3 100%); }
.stat-card.total { background: linear-gradient(135deg, #909399 0%, #bdc3c7 100%); }

.stat-label { font-size: 14px; opacity: 0.9; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-sub { font-size: 12px; opacity: 0.8; }

.chart-card, .table-card {
  border-radius: 12px;
  border: none;
}

.income-chart {
  height: 300px;
}

.fee-text { color: #f56c6c; font-weight: bold; }
.finish-time { color: #67c23a; }
.cancel-text { color: #909399; }

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
