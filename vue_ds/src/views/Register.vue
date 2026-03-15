<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-logo">
        <el-icon class="logo-icon"><Shop /></el-icon>
        <span class="logo-text">校园外卖平台</span>
      </div>
      <el-card class="register-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h3>加入我们</h3>
            <p class="subtitle">开启您的校园美食之旅</p>
          </div>
        </template>
        <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleRegister">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请设置用户名" 
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请设置密码" 
              :prefix-icon="Lock"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="请确认您的密码" 
              :prefix-icon="Check"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item prop="role">
            <div class="role-selection">
              <span class="role-label">我想注册为：</span>
              <el-radio-group v-model="form.role">
                <el-radio-button label="STUDENT">学生用户</el-radio-button>
                <el-radio-button label="MERCHANT">商家合作伙伴</el-radio-button>
              </el-radio-group>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleRegister" 
              :loading="loading"
              class="register-btn"
              size="large"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-link">
          <span>已有账号？</span>
          <el-link type="primary" @click="$router.push('/login')" :underline="false">立即登录</el-link>
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
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import { ElMessage } from 'element-plus';
import { User, Lock, Check, Shop } from '@element-plus/icons-vue';

const router = useRouter();
const formRef = ref();
const loading = ref(false);

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  role: 'STUDENT'
});

const validatePass = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== form.value.password) {
    callback(new Error('两次输入密码不一致!'));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择注册角色', trigger: 'change' }]
};

const handleRegister = () => {
  formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true;
      try {
        await request.post('/login/register', {
          username: form.value.username,
          password: form.value.password,
          role: form.value.role
        });
        ElMessage.success('注册成功，欢迎加入');
        router.push('/login');
      } catch (e) {
        // 错误处理已在拦截器中完成
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  background-image: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

.register-box {
  width: 440px;
  z-index: 10;
}

.register-logo {
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

.register-card {
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

.role-selection {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.role-label {
  font-size: 14px;
  color: #606266;
}

.register-btn {
  width: 100%;
  border-radius: 8px;
  font-weight: bold;
  font-size: 16px;
  height: 48px;
}

.login-link {
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
  right: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: rgba(245, 108, 108, 0.08);
  bottom: -50px;
  left: -50px;
}

:deep(.el-card__header) {
  border-bottom: none;
  padding-bottom: 0;
}

:deep(.el-radio-button__inner) {
  width: 100%;
}

:deep(.el-radio-group) {
  display: flex;
  width: 100%;
}

:deep(.el-radio-button) {
  flex: 1;
}
</style>
