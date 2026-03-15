<template>
  <el-card shadow="hover" class="merchant-card" :body-style="{ padding: '0px' }">
    <div class="card-image-wrap">
      <el-image
        :src="merchant.avatar || 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'"
        fit="cover"
        class="merchant-cover"
        lazy
      />
      <div class="image-overlay">
        <el-tag size="small" effect="dark" class="delivery-tag">30分钟送达</el-tag>
      </div>
    </div>
    <div class="merchant-content">
      <div class="merchant-header">
        <h3 class="merchant-name" v-html="highlightText(merchant.username, highlight)"></h3>
        <div class="rating-wrap">
          <el-icon class="star-icon"><StarFilled /></el-icon>
          <span class="rating-score">{{ (Math.random() * 1.5 + 3.5).toFixed(1) }}</span>
        </div>
      </div>
      
      <div class="merchant-stats">
        <span class="sales">月售 {{ Math.floor(Math.random() * 1000) }}+</span>
        <span v-if="merchant.merchantDetails?.category" class="category-tag" v-html="highlightText(merchant.merchantDetails.category, highlight)"></span>
        <span class="divider">|</span>
        <span class="delivery-info">起送 ¥20 配送 ¥3</span>
      </div>

      <div class="merchant-tags">
        <el-tag size="small" type="danger" effect="plain">满30减5</el-tag>
        <el-tag size="small" type="warning" effect="plain">新店特惠</el-tag>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { defineProps } from 'vue';
import { StarFilled } from '@element-plus/icons-vue';

const props = defineProps({
  merchant: {
    type: Object,
    required: true
  },
  highlight: {
    type: String,
    default: ''
  }
});

const highlightText = (text: string, keyword: string) => {
  if (!text) return '';
  if (!keyword) return text;
  const reg = new RegExp(`(${keyword})`, 'gi');
  return text.replace(reg, '<span class="highlight-keyword">$1</span>');
};
</script>

<style scoped>
.merchant-card {
  border-radius: 16px;
  overflow: hidden;
  border: none;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  cursor: pointer;
  height: 100%;
}

.merchant-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0,0,0,0.12);
}

.card-image-wrap {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.merchant-cover {
  width: 100%;
  height: 100%;
  transition: transform 0.6s ease;
}

.merchant-card:hover .merchant-cover {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  bottom: 12px;
  left: 12px;
  z-index: 2;
}

.delivery-tag {
  background-color: rgba(0, 0, 0, 0.6);
  border: none;
  backdrop-filter: blur(4px);
}

.merchant-content {
  padding: 16px;
}

.merchant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.merchant-name {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.highlight-keyword) {
  color: #4f46e5;
  font-weight: 800;
  background-color: rgba(79, 70, 229, 0.1);
  padding: 0 2px;
  border-radius: 2px;
}

.rating-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: #fffbeb;
  padding: 2px 8px;
  border-radius: 6px;
}

.star-icon {
  color: #fbbf24;
  font-size: 14px;
}

.rating-score {
  font-size: 13px;
  font-weight: 700;
  color: #d97706;
}

.merchant-stats {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 12px;
}

.category-tag {
  margin-left: 8px;
  background-color: #f1f5f9;
  color: #475569;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.divider {
  margin: 0 8px;
  color: #e2e8f0;
}

.merchant-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

:deep(.el-tag--plain) {
  border-radius: 4px;
  font-weight: 500;
}
</style>
