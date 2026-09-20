<template>
  <div class="staff-home">
    <header class="staff-header">
      <div class="header-left">
        <img src="/src/assets/logo.svg" alt="家政服务" class="logo" />
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
                <span class="user-name">
                  {{ user.name }}
                  <el-tag v-if="user.type === 'cs'" type="danger" size="small" style="margin-left: 4px">客服</el-tag>
                </span>
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
          <el-radio-group v-model="orderTab" @change="changeOrderTab" class="order-tab-group">
            <el-radio-button value="pending">待接订单</el-radio-button>
            <el-radio-button value="accepted">已接订单</el-radio-button>
          </el-radio-group>
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
                <el-tag v-if="orderTab === 'pending'" type="warning" size="small">
                  待接单
                </el-tag>
                <el-tag v-else type="success" size="small">
                  已接单
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
              <div class="order-actions" v-if="orderTab === 'accepted'" @click.stop>
                <el-button type="danger" size="small" @click="handleCancelOrder(order)">取消订单</el-button>
                <el-button type="success" size="small" @click="showCompleteDialog(order)">完成订单</el-button>
              </div>
            </div>
          </div>
          <el-empty v-if="!orderLoading && orders.length === 0" :description="orderTab === 'pending' ? '暂无待接订单' : '暂无已接订单'" />
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

    <!-- 完成订单弹窗 -->
    <el-dialog v-model="completeDialogVisible" title="完成订单" width="700px" destroy-on-close>
      <div class="complete-order-content">
        <el-descriptions :column="1" border size="small" class="order-info">
          <el-descriptions-item label="订单编号">{{ completeForm.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="服务项目">{{ completeForm.serviceItem }}</el-descriptions-item>
          <el-descriptions-item label="服务地址">{{ completeForm.serviceAddress }}</el-descriptions-item>
          <el-descriptions-item label="用户账号">{{ completeForm.userAccount }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">上传服务完成图片</el-divider>

        <el-form :model="completeForm" label-width="100px">
          <el-form-item label="打扫前图片">
            <el-upload
              v-model:file-list="beforeCleanFiles"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :limit="5"
              accept="image/*"
              :on-change="(file) => handleImageChange(file, 'before')"
              :on-remove="(file) => handleImageRemove(file, 'before')"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">最多上传5张图片，支持jpg、png格式</div>
          </el-form-item>

          <el-form-item label="打扫后图片">
            <el-upload
              v-model:file-list="afterCleanFiles"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :limit="5"
              accept="image/*"
              :on-change="(file) => handleImageChange(file, 'after')"
              :on-remove="(file) => handleImageRemove(file, 'after')"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">最多上传5张图片，支持jpg、png格式</div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="completeLoading" @click="handleCompleteOrder">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Location, Clock, User, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderListApi, getUserNameApi, rejectOrderApi, getChatRoomListApi, completeOrderApi, cancelOrderApi, uploadOrderImgApi } from '../api/admin'
import { messagePreview, formatListTime } from '../utils/chatMessage'
const router = useRouter()

const userName = ref('')
const userInitial = computed(() => userName.value ? userName.value.charAt(0).toUpperCase() : 'S')
const avatar = ref('')
const staffAccount = ref('')

const chatUsers = ref([])
const orders = ref([])
const orderLoading = ref(false)
const userLoading = ref(false)
const activeUser = ref('')
const activeUserName = ref('')
const dispatchSectionVisible = ref(true)
const orderTab = ref('pending')

const pendingOrders = computed(() => {
  return allOrders.value.filter(o => o.orderStatus === 0)
})

const acceptedOrders = computed(() => {
  return allOrders.value.filter(o => o.orderStatus === 1 && o.staffAccount === staffAccount.value)
})

const dispatchedOrders = computed(() => {
  return allOrders.value.filter(o => 
    o.dispatchStatus === 1 && 
    o.dispatchAdminAccount && 
    o.staffAccount === staffAccount.value
  )
})

const allOrders = ref([])

const completeDialogVisible = ref(false)
const completeLoading = ref(false)
const completeForm = ref({
  orderId: '',
  orderNo: '',
  serviceItem: '',
  serviceAddress: '',
  userAccount: ''
})
const beforeCleanFiles = ref([])
const afterCleanFiles = ref([])
const beforeCleanImages = ref([])
const afterCleanImages = ref([])

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
  userLoading.value = true
  try {
    const stored = localStorage.getItem('staff_chat_users')
    let localUsers = []
    if (stored) {
      try {
        localUsers = JSON.parse(stored)
      } catch { /* ignore */ }
    }
    
    // 从后端获取会话列表（包含与管理员的聊天）
    if (staffAccount.value) {
      try {
        const res = await getChatRoomListApi(staffAccount.value)
        if (res && res.data && Array.isArray(res.data)) {
          const backendRooms = res.data
          
          // 合并后端数据和本地数据
          const mergedUsers = []
          const processedKeys = new Set()
          
          // 生成唯一标识：优先用 account，其次用 id 或 roomId，最后用 name
          const getUniqueKey = (room) => {
            return room.account ||
                   room.id ||
                   room.roomId ||
                   room.room_id ||
                   room.name ||
                   ''
          }
          
          // 先处理后端数据
          for (const room of backendRooms) {
            const key = getUniqueKey(room)
            if (!key) continue
            
            // 标准化 key：去除下划线分隔符的顺序差异（如 "a_b" 和 "b_a" 视为相同）
            const normalizedKey = key.includes('_') 
              ? key.split('_').sort().join('_') 
              : key
              
            if (processedKeys.has(normalizedKey)) {
              console.log('[StaffHomePage] 跳过重复用户:', { key, normalizedKey, room })
              continue
            }
            processedKeys.add(normalizedKey)
            
            // 确定真实的 account（用于后续 API 调用）
            const account = room.account || (key.includes('_') ? key.split('_').find(p => p !== staffAccount.value) : key) || key
            
            const user = {
              account: account,
              name: room.name || account,
              avatar: room.avatar || '',
              lastMsg: room.lastMsg || '',
              lastTime: room.lastTime || '',
              unread: room.unread || 0,
              lastTimestamp: room.lastTime ? new Date(room.lastTime).getTime() : 0,
              type: room.type || 'user',
              roomId: room.roomId || room.room_id || room.id || ''
            }
            mergedUsers.push(user)
            
            // 同时用原始 account 也标记为已处理（防止同一用户用不同字段出现两次）
            if (account && account !== normalizedKey) {
              processedKeys.add(account)
            }
          }
          
          console.log('[StaffHomePage] 后端返回', backendRooms.length, '条记录，去重后', mergedUsers.length, '个用户')
          
          // 再添加本地数据中后端没有的
          for (const localUser of localUsers) {
            if (!processedKeys.has(localUser.account)) {
              mergedUsers.push(localUser)
              processedKeys.add(localUser.account)
            }
          }
          
          // 为每个用户获取真实姓名
          for (const user of mergedUsers) {
            if (user.account && user.name === user.account) {
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
          
          // 订单详情消息的 lastMsg 是 "Xiangqing{...}" 原文，列表里要显示成「订单详情」
          mergedUsers.forEach(u => {
            u.lastMsg = messagePreview(u.lastMsg)
            u.lastTime = formatListTime(u.lastTime)
          })

          chatUsers.value = mergedUsers
          chatUsers.value.sort((a, b) => new Date(b.lastTimestamp || 0) - new Date(a.lastTimestamp || 0))
          // 更新 localStorage
          localStorage.setItem('staff_chat_users', JSON.stringify(mergedUsers))
          return
        }
      } catch (e) {
        console.warn('获取会话列表失败，使用本地数据:', e)
      }
    }
    
    // 如果后端获取失败，使用本地数据
    if (localUsers.length > 0) {
      for (const user of localUsers) {
        if (user.account && user.name === user.account) {
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
      localUsers.forEach(u => {
        u.lastMsg = messagePreview(u.lastMsg)
        u.lastTime = formatListTime(u.lastTime)
      })
      chatUsers.value = localUsers
      chatUsers.value.sort((a, b) => new Date(b.lastTimestamp || 0) - new Date(a.lastTimestamp || 0))
      localStorage.setItem('staff_chat_users', JSON.stringify(localUsers))
    }
  } finally {
    userLoading.value = false
  }
}

const selectUser = (user) => {
  // 如果是客服/管理员，跳转到商家聊天页面（管理员聊天）
  if (user.type === 'cs') {
    router.push({
      path: '/merchant',
      query: {
        merchantId: user.account
      }
    })
  } else {
    // 普通用户，跳转到家政人员聊天页面
    router.push({
      path: '/staff/chat',
      query: {
        userAccount: user.account,
        userAvatar: user.avatar || ''
      }
    })
  }
}

const fetchOrders = async () => {
  orderLoading.value = true
  try {
    const res = await getOrderListApi()
    allOrders.value = Array.isArray(res.data) ? res.data : []
    if (orderTab.value === 'pending') {
      orders.value = pendingOrders.value
    } else {
      orders.value = acceptedOrders.value
    }
  } catch {
    allOrders.value = []
    orders.value = []
  } finally {
    orderLoading.value = false
  }
}

const changeOrderTab = (tab) => {
  orderTab.value = tab
  if (tab === 'pending') {
    orders.value = pendingOrders.value
  } else {
    orders.value = acceptedOrders.value
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

const showCompleteDialog = (order) => {
  completeForm.value = {
    orderId: order.id,
    orderNo: order.orderNo,
    serviceItem: order.serviceItem,
    serviceAddress: order.serviceAddress,
    userAccount: order.userAccount
  }
  beforeCleanFiles.value = []
  afterCleanFiles.value = []
  beforeCleanImages.value = []
  afterCleanImages.value = []
  completeDialogVisible.value = true
}

const handleImageChange = (file, type) => {
  if (type === 'before') {
    beforeCleanImages.value.push(file.raw)
  } else {
    afterCleanImages.value.push(file.raw)
  }
}

const handleImageRemove = (file, type) => {
  if (type === 'before') {
    const index = beforeCleanImages.value.findIndex(img => img.name === file.name)
    if (index > -1) {
      beforeCleanImages.value.splice(index, 1)
    }
  } else {
    const index = afterCleanImages.value.findIndex(img => img.name === file.name)
    if (index > -1) {
      afterCleanImages.value.splice(index, 1)
    }
  }
}

const uploadImageToOss = async (file) => {
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await uploadOrderImgApi(formData)
    if (res.code === 200 && res.data) {
      return res.data
    } else {
      throw new Error(res.message || '上传失败')
    }
  } catch (error) {
    throw error
  }
}

const handleCompleteOrder = async () => {
  if (beforeCleanImages.value.length === 0 && afterCleanImages.value.length === 0) {
    ElMessage.warning('请至少上传一张服务图片（打扫前或打扫后）')
    return
  }

  try {
    completeLoading.value = true

    const beforeImgUrls = []
    const afterImgUrls = []

    for (const file of beforeCleanImages.value) {
      const url = await uploadImageToOss(file)
      beforeImgUrls.push(url)
    }

    for (const file of afterCleanImages.value) {
      const url = await uploadImageToOss(file)
      afterImgUrls.push(url)
    }

    const res = await completeOrderApi({
      orderId: completeForm.value.orderId,
      orderNo: completeForm.value.orderNo,
      staffAccount: staffAccount.value,
      userAccount: completeForm.value.userAccount,
      beforeCleanImgs: beforeImgUrls,
      afterCleanImgs: afterImgUrls
    })

    // 后端 Result.success 固定返回 code=200 / message='操作成功'，
    // CompletedOrder 业务结果放在 data 里（"订单已完成成功" / "订单已完成失败"）
    if (res.code === 200 && (!res.data || res.data === '订单已完成成功')) {
      ElMessage.success('订单已完成')
      completeDialogVisible.value = false
      fetchOrders()
    } else {
      ElMessage.error(res.message || res.data || '订单完成失败')
    }
  } catch (e) {
    console.error('完成订单失败:', e)
    ElMessage.error('订单完成失败：' + (e.message || '未知错误'))
  } finally {
    completeLoading.value = false
  }
}



const handleCancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      '确认要取消该订单吗？取消后订单将无法恢复。',
      '取消订单确认',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        type: 'warning'
      }
    )

    const res = await cancelOrderApi({
      id: order.id,
      staffAccount: staffAccount.value
    })

    if (res.code === 200 || res.message === '订单取消成功') {
      ElMessage.success('订单已取消')
      orders.value = orders.value.filter(o => o.id !== order.id)
      fetchOrders()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('取消订单失败，请重试')
    }
  }
}

const goChat = (order) => {
  router.push({
    path: '/staff/order-chat',
    query: {
      userAccount: order.userAccount,
      userName: order.userAccount,
      userAvatar: '',
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
  background: var(--surface-page);
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
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  width: 34px;
  height: 34px;
  /* 圆角已画在 SVG 里，这里不要再加 border-radius */
  flex-shrink: 0;
  filter: drop-shadow(0 2px 6px rgba(var(--brand-rgb), .22));
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
  color: var(--text-primary);
}

.user-name {
  font-size: 14px;
}

.arrow {
  font-size: 12px;
  color: var(--text-secondary);
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
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
}

.panel-title {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-light);
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
  background: var(--surface-page);
}

.user-item.active {
  background: var(--brand-50);
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
  color: var(--text-primary);
}

.user-time {
  font-size: 11px;
  color: var(--text-disabled);
}

.user-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-last {
  font-size: 12px;
  color: var(--text-secondary);
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
  background: var(--surface-page);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.right-panel .panel-title {
  background: #fff;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-tab-group {
  margin-right: auto;
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
  border: 2px solid var(--border-light);
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
  color: var(--text-primary);
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
  color: var(--text-regular);
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--border-light);
}

.order-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent);
}

.order-user {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 管理员派单区域 */
.admin-dispatch-section {
  background: #fff;
  border-top: 2px solid var(--border-light);
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
  background: var(--surface-page);
}

.dispatch-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.toggle-icon {
  font-size: 16px;
  color: var(--text-secondary);
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
  border: 2px solid var(--color-danger);
  transition: all 0.2s;
}

.dispatch-card:hover {
  border-color: var(--el-color-danger-light-3);
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
  color: var(--text-primary);
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
  color: var(--text-regular);
}

.dispatch-admin {
  color: var(--color-danger) !important;
  font-weight: 500;
}

.dispatch-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--border-light);
}

.dispatch-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent);
}

.dispatch-user {
  font-size: 12px;
  color: var(--text-secondary);
}

.dispatch-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  margin-top: 10px;
  border-top: 1px solid var(--border-light);
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  margin-top: 10px;
  border-top: 1px solid var(--border-light);
}

.complete-order-content {
  padding: 10px 0;
}

.complete-order-content .order-info {
  margin-bottom: 20px;
}

.upload-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 5px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>