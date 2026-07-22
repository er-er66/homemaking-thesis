import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../components/HelloWorld.vue'
import LoginPage from '../login/LoginPage.vue'
import RegisterPage from '../registrant/RegisterPage.vue'
import AdminDashboard from '../admin/AdminDashboard.vue'
import AdminUserDetail from '../admin/AdminUserDetail.vue'
import OrderPage from '../order/OrderPage.vue'
import ProfilePage from '../profile/ProfilePage.vue'
import MerchantPage from '../merchant/MerchantPage.vue'
import StaffHomePage from '../staff/StaffHomePage.vue'
import StaffChatPage from '../staff/StaffChatPage.vue'

const routes = [
  { path: '/', name: 'home', component: HomePage },
  { path: '/login', name: 'login', component: LoginPage },
  { path: '/register', name: 'register', component: RegisterPage },
  { path: '/order', name: 'order', component: OrderPage, meta: { requiresAuth: true } },
  { path: '/profile', name: 'profile', component: ProfilePage, meta: { requiresAuth: true } },
  { path: '/merchant', name: 'merchant', component: MerchantPage, meta: { requiresAuth: true } },
  { path: '/staff/home', name: 'staffHome', component: StaffHomePage, meta: { requiresAuth: true, requiresStaff: true } },
  { path: '/staff/chat', name: 'staffChat', component: StaffChatPage, meta: { requiresAuth: true, requiresStaff: true } },
  { 
    path: '/admin/dashboard', 
    name: 'adminDashboard', 
    component: AdminDashboard,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  { 
    path: '/admin/user/:id', 
    name: 'adminUserDetail', 
    component: AdminUserDetail,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  { 
    path: '/admin/emp/:id', 
    name: 'adminStaffDetail', 
    component: AdminUserDetail,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  { 
    path: '/admin/admin/:id', 
    name: 'adminAdminDetail', 
    component: AdminUserDetail,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/staff/order-chat',
    name: 'StaffOrderChat',
    component: () => import('../staff/StaffOrderChatPage.vue'),
    meta: { title: '订单聊天' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  let userInfo = {}
  try { userInfo = JSON.parse(userInfoStr) || {} } catch { /* ignore */ }

  if (to.meta.requiresAuth && !token) {
    return '/login'
  }

  if (to.meta.requiresAdmin && userInfo.role !== 'super_admin' && userInfo.role !== 'admin') {
    return '/'
  }

  if (to.meta.requiresStaff && userInfo.role !== 'staff' && userInfo.roleCode !== '002') {
    return '/'
  }

  return true
})

export default router