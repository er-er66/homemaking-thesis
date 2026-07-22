<template>
  <div class="staff-home">
    <header class="staff-header">
      <div class="header-left">
        <img src="/src/assets/jiazen1.png" alt="logo" class="logo" />
        <span class="logo-text">家政服务</span>
      </div>
      <div class="header-right">
        <el-dropdown trigger="click">
          <span class="user-info">
            <el-avatar :size="32" :src="avatar">{{ userInitial }}</el-avatar>
            <span class="user-name">{{ userName }}</span>
            <el-icon class="arrow"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="staff-body">
      <div class="left-panel">
        <div class="panel-title">消息列表</div>
        <div class="user-list" v-loading="userLoading">
          <div
            v-for="user in chatUsers"
            :key="user.account"
            class="user-item"
            :class="{ active: activeUser === user.account }"
            @click="selectUser(user)"
          >
            <el-avatar :size="42" :src="user.avatar" shape="square">{{ user.name.charAt(0) }}</el-avatar>
            <div class="user-info">
              <div class="user-top">
                <span class="user-name">{{ user.name }}</span>
                <span class="user-time">{{ user.lastTime }}</span>
              </div>
              <div class="user-bottom">
                <span class="user-last">{{ user.lastMsg || '暂无消息' }}</span>
                <el-badge v-if="user.unread" :value="user.unread" class="unread-dot" />
              </div>
            </div>
          </div>
          <el-empty v-if="!userLoading && chatUsers.length === 0" description="暂无聊天用户" />
        </div>
      </div>

      <div class="right-panel">
        <div class="panel-title">
          <span>用户发布的订单</span>
          <el-tag v-if="orders.length" type="info" size="small">共 {{ orders.length }} 单</el-tag>
        </div>
        <div class="order-list" v-loading="orderLoading">
          <div v-if="orders.length > 0" class="order-cards">
            <div
              v-for="order in orders"
              :key="order.id"
              class="order-card"
              @click="goChat(order)"
            >
              <div class="order-top">
                <span class="order-service">{{ order.serviceItem }}</span>
                <el-tag v-if="order.dispatchStatus === 1 && order.dispatchAdminAccount && order.staffAccount === staffAccount" type="danger" size="small">
                  派送订单
                </el-tag>
                <el-tag :type="order.orderStatus === 0 ? 'warning' : 'success'" size="small">
                  {{ order.orderStatus === 0 ? '待接单' : '已接单' }}
                </el-tag>
              </div>
              <div class="order-body">
                <p class="order-addr">
                  <el-icon size="14"><Location /></el-icon>
                  {{ order.serviceAddress }}
                </p>
                <p class="order-time">
                  <el-icon size="14"><Clock /></el-icon>
                  {{ order.serviceTime }}
                </p>
              </div>
              <div class="order-footer">
                <span class="order-amount">¥{{ order.orderAmount }}</span>
                <span class="order-user">{{ order.userAccount }}</span>
              </div>
            </div>
          </div>
          <el-empty v-if="!orderLoading && orders.length === 0" description="暂无用户发布的订单" />
        </div>

        <!-- 管理员派单区域 -->
        <div class="admin-dispatch-section" v-if="dispatchedOrders.length > 0">
          <div class="dispatch-header" @click="dispatchSectionVisible = !dispatchSectionVisible">
            <span class="dispatch-title">管理员派单</span>
            <el-icon class="toggle-icon" :class="{ rotated: dispatchSectionVisible }">
              <ArrowDown />
            </el-icon>
          </div>
          <div class="dispatch-content" v-show="dispatchSectionVisible">
            <div class="dispatch-cards">
              <div
                v-for="order in dispatchedOrders"
                :key="order.id"
                class="dispatch-card"
              >
                <div class="dispatch-top">
                  <span class="dispatch-service">{{ order.serviceItem }}</span>
                  <el-tag type="danger" size="small">派送订单</el-tag>
                </div>
                <div class="dispatch-body">
                  <p class="dispatch-addr">
                    <el-icon size="14"><Location /></el-icon>
                    {{ order.serviceAddress }}
                  </p>
                  <p class="dispatch-time">
                    <el-icon size="14"><Clock /></el-icon>
                    {{ order.serviceTime }}
                  </p>
                  <p class="dispatch-admin">
                    <el-icon size="14"><User /></el-icon>
                    派单管理员：{{ order.dispatchAdminAccount }}
                  </p>
                </div>
                <div class="dispatch-footer">
                  <span class="dispatch-amount">¥{{ order.orderAmount }}</span>
                  <span class="dispatch-user">{{ order.userAccount }}</span>
                </div>
                <div class="dispatch-actions">
                  <el-button type="primary" size="small" @click="goChat(order)">接单</el-button>
                  <el-button type="danger" size="small" @click="handleRejectOrder(order)">拒单</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Location, Clock, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderListApi, getUserNameApi, rejectOrderApi } from '../api/admin'

const router = useRouter()

const userName = ref('')
const userInitial = computed(() => userName.value ? userName.value.charAt(0).toUpperCase() : 'S')
const avatar = ref('')
const staffAccount = ref('')

const chatUsers = ref([])
const userLoading = ref(false)
const orders = ref([])
const orderLoading = ref(false)
const activeUser = ref('')
const activeUserName = ref('')
const dispatchSectionVisible = ref(true)

const dispatchedOrders = computed(() => {
  return orders.value.filter(o => 
    o.dispatchStatus === 1 && 
    o.dispatchAdminAccount && 
    o.staffAccount === staffAccount.value
  )
})

onMounted(() => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      userName.value = info.username || info.account || '家政人员'
      avatar.value = info.avatar || ''
      staffAccount.value = info.account || ''
    } catch { /* ignore */ }
  }
  fetchChatUsers()
  fetchOrders()
})

const fetchChatUsers = async () => {
  const stored = localStorage.getItem('staff_chat_users')
  if (stored) {
    try {
      const users = JSON.parse(stored)
      // 为每个用户获取真实姓名
      for (const user of users) {
        if (user.account) {
          // 如果 name 等于 account（说明之前存的是账号），则重新获取真实姓名
          if (user.name === user.account) {
            try {
              const res = await getUserNameApi(user.account)
              if (res.code === 200 && res.data) {
                user.name = res.data.username || res.data.name || user.account
                user.avatar = res.data.avatar || user.avatar || ''
              }
            } catch (e) {
              console.error(`获取用户 ${user.account} 信息失败:`, e)
            }
          }
        }
      }
      chatUsers.value = users
      chatUsers.value.sort((a, b) => new Date(b.lastTimestamp || 0) - new Date(a.lastTimestamp || 0))
      // 更新 localStorage
      localStorage.setItem('staff_chat_users', JSON.stringify(users))
    } catch { /* ignore */ }
  }
}

const selectUser = (user) => {
  router.push({
    path: '/staff/chat',
    query: {
      userAccount: user.account,
      userAvatar: user.avatar || ''
    }
  })
}

const fetchOrders = async () => {
  orderLoading.value = true
  try {
    const res = await getOrderListApi()
    const allOrders = Array.isArray(res.data) ? res.data : []
    orders.value = allOrders.filter(o => o.orderStatus === 0)
  } catch {
    orders.value = []
  } finally {
    orderLoading.value = false
  }
}

const handleRejectOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确认拒单吗？如有问题可联系管理员 ${order.dispatchAdminAccount}`,
      '拒单确认',
      {
        confirmButtonText: '确认拒单',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await rejectOrderApi({
      id: order.id,
      staffAccount: staffAccount.value
    })
    if (res.code === 200 || res.message === '订单拒单成功') {
      ElMessage.success('已拒单')
      orders.value = orders.value.filter(o => o.id !== order.id)
      fetchOrders()
    } else {
      ElMessage.error(res.message || '拒单失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('拒单失败，请重试')
    }
  }
}

const goChat = async (order) => {
  let userName = order.userAccount
  let userAvatar = ''
  
  // 获取用户真实姓名
  try {
    const res = await getUserNameApi(order.userAccount)
    if (res.code === 200 && res.data) {
      userName = res.data.username || res.data.name || order.userAccount
      userAvatar = res.data.avatar || ''
    }
  } catch (e) {
    console.error('获取用户信息失败:', e)
  }
  
  router.push({
    path: '/staff/order-chat',
    query: {
      userAccount: order.userAccount,
      userName: userName,
      userAvatar: userAvatar,
      orderId: order.id,
      orderNo: order.orderNo
    }
  })
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.staff-home {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.staff-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  width: 32px;
  height: 32px;
  border-radius: 6px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: var(--accent);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #303133;
}

.user-name {
  font-size: 14px;
}

.arrow {
  font-size: 12px;
  color: #909399;
}

.staff-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.left-panel {
  width: 320px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}

.panel-title {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  margin: 4px 0;
}

.user-item:hover {
  background: #f5f7fa;
}

.user-item.active {
  background: #fff0f0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.user-top .user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.user-time {
  font-size: 11px;
  color: #c0c4cc;
}

.user-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-last {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
}

.unread-dot {
  flex-shrink: 0;
}

.right-panel {
  flex: 1;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.right-panel .panel-title {
  background: #fff;
  padding: 16px 24px;
}

.order-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

.order-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 16px;
}

.order-card {
  background: #fff;
  border-radius: 10px;
  padding: 18px;
  cursor: pointer;
  border: 2px solid #ebeef5;
  transition: all 0.2s;
}

.order-card:hover {
  border-color: var(--accent);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.1);
  transform: translateY(-1px);
}

.order-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-service {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.order-body {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.order-body p {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.order-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent);
}

.order-user {
  font-size: 12px;
  color: #909399;
}

/* 管理员派单区域 */
.admin-dispatch-section {
  background: #fff;
  border-top: 2px solid #f0f0f0;
}

.dispatch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  cursor: pointer;
  transition: background 0.15s;
}

.dispatch-header:hover {
  background: #f5f7fa;
}

.dispatch-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.toggle-icon {
  font-size: 16px;
  color: #909399;
  transition: transform 0.3s;
}

.toggle-icon.rotated {
  transform: rotate(180deg);
}

.dispatch-content {
  padding: 0 24px 20px;
  max-height: 400px;
  overflow-y: auto;
}

.dispatch-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 16px;
}

.dispatch-card {
  background: #fff;
  border-radius: 10px;
  padding: 18px;
  cursor: pointer;
  border: 2px solid #ff4d4f;
  transition: all 0.2s;
}

.dispatch-card:hover {
  border-color: #ff7875;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.15);
  transform: translateY(-1px);
}

.dispatch-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.dispatch-service {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.dispatch-body {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.dispatch-body p {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.dispatch-admin {
  color: #ff4d4f !important;
  font-weight: 500;
}

.dispatch-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.dispatch-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent);
}

.dispatch-user {
  font-size: 12px;
  color: #909399;
}

.dispatch-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  margin-top: 10px;
  border-top: 1px solid #f0f0f0;
}
</style>