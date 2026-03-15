<template>
  <div class="menu-management">
    <div v-if="userStore.merchantStatus === 2" class="content-wrapper">
      <el-row :gutter="24">
        <!-- 左侧分类导航 -->
        <el-col :xs="24" :sm="8" :md="6">
          <el-card class="category-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="title">菜品分类</span>
                <el-tooltip content="添加新分类" placement="top">
                  <el-button type="primary" :icon="Plus" circle size="small" @click="openCategoryDialog()" />
                </el-tooltip>
              </div>
            </template>
            <div class="category-list">
              <div 
                v-for="cat in categoryList" 
                :key="cat.id" 
                class="category-item"
                :class="{ active: currentCategoryId === cat.id.toString() }"
                @click="handleCategorySelect(cat.id.toString())"
              >
                <span class="cat-name">{{ cat.name }}</span>
                <div class="cat-actions">
                  <el-button link :icon="Edit" @click.stop="openCategoryDialog(cat)" />
                  <el-button link :icon="Delete" type="danger" @click.stop="deleteCategory(cat.id)" />
                </div>
              </div>
              <el-empty v-if="categoryList.length === 0" description="暂无分类" :image-size="60" />
            </div>
          </el-card>
        </el-col>

        <!-- 右侧菜品管理 -->
        <el-col :xs="24" :sm="16" :md="18">
          <el-card class="dish-card" shadow="never">
            <template #header>
              <div class="dish-header">
                <div class="header-left">
                  <h3 class="current-cat-title">{{ currentCategoryName }}</h3>
                  <el-input
                    v-model="dishSearch"
                    placeholder="搜索菜品名称..."
                    class="dish-search"
                    clearable
                    :prefix-icon="Search"
                  />
                  <el-button :icon="Refresh" circle @click="fetchAllData" :loading="loading" />
                </div>
                <el-button type="primary" :icon="Plus" @click="openAddDish" :disabled="!currentCategoryId">
                  添加菜品
                </el-button>
              </div>
            </template>

            <el-table 
              v-loading="loading"
              :data="displayDishes" 
              style="width: 100%"
              class="dish-table"
              :header-cell-style="{ backgroundColor: '#f8fafc', color: '#64748b', textAlign: 'center' }"
              :cell-style="{ textAlign: 'center' }"
            >
              <el-table-column label="商品图片" width="120" align="center">
                <template #default="scope">
                  <el-image 
                    :src="scope.row.image || defaultDishImg" 
                    class="dish-img" 
                    fit="cover"
                    :preview-src-list="[scope.row.image || defaultDishImg]"
                    preview-teleported
                  >
                    <template #error>
                      <div class="image-error"><el-icon><Picture /></el-icon></div>
                    </template>
                  </el-image>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="菜品名称" min-width="180" align="left">
                <template #default="scope">
                  <div class="dish-name-info">
                    <span class="name">{{ scope.row.name }}</span>
                    <p class="desc">{{ scope.row.description || '暂无描述' }}</p>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="price" label="价格" width="100" align="center">
                <template #default="scope">
                  <span class="price-text">¥{{ scope.row.price.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="库存" width="120" align="center">
                <template #default="scope">
                  <div class="stock-info">
                    <el-tag :type="getStockStatusType(scope.row.stock)" size="small" effect="plain">
                      {{ scope.row.stock }}
                    </el-tag>
                    <span class="stock-label">{{ getStockStatusText(scope.row.stock) }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="scope">
                  <el-switch
                    v-model="scope.row.status"
                    :active-value="1"
                    :inactive-value="0"
                    inline-prompt
                    active-text="上"
                    inactive-text="下"
                    @change="(val: any) => handleStatusChange(scope.row.id, val)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140" fixed="right" align="center">
                <template #default="scope">
                  <div class="action-btns">
                    <el-tooltip content="编辑菜品" placement="top">
                      <el-button 
                        type="primary" 
                        :icon="Edit" 
                        circle 
                        size="small" 
                        plain
                        @click="editDish(scope.row)" 
                      />
                    </el-tooltip>
                    <el-tooltip content="删除菜品" placement="top">
                      <el-button 
                        type="danger" 
                        :icon="Delete" 
                        circle 
                        size="small" 
                        plain
                        @click="deleteDish(scope.row.id)" 
                      />
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <FeatureLock v-else />

    <!-- 添加/编辑菜品弹窗 -->
    <el-dialog 
      v-model="showDishDialog" 
      :title="dishForm.id ? '编辑菜品' : '添加菜品'" 
      width="560px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form :model="dishForm" label-width="80px" label-position="left">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="菜品名称" required>
              <el-input v-model="dishForm.name" placeholder="请输入菜品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售价格" required>
              <el-input-number v-model="dishForm.price" :precision="2" :step="1" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="剩余库存" required>
              <el-input-number v-model="dishForm.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所属分类">
              <el-select v-model="dishForm.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="菜品图片">
              <ImageUpload v-model="dishForm.image" :circle="false" :size="120" />
              <p class="upload-tip">建议尺寸 400x400，大小不超过 2MB</p>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述介绍">
              <el-input 
                v-model="dishForm.description" 
                type="textarea" 
                :rows="3" 
                placeholder="简短介绍一下菜品特色吧..."
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="立即上架">
              <el-switch v-model="dishForm.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showDishDialog = false" round>取消</el-button>
          <el-button type="primary" @click="saveDish" round>确认保存</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 添加/编辑分类弹窗 -->
    <el-dialog v-model="showCategoryDialog" :title="categoryForm.id ? '修改分类' : '新增分类'" width="420px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" placeholder="如：主食、特色小吃" />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="categoryForm.sort" :min="0" style="width: 100%" />
          <p class="input-tip">数字越小，在手机端显示越靠前</p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCategoryDialog = false" round>取消</el-button>
        <el-button type="primary" @click="saveCategory" round>确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import request from '../../utils/request';
import ImageUpload from '../../components/ImageUpload.vue';
import FeatureLock from '../../components/merchant/FeatureLock.vue';
import { useUserStore } from '../../store/user';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import { Plus, Edit, Delete, Search, Picture, Refresh } from '@element-plus/icons-vue';

const userStore = useUserStore();
const allDishes = ref<any[]>([]);
const categoryList = ref<any[]>([]);
const currentCategoryId = ref<string | null>(null);
const loading = ref(false);
const dishSearch = ref('');
let socket: WebSocket | null = null;

const defaultDishImg = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png';
const showDishDialog = ref(false);
const showCategoryDialog = ref(false);

const initWebSocket = () => {
  const userId = userStore.userId;
  if (!userId) return;
  
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  socket = new WebSocket(`${protocol}//localhost:8088/ws/${userId}`);
  
  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      // 只要有新订单或订单取消，就刷新菜品列表（因为涉及库存变动）
      if (data.type === 'NEW_ORDER' || data.type === 'ORDER_CANCELLED') {
        fetchAllData();
        ElNotification({
          title: '库存变动提醒',
          message: data.type === 'NEW_ORDER' ? '有新订单，库存已更新' : '有订单取消，库存已恢复',
          type: 'info',
          position: 'bottom-right'
        });
      }
    } catch (e) {}
  };
};

const initialDishForm = {
  id: undefined,
  name: '',
  price: 0,
  categoryId: undefined,
  image: '',
  status: 1,
  stock: 99,
  description: ''
};
const dishForm = ref({ ...initialDishForm });

const initialCategoryForm = { id: undefined, name: '', sort: 0 };
const categoryForm = ref({ ...initialCategoryForm });

const displayDishes = computed(() => {
  let result = allDishes.value;
  if (currentCategoryId.value) {
    result = result.filter(dish => dish.categoryId.toString() === currentCategoryId.value);
  }
  if (dishSearch.value) {
    const q = dishSearch.value.toLowerCase();
    result = result.filter(dish => dish.name.toLowerCase().includes(q));
  }
  return result;
});

const currentCategoryName = computed(() => {
  if (!currentCategoryId.value) return '所有菜品';
  const category = categoryList.value.find(cat => cat.id.toString() === currentCategoryId.value);
  return category ? category.name : '未知分类';
});

const handleCategorySelect = (index: string) => {
  currentCategoryId.value = index;
};

const fetchAllData = async () => {
  if (!userStore.userId) return;
  loading.value = true;
  try {
    const [dishesRes, categoriesRes] = await Promise.all([
      request.get(`/dish/list/${userStore.userId}`),
      request.get('/category/list')
    ]);
    allDishes.value = dishesRes.data;
    categoryList.value = categoriesRes.data.sort((a: any, b: any) => a.sort - b.sort);
    if (!currentCategoryId.value && categoryList.value.length > 0) {
      currentCategoryId.value = categoryList.value[0].id.toString();
    }
  } catch (error) {
    console.error("加载数据失败", error);
  } finally {
    loading.value = false;
  }
};

const openCategoryDialog = (category: any = null) => {
  if (category) {
    categoryForm.value = { ...category };
  } else {
    categoryForm.value = { ...initialCategoryForm };
  }
  showCategoryDialog.value = true;
};

const saveCategory = async () => {
  if (!categoryForm.value.name) {
    ElMessage.warning('请输入分类名称');
    return;
  }
  try {
    if (categoryForm.value.id) {
      await request.put('/category', categoryForm.value);
    } else {
      await request.post('/category', categoryForm.value);
    }
    ElMessage.success('分类保存成功');
    showCategoryDialog.value = false;
    await fetchAllData();
  } catch (e) {
    ElMessage.error('操作失败');
  }
};

const deleteCategory = (id: number) => {
  ElMessageBox.confirm('删除分类将同时删除该分类下的所有菜品，此操作不可撤销，确定吗?', '严正警告', {
    type: 'error',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    roundButton: true
  })
    .then(async () => {
      try {
        await request.delete(`/category/${id}`);
        ElMessage.success('分类已删除');
        if (currentCategoryId.value === id.toString()) {
          currentCategoryId.value = null;
        }
        await fetchAllData();
      } catch (e) {
        ElMessage.error('删除失败');
      }
    })
    .catch(() => {});
};

const openAddDish = () => {
  dishForm.value = { 
    ...initialDishForm,
    categoryId: currentCategoryId.value ? parseInt(currentCategoryId.value) : undefined
  };
  showDishDialog.value = true;
};

const saveDish = async () => {
  if (!dishForm.value.name || !dishForm.value.categoryId) {
    ElMessage.warning('请填写菜品名称并选择分类');
    return;
  }
  try {
    if (dishForm.value.id) {
      await request.put('/dish', dishForm.value);
    } else {
      await request.post('/dish', dishForm.value);
    }
    ElMessage.success('菜品保存成功');
    showDishDialog.value = false;
    await fetchAllData();
  } catch (e) {
    ElMessage.error('操作失败');
  }
};

const editDish = async (row: any) => {
  try {
    const res: any = await request.get(`/dish/${row.id}`);
    dishForm.value = { ...res.data };
    showDishDialog.value = true;
  } catch (e) {
    ElMessage.error('获取菜品详情失败');
  }
};

const deleteDish = (id: number) => {
  ElMessageBox.confirm('确定要删除这个菜品吗?', '确认删除', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '返回',
    roundButton: true
  })
    .then(async () => {
      await request.delete(`/dish/${id}`);
      ElMessage.success('已成功删除');
      await fetchAllData();
    })
    .catch(() => {});
};

const getStockStatusText = (stock: number) => {
  if (stock === 0) return '已售罄';
  if (stock > 0 && stock <= 10) return '库存告急';
  return '库存充足';
};

const getStockStatusType = (stock: number) => {
  if (stock === 0) return 'danger';
  if (stock > 0 && stock <= 10) return 'warning';
  return 'success';
};

const handleStatusChange = async (id: number, status: number) => {
  try {
    await request.put(`/dish/status/${id}/${status}`);
    ElMessage.success(status === 1 ? '菜品已上架' : '菜品已下架');
    // fetchAllData(); // Switch is already updated via v-model
  } catch (e) {
    ElMessage.error('状态更新失败');
    // 失败时回滚状态可能需要更复杂的逻辑，这里简化处理
  }
};

onMounted(() => {
  fetchAllData();
  initWebSocket();
});

onUnmounted(() => {
  if (socket) socket.close();
});
</script>

<style scoped>
.menu-management {
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 60px);
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
}

.category-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-weight: 700;
  color: #1e293b;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: #475569;
}

.category-item:hover {
  background-color: #f1f5f9;
  color: #3b82f6;
}

.category-item.active {
  background-color: #eff6ff;
  color: #3b82f6;
  font-weight: 600;
}

.cat-actions {
  display: none;
  gap: 4px;
}

.category-item:hover .cat-actions {
  display: flex;
}

.dish-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.dish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
}

.current-cat-title {
  margin: 0;
  font-size: 18px;
  color: #1e293b;
  font-weight: 700;
  min-width: 100px;
}

.dish-search {
  width: 280px;
}

.dish-table-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.dish-img {
  width: 64px;
  height: 64px;
  border-radius: 8px;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f1f5f9;
  color: #94a3b8;
  font-size: 20px;
}

.dish-name-info .name {
  font-weight: 600;
  color: #1e293b;
  display: block;
  margin-bottom: 4px;
}

.dish-name-info .desc {
  font-size: 12px;
  color: #94a3b8;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price-text {
  font-weight: 700;
  color: #ef4444;
  font-size: 15px;
}

.action-btns {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.stock-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stock-label {
  font-size: 11px;
  color: #94a3b8;
}

.upload-tip, .input-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-table__row) {
  transition: background-color 0.2s;
}

:deep(.el-table__row:hover) {
  background-color: #f8fafc !important;
}
</style>
