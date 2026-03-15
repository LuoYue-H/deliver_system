<template>
  <div class="audit-container">
    <div class="header">
      <h2>商家入驻审核</h2>
      <el-button :icon="Refresh" circle @click="fetchAuditList" />
    </div>

    <el-table :data="auditList" v-loading="loading" style="width: 100%">
      <el-table-column prop="username" label="店铺名称" />
      <el-table-column label="注册时间">
        <template #default="scope">
          {{ scope.row.createTime ? formatTime(scope.row.createTime) : '未知' }}
        </template>
      </el-table-column>
      <el-table-column label="审核提交时间">
        <template #default="scope">
          {{ scope.row.merchantDetails?.submitTime ? formatTime(scope.row.merchantDetails.submitTime) : '尚未提交' }}
        </template>
      </el-table-column>
      <el-table-column label="经营品类">
        <template #default="scope">
          <el-tag v-if="scope.row.merchantDetails?.category" size="small">{{ scope.row.merchantDetails.category }}</el-tag>
          <span v-else class="text-gray-400">未填写</span>
        </template>
      </el-table-column>
      <el-table-column prop="license" label="营业执照">
        <template #default="scope">
          <el-button link type="primary" @click="viewImage(scope.row.merchantDetails?.licenseUrl)">查看执照</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="idCard" label="身份证明">
        <template #default="scope">
          <div class="id-card-btns">
            <el-button link type="primary" @click="viewImage(scope.row.merchantDetails?.idCardFrontUrl)">人像面</el-button>
            <el-button link type="primary" @click="viewImage(scope.row.merchantDetails?.idCardBackUrl)">国徽面</el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="success" size="small" @click="handleApprove(scope.row.id)">通过</el-button>
          <el-button type="danger" size="small" @click="handleReject(scope.row.id)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="查看图片" width="50%">
      <img :src="previewUrl" style="width: 100%" alt="预览" />
    </el-dialog>

    <!-- 驳回原因对话框 -->
    <el-dialog v-model="rejectVisible" title="驳回审核" width="400px">
      <el-form :model="rejectForm">
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectForm.reason" type="textarea" placeholder="请详细说明驳回原因..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import request from '../../utils/request'

const auditList = ref<any[]>([])
const loading = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const rejectVisible = ref(false)
const rejectForm = ref({ id: null, reason: '' })

const fetchAuditList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/review/list')
    auditList.value = res.data
  } catch (e) {
    ElMessage.error('获取审核列表失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = (id: number) => {
  ElMessageBox.confirm('确定通过该商家的入驻申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.put(`/admin/review/approve/${id}`)
    ElMessage.success('审核已通过')
    fetchAuditList()
  })
}

const handleReject = (id: any) => {
  rejectForm.value = { id, reason: '' }
  rejectVisible.value = true
}

const submitReject = async () => {
  if (!rejectForm.value.reason) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  await request.put(`/admin/review/reject/${rejectForm.value.id}`, { reason: rejectForm.value.reason })
  ElMessage.success('已驳回申请')
  rejectVisible.value = false
  fetchAuditList()
}

const viewImage = (url: string) => {
  if (!url) {
    ElMessage.info('该商家尚未上传此证件')
    return
  }
  previewUrl.value = url
  previewVisible.value = true
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return 'N/A'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(fetchAuditList)
</script>

<style scoped>
.audit-container {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.id-card-btns {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
</style>