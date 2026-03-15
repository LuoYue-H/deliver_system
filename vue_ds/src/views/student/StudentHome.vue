<template>
  <div class="home-container">
    <!-- 顶部 Hero 区域 -->
    <div class="hero-section">
      <div class="hero-bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">开启校园美食之旅</h1>
        <p class="hero-subtitle">甄选周边优质商家，极速配送，美味即刻送达</p>
        <div class="search-box">
          <el-input
            v-model="searchQuery"
            placeholder="搜索您想吃的商家或美食..."
            class="hero-search"
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
    </div>

    <el-backtop target=".layout-main" :right="40" :bottom="40" />

    <div class="content-wrapper">
      <!-- 轮播图区域 -->
      <div class="banner-section">
        <el-carousel :interval="4000" type="card" height="240px" indicator-position="outside">
          <el-carousel-item v-for="(banner, index) in banners" :key="index">
            <div class="banner-card" :style="{ backgroundImage: `url(${banner.image})` }">
              <div class="banner-overlay">
                <h3>{{ banner.title }}</h3>
                <p>{{ banner.subtitle }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 分类快捷入口 -->
      <div class="category-section">
        <div 
          v-for="cat in categories" 
          :key="cat.name" 
          class="category-item"
          @click="quickFilter(cat.keyword)"
        >
          <div class="category-icon" :style="{ backgroundColor: cat.color }">
            <el-icon><component :is="cat.icon" /></el-icon>
          </div>
          <span class="category-name">{{ cat.name }}</span>
        </div>
      </div>

      <!-- 商家列表区域 -->
      <div class="merchant-section">
        <div class="section-header">
          <div class="title-wrap">
            <h2 class="section-title">精选商家</h2>
            <span class="section-badge">附近共 {{ filteredMerchants.length }} 家</span>
          </div>
          <div class="filter-actions">
            <el-radio-group v-model="sortBy" size="small" @change="handleSort">
              <el-radio-button label="default">综合排序</el-radio-button>
              <el-radio-button label="sales">销量优先</el-radio-button>
              <el-radio-button label="rating">评分最高</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 骨架屏 -->
        <div v-if="loading" class="skeleton-wrapper">
          <el-row :gutter="24">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="n in 8" :key="n">
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

        <!-- 商家列表 -->
        <div v-else>
          <el-empty v-if="filteredMerchants.length === 0" description="未找到匹配的商家，换个词试试吧" />
          <el-row v-else :gutter="24">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="merchant in filteredMerchants" :key="merchant.id">
              <MerchantCard :merchant="merchant" @click="goToOrder(merchant.id)" />
            </el-col>
          </el-row>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import request from '../../utils/request';
import { useRouter } from 'vue-router';
import MerchantCard from '@/components/MerchantCard.vue';
import { 
  Search, Coffee, IceDrink, HotWater, 
  KnifeFork, Dessert, Food 
} from '@element-plus/icons-vue';

const router = useRouter();
const merchantList = ref<any[]>([]);
const loading = ref(true);
const searchQuery = ref('');
const sortBy = ref('default');

const banners = [
  { 
    title: '春季新品特惠', 
    subtitle: '满30减10，快来抢购', 
    image: 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&q=80&w=1000' 
  },
  { 
    title: '校园人气王', 
    subtitle: '这些商家你还没吃过吗？', 
    image: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&q=80&w=1000' 
  },
  { 
    title: '深夜食堂', 
    subtitle: '22:00后部分商家低至5折', 
    image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&q=80&w=1000' 
  }
];

const categories = [
  { name: '快餐便当', keyword: '便当', icon: Food, color: '#ff7675' },
  { name: '奶茶饮品', keyword: '奶茶', icon: IceDrink, color: '#74b9ff' },
  { name: '甜品蛋糕', keyword: '蛋糕', icon: Dessert, color: '#fab1a0' },
  { name: '特色小吃', keyword: '小吃', icon: KnifeFork, color: '#fdcb6e' },
  { name: '咖啡午茶', keyword: '咖啡', icon: Coffee, color: '#a29bfe' },
  { name: '麻辣烫', keyword: '辣', icon: HotWater, color: '#e17055' }
];

const filteredMerchants = computed(() => {
  let result = [...merchantList.value];
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(m => 
      m.username.toLowerCase().includes(query) || 
      (m.merchantDetails && m.merchantDetails.category && m.merchantDetails.category.toLowerCase().includes(query))
    );
  }
  
  if (sortBy.value === 'sales') {
    result.sort(() => Math.random() - 0.5);
  } else if (sortBy.value === 'rating') {
    result.sort(() => 0.5 - Math.random());
  }
  
  return result;
});

const fetchMerchants = async () => {
  loading.value = true;
  try {
    const res: any = await request.get('/user/list/merchant');
    setTimeout(() => {
      // 智能分类逻辑：如果商家没有设置分类，根据名称关键词进行初步归类
      merchantList.value = res.data.map((m: any) => {
        if (!m.merchantDetails) m.merchantDetails = {};
        if (!m.merchantDetails.category) {
          const name = m.username;
          if (name.includes('奶茶') || name.includes('茶') || name.includes('饮')) m.merchantDetails.category = '奶茶';
          else if (name.includes('咖啡') || name.includes('拿铁')) m.merchantDetails.category = '咖啡';
          else if (name.includes('蛋糕') || name.includes('甜')) m.merchantDetails.category = '蛋糕';
          else if (name.includes('麻辣烫') || name.includes('冒菜') || name.includes('辣')) m.merchantDetails.category = '辣';
          else if (name.includes('便当') || name.includes('饭') || name.includes('食堂')) m.merchantDetails.category = '便当';
          else if (name.includes('小吃') || name.includes('炸') || name.includes('串')) m.merchantDetails.category = '小吃';
        }
        return m;
      });
      loading.value = false;
    }, 600);
  } catch (error) {
    console.error("获取商家列表失败", error);
    loading.value = false;
  }
};

const goToOrder = (merchantId: number) => {
  router.push(`/student/order?merchantId=${merchantId}`);
};

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      path: '/student/search',
      query: { q: searchQuery.value }
    });
  }
};

const handleSort = () => {};

const quickFilter = (keyword: string) => {
  router.push({
    path: '/student/search',
    query: { q: keyword }
  });
};

onMounted(() => {
  fetchMerchants();
});
</script>

<style scoped>
.home-container {
  padding-bottom: 60px;
}

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%);
  padding: 80px 40px;
  color: #fff;
  text-align: center;
  position: relative;
  overflow: hidden;
  margin-bottom: -40px; /* Overlap with content */
  padding-bottom: 120px;
}

.hero-bg-shapes .shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  z-index: 0;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.1);
  top: -100px;
  right: -100px;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: rgba(96, 165, 250, 0.2);
  bottom: -50px;
  left: -50px;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  margin: 0 0 16px 0;
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: 20px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.search-box {
  max-width: 640px;
  margin: 0 auto;
}

:deep(.hero-search .el-input__wrapper) {
  border-radius: 16px 0 0 16px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  height: 60px;
  padding-left: 20px;
  border: none;
}

:deep(.hero-search .el-input-group__append) {
  background-color: #1e293b;
  color: #fff;
  border: none;
  border-radius: 0 16px 16px 0;
  padding: 0 35px;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

:deep(.hero-search .el-input-group__append:hover) {
  background-color: #0f172a;
}

/* Content Wrapper */
.content-wrapper {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 2;
}

/* Banner Section */
.banner-section {
  margin-bottom: 40px;
}

.banner-card {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  border-radius: 20px;
  position: relative;
  overflow: hidden;
}

.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24px;
  background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
  color: white;
}

.banner-overlay h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 700;
}

.banner-overlay p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

/* Category Section */
.category-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 40px;
  background: white;
  padding: 24px;
  border-radius: 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.03);
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.3s;
}

.category-item:hover {
  transform: translateY(-5px);
}

.category-icon {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 8px 15px rgba(0,0,0,0.1);
}

.category-name {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

/* Merchant Section */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  padding: 0 4px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title {
  font-size: 26px;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
}

.section-badge {
  font-size: 13px;
  color: #64748b;
  background-color: #f1f5f9;
  padding: 4px 12px;
  border-radius: 20px;
}

.el-col {
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }
  .category-section {
    flex-wrap: wrap;
    justify-content: center;
    gap: 20px;
  }
  .category-item {
    width: 25%;
  }
}
</style>
