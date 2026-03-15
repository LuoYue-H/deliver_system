<template>
  <div class="admin-user-container">
    <div class="page-header">
      <h2 class="page-title">商家管理</h2>
      <el-button type="primary" @click="fetchUsers" :icon="Refresh" circle />
    </div>

    <el-card shadow="never" class="table-card">
      <el-table 
        :data="merchantList" 
        style="width: 100%" 
        v-loading="loading" 
        stripe
        :header-cell-style="{ textAlign: 'center' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="店铺名称" min-width="150" align="center" />
        <el-table-column label="经营品类" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.merchantDetails?.category" size="small">{{ scope.row.merchantDetails.category }}</el-tag>
            <span v-else class="text-gray-400">未填写</span>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="150" align="center">
          <template #default="scope">
            <el-tag 
              v-if="scope.row.merchantDetails" 
              :type="getStatusType(scope.row.merchantDetails.status)"
              effect="light"
            >
              {{ formatStatus(scope.row.merchantDetails.status) }}
            </el-tag>
            <el-tag v-else type="info">未提交资料</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" align="center">
          <template #default="scope">
            <span>{{ formatTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="审核提交时间" width="180" align="center">
          <template #default="scope">
            <span :class="{ 'text-primary': scope.row.merchantDetails?.submitTime, 'text-gray-400': !scope.row.merchantDetails?.submitTime }">
              {{ scope.row.merchantDetails?.submitTime ? formatTime(scope.row.merchantDetails.submitTime) : '尚未提交' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              v-if="scope.row.merchantDetails && scope.row.merchantDetails.status === 2"
              size="small" 
              type="warning" 
              plain
              :icon="Warning"
              @click="showReauditDialog(scope.row.id)"
            >
              要求重审
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 要求重审弹窗 -->
    <el-dialog v-model="reauditVisible" title="发起重新审核" width="450px">
      <div class="reaudit-form">
        <p class="form-tip">请输入要求该商家重新提交审核的原因：</p>
        <el-input 
          v-model="reauditReason" 
          type="textarea" 
          :rows="4"
          placeholder="例如：检测到店铺信息有误，请重新上传资质文件。"
        />
      </div>
      <template #footer>
        <el-button @click="reauditVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReaudit" :loading="reauditLoading">确认发起</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import request from '../../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, Warning } from '@element-plus/icons-vue';

const userList = ref([]);
const loading = ref(false);
const reauditVisible = ref(false);
const reauditLoading = ref(false);
const reauditReason = ref('');
const currentId = ref<number | null>(null);

const merchantList = computed(() => userList.value.filter((user: any) => user.role === 'MERCHANT'));

const fetchUsers = async () => {
  loading.value = true;
  try {
    const res: any = await request.get('/admin/users/list');
    userList.value = res.data;
  } catch (e) {
  } finally {
    loading.value = false;
  }
};

onMounted(fetchUsers);

const formatStatus = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '未提交',
    1: '待审核',
    2: '审核通过',
    3: '审核不通过'
  };
  return statusMap[status] || '未知';
};

const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  };
  return typeMap[status] || 'info';
};

const showReauditDialog = (id: number) => {
  currentId.value = id;
  reauditReason.value = '';
  reauditVisible.value = true;
};

const handleReaudit = async () => {
  if (!reauditReason.value) {
    ElMessage.error('请输入重审原因');
    return;
  }
  
  ElMessageBox.confirm('发起重审后，商家需重新上传资质方可继续营业，确认发起？', '重审确认', {
    confirmButtonText: '确认发起',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    reauditLoading.value = true;
    try {
      await request.put(`/admin/trigger-reaudit/${currentId.value}`, { reason: reauditReason.value });
      ElMessage.success('已发起重新审核要求');
      reauditVisible.value = false;
      fetchUsers();
    } catch (e) {
    } finally {
      reauditLoading.value = false;
    }
  });
};

const formatTime = (time: string) => {
  if (!time) return '';
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};
</script>

<style scoped>
.admin-user-container {
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

.table-card {
  border-radius: 8px;
  border: none;
}

.reaudit-form .form-tip {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

:deep(.el-table__header) {
  background-color: #f5f7fa;
}
</style>
