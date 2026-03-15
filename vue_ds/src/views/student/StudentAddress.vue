<template>
  <div class="address-page">
    <div class="page-header">
      <div class="title-section">
        <h2 class="main-title">收货地址</h2>
        <p class="sub-title">管理您的配送地址，下单更快捷</p>
      </div>
      <el-button type="primary" size="large" :icon="Plus" round @click="handleAdd" class="add-btn">
        新增地址
      </el-button>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="skeleton-container">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" v-for="n in 3" :key="n">
          <el-skeleton animated>
            <template #template>
              <el-card class="skeleton-card">
                <el-skeleton-item variant="text" style="width: 40%; height: 20px; margin-bottom: 15px;" />
                <el-skeleton-item variant="text" style="width: 70%;" />
                <el-skeleton-item variant="text" style="width: 80%; margin-top: 10px;" />
                <div style="margin-top: 20px; display: flex; justify-content: flex-end; gap: 10px;">
                  <el-skeleton-item variant="circle" style="width: 24px; height: 24px;" />
                  <el-skeleton-item variant="circle" style="width: 24px; height: 24px;" />
                </div>
              </el-card>
            </template>
          </el-skeleton>
        </el-col>
      </el-row>
    </div>

    <!-- 空状态 -->
    <div v-else-if="addressList.length === 0" class="empty-section">
      <el-empty description="暂无收货地址，点击上方按钮添加吧" />
    </div>

    <!-- 地址列表 -->
    <el-row v-else :gutter="20" class="address-list">
      <el-col :xs="24" :sm="12" :md="8" v-for="item in addressList" :key="item.id">
        <el-card 
          class="address-card" 
          :class="{ 'default-active': item.isDefault === 1 }"
          shadow="hover"
        >
          <div class="card-top">
            <div class="user-info">
              <span class="name">{{ item.contactName }}</span>
              <span class="phone">{{ maskPhone(item.contactPhone) }}</span>
            </div>
            <el-tag v-if="item.isDefault === 1" type="warning" effect="dark" size="small" round>默认</el-tag>
          </div>
          
          <div class="address-detail">
            <el-icon class="location-icon"><Location /></el-icon>
            <p class="address-text">{{ item.address }}</p>
          </div>

          <div class="card-footer">
            <div class="default-action">
              <el-button 
                v-if="item.isDefault !== 1" 
                link 
                type="primary" 
                size="small" 
                @click="setDefault(item)"
              >
                设为默认
              </el-button>
            </div>
            <div class="actions">
              <el-tooltip content="编辑" placement="top">
                <el-button :icon="Edit" circle size="small" @click="handleEdit(item)" />
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button :icon="Delete" circle size="small" type="danger" plain @click="handleDelete(item.id)" />
              </el-tooltip>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 地址弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑收货地址' : '新增收货地址'" 
      width="440px"
      append-to-body
      class="address-dialog"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请填写收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号码" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请填写收货人手机号" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input 
            v-model="form.address" 
            type="textarea" 
            :rows="3" 
            placeholder="请填写详细的校内配送地址（如：东苑5舍302）" 
          />
        </el-form-item>
        <el-form-item label="设为默认地址">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" round>取消</el-button>
          <el-button type="primary" @click="handleSubmit" round :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import request from '../../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Location } from '@element-plus/icons-vue';

const addressList = ref<any[]>([]);
const loading = ref(true);
const submitting = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref();

const form = ref({
  id: null,
  contactName: '',
  contactPhone: '',
  address: '',
  isDefault: 0
});

const rules = {
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
};

const fetchAddresses = async () => {
  loading.value = true;
  try {
    const res: any = await request.get('/address/list');
    addressList.value = res.data.sort((a: any, b: any) => b.isDefault - a.isDefault);
  } catch (e) {
    console.error("获取地址失败", e);
  } finally {
    loading.value = false;
  }
};

const maskPhone = (phone: string) => {
  if (!phone) return '';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

const handleAdd = () => {
  isEdit.value = false;
  form.value = { id: null, contactName: '', contactPhone: '', address: '', isDefault: 0 };
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  isEdit.value = true;
  form.value = { ...row };
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true;
      try {
        if (isEdit.value) {
          await request.put('/address/update', form.value);
          ElMessage.success('更新成功');
        } else {
          await request.post('/address/save', form.value);
          ElMessage.success('保存成功');
        }
        dialogVisible.value = false;
        fetchAddresses();
      } catch (e) {
        ElMessage.error('操作失败');
      } finally {
        submitting.value = false;
      }
    }
  });
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除这个地址吗? 删除后不可恢复。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    roundButton: true
  }).then(async () => {
    try {
      await request.delete(`/address/delete/${id}`);
      ElMessage.success('删除成功');
      fetchAddresses();
    } catch (e) {
      ElMessage.error('删除失败');
    }
  });
};

const setDefault = async (row: any) => {
  try {
    await request.put(`/address/default/${row.id}`);
    ElMessage.success('默认地址设置成功');
    fetchAddresses();
  } catch (e) {
    ElMessage.error('设置失败');
  }
};

onMounted(fetchAddresses);
</script>

<style scoped>
.address-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 30px;
}

.main-title {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.sub-title {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.add-btn {
  padding: 12px 24px;
  font-weight: 600;
}

.address-list {
  margin-top: 10px;
}

.address-card {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  margin-bottom: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.address-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.06);
  border-color: #409eff;
}

.default-active {
  background: linear-gradient(to bottom right, #ffffff, #f0f7ff);
  border-color: #409eff;
  border-width: 1.5px;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.phone {
  font-size: 14px;
  color: #64748b;
}

.address-detail {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  min-height: 48px;
}

.location-icon {
  margin-top: 3px;
  color: #94a3b8;
  font-size: 16px;
}

.address-text {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  margin: 0;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f1f5f9;
}

.actions {
  display: flex;
  gap: 8px;
}

/* 骨架屏 */
.skeleton-card {
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
}

.empty-section {
  padding: 100px 0;
}

/* 弹窗样式 */
:deep(.address-dialog) {
  border-radius: 20px;
}

:deep(.el-dialog__header) {
  margin-right: 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #f1f5f9;
}

:deep(.el-dialog__title) {
  font-weight: 700;
  font-size: 18px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
