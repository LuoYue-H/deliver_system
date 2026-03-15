<template>
  <div class="admin-rider-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">骑手管理</h2>
        <span class="sub-title">管理平台配送骑手账号</span>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openAddDialog">新增骑手</el-button>
        <el-button :icon="Refresh" @click="fetchRiders" circle />
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table 
        :data="riders" 
        style="width: 100%" 
        v-loading="loading" 
        stripe
        :header-cell-style="{ textAlign: 'center' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="骑手用户名" min-width="150" align="center">
          <template #default="scope">
            <div class="user-cell" style="display: flex; align-items: center; justify-content: center;">
              <el-avatar :size="28" :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="mr-2" />
              <span>{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="120" align="center">
          <template #default>
            <el-tag type="success" effect="plain" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="初始密码" width="150" align="center">
          <template #default>
            <span class="password-placeholder">已加密存储</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="openEditDialog(scope.row)">修改</el-button>
            <el-button size="small" type="danger" plain :icon="Delete" @click="deleteRider(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/修改骑手弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="420px"
      border-radius="8px"
    >
      <el-form :model="form" label-position="top" class="rider-form">
        <el-form-item label="登录账号" required>
          <el-input v-model="form.username" placeholder="请输入骑手登录用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="登录密码" :required="!isEdit">
          <el-input 
            v-model="form.password" 
            type="password" 
            show-password
            :prefix-icon="Lock"
            :placeholder="isEdit ? '若不修改请留空' : '请输入登录密码'" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import request from '@/utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, Edit, Delete, UserFilled, User, Lock } from '@element-plus/icons-vue';

interface Rider {
  id: number;
  username: string;
  password?: string;
}

const riders = ref<Rider[]>([]);
const loading = ref(false);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const isEdit = ref(false);

const form = reactive<Partial<Rider>>({
  id: undefined,
  username: '',
  password: '',
});

const fetchRiders = async () => {
  loading.value = true;
  try {
    const response = await request.get('/admin/riders');
    riders.value = response.data;
  } catch (error) {
    ElMessage.error('获取骑手列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchRiders();
});

const resetForm = () => {
  form.id = undefined;
  form.username = '';
  form.password = '';
};

const openAddDialog = () => {
  resetForm();
  isEdit.value = false;
  dialogTitle.value = '新增配送骑手';
  dialogVisible.value = true;
};

const openEditDialog = (rider: Rider) => {
  resetForm();
  isEdit.value = true;
  dialogTitle.value = '编辑骑手信息';
  form.id = rider.id;
  form.username = rider.username;
  dialogVisible.value = true;
};

const deleteRider = (id: number) => {
  ElMessageBox.confirm('确认删除该骑手账号？删除后该骑手将无法登录系统。', '风险提示', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      await request.delete(`/admin/riders/${id}`);
      ElMessage.success('已成功删除');
      fetchRiders();
    } catch (e) {}
  });
};

const handleSubmit = async () => {
  if (!form.username) {
    ElMessage.error('请输入用户名');
    return;
  }
  if (!isEdit.value && !form.password) {
    ElMessage.error('请输入初始密码');
    return;
  }

  submitLoading.value = true;
  try {
    if (isEdit.value) {
      await request.put('/admin/riders', form);
      ElMessage.success('修改成功');
    } else {
      await request.post('/admin/riders', form);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    fetchRiders();
  } catch (error) {
  } finally {
    submitLoading.value = false;
  }
};
</script>

<style scoped>
.admin-rider-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.sub-title {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
  display: block;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.table-card {
  border-radius: 8px;
  border: none;
}

.user-cell {
  display: flex;
  align-items: center;
}

.mr-2 {
  margin-right: 8px;
}

.password-placeholder {
  color: #c0c4cc;
  font-size: 12px;
  font-style: italic;
}

.rider-form {
  padding: 10px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-table__header) {
  background-color: #f5f7fa;
}
</style>
