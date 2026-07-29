<template>
  <div class="merchant-page">
    <div class="merchant-header">
      <div class="header-left">
        <el-link type="info" :underline="false" @click="router.push('/')" class="back-link">
          ← 返回
        </el-link>
        <h2>{{ isStaff ? '用户消息' : '商家消息' }}</h2>
      </div>
    </div>

    <div :class="['merchant-body', { 'has-chat': currentMerchant.id }]" v-loading="loading">
      <div class="merchant-sidebar">
        <div class="sidebar-title">{{ isStaff ? '用户列表' : '商家列表' }}</div>
        <div class="merchant-list">
          <template v-if="merchantList.length > 0">
            <div
              v-for="item in merchantList"
              :key="item.id"
              :class="['merchant-item', { active: currentMerchant.id === item.id }]"
              @click="openChat(item)"
            >
              <el-avatar :size="44" :src="item.avatar" shape="square">
                {{ item.name.charAt(0) }}
              </el-avatar>
              <div class="merchant-info">
                <div class="merchant-top">
                  <span class="merchant-name">{{ item.name }}<span v-if="item.type === 'cs' && !item.name.includes('客服')" class="cs-tag">（客服）</span></span>
                  <span class="merchant-time">{{ item.time }}</span>
                </div>
                <div class="merchant-bottom">
                  <span class="merchant-last">{{ item.lastMsg }}</span>
                  <el-badge v-if="item.unread" :value="item.unread" />
                </div>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无商家" :image-size="80" />
        </div>
      </div>

      <div class="merchant-chat" v-if="currentMerchant.id">
        <div class="chat-header">
          <span class="chat-title">{{ currentMerchant.name }}<span v-if="currentMerchant.type === 'cs' && !currentMerchant.name.includes('客服')" class="cs-tag">（客服）</span></span>
        </div>
        <div class="chat-body">
          <div class="chat-messages" ref="msgBox">
            <template v-for="(msg, idx) in chatMessages" :key="idx">
              <div v-if="msg.type === 'order'" class="chat-msg msg-left">
                <div class="order-card" @click="showOrderDetail(msg.orderData)">
                  <div class="order-card-header">
                    <el-icon size="18"><Document /></el-icon>
                    <span class="order-card-title">订单详情</span>
                    <el-tag size="small" :type="msg.orderData.orderStatus === 0 ? 'warning' : 'success'">
                      {{ msg.orderData.orderStatus === 0 ? '待接单' : '已接单' }}
                    </el-tag>
                  </div>
                  <div class="order-card-body">
                    <div class="order-card-row">
                      <span class="order-label">服务项目</span>
                      <span class="order-value">{{ msg.orderData.serviceItem }}</span>
                    </div>
                    <div class="order-card-row">
                      <span class="order-label">服务地址</span>
                      <span class="order-value">{{ msg.orderData.serviceAddress }}</span>
                    </div>
                    <div class="order-card-row">
                      <span class="order-label">预约时间</span>
                      <span class="order-value">{{ msg.orderData.serviceTime }}</span>
                    </div>
                    <div class="order-card-row">
                      <span class="order-label">订单金额</span>
                      <span class="order-amount">¥{{ msg.orderData.orderAmount }}</span>
                    </div>
                  </div>
                  <div class="order-card-footer">
                    <span class="order-click-hint">点击查看详情 →</span>
                  </div>
                </div>
                <div class="msg-time">{{ msg.time }}</div>
              </div>
              <div
                v-else
                :class="['chat-msg', msg.from === 'me' ? 'msg-right' : 'msg-left']"
              >
                <div class="msg-bubble">{{ msg.text }}</div>
                <div class="msg-status-row">
                  <span class="msg-time">{{ msg.time }}</span>
                  <span v-if="msg.from === 'merchant'" class="msg-read-status">
                    {{ msg.isRead ? '已读' : '未读' }}
                  </span>
                </div>
              </div>
            </template>
          </div>
        </div>
        <div class="chat-input-row">
          <el-input
            v-model="newMsg"
            placeholder="输入消息..."
            @keyup.enter="sendMsg"
          />
          <el-button type="primary" @click="sendMsg">发送</el-button>
        </div>
      </div>

      <div class="merchant-chat-empty" v-else>
        <el-empty description="请选择左侧商家开始聊天" :image-size="120" />
      </div>
    </div>

    <el-dialog
      v-model="showOrderDialog"
      title="订单详情"
      width="480px"
      :close-on-click-modal="false"
      class="order-detail-dialog"
    >
      <div class="order-detail" v-if="selectedOrder">
        <div class="order-detail-header">
          <span class="order-detail-no">订单号：{{ selectedOrder.orderNo || '--' }}</span>
          <el-tag size="small" :type="selectedOrder.orderStatus === 0 ? 'warning' : 'success'">
            {{ selectedOrder.orderStatus === 0 ? '待接单' : '已接单' }}
          </el-tag>
        </div>
        <el-divider />
        <div class="order-detail-body">
          <div class="detail-row">
            <span class="detail-label">服务项目</span>
            <span class="detail-value">{{ selectedOrder.serviceItem }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">服务地址</span>
            <span class="detail-value">{{ selectedOrder.serviceAddress }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">预约时间</span>
            <span class="detail-value">{{ selectedOrder.serviceTime }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">订单金额</span>
            <span class="detail-value order-price">¥{{ selectedOrder.orderAmount }}</span>
          </div>
          <div class="detail-row" v-if="selectedOrder.remark">
            <span class="detail-label">备注</span>
            <span class="detail-value">{{ selectedOrder.remark }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showOrderDialog = false">关闭</el-button>
        <el-button
          v-if="selectedOrder && selectedOrder.orderStatus === 0"
          type="primary"
          :loading="takingOrder"
          @click="handleTakeOrder"
        >
          接单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { getMerchants, clearUnread, updateLastMsg, recvMsgFromMerchant, setMerchants, addMerchant } from '../store/merchantStore'
import { getOrderDetailApi, takeOrderApi, getChatHistoryApi, getMerchantListApi, getUserNameApi, saveChatMsgApi, markChatReadApi, getChatRoomListApi } from '../api/admin'
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const currentMerchant = ref({})
const newMsg = ref('')
const chatMessages = ref([])
const msgBox = ref(null)
let ws = null

const showOrderDialog = ref(false)
const selectedOrder = ref(null)
const takingOrder = ref(false)

const merchantList = computed(() => getMerchants())

const isStaff = computed(() => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      return info.roleCode === '02'
    } catch { /* ignore */ }
  }
  return false
})

const connectWebSocket = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  let userId = ''
  let role = 'merchant'
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      userId = info.account || ''
      if (info.roleCode === '02') {
        role = 'staff'
      }
    } catch { /* ignore */ }
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = import.meta.env.VITE_WS_HOST || window.location.host
  const wsUrl = `${protocol}//${host}/ws/chat?userId=${userId}&role=${role}`
  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    console.log('WebSocket 连接成功')
  }

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      handleIncomingMessage(data)
    } catch (e) {
      console.error('解析消息失败:', e)
    }
  }

  ws.onerror = (error) => {
    console.error('WebSocket 错误:', error)
    ElMessage.error('WebSocket 连接出错')
  }

  ws.onclose = (event) => {
    console.log('WebSocket 连接关闭，代码:', event.code, '原因:', event.reason)
    // 被踢下线：不重连，提示并跳转登录页
    if (event.reason && event.reason.includes('其他设备登录')) {
      ElMessage.error('账号已在其他设备登录，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
      return
    }
    // 正常关闭（页面离开等）：不重连
    if (event.code === 1000) {
      return
    }
    // 异常断开：3秒后重连
    setTimeout(() => {
      console.log('尝试重新连接...')
      connectWebSocket()
    }, 3000)
  }
}

const handleIncomingMessage = (data) => {
  const { type, merchantId, merchantName, merchantAvatar, message, orderData } = data

  if (type === 'order' && merchantId && orderData) {
    const now = new Date()
    const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    const orderMsg = {
      from: 'merchant',
      type: 'order',
      orderData,
      time
    }
    if (currentMerchant.value.id === merchantId) {
      chatMessages.value.push(orderMsg)
      updateLastMsg(merchantId, '[订单] ' + (orderData.serviceItem || '新订单'), time)
      nextTick(scrollToBottom)
      markChatReadApi({ userId: getMyId(), merchantId: merchantId }).catch(() => {})
    } else {
      recvMsgFromMerchant({
        merchantId,
        merchantName,
        merchantAvatar,
        text: '[订单] ' + (orderData.serviceItem || '新订单')
      })
    }
    return
  }

  if (type === 'message' && merchantId && message) {
    if (currentMerchant.value.id === merchantId) {
      const now = new Date()
      const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
      chatMessages.value.push({ from: 'merchant', text: message, time, isRead: true })
      updateLastMsg(merchantId, message, time)
      nextTick(scrollToBottom)
      markChatReadApi({ userId: getMyId(), merchantId: merchantId }).catch(() => {})
    } else {
      recvMsgFromMerchant({
        merchantId,
        merchantName,
        merchantAvatar,
        text: message
      })
    }
  }
}
const getMyId = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      return info.account || ''
    } catch { /* ignore */ }
  }
  return ''
}
const sendMessage = (data) => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(data))
    return true
  } else {
    ElMessage.error('连接已断开，请刷新页面重试')
    return false
  }
}

const showOrderDetail = (order) => {
  selectedOrder.value = order
  showOrderDialog.value = true
}

const handleTakeOrder = async () => {
  if (!selectedOrder.value) return

  const userInfoStr = localStorage.getItem('userInfo')
  let staffAccount = ''
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      staffAccount = info.account || ''
    } catch { /* ignore */ }
  }

  if (!staffAccount) {
    ElMessage.error('请先登录')
    return
  }

  takingOrder.value = true
  try {
    await takeOrderApi({
      orderId: selectedOrder.value.id,
      staffAccount
    })
    takingOrder.value = false
    ElMessage.success('接单成功')

    selectedOrder.value.orderStatus = 1
    if (currentMerchant.value.id && chatMessages.value.length > 0) {
      const lastMsg = chatMessages.value[chatMessages.value.length - 1]
      if (lastMsg.type === 'order' && lastMsg.orderData.id === selectedOrder.value.id) {
        lastMsg.orderData.orderStatus = 1
      }
    }
    showOrderDialog.value = false

    const now = new Date()
    const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    const successMsg = {
      from: 'me',
      text: `已接单，订单号：${selectedOrder.value.orderNo || selectedOrder.value.id}`,
      time
    }
    chatMessages.value.push(successMsg)
    updateLastMsg(currentMerchant.value.id, successMsg.text, time)
    nextTick(scrollToBottom)

    sendMessage({
      type: 'message',
      merchantId: currentMerchant.value.id,
      message: `商家已接单，订单号：${selectedOrder.value.orderNo || selectedOrder.value.id}`
    })
  } catch {
    takingOrder.value = false
    ElMessage.error('接单失败，请重试')
  }
}

onMounted(async () => {
  loading.value = true
  
  const userInfoStr = localStorage.getItem('userInfo')
  let userId = ''
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      userId = info.account || ''
    } catch { /* ignore */ }
  }
  
  if (userId) {
    try {
      const res = await getChatRoomListApi(userId)
      if (res && res.data && Array.isArray(res.data)) {
        setMerchants(res.data)
      }
    } catch (e) {
      console.warn('获取会话列表失败:', e)
    }
  }
  
  connectWebSocket()
  setTimeout(() => {
    loading.value = false
  }, 300)

  // 如果 URL 中带有 merchantId 参数，自动打开与该商家的聊天
  const targetMerchantId = route.query.merchantId
  if (targetMerchantId) {
    console.log('[MerchantPage] targetMerchantId:', targetMerchantId)
    console.log('[MerchantPage] merchantList:', merchantList.value)
    
    // 等待 merchantList 更新后再匹配
    await nextTick()
    
    const findAndOpen = async () => {
      const targetMerchant = merchantList.value.find(m => {
        console.log('[MerchantPage] checking merchant:', m.id, m.account)
        return m.id === targetMerchantId || m.account === targetMerchantId
      })
      
      console.log('[MerchantPage] targetMerchant:', targetMerchant)
      
      if (targetMerchant) {
        await openChat(targetMerchant)
      } else {
        // 如果商家不在列表中，创建一个临时商家对象并打开聊天
        console.log('[MerchantPage] creating tempMerchant')
        const tempMerchant = {
          id: targetMerchantId,
          account: targetMerchantId,
          name: String(targetMerchantId),
          avatar: '',
          lastMsg: '',
          time: '',
          unread: 0
        }
        await openChat(tempMerchant)
      }
    }
    
    // 如果 merchantList 还是空的，等待一下再试
    if (merchantList.value.length === 0) {
      setTimeout(findAndOpen, 500)
    } else {
      await findAndOpen()
    }
  }
})

onUnmounted(() => {
  if (ws) {
    ws.close(1000, '页面离开')
  }
})

const openChat = async (merchant) => {
  if (!merchant || !merchant.id) {
    ElMessage.error('商家信息不完整')
    return
  }
  
  currentMerchant.value = merchant
  clearUnread(merchant.id)

  const myId = getMyId()

  // 构造正确的 roomId 格式：家政人员账号_管理员账号
  const roomId = merchant.id.includes('_') 
    ? merchant.id 
    : `${merchant.id}_${myId}`

  chatMessages.value = []
  try {
    const res = await getChatHistoryApi({
      room_id: roomId
    })
    if (res && res.data && Array.isArray(res.data)) {
      res.data.forEach(msg => {
        const isMe = String(msg.senderId) === String(myId)
        chatMessages.value.push({
          from: isMe ? 'me' : 'merchant',
          type: msg.msgType || 'text',
          text: msg.content,
          orderData: msg.orderData,
          time: formatTime(new Date(msg.createdAt)),
          isRead: msg.isRead === true || msg.isRead === 1 || msg.isRead === '1'
        })
      })
    }
  } catch {
    console.warn('获取历史消息失败')
  }

  nextTick(scrollToBottom)

  // 使用正确的 roomId 标记已读
  markChatReadApi({ room_id: roomId, userId: myId }).catch(() => {})
  
  // 标记已读后，更新本地消息的已读状态
  chatMessages.value.forEach(msg => {
    if (msg.from === 'merchant') {
      msg.isRead = true
    }
  })
  
  // 同时清除本地未读计数
  clearUnread(merchant.id)
}

const sendMsg = async () => {
  const text = newMsg.value.trim()
  if (!text) return
  
  const myId = getMyId()
  
  // 构造正确的 roomId 格式：家政人员账号_管理员账号
  const roomId = currentMerchant.value.id.includes('_') 
    ? currentMerchant.value.id 
    : `${currentMerchant.value.id}_${myId}`
  
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  chatMessages.value.push({ from: 'me', text, time })
  updateLastMsg(currentMerchant.value.id, text, time)
  newMsg.value = ''
  nextTick(scrollToBottom)

  try {
    await saveChatMsgApi({
      roomId: roomId,
      senderId: myId,
      content: text,
      msgType: 'text'
    })
  } catch (e) {
    console.error('保存消息失败:', e)
    ElMessage.error('消息发送失败')
  }

  const success = sendMessage({
    type: 'message',
    merchantId: currentMerchant.value.id,
    message: text
  })
  if (!success) {
    ElMessage.error('实时推送失败，连接已断开')
  }
}

const scrollToBottom = () => {
  if (msgBox.value) {
    msgBox.value.scrollTop = msgBox.value.scrollHeight
  }
}

const formatTime = (date) => {
  if (!date || isNaN(date.getTime())) return ''
  const h = date.getHours().toString().padStart(2, '0')
  const m = date.getMinutes().toString().padStart(2, '0')
  return `${h}:${m}`
}
</script>

<style scoped>
.merchant-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.merchant-header {
  flex-shrink: 0;
  background: white;
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.merchant-header h2 {
  font-size: 18px;
  margin: 0;
}

.back-link {
  display: inline-block;
}

.merchant-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.merchant-sidebar {
  width: 300px;
  flex-shrink: 0;
  background: white;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  padding: 16px 20px 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-h);
  border-bottom: 1px solid #f0f0f0;
}

.merchant-list {
  flex: 1;
  overflow-y: auto;
}

.merchant-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.merchant-item:hover {
  background: #f5f7fa;
}

.merchant-item.active {
  background: #ecf5ff;
}

.merchant-item:last-child {
  border-bottom: none;
}

.merchant-info {
  flex: 1;
  min-width: 0;
}

.merchant-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.merchant-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-h);
}
.cs-tag {
  font-size: 12px;
  color: var(--text-light);
  font-weight: 400;
}
.merchant-time {
  font-size: 11px;
  color: var(--text-light);
  flex-shrink: 0;
}

.merchant-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.merchant-last {
  font-size: 13px;
  color: var(--text-light);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
}

.merchant-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.chat-header {
  padding: 14px 20px;
  background: white;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-h);
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-msg {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}

.msg-left {
  align-items: flex-start;
}

.msg-right {
  align-items: flex-end;
}

.msg-bubble {
  max-width: 60%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
}

.msg-left .msg-bubble {
  background: white;
  border-bottom-left-radius: 4px;
}

.msg-right .msg-bubble {
  background: var(--accent);
  color: white;
  border-bottom-right-radius: 4px;
}

.msg-time {
  font-size: 11px;
  color: var(--text-light);
  margin-top: 4px;
  padding: 0 4px;
}
.msg-status-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  padding: 0 4px;
}

.msg-time {
  font-size: 11px;
  color: var(--text-light);
}

.msg-read-status {
  font-size: 11px;
  color: #909399;
}

.msg-left .msg-read-status {
  color: #909399;
}

.msg-right .msg-read-status {
  display: none;
}
.chat-input-row {
  display: flex;
  gap: 10px;
  padding: 14px 20px;
  background: white;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}

.merchant-chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.order-card {
  background: white;
  border-radius: 8px;
  padding: 12px 14px;
  min-width: 280px;
  max-width: 80%;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.2s;
}

.order-card:hover {
  border-color: var(--accent);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.12);
}

.order-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.order-card-title {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-h);
}

.order-card-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

.order-card-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-label {
  font-size: 13px;
  color: var(--text-light);
}

.order-value {
  font-size: 13px;
  color: var(--text-h);
  max-width: 180px;
  text-align: right;
}

.order-amount {
  font-size: 16px;
  font-weight: 600;
  color: var(--accent);
}

.order-card-footer {
  padding-top: 6px;
  border-top: 1px solid #f0f0f0;
}

.order-click-hint {
  font-size: 12px;
  color: var(--text-light);
  text-align: right;
}

.order-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-detail-no {
  font-size: 14px;
  color: var(--text-light);
}

.order-detail-body {
  padding: 10px 0;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
}

.detail-label {
  width: 80px;
  flex-shrink: 0;
  font-size: 14px;
  color: var(--text-light);
  text-align: left;
}

.detail-value {
  flex: 1;
  font-size: 14px;
  color: var(--text-h);
  word-break: break-all;
}

.detail-value.order-price {
  font-size: 18px;
  font-weight: 600;
  color: var(--accent);
}

@media (max-width: 768px) {
  .merchant-sidebar {
    width: 100%;
  }

  .merchant-chat,
  .merchant-chat-empty {
    display: none;
  }

  .merchant-body.has-chat .merchant-sidebar {
    display: none;
  }

  .merchant-body.has-chat .merchant-chat {
    display: flex;
  }
}

.cs-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 360px;
  overflow-y: auto;
  padding: 4px 0;
}

.cs-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 2px solid #ebeef5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.cs-item:hover {
  border-color: #ffb0b0;
  background: #fffbfb;
}

.cs-item.active {
  border-color: var(--accent);
  background: #fff5f5;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.12);
}

.cs-info {
  flex: 1;
  min-width: 0;
}

.cs-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.cs-account {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>