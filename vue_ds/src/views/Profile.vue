<template>
  <div>
    <el-card>
      <h2>{{ userRole === 'MERCHANT' ? '店铺信息' : '个人信息' }}</h2>
      <el-form :model="form" label-width="100px" style="max-width: 500px">
        <el-form-item :label="userRole === 'MERCHANT' ? '店铺名' : '用户名'">
          <el-input v-model="form.username" />
        </el-form-item>

        <div v-if="userRole === 'MERCHANT'">
          <el-form-item label="主营品类">
            <el-select v-model="form.merchantDetails.category" placeholder="请选择您的经营范围" style="width: 100%">
              <el-option label="快餐便当" value="便当" />
              <el-option label="奶茶饮品" value="奶茶" />
              <el-option label="甜品蛋糕" value="蛋糕" />
              <el-option label="特色小吃" value="小吃" />
              <el-option label="咖啡午茶" value="咖啡" />
              <el-option label="麻辣烫" value="辣" />
            </el-select>
          </el-form-item>
          <el-form-item label="店铺地址">
            <el-input v-model="form.address" type="textarea" />
          </el-form-item>
          <el-form-item label="联系人">
            <el-input v-model="form.contactName" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="form.contactPhone" />
          </el-form-item>
        </div>

        <el-form-item label="电话" v-if="userRole !== 'MERCHANT'">
          <el-input v-model="form.phone" />
        </el-form-item>

        <el-form-item :label="userRole === 'MERCHANT' ? '店铺头像' : '头像'">
          <ImageUpload v-model="form.avatar" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleProfileSubmit">保存修改</el-button>
          <el-button type="warning" @click="passwordVisible = true">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="passwordVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit">确定修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import request from '../utils/request';
import { ElMessage } from 'element-plus';
import ImageUpload from '../components/ImageUpload.vue';
import { useUserStore } from '../store/user';

const userRole = localStorage.getItem('role');
const userStore = useUserStore();

const form = ref<any>({
  username: '',
  phone: '',
  avatar: '',
  address: '',
  contactName: '',
  contactPhone: '',
  merchantDetails: {
    category: ''
  }
});

const passwordVisible = ref(false);
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const fetchProfile = async () => {
  const res: any = await request.get('/login/profile');
  form.value = { ...form.value, ...res.data };
  userStore.setUser(res.data); // 同步更新全局 store 中的用户信息
};

onMounted(fetchProfile);

const handleProfileSubmit = async () => {
  await request.put('/login/update', form.value);
  ElMessage.success('更新成功');
  await fetchProfile();
};

const handlePasswordSubmit = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.error('请输入完整信息');
    return;
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入密码不一致');
    return;
  }
  
  try {
    await request.put('/login/update-password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    });
    ElMessage.success('密码修改成功，请重新登录');
    passwordVisible.value = false;
    // 强制重新登录
    localStorage.clear();
    location.reload();
  } catch (e: any) {
    // 错误处理已在 request 拦截器中
  }
};
</script>
