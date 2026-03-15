<template>
  <div class="map-container">
      <h2>Mock 配送地图演示</h2>
      <div class="mock-map">
          <!-- 模拟地图背景 -->
          <div class="map-bg"></div>
          
          <!-- 商家点 -->
          <div class="point merchant">
              <el-icon><Shop /></el-icon>
              <span>商家</span>
          </div>
          
          <!-- 用户点 -->
          <div class="point user">
              <el-icon><User /></el-icon>
              <span>用户</span>
          </div>
          
          <!-- 骑手点 (动画移动) -->
          <div class="point rider">
              <el-icon><Bicycle /></el-icon>
              <span>骑手</span>
          </div>
          
          <!-- 路线虚线 -->
          <svg class="route-line" width="100%" height="100%">
              <line x1="100" y1="100" x2="500" y2="400" style="stroke:rgb(64,158,255);stroke-width:2;stroke-dasharray:5,5" />
          </svg>
      </div>
      <div class="tips">
          <p>当前演示：骑手正在从商家前往用户地址</p>
          <p>距离：2.5km | 预计时间：15分钟</p>
      </div>
  </div>
</template>

<script setup lang="ts">
import { Shop, User, Bicycle } from '@element-plus/icons-vue'
</script>

<style scoped>
.mock-map {
    position: relative;
    width: 100%;
    height: 500px;
    background-color: #f0f2f5;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;
}

.point {
    position: absolute;
    display: flex;
    flex-direction: column;
    align-items: center;
    font-weight: bold;
    z-index: 2;
}

.point .el-icon {
    font-size: 24px;
    background: white;
    padding: 5px;
    border-radius: 50%;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.merchant {
    top: 80px;
    left: 80px;
    color: #E6A23C;
}

.user {
    top: 380px;
    left: 480px;
    color: #F56C6C;
}

.rider {
    top: 80px;
    left: 80px;
    color: #409EFF;
    animation: move 10s linear infinite;
}

@keyframes move {
    0% {
        top: 80px;
        left: 80px;
    }
    100% {
        top: 380px;
        left: 480px;
    }
}

.route-line {
    position: absolute;
    top: 0;
    left: 0;
    z-index: 1;
}

.tips {
    margin-top: 20px;
    padding: 15px;
    background: #ecf5ff;
    border-radius: 4px;
    color: #409EFF;
}
</style>
