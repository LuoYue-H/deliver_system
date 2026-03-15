<template>
  <el-container class="rider-layout">
    <el-header class="rider-header">
      <div class="header-logo">
        <el-icon class="logo-icon"><Bicycle /></el-icon>
        <span class="logo-text">校园配送 · 骑手版</span>
      </div>
      <el-menu mode="horizontal" router :default-active="$route.path" class="rider-menu">
        <el-menu-item index="/rider/hall">
          <el-icon><Shop /></el-icon>抢单大厅
        </el-menu-item>
        <el-menu-item index="/rider/delivery">
          <el-icon><List /></el-icon>我的配送
        </el-menu-item>
        <el-menu-item index="/rider/history">
          <el-icon><TrendCharts /></el-icon>收入统计
        </el-menu-item>
        <el-menu-item index="/rider/map">
          <el-icon><Location /></el-icon>配送地图
        </el-menu-item>
      </el-menu>
      <div class="header-user">
        <el-dropdown>
          <span class="user-info">
            <el-avatar :size="32" :src="userStore.avatar || defaultAvatar" />
            <span class="username">{{ userStore.username }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/rider/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="rider-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { useUserStore } from '../store/user'
import { useRouter } from 'vue-router'
import { onMounted, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'
import { Bicycle, Shop, List, TrendCharts, Location } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

let socket: WebSocket | null = null

const initWebSocket = () => {
  const userId = userStore.userId
  if (!userId) return
  
  socket = new WebSocket(`ws://localhost:8088/ws/${userId}`)
  
  socket.onmessage = (event) => {
    let messageContent = event.data
    try {
      const data = JSON.parse(event.data)
      messageContent = data.message || messageContent
    } catch (e) {
      // 简单字符串消息
    }
    
    ElNotification({
      title: '新任务提醒',
      message: messageContent,
      type: 'warning',
      duration: 5000,
      position: 'bottom-right'
    })
    
    // 播放提示音 (可选)
    playNotificationSound()
  }
}

const playNotificationSound = () => {
  const audio = new Audio('https://assets.mixkit.cn/active_storage/sfx/2358/2358-preview.mp3')
  audio.play().catch(() => {
    // 忽略浏览器策略导致的自动播放失败
  })
}

const logout = () => {
  if (socket) socket.close()
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  initWebSocket()
})

onUnmounted(() => {
  if (socket) socket.close()
})
</script>

<style scoped>
.rider-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.rider-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  padding: 0 40px;
  z-index: 100;
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 28px;
  color: #ff9900;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.rider-menu {
  flex: 1;
  border-bottom: none;
  margin: 0 40px;
}

.header-user {
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
  background-color: #f0f2f5;
}

.username {
  font-size: 14px;
  color: #606266;
}

.rider-main {
  padding: 24px 40px;
}
</style>
