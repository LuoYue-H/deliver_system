<template>
  <el-upload
    class="avatar-uploader"
    action="/api/upload" 
    :show-file-list="false"
    :on-success="handleAvatarSuccess"
    :before-upload="beforeAvatarUpload"
    :headers="headers"
    :style="uploadStyle"
  >
    <img v-if="imageUrl" :src="imageUrl" class="avatar" :style="uploadStyle" />
    <el-icon v-else class="avatar-uploader-icon" :style="{ fontSize: size/4 + 'px', width: size + 'px', height: size + 'px' }"><Plus /></el-icon>
  </el-upload>
</template>

<script setup lang="ts">
import { ref, computed, withDefaults } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadProps } from 'element-plus'

const props = withDefaults(defineProps<{
  modelValue: string,
  size?: number,
  circle?: boolean
}>(), {
  size: 100,
  circle: true
})

const emit = defineEmits(['update:modelValue'])

const imageUrl = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const uploadStyle = computed(() => ({
  width: props.size + 'px',
  height: props.size + 'px',
  lineHeight: props.size + 'px',
  borderRadius: props.circle ? '50%' : '8px'
}))

const headers = computed(() => {
    return {
        Authorization: 'Bearer ' + localStorage.getItem('token')
    }
})

const handleAvatarSuccess: UploadProps['onSuccess'] = (
  response,
  uploadFile
) => {
  // 假设后端返回 { code: 1, data: 'url' }
  if(response.code === 1) {
      imageUrl.value = response.data
      ElMessage.success('上传成功')
  } else {
      ElMessage.error('上传失败')
  }
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('Avatar picture must be JPG format!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('Avatar picture size can not exceed 2MB!')
    return false
  }
  return true
}
</script>

<style scoped>
.avatar-uploader .avatar {
  display: block;
  object-fit: cover;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  color: #8c939d;
  text-align: center;
}
</style>
