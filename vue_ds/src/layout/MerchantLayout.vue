<template>
  <div v-if="isReady">
    <el-container class="layout-container">
      <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
        <div class="aside-logo">
          <el-icon v-if="isCollapse"><Shop /></el-icon>
          <span v-else>校园外卖 · 商家端</span>
        </div>
        <el-menu
          :default-active="$route.path"
          class="el-menu-vertical"
          :collapse="isCollapse"
          router
        >
          <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="layout-header">
          <div class="header-left">
            <el-icon @click="isCollapse = !isCollapse" class="collapse-icon">
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>商家后台</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentRouteTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-tag v-if="merchantStatus !== 2" type="warning" class="status-tag" effect="plain">
              店铺未开通
            </el-tag>
            <el-dropdown>
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                <span class="username">{{ userStore.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/merchant/profile')">店铺设置</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/merchant/review-status')">审核进度</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="layout-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
  <div v-else class="loading-overlay">
    <el-icon class="is-loading"><Loading /></el-icon>
    <span>正在加载商家中心...</span>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import request from '../utils/request';
import { useUserStore } from '../store/user';
import { DataLine, Menu, Tickets, PieChart, Fold, Expand, Shop, Loading } from '@element-plus/icons-vue';

const menuItems = [
  { index: '/merchant/dashboard', icon: DataLine, title: '实时订单' },
  { index: '/merchant/menu', icon: Menu, title: '菜单管理' },
  { index: '/merchant/history', icon: Tickets, title: '历史订单' },
  { index: '/merchant/stats', icon: PieChart, title: '数据统计' },
];

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const isReady = ref(false);
const isCollapse = ref(false);

const merchantStatus = computed(() => userStore.merchantStatus);
const isApproved = computed(() => merchantStatus.value === 2);

const currentRouteTitle = computed(() => {
  const item = menuItems.find(m => m.index === route.path);
  if (item) return item.title;
  if (route.path === '/merchant/review-status') return '审核进度';
  if (route.path === '/merchant/review-form') return '提交审核';
  if (route.path === '/merchant/profile') return '店铺设置';
  return '';
});

const checkStatus = async () => {
  try {
    const res: any = await request.get('/login/profile');
    userStore.setUser(res.data);
    isReady.value = true;
  } catch (e) {
    router.push('/login');
  }
};

onMounted(checkStatus);

const logout = () => {
  userStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background-color: #f0f2f5;
}

.layout-aside {
  background-color: #001529;
  transition: width 0.3s;
  box-shadow: 2px 0 8px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
}

.aside-logo {
  height: 64px;
  line-height: 64px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #002140;
  overflow: hidden;
  white-space: nowrap;
}

.el-menu-vertical {
  border-right: none;
  background-color: transparent;
}

:deep(.el-menu) {
  background-color: transparent;
  border-right: none;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.65);
}

:deep(.el-menu-item:hover) {
  color: #fff;
  background-color: transparent;
}

:deep(.el-menu-item.is-active) {
  color: #fff;
  background-color: #1890ff;
}

.layout-header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-tag {
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #303133;
}

.layout-main {
  padding: 24px;
  overflow-y: auto;
}

.loading-overlay {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
  font-size: 14px;
}

.loading-overlay .el-icon {
  font-size: 32px;
  color: #409eff;
}
</style>
