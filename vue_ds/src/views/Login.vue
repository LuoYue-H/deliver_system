<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-logo">
        <el-icon class="logo-icon"><Shop /></el-icon>
        <span class="logo-text">校园外卖平台</span>
      </div>
      <el-card class="login-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h3>欢迎回来</h3>
            <p class="subtitle">请登录您的账号以继续</p>
          </div>
        </template>
        <el-form :model="form" @keyup.enter="handleLogin">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock"
              show-password
              size="large"
            />
          </el-form-item>
          <div class="form-actions">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleLogin" 
              :loading="loading"
              class="login-btn"
              size="large"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="register-link">
          <span>还没有账号？</span>
          <el-link type="primary" @click="$router.push('/register')" :underline="false">立即注册</el-link>
        </div>
      </el-card>
    </div>
    <div class="background-decor">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { User, Lock, Shop } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const rememberMe = ref(false)

const form = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    const res: any = await request.post('/login', form.value)
    const data = res.data
    userStore.setLoginInfo(data.token, data.role, data.userId) 
    
    ElMessage.success('登录成功，欢迎回来')

    const routes: Record<string, string> = {
      'STUDENT': '/student/home',
      'MERCHANT': '/merchant/dashboard',
      'RIDER': '/rider/hall',
      'ADMIN': '/admin/user'
    }

    const targetPath = routes[data.role]
    if (targetPath) {
      await router.push(targetPath)
    } else {
      ElMessage.error('未知角色权限')
    }
  } catch (e) {
    // 错误处理已在拦截器中完成
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  background-image: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

.login-box {
  width: 420px;
  z-index: 10;
}

.login-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
  gap: 12px;
}

.logo-icon {
  font-size: 40px;
  color: #409eff;
}

.logo-text {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  letter-spacing: 2px;
}

.login-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
  padding: 10px 0;
}

.card-header h3 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.subtitle {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  border-radius: 8px;
  font-weight: bold;
  font-size: 16px;
  height: 48px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #606266;
}

.background-decor .circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 1;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: rgba(64, 158, 255, 0.15);
  top: -100px;
  left: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: rgba(103, 194, 58, 0.1);
  bottom: -50px;
  right: -50px;
}

:deep(.el-card__header) {
  border-bottom: none;
  padding-bottom: 0;
}
</style>
