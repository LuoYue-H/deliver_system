<template>
  <div class="page-container">
    <!-- 骨架屏加载 -->
    <div v-if="loading" class="skeleton-container">
      <el-skeleton animated :rows="10" />
    </div>

    <el-empty v-else-if="!merchantInfo" description="商家信息加载失败或不存在" />

    <div v-else class="order-layout">
      <!-- 左侧分类导航 -->
      <aside class="side-nav">
        <el-affix :offset="100">
          <div class="category-list">
            <h3 class="nav-title">商品分类</h3>
            <a 
              v-for="cat in categoriesWithDishes" 
              :key="cat.id" 
              :href="`#category-${cat.id}`"
              class="nav-item"
            >
              <el-icon class="nav-icon"><CollectionTag /></el-icon>
              {{ cat.name }}
              <el-badge :value="getCategoryCount(cat)" :hidden="getCategoryCount(cat) === 0" class="nav-badge" />
            </a>
          </div>
        </el-affix>
      </aside>

      <!-- 中间菜品列表 -->
      <main class="menu-content">
        <el-card class="merchant-banner" :body-style="{ padding: '0px' }">
          <div class="banner-image" :style="{ backgroundImage: `url(${merchantInfo.avatar || defaultAvatar})` }"></div>
          <div class="banner-overlay">
            <el-avatar :size="90" :src="merchantInfo.avatar || defaultAvatar" class="merchant-logo" />
            <div class="merchant-info">
              <h1 class="merchant-name">{{ merchantInfo.username }}</h1>
              <div class="merchant-meta">
                <span class="meta-item"><el-icon><StarFilled /></el-icon> 4.8</span>
                <span class="meta-item">月售 1200+</span>
                <span class="meta-item">约 35 分钟</span>
              </div>
              <p class="merchant-notice">
                <el-icon><InfoFilled /></el-icon>
                公告：欢迎光临，本店坚持新鲜食材，极速配送。
              </p>
            </div>
          </div>
        </el-card>

        <div v-for="cat in categoriesWithDishes" :key="cat.id" :id="`category-${cat.id}`" class="menu-section">
          <h2 class="section-title">{{ cat.name }}</h2>
          <div class="dish-grid">
            <el-card v-for="dish in cat.dishes" :key="dish.id" class="dish-card" hoverable :body-style="{ padding: '0px' }">
              <div class="dish-body">
                <el-image :src="dish.image || defaultDishImg" class="dish-img" fit="cover" lazy />
                <div class="dish-info">
                  <h3 class="dish-name">{{ dish.name }}</h3>
                  <p class="dish-desc">{{ dish.description || '精选优质食材，纯手工制作，美味不可挡。' }}</p>
                  <div class="dish-footer">
                    <div class="price-wrap">
                      <span class="currency">¥</span>
                      <span class="amount">{{ dish.price }}</span>
                    </div>
                    <div v-if="dish.status === 1 && dish.stock > 0" class="action-wrap">
                      <transition name="el-fade-in">
                        <div v-if="getCartCount(dish) > 0" class="counter-wrap">
                          <el-button circle size="small" :icon="Minus" @click="removeFromCart(dish)" class="minus-btn" />
                          <span class="count-num">{{ getCartCount(dish) }}</span>
                        </div>
                      </transition>
                      <el-button circle size="small" :icon="Plus" type="primary" @click="addToCart(dish)" class="plus-btn" />
                    </div>
                    <el-tag v-else :type="dish.status === 0 ? 'info' : 'danger'" effect="dark" size="small">
                      {{ dish.status === 0 ? '已下架' : '已售罄' }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </main>

      <!-- 右侧购物车 -->
      <aside class="cart-sidebar">
        <el-affix :offset="100">
          <el-card class="cart-card" :body-style="{ padding: '0px' }">
            <div class="cart-header">
              <span class="title"><el-icon><ShoppingCart /></el-icon> 购物车</span>
              <el-button link type="primary" @click="clearCart" :disabled="cart.length === 0">
                <el-icon><Delete /></el-icon> 清空
              </el-button>
            </div>
            
            <div class="cart-body">
              <el-empty v-if="cart.length === 0" description="暂无选购商品" :image-size="60" />
              <div v-else class="cart-list">
                <div v-for="item in cart" :key="item.dishId" class="cart-item">
                  <div class="item-main">
                    <span class="item-name">{{ item.name }}</span>
                    <span class="item-price">¥{{ (item.price * item.number).toFixed(2) }}</span>
                  </div>
                  <div class="item-ctrl">
                    <el-button circle size="small" :icon="Minus" @click="removeFromCart(item)" />
                    <span class="num">{{ item.number }}</span>
                    <el-button circle size="small" :icon="Plus" @click="addToCart(item)" />
                  </div>
                </div>
              </div>
            </div>

            <div class="cart-footer">
              <div class="price-summary">
                <span class="label">合计金额</span>
                <span class="total">¥{{ totalPrice.toFixed(2) }}</span>
              </div>
              <el-button type="primary" size="large" round block class="checkout-btn" @click="showPayDialog = true" :disabled="cart.length === 0">
                去结算 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </el-card>
        </el-affix>
      </aside>
    </div>

    <!-- 结算对话框 -->
    <el-dialog v-model="showPayDialog" title="确认订单" width="460px" class="checkout-dialog" align-center @open="onOpenPayDialog">
      <div v-if="addressList.length === 0" class="empty-address">
        <el-result icon="warning" title="未设置收货地址" sub-title="请先添加收货地址以便配送">
          <template #extra>
            <el-button type="primary" @click="goToAddressPage">立即添加</el-button>
          </template>
        </el-result>
      </div>
      <el-form v-else label-position="top">
        <el-form-item label="收货信息">
          <el-select v-model="selectedAddressId" placeholder="请选择收货地址" class="full-width" @change="onAddressChange">
            <el-option
              v-for="item in addressList"
              :key="item.id"
              :label="`${item.address} (${item.contactName})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <div class="address-preview" v-if="orderForm.address">
          <p><strong>地址：</strong>{{ orderForm.address }}</p>
          <p><strong>联系人：</strong>{{ orderForm.contactName }} ({{ orderForm.contactPhone }})</p>
        </div>
        <el-divider />
        <div class="order-summary">
          <div class="summary-row">
            <span>商品总额</span>
            <span>¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <div class="summary-row">
            <span>配送费</span>
            <span>¥3.00</span>
          </div>
          <div class="summary-row final">
            <span>实付款</span>
            <span class="total-amount">¥{{ (totalPrice + 3).toFixed(2) }}</span>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-actions">
          <el-button @click="showPayDialog = false" round>再看看</el-button>
          <el-button type="primary" @click="submitOrder" round class="submit-order-btn">确认并支付</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '../../utils/request';
import { Plus, Minus, Delete, ShoppingCart, CollectionTag, StarFilled, InfoFilled, ArrowRight } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const merchantId = route.query.merchantId;
const merchantInfo = ref<any>(null);
const categories = ref<any[]>([]);
const dishList = ref<any[]>([]);
const loading = ref(true);

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const defaultDishImg = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png';

const categoriesWithDishes = computed(() => {
  return categories.value.map(cat => ({
    ...cat,
    dishes: dishList.value.filter(dish => dish.categoryId === cat.id)
  })).filter(cat => cat.dishes.length > 0);
});

const getCategoryCount = (cat: any) => {
  return cat.dishes.reduce((sum: number, dish: any) => sum + getCartCount(dish), 0);
};

const fetchAllData = async () => {
  if (!merchantId) return;
  loading.value = true;
  try {
    const [merchantRes, categoryRes, dishRes] = await Promise.all([
      request.get(`/user/merchant/${merchantId}`),
      request.get(`/category/list/${merchantId}`),
      request.get(`/dish/list/${merchantId}`)
    ]);
    merchantInfo.value = merchantRes.data;
    categories.value = categoryRes.data;
    dishList.value = dishRes.data;
  } catch (error) {
    console.error("加载页面数据失败", error);
    ElMessage.error('加载商家信息失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const cart = ref<any[]>([]);
const showPayDialog = ref(false);
const addressList = ref<any[]>([]);
const selectedAddressId = ref<number | null>(null);
const orderForm = ref({ address: '', contactName: '', contactPhone: '' });

const getCartCount = (dish: any) => {
  const item = cart.value.find(c => c.dishId === dish.id);
  return item ? item.number : 0;
};

const addToCart = (itemToAdd: any) => {
  const dishId = itemToAdd.id || itemToAdd.dishId;
  const existingItem = cart.value.find(c => c.dishId === dishId);
  if (existingItem) {
    existingItem.number++;
  } else {
    cart.value.push({
      dishId: itemToAdd.id || itemToAdd.dishId,
      name: itemToAdd.name,
      price: itemToAdd.price,
      number: 1
    });
  }
};

const removeFromCart = (itemToRemove: any) => {
  const dishId = itemToRemove.id || itemToRemove.dishId;
  const existingItem = cart.value.find(c => c.dishId === dishId);
  if (existingItem) {
    if (existingItem.number > 1) {
      existingItem.number--;
    } else {
      cart.value = cart.value.filter(c => c.dishId !== dishId);
    }
  }
};

const clearCart = () => {
  cart.value = [];
};

const totalPrice = computed(() => {
  return cart.value.reduce((total, item) => total + item.price * item.number, 0);
});

const onOpenPayDialog = async () => {
  try {
    const res: any = await request.get('/address/list');
    addressList.value = res.data;
    if (addressList.value.length > 0) {
      const defaultAddr = addressList.value.find(a => a.isDefault === 1) || addressList.value[0];
      selectedAddressId.value = defaultAddr.id;
      onAddressChange(defaultAddr.id);
    }
  } catch (e) {
    ElMessage.error('获取收货地址失败');
  }
};

const onAddressChange = (id: any) => {
  const addr = addressList.value.find(a => a.id === id);
  if (addr) {
    orderForm.value.address = addr.address;
    orderForm.value.contactName = addr.contactName;
    orderForm.value.contactPhone = addr.phone;
  }
};

const submitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址');
    return;
  }

  const orderData = {
    merchantId: Number(merchantId),
    address: orderForm.value.address,
    contactName: orderForm.value.contactName,
    contactPhone: orderForm.value.contactPhone,
    items: cart.value.map(item => ({
      dishId: item.dishId,
      name: item.name,
      price: item.price,
      number: item.number
    }))
  };

  try {
    await request.post('/order/submit', orderData);
    ElMessage.success('下单成功！');
    showPayDialog.value = false;
    clearCart();
    router.push('/student/timeline');
  } catch (e) {
    ElMessage.error('下单失败，请重试');
  }
};

const goToAddressPage = () => {
  showPayDialog.value = false;
  router.push('/student/address');
};

onMounted(fetchAllData);
</script>

<style scoped>
.page-container {
  min-height: calc(100vh - 70px);
  padding: 24px;
}

.order-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  max-width: 1400px;
  margin: 0 auto;
}

/* 左侧导航 */
.side-nav {
  width: 200px;
  flex-shrink: 0;
}

.category-list {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.nav-title {
  font-size: 14px;
  color: #94a3b8;
  margin: 0 0 16px 0;
  padding-left: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  color: #475569;
  text-decoration: none;
  font-size: 15px;
  border-radius: 8px;
  transition: all 0.2s;
  position: relative;
}

.nav-item:hover {
  background-color: #f1f5f9;
  color: #409eff;
}

.nav-icon {
  font-size: 18px;
}

.nav-badge {
  position: absolute;
  right: 12px;
}

/* 中间内容 */
.menu-content {
  flex-grow: 1;
  min-width: 0;
}

.merchant-banner {
  border-radius: 20px;
  border: none;
  overflow: hidden;
  margin-bottom: 30px;
  position: relative;
}

.banner-image {
  height: 160px;
  background-size: cover;
  background-position: center;
  filter: blur(20px) brightness(0.8);
  transform: scale(1.2);
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0,0,0,0.2), rgba(0,0,0,0.7));
  display: flex;
  align-items: center;
  padding: 0 40px;
  gap: 24px;
}

.merchant-logo {
  border: 4px solid rgba(255,255,255,0.3);
  box-shadow: 0 8px 16px rgba(0,0,0,0.2);
}

.merchant-name {
  color: #fff;
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
}

.merchant-meta {
  display: flex;
  gap: 16px;
  color: rgba(255,255,255,0.9);
  font-size: 14px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.merchant-notice {
  color: rgba(255,255,255,0.7);
  font-size: 13px;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 40px 0 20px 0;
  padding-left: 12px;
  border-left: 4px solid #409eff;
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.dish-card {
  border-radius: 16px;
  border: none;
  transition: all 0.3s;
}

.dish-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.08);
}

.dish-body {
  display: flex;
  padding: 12px;
  gap: 16px;
}

.dish-img {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  flex-shrink: 0;
}

.dish-info {
  flex-grow: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.dish-name {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px 0;
}

.dish-desc {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dish-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-wrap {
  color: #ef4444;
  font-weight: 700;
}

.currency { font-size: 14px; }
.amount { font-size: 20px; }

.action-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.counter-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.count-num {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  min-width: 20px;
  text-align: center;
}

/* 右侧购物车 */
.cart-sidebar {
  width: 320px;
  flex-shrink: 0;
}

.cart-card {
  border-radius: 20px;
  border: none;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  overflow: hidden;
}

.cart-header {
  padding: 20px;
  background-color: #f8fafc;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f1f5f9;
}

.cart-header .title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
}

.cart-body {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px 20px;
}

.cart-item {
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
}

.cart-item:last-child { border-bottom: none; }

.item-main {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.item-name {
  font-size: 15px;
  font-weight: 500;
  color: #1e293b;
}

.item-price {
  font-weight: 700;
  color: #475569;
}

.item-ctrl {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.cart-footer {
  padding: 20px;
  background-color: #fff;
  border-top: 1px dashed #e2e8f0;
}

.price-summary {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
}

.price-summary .label {
  color: #64748b;
  font-size: 14px;
}

.price-summary .total {
  font-size: 26px;
  font-weight: 800;
  color: #ef4444;
}

.checkout-btn {
  width: 100%;
  height: 50px;
  font-size: 17px;
  font-weight: 700;
}

/* 结算弹窗样式 */
.address-preview {
  background-color: #f8fafc;
  padding: 16px;
  border-radius: 12px;
  margin-top: 12px;
  font-size: 14px;
}

.address-preview p { margin: 4px 0; }

.order-summary {
  margin-top: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #64748b;
}

.summary-row.final {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
  color: #1e293b;
  font-weight: 700;
}

.total-amount {
  color: #ef4444;
  font-size: 22px;
}

.dialog-actions {
  display: flex;
  gap: 12px;
}

.submit-order-btn {
  flex-grow: 1;
  height: 44px;
  font-size: 16px;
}

.full-width { width: 100%; }

@media (max-width: 1024px) {
  .side-nav { display: none; }
  .cart-sidebar { display: none; }
}
</style>
