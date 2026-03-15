<template>
  <div class="admin-review-container">
    <div class="page-header">
      <h2 class="page-title">商家入驻审核</h2>
      <el-button type="primary" @click="fetchReviewList" :icon="Refresh" circle />
    </div>

    <el-card shadow="never" class="table-card">
      <el-table 
        :data="reviewList" 
        style="width: 100%" 
        v-loading="loading" 
        stripe
        :header-cell-style="{ textAlign: 'center' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="申请店铺名" min-width="150" align="center" />
        <el-table-column label="经营品类" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.merchantDetails?.category" size="small">{{ scope.row.merchantDetails.category }}</el-tag>
            <span v-else class="text-gray-400">未填写</span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180" align="center">
          <template #default="scope">
            <el-icon style="vertical-align: middle; margin-right: 4px"><Clock /></el-icon>
            <span>{{ formatTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="审核提交时间" width="180" align="center">
          <template #default="scope">
            <el-icon style="vertical-align: middle; margin-right: 4px"><Clock /></el-icon>
            <span :class="{ 'text-primary': scope.row.merchantDetails?.submitTime, 'text-gray-400': !scope.row.merchantDetails?.submitTime }">
              {{ scope.row.merchantDetails?.submitTime ? formatTime(scope.row.merchantDetails.submitTime) : '尚未提交' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120" align="center">
          <template #default>
            <el-tag type="warning" effect="light">待审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="scope">
            <el-button size="small" :icon="View" @click="showDetails(scope.row)">详情</el-button>
            <el-button size="small" type="success" :icon="Check" @click="handleApprove(scope.row.id)">通过</el-button>
            <el-button size="small" type="danger" :icon="Close" @click="showRejectDialog(scope.row.id)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailsVisible" title="商家资质详情" width="600px" destroy-on-close>
      <div v-if="selectedMerchant && selectedMerchant.merchantDetails" class="merchant-details">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请人账号">{{ selectedMerchant.username }}</el-descriptions-item>
          <el-descriptions-item label="主营品类">
            <el-tag v-if="selectedMerchant.merchantDetails.category" type="info">{{ selectedMerchant.merchantDetails.category }}</el-tag>
            <span v-else>未填写</span>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTime(selectedMerchant.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核提交时间">
            {{ selectedMerchant.merchantDetails.submitTime ? formatTime(selectedMerchant.merchantDetails.submitTime) : '尚未提交' }}
          </el-descriptions-item>
          <el-descriptions-item label="食品经营许可证">
            <el-image 
              :src="selectedMerchant.merchantDetails.licenseUrl || 'https://cube.elemecdn.com/9/c1/3f8e088713835624736636746815epng.png'" 
              :preview-src-list="[selectedMerchant.merchantDetails.licenseUrl || 'https://cube.elemecdn.com/9/c1/3f8e088713835624736636746815epng.png']"
              class="detail-img"
              fit="contain"
            />
          </el-descriptions-item>
          <el-descriptions-item label="法人身份证(正面)">
            <el-image 
              :src="selectedMerchant.merchantDetails.idCardFrontUrl || 'https://cube.elemecdn.com/9/c1/3f8e088713835624736636746815epng.png'" 
              :preview-src-list="[selectedMerchant.merchantDetails.idCardFrontUrl || 'https://cube.elemecdn.com/9/c1/3f8e088713835624736636746815epng.png']"
              class="detail-img"
              fit="contain"
            />
          </el-descriptions-item>
          <el-descriptions-item label="法人身份证(背面)">
            <el-image 
              :src="selectedMerchant.merchantDetails.idCardBackUrl || 'https://cube.elemecdn.com/9/c1/3f8e088713835624736636746815epng.png'" 
              :preview-src-list="[selectedMerchant.merchantDetails.idCardBackUrl || 'https://cube.elemecdn.com/9/c1/3f8e088713835624736636746815epng.png']"
              class="detail-img"
              fit="contain"
            />
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectVisible" title="驳回审核" width="400px">
      <div class="reject-form">
        <p class="form-tip">请填写驳回原因，将通知商家进行修改：</p>
        <el-input 
          v-model="rejectReason" 
          type="textarea" 
          :rows="4"
          placeholder="例如：身份证照片模糊、许可证已过期等"
        />
      </div>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReject" :loading="rejectLoading">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import request from '../../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, Clock, View, Check, Close } from '@element-plus/icons-vue';

const reviewList = ref([]);
const loading = ref(false);
const detailsVisible = ref(false);
const rejectVisible = ref(false);
const rejectLoading = ref(false);
const selectedMerchant = ref<any>(null);
const rejectReason = ref('');
const currentId = ref<number | null>(null);

const fetchReviewList = async () => {
  loading.value = true;
  try {
    const res: any = await request.get('/admin/review/list');
    reviewList.value = res.data;
  } catch (e) {
  } finally {
    loading.value = false;
  }
};

onMounted(fetchReviewList);

const showDetails = (merchant: any) => {
  selectedMerchant.value = merchant;
  detailsVisible.value = true;
};

const handleApprove = (id: number) => {
  ElMessageBox.confirm('确认该商家资质真实有效，准予入驻？', '通过审核', {
    confirmButtonText: '确定通过',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    await request.put(`/admin/review/approve/${id}`);
    ElMessage.success('操作成功');
    fetchReviewList();
  });
};

const showRejectDialog = (id: number) => {
  currentId.value = id;
  rejectReason.value = '';
  rejectVisible.value = true;
};

const handleReject = async () => {
  if (!rejectReason.value) {
    ElMessage.error('请输入驳回原因');
    return;
  }
  rejectLoading.value = true;
  try {
    await request.put(`/admin/review/reject/${currentId.value}`, { reason: rejectReason.value });
    ElMessage.success('已成功驳回');
    rejectVisible.value = false;
    fetchReviewList();
  } catch (e) {
  } finally {
    rejectLoading.value = false;
  }
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
.admin-review-container {
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

.detail-img {
  width: 100%;
  max-height: 200px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.reject-form .form-tip {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

:deep(.el-table__header) {
  background-color: #f5f7fa;
}
</style>
