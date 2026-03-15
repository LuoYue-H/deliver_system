<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="admin-aside">
      <div class="aside-logo">
        <el-icon v-if="isCollapse"><Management /></el-icon>
        <span v-else>校园外卖 · 管理端</span>
      </div>
      <el-menu
        router
        :default-active="$route.path"
        class="admin-menu"
        :collapse="isCollapse"
        background-color="#1f2d3d"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/review">
          <el-icon><Stamp /></el-icon>
          <template #title>商家审核</template>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><Shop /></el-icon>
          <template #title>商家管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/rider">
          <el-icon><Bicycle /></el-icon>
          <template #title>骑手管理</template>
        </el-menu-item>
        <!-- 可以预留数据大屏等入口 -->
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon @click="isCollapse = !isCollapse" class="collapse-icon">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>管理中心</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              <span class="username">系统管理员</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Management, Stamp, Shop, Bicycle, Fold, Expand } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const isCollapse = ref(false);

const currentRouteTitle = computed(() => {
  const titles: Record<string, string> = {
    '/admin/review': '商家审核',
    '/admin/user': '商家管理',
    '/admin/rider': '骑手管理'
  };
  return titles[route.path] || '';
});

const logout = () => {
  localStorage.clear();
  router.push('/login');
};
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background-color: #f0f2f5;
}

.admin-aside {
  background-color: #1f2d3d;
  transition: width 0.3s;
  box-shadow: 2px 0 6px rgba(0,21,41,0.35);
  z-index: 10;
}

.aside-logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background-color: #2b2f3a;
  overflow: hidden;
  white-space: nowrap;
}

.admin-menu {
  border-right: none;
}

.admin-header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.header-right {
  display: flex;
  align-items: center;
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
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
}

.admin-main {
  padding: 24px;
  overflow-y: auto;
}
</style>
