<template>
  <div class="form-container">
    <el-card shadow="hover" class="form-card">
      <template #header>
        <div class="card-header">
          <span class="title">资质资料上传</span>
          <el-button @click="$router.back()">取消修改</el-button>
        </div>
      </template>

      <el-alert
        title="资料上传须知"
        type="info"
        description="请确保上传的证件照片清晰、完整，且在有效期内。平台将在1-2个工作日内完成审核。"
        show-icon
        :closable="false"
        class="mb-6"
      />

      <el-form :model="form" label-position="top" class="review-form">
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="主营品类" required>
              <el-select v-model="form.category" placeholder="请选择您的经营范围" style="width: 100%" size="large">
                <el-option label="快餐便当" value="便当" />
                <el-option label="奶茶饮品" value="奶茶" />
                <el-option label="甜品蛋糕" value="蛋糕" />
                <el-option label="特色小吃" value="小吃" />
                <el-option label="咖啡午茶" value="咖啡" />
                <el-option label="麻辣烫" value="辣" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="1. 食品经营许可证" required>
              <div class="upload-section">
                <el-upload
                  class="license-uploader"
                  :http-request="(options) => handleHttpRequest(options, 'licenseUrl')"
                  :show-file-list="false"
                  accept="image/*"
                >
                  <div v-if="form.licenseUrl" class="image-wrapper">
                    <img :src="form.licenseUrl" class="preview-img" />
                    <div class="image-mask">
                      <el-icon><Edit /></el-icon>
                      <span>更换图片</span>
                    </div>
                  </div>
                  <div v-else class="upload-placeholder">
                    <el-icon class="upload-icon"><Plus /></el-icon>
                    <div class="upload-text">点击上传许可证</div>
                  </div>
                </el-upload>
                <div class="upload-tip">需上传正本或副本原件扫描件，支持 jpg/png 格式</div>
              </div>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="2. 法人身份证 (正面)" required>
              <div class="upload-section">
                <el-upload
                  class="id-uploader"
                  :http-request="(options) => handleHttpRequest(options, 'idCardFrontUrl')"
                  :show-file-list="false"
                  accept="image/*"
                >
                  <div v-if="form.idCardFrontUrl" class="image-wrapper">
                    <img :src="form.idCardFrontUrl" class="preview-img" />
                    <div class="image-mask">
                      <el-icon><Edit /></el-icon>
                    </div>
                  </div>
                  <div v-else class="upload-placeholder id-card">
                    <el-icon><Plus /></el-icon>
                  </div>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="3. 法人身份证 (背面)" required>
              <div class="upload-section">
                <el-upload
                  class="id-uploader"
                  :http-request="(options) => handleHttpRequest(options, 'idCardBackUrl')"
                  :show-file-list="false"
                  accept="image/*"
                >
                  <div v-if="form.idCardBackUrl" class="image-wrapper">
                    <img :src="form.idCardBackUrl" class="preview-img" />
                    <div class="image-mask">
                      <el-icon><Edit /></el-icon>
                    </div>
                  </div>
                  <div v-else class="upload-placeholder id-card">
                    <el-icon><Plus /></el-icon>
                  </div>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-footer">
          <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting" class="submit-btn">
            提交审核材料
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '../../utils/request';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { Plus, Edit } from '@element-plus/icons-vue';
import type { UploadRequestOptions } from 'element-plus';

const router = useRouter();
const submitting = ref(false);
const form = ref({
  category: '',
  licenseUrl: '',
  idCardFrontUrl: '',
  idCardBackUrl: ''
});

onMounted(async () => {
  try {
    const res: any = await request.get('/login/profile');
    if (res.data.merchantDetails) {
      form.value.category = res.data.merchantDetails.category || '';
      form.value.licenseUrl = res.data.merchantDetails.licenseUrl || '';
      form.value.idCardFrontUrl = res.data.merchantDetails.idCardFrontUrl || '';
      form.value.idCardBackUrl = res.data.merchantDetails.idCardBackUrl || '';
    }
  } catch (error) {
    console.error('获取商家信息失败', error);
  }
});

const handleHttpRequest = async (options: UploadRequestOptions, field: 'licenseUrl' | 'idCardFrontUrl' | 'idCardBackUrl') => {
  const formData = new FormData();
  formData.append('file', options.file);

  try {
    const response = await axios.post('/api/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });

    const result = response.data;
    if (result.code == 1) {
      form.value[field] = result.data;
      ElMessage.success('上传成功');
    } else {
      ElMessage.error(result.msg || '上传失败');
    }
  } catch (error) {
    ElMessage.error('上传过程中发生错误');
    return Promise.reject(error);
  }
};

const handleSubmit = async () => {
  if (!form.value.category || !form.value.licenseUrl || !form.value.idCardFrontUrl || !form.value.idCardBackUrl) {
    ElMessage.error('请填写主营品类并上传所有必需的审核材料');
    return;
  }
  
  submitting.value = true;
  try {
    await request.put('/user/submit-review', form.value);
    ElMessage.success('材料已提交更新，请等待审核');
    router.push('/merchant/review-status');
  } catch (e) {
    ElMessage.error('提交失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.form-container {
  padding: 40px 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.form-card {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 18px;
  font-weight: bold;
}

.mb-6 { margin-bottom: 24px; }

.upload-section {
  width: 100%;
}

.upload-placeholder {
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  background-color: #fafafa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-placeholder:hover {
  border-color: #409eff;
}

.license-uploader .upload-placeholder {
  height: 200px;
}

.id-uploader .upload-placeholder {
  height: 120px;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  color: #606266;
}

.image-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-wrapper:hover .image-mask {
  opacity: 1;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.form-footer {
  margin-top: 40px;
  text-align: center;
}

.submit-btn {
  width: 240px;
  height: 48px;
  font-size: 16px;
  font-weight: bold;
}
</style>
