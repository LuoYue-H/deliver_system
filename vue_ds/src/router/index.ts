import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/student',
      meta: { role: 'STUDENT' },
      component: () => import('../layout/StudentLayout.vue'),
      children: [
        { path: 'home', component: () => import('../views/student/StudentHome.vue') },
        { path: 'search', component: () => import('../views/student/StudentSearch.vue') },
        { path: 'order', component: () => import('../views/student/StudentOrder.vue') },
        { path: 'timeline', component: () => import('../views/student/StudentTimeline.vue') },
      { path: 'address', component: () => import('../views/student/StudentAddress.vue') },
      { path: 'profile', component: () => import('../views/Profile.vue') }
      ]
    },
    {
      path: '/merchant',
      meta: { role: 'MERCHANT' },
      component: () => import('../layout/MerchantLayout.vue'),
      children: [
        { path: 'dashboard', component: () => import('../views/merchant/MerchantDashboard.vue') },
        { path: 'menu', component: () => import('../views/merchant/MerchantMenu.vue') },
        { path: 'history', component: () => import('../views/merchant/MerchantHistory.vue') },
        { path: 'stats', component: () => import('../views/merchant/MerchantStats.vue') },
      { path: 'review-status', component: () => import('../views/merchant/ReviewStatus.vue') },
      { path: 'review-form', component: () => import('../views/merchant/ReviewForm.vue') },
      { path: 'profile', component: () => import('../views/Profile.vue') }
      ]
    },
    {
      path: '/rider',
      meta: { role: 'RIDER' },
      component: () => import('../layout/RiderLayout.vue'),
      children: [
        { path: 'hall', component: () => import('../views/rider/RiderHall.vue') },
        { path: 'delivery', component: () => import('../views/rider/RiderDelivery.vue') },
        { path: 'history', component: () => import('../views/rider/RiderHistory.vue') },
      { path: 'profile', component: () => import('../views/Profile.vue') },
        { path: 'map', component: () => import('../views/rider/RiderMap.vue') }
      ]
    },
    {
      path: '/admin',
      meta: { role: 'ADMIN' },
      component: () => import('../layout/AdminLayout.vue'),
      children: [
        { path: 'user', component: () => import('../views/admin/AdminUser.vue') },
        { path: 'audit', component: () => import('../views/admin/AdminAudit.vue') },
        { path: 'review', component: () => import('../views/admin/AdminReview.vue') },
        { path: 'rider', component: () => import('../views/admin/AdminRider.vue') }
      ]
    },
    {
      path: '/',
      redirect: '/login'
    }
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (['/login', '/register'].includes(to.path)) {
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  if (to.meta.role && to.meta.role !== role) {
    // 简单权限控制，角色不符跳转到对应角色的首页
    // 实际应更细致
    if (role === 'STUDENT') next('/student/home')
    else if (role === 'MERCHANT') next('/merchant/dashboard')
    else if (role === 'RIDER') next('/rider/hall')
    else if (role === 'ADMIN') next('/admin/user')
    else next('/login')
    return
  }

  next()
})

export default router
