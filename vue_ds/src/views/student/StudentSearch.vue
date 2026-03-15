<template>
  <div class="search-page">
    <!-- 顶部搜索栏 -->
    <div class="search-header">
      <div class="header-content">
        <el-button :icon="ArrowLeft" circle @click="router.back()" class="back-btn" />
        <el-input
          v-model="searchQuery"
          placeholder="搜索您想吃的商家或美食..."
          class="search-input"
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <el-backtop target=".layout-main" :right="40" :bottom="40" />

    <div class="results-container">
      <!-- 搜索状态提示 -->
      <div class="results-meta" v-if="!loading">
        <span v-if="results.length > 0">
          为您找到 <span class="highlight">{{ results.length }}</span> 家相关商家
        </span>
        <span v-else-if="hasSearched">
          未找到与 "<span class="highlight">{{ lastQuery }}</span>" 相关的商家
        </span>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="skeleton-list">
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="n in 4" :key="n">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" style="width: 100%; height: 180px; border-radius: 12px" />
                <div style="padding: 16px;">
                  <el-skeleton-item variant="h3" style="width: 60%" />
                  <div style="display: flex; align-items: center; margin-top: 12px;">
                    <el-skeleton-item variant="text" style="width: 40%; margin-right: 16px;" />
                    <el-skeleton-item variant="text" style="width: 30%;" />
                  </div>
                </div>
              </template>
            </el-skeleton>
          </el-col>
        </el-row>
      </div>

      <!-- 结果列表 -->
      <div v-else class="results-list">
        <el-row :gutter="24" v-if="results.length > 0">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="merchant in results" :key="merchant.id">
            <MerchantCard :merchant="merchant" :highlight="lastQuery" @click="goToOrder(merchant.id)" />
          </el-col>
        </el-row>

        <!-- 空状态 -->
        <div v-else-if="hasSearched" class="empty-results">
          <el-empty :description="`抱歉，没有找到“${lastQuery}”相关的商家`" />
          <div class="recommend-title">为您推荐其他热门商家</div>
          <el-row :gutter="24">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="merchant in recommendations" :key="merchant.id">
              <MerchantCard :merchant="merchant" @click="goToOrder(merchant.id)" />
            </el-col>
          </el-row>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Search, ArrowLeft } from '@element-plus/icons-vue';
import request from '../../utils/request';
import MerchantCard from '@/components/MerchantCard.vue';

const route = useRoute();
const router = useRouter();
const searchQuery = ref((route.query.q as string) || '');
const lastQuery = ref('');
const loading = ref(false);
const hasSearched = ref(false);
const results = ref<any[]>([]);
const recommendations = ref<any[]>([]);
const allMerchants = ref<any[]>([]);

// 智能评分逻辑：根据包含程度排序
const calculateScore = (merchant: any, query: string) => {
  const name = merchant.username.toLowerCase();
  const cat = (merchant.merchantDetails?.category || '').toLowerCase();
  const q = query.toLowerCase();
  let score = 0;

  // 1. 基础匹配分数
  if (name === q) {
    score += 1000; // 完全匹配，最高优先级
  } else if (name.startsWith(q)) {
    score += 800;  // 前缀匹配
  } else if (name.includes(q)) {
    score += 500;  // 包含匹配
    // 包含位置加成：越靠前分数越高
    score += (1 - name.indexOf(q) / name.length) * 100;
  }

  // 2. 品类匹配分数
  if (cat.includes(q)) {
    score += 300;
    if (cat === q) score += 200; // 品类完全一致
  }

  // 3. 匹配比例加成 (query长度 / 名字长度)
  if (score > 0) {
    score += (q.length / name.length) * 100;
  }

  // 4. 销量与评分微调 (作为打破平局的依据)
  // 假设 merchant 有 sales 和 rating 字段，如果没有则默认为 0
  const sales = merchant.sales || 0;
  const rating = merchant.rating || 0;
  score += (sales / 1000) * 10; // 每 100 销量加 1 分
  score += rating * 2;         // 每 1 分评分加 2 分

  return score;
};

const performSearch = () => {
  if (!searchQuery.value.trim()) {
    results.value = [];
    hasSearched.value = false;
    return;
  }

  loading.value = true;
  hasSearched.value = true;
  lastQuery.value = searchQuery.value;

  // 模拟搜索延迟，提升体验
  setTimeout(() => {
    const scoredResults = allMerchants.value
      .map(m => ({ ...m, score: calculateScore(m, searchQuery.value) }))
      .filter(m => m.score > 0)
      .sort((a, b) => b.score - a.score);

    results.value = scoredResults;
    loading.value = false;
  }, 400);
};

const fetchAllMerchants = async () => {
  try {
    const res: any = await request.get('/user/list/merchant');
    allMerchants.value = res.data;
    // 获取推荐商家（随机选几个）
    recommendations.value = [...res.data].sort(() => Math.random() - 0.5).slice(0, 4);
    
    if (searchQuery.value) {
      performSearch();
    }
  } catch (e) {
    console.error('获取商家失败', e);
  }
};

const handleSearch = () => {
  if (searchQuery.value) {
    if (route.query.q === searchQuery.value) {
      performSearch();
    } else {
      router.replace({ query: { q: searchQuery.value } });
    }
  }
};

const goToOrder = (merchantId: number) => {
  router.push(`/student/order?merchantId=${merchantId}`);
};

onMounted(fetchAllMerchants);

// 监听路由参数变化（如点击分类或从其他地方跳转）
watch(() => route.query.q, (newQ) => {
  if (newQ !== undefined) {
    searchQuery.value = newQ as string;
    performSearch();
  }
});
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background-color: #f8fafc;
}

.search-header {
  background: white;
  padding: 20px 0;
  box-shadow: 0 2px 15px rgba(0,0,0,0.05);
}

.header-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  flex-shrink: 0;
}

.search-input {
  max-width: 800px;
}

:deep(.search-input .el-input__wrapper) {
  border-radius: 12px 0 0 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

:deep(.search-input .el-input-group__append) {
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 0 12px 12px 0;
  padding: 0 25px;
}

.results-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 30px 20px;
}

.results-meta {
  margin-bottom: 24px;
  color: #64748b;
  font-size: 15px;
}

.highlight {
  color: #4f46e5;
  font-weight: 700;
  margin: 0 4px;
}

.empty-results {
  padding: 40px 0;
}

.recommend-title {
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
  margin: 60px 0 24px 0;
  text-align: center;
}

.el-col {
  margin-bottom: 24px;
}

@media (max-width: 640px) {
  .header-content {
    gap: 10px;
  }
  .search-input {
    flex: 1;
  }
}
</style>
