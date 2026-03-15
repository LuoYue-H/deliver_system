<template>
  <div class="status-container">
    <el-row :gutter="24">
      <!-- 左侧：当前状态卡片 -->
      <el-col :span="16">
        <el-card shadow="hover" class="main-card">
          <template #header>
            <div class="card-header">
              <span class="title">入驻审核进度</span>
              <el-tag :type="statusConfig.type" effect="dark">{{ statusConfig.text }}</el-tag>
            </div>
          </template>

          <div class="status-content">
            <div class="status-icon-wrapper" :class="statusConfig.class">
              <el-icon><component :is="statusConfig.icon" /></el-icon>
            </div>
            
            <div class="status-info">
              <h2 :class="statusConfig.class">{{ statusConfig.title }}</h2>
              <p class="description">{{ statusConfig.description }}</p>
              
              <div class="info-grid">
                <div class="info-item">
                  <span class="label">店铺名称</span>
                  <span class="value">{{ profile.username }}</span>
                </div>
                <div class="info-item">
                  <span class="label">主营品类</span>
                  <span class="value">
                    <el-tag v-if="profile.merchantDetails?.category" size="small">{{ profile.merchantDetails.category }}</el-tag>
                    <span v-else class="text-gray-400">未设置</span>
                  </span>
                </div>
                <div class="info-item">
                  <span class="label">注册时间</span>
                  <span class="value">{{ profile.createTime ? formatTime(profile.createTime) : '未知' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">审核提交时间</span>
                  <span class="value">{{ profile.merchantDetails?.submitTime ? formatTime(profile.merchantDetails.submitTime) : '尚未提交' }}</span>
                </div>
              </div>
              
              <div v-if="profile.merchantDetails?.status === 3" class="reject-reason">
                <el-alert
                  title="驳回反馈"
                  type="error"
                  :description="profile.merchantDetails.rejectReason"
                  show-icon
                  :closable="false"
                />
              </div>

              <div class="action-buttons">
                <el-button 
                  v-if="[0, 1, 3].includes(profile.merchantDetails?.status)" 
                  type="primary" 
                  @click="$router.push('/merchant/review-form')"
                >
                  {{ profile.merchantDetails?.status === 1 ? '修改申请材料' : '立即处理' }}
                </el-button>
                <el-button 
                  v-if="profile.merchantDetails?.status === 2" 
                  type="primary" 
                  plain
                  @click="$router.push('/merchant/profile')"
                >
                  修改店铺资料
                </el-button>
                <el-button @click="showMaterialDialog = true">查看已提交材料</el-button>
                <el-button v-if="profile.merchantDetails?.status === 2" @click="$router.push('/merchant/dashboard')">
                  返回后台首页
                </el-button>
              </div>
            </div>
          </div>

          <el-divider>当前资料预览</el-divider>
          <div class="material-preview-grid" v-if="profile.merchantDetails">
            <div class="preview-item">
              <p>食品经营许可证</p>
              <el-image 
                :src="profile.merchantDetails.licenseUrl" 
                :preview-src-list="[profile.merchantDetails.licenseUrl]"
                fit="cover"
              />
            </div>
            <div class="preview-item">
              <p>法人身份证(正)</p>
              <el-image 
                :src="profile.merchantDetails.idCardFrontUrl" 
                :preview-src-list="[profile.merchantDetails.idCardFrontUrl]"
                fit="cover"
              />
            </div>
            <div class="preview-item">
              <p>法人身份证(背)</p>
              <el-image 
                :src="profile.merchantDetails.idCardBackUrl" 
                :preview-src-list="[profile.merchantDetails.idCardBackUrl]"
                fit="cover"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：审核历史时间线 -->
      <el-col :span="8">
        <el-card shadow="hover" class="history-card">
          <template #header>
            <div class="card-header">
              <span class="title">审核历史记录</span>
            </div>
          </template>
          
          <el-scrollbar height="500px">
            <el-timeline v-if="history.length > 0">
              <el-timeline-item
                v-for="(item, index) in history"
                :key="item.id"
                :timestamp="formatTime(item.createTime)"
                :type="index === 0 ? 'primary' : ''"
                :hollow="index !== 0"
              >
                <div class="history-item">
                  <span class="action-tag">{{ getActionText(item.action) }}</span>
                  <p class="comment" v-if="item.comment">{{ item.comment }}</p>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无历史记录" />
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>

    <!-- 材料详情弹窗 -->
    <el-dialog v-model="showMaterialDialog" title="已提交材料详情" width="60%">
      <div class="dialog-materials">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="食品经营许可证">
            <el-image :src="profile.merchantDetails?.licenseUrl" style="max-width: 400px" />
          </el-descriptions-item>
          <el-descriptions-item label="法人身份证(正面)">
            <el-image :src="profile.merchantDetails?.idCardFrontUrl" style="max-width: 400px" />
          </el-descriptions-item>
          <el-descriptions-item label="法人身份证(背面)">
            <el-image :src="profile.merchantDetails?.idCardBackUrl" style="max-width: 400px" />
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import request from '../../utils/request';
import { Clock, CircleCheck, CircleClose, Warning } from '@element-plus/icons-vue';

const profile = ref<any>({});
const history = ref<any[]>([]);
const showMaterialDialog = ref(false);

const statusConfig = computed(() => {
  const status = profile.value.merchantDetails?.status;
  switch (status) {
    case 1:
      return {
        text: '审核中',
        type: 'warning',
        icon: Clock,
        class: 'warning',
        title: '资料审核中',
        description: '您的入驻申请已收到，平台正在抓紧审核中，预计 1-2 个工作日完成。'
      };
    case 2:
      return {
        text: '已通过',
        type: 'success',
        icon: CircleCheck,
        class: 'success',
        title: '入驻成功',
        description: '恭喜！您的店铺已通过审核，现在可以开始管理您的菜单和订单了。'
      };
    case 3:
      return {
        text: '未通过',
        type: 'danger',
        icon: CircleClose,
        class: 'danger',
        title: '审核未通过',
        description: '很抱歉，您的入驻申请未能通过。请根据下方反馈原因修改资料后重新提交。'
      };
    default:
      return {
        text: '未提交',
        type: 'info',
        icon: Warning,
        class: 'info',
        title: '尚未提交申请',
        description: '欢迎入驻校园外卖平台！请先提交您的店铺资质材料。'
      };
  }
});

const getActionText = (action: string) => {
  const map: any = {
    'SUBMIT': '提交申请',
    'APPROVE': '审核通过',
    'REJECT': '审核驳回',
    'RE_AUDIT_REQUESTED': '要求重新审核'
  };
  return map[action] || action;
};

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

onMounted(async () => {
  const profileRes: any = await request.get('/login/profile');
  profile.value = profileRes.data;

  const historyRes: any = await request.get('/user/review/history');
  history.value = historyRes.data.reverse(); // 最新在最前
});
</script>

<style scoped>
.status-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.main-card, .history-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.status-content {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 20px 0;
}

.status-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}

.status-icon-wrapper.warning { background-color: #fdf6ec; color: #e6a23c; }
.status-icon-wrapper.success { background-color: #f0f9eb; color: #67c23a; }
.status-icon-wrapper.danger { background-color: #fef0f0; color: #f56c6c; }
.status-icon-wrapper.info { background-color: #f4f4f5; color: #909399; }

.status-info h2 { margin: 0 0 12px 0; }
.status-info .description { color: #606266; margin-bottom: 24px; font-size: 15px; }

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  background-color: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: #94a3b8;
}

.info-item .value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.reject-reason {
  margin-bottom: 24px;
  max-width: 500px;
}

.material-preview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.preview-item {
  text-align: center;
}

.preview-item p {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.preview-item .el-image {
  width: 100%;
  height: 120px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.history-item {
  padding: 8px 0;
}

.action-tag {
  font-weight: bold;
  color: #303133;
  font-size: 14px;
}

.comment {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #606266;
}

.warning { color: #e6a23c; }
.success { color: #67c23a; }
.danger { color: #f56c6c; }
.info { color: #909399; }

:deep(.el-timeline-item__node--primary) {
  background-color: #409eff;
}
</style>
