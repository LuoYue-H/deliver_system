<template>
  <div class="feature-lock-container">
    <el-result
      icon="warning"
      title="功能尚未开通"
      sub-title="您的店铺资料尚未提交审核或正在审核中，请先完善并提交审核材料以解锁此功能。"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/merchant/review-status')">
          查看审核进度
        </el-button>
        <el-button @click="$router.push('/merchant/review-form')">
          前往提交资料
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
// 无需逻辑
</script>

<style scoped>
.feature-lock-container {
  height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
</style>
