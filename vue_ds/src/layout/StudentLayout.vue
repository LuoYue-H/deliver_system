<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-content">
        <div class="logo-area" @click="$router.push('/student/home')">
          <el-icon class="logo-icon"><IceTea /></el-icon>
          <span class="system-title">校园外卖</span>
        </div>
        <el-menu mode="horizontal" router :default-active="$route.path" class="main-menu">
          <el-menu-item index="/student/home">
            <el-icon><HomeFilled /></el-icon>首页
          </el-menu-item>
          <el-menu-item index="/student/timeline">
            <el-icon><List /></el-icon>我的订单
          </el-menu-item>
          <el-menu-item index="/student/address">
            <el-icon><Location /></el-icon>地址管理
          </el-menu-item>
        </el-menu>

        <!-- 顶部搜索框 (除首页和搜索页外显示) -->
        <div class="header-search" v-if="$route.path !== '/student/home' && $route.path !== '/student/search'">
          <el-input
            v-model="headerSearchQuery"
            placeholder="搜索商家..."
            class="header-search-input"
            size="default"
            clearable
            @keyup.enter="handleHeaderSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="user-area">
          <el-dropdown trigger="click">
            <span class="el-dropdown-link">
              <el-avatar :size="36" :src="userStore.avatar || defaultAvatar" />
              <div class="user-meta">
                <span class="username">{{ userStore.username }}</span>
                <span class="role-tag">学生用户</span>
              </div>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToProfile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    <el-main class="layout-main">
      <div class="main-content">
        <router-view />
      </div>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { useUserStore } from '../store/user';
import { useRouter } from 'vue-router';
import { ref, onMounted, onUnmounted } from 'vue';
import { ElNotification, ElMessageBox, ElMessage } from 'element-plus';
import request from '../utils/request';
import { 
  IceTea, HomeFilled, List, Location, 
  User, SwitchButton, ArrowDown, Search 
} from '@element-plus/icons-vue';

const userStore = useUserStore();
const router = useRouter();
const headerSearchQuery = ref('');
let socket: WebSocket | null = null;

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

const handleHeaderSearch = () => {
  if (headerSearchQuery.value.trim()) {
    router.push({
      path: '/student/search',
      query: { q: headerSearchQuery.value }
    });
    headerSearchQuery.value = ''; // 清空输入框
  }
};

const goToProfile = () => {
  router.push('/student/profile');
};

const handleLogout = () => {
  ElMessageBox.confirm('确认退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    roundButton: true
  }).then(() => {
    if (socket) socket.close();
    userStore.logout();
    router.push('/login');
    ElMessage.success('已安全退出');
  }).catch(() => {});
};

const initWebSocket = () => {
  const userId = userStore.userId;
  if (!userId) return;
  
  socket = new WebSocket(`ws://localhost:8088/ws/${userId}`);
  
  socket.onmessage = (event) => {
    let messageContent = event.data;
    try {
      const data = JSON.parse(event.data);
      messageContent = data.message || messageContent;
    } catch (e) {}
    
    ElNotification({
      title: '订单新动态',
      message: messageContent,
      type: 'success',
      duration: 5000,
      position: 'bottom-right'
    });
  };
};

onMounted(async () => {
  if (!userStore.username) {
    try {
      const res: any = await request.get('/login/profile');
      userStore.setUser(res.data);
    } catch (e) {
      console.error('获取用户信息失败', e);
    }
  }
  initWebSocket();
});

onUnmounted(() => {
  if (socket) socket.close();
});
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background-color: #f8fafc;
}

.layout-header {
  background-color: #fff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  padding: 0 40px;
  height: 70px;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  margin-right: 40px;
}

.logo-icon {
  font-size: 32px;
  color: #409eff;
}

.system-title {
  font-size: 22px;
  font-weight: 800;
  color: #1e293b;
  letter-spacing: 1px;
}

.main-menu {
  flex-grow: 1;
  border-bottom: none;
  height: 100%;
}

:deep(.el-menu--horizontal .el-menu-item) {
  height: 70px;
  line-height: 70px;
  font-size: 16px;
  font-weight: 500;
  color: #64748b;
  border-bottom-width: 3px;
}

:deep(.el-menu--horizontal .el-menu-item.is-active) {
  color: #409eff;
  font-weight: 600;
}

.header-search {
  margin: 0 20px;
  width: 240px;
}

.header-search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  background-color: #f1f5f9;
  box-shadow: none !important;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.header-search-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #409eff;
  box-shadow: 0 0 0 1px #409eff !important;
}

.user-area {
  display: flex;
  align-items: center;
  padding-left: 20px;
  border-left: 1px solid #e2e8f0;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  border-radius: 12px;
  transition: all 0.3s;
}

.el-dropdown-link:hover {
  background-color: #f1f5f9;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.username {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.role-tag {
  font-size: 12px;
  color: #94a3b8;
}

.layout-main {
  padding: 30px 40px;
  overflow-y: auto;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
}

.logout-item {
  color: #ef4444;
}

.logout-item:hover {
  background-color: #fef2f2 !important;
  color: #ef4444 !important;
}
</style>
