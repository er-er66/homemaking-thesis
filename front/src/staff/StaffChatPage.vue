<template>
  <div class="chat-page">
    <header class="chat-header">
      <el-link type="primary" :underline="false" @click="router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-link>
      <div class="chat-title">
        <el-avatar :size="36" :src="userAvatar" shape="square">{{ userName.charAt(0) }}</el-avatar>
        <span class="chat-name">{{ userName }}</span>
      </div>
      <div class="order-info" v-if="orderId">
        <el-tag size="small">订单号：{{ orderNo }}</el-tag>
      </div>
    </header>

    <div class="chat-body">
      <div class="chat-messages" ref="msgBox">
        <template v-for="(msg, idx) in messages" :key="idx">
          <div v-if="msg.type === 'order'" class="chat-msg item-left">
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
            :class="['chat-item', msg.from === 'me' ? 'item-right' : 'item-left']"
          >
            <div class="msg-avatar" v-if="msg.from !== 'me'">
              <el-avatar :size="36" :src="userAvatar" shape="square">{{ userName.charAt(0) }}</el-avatar>
            </div>
            <div class="msg-content">
              <div class="msg-sender" v-if="msg.from !== 'me'">{{ userName }}</div>
              <div class="msg-sender" v-else>{{ staffName }}</div>
              <div class="msg-bubble">{{ msg.text }}</div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
            <div class="msg-avatar" v-if="msg.from === 'me'">
              <el-avatar :size="36" :src="staffAvatar" shape="square">{{ staffName.charAt(0) }}</el-avatar>
            </div>
          </div>
        </template>
        <div v-if="typing" class="chat-item item-left">
          <div class="msg-bubble typing-bubble">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </div>
        </div>
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

    <footer class="chat-footer">
      <div class="chat-input-row">
        <el-input
          v-model="newMsg"
          placeholder="输入消息..."
          @keyup.enter="sendMsg"
          size="large"
        />
        <el-button type="primary" size="large" @click="sendMsg">发送</el-button>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { takeOrderApi, saveChatMsgApi, getChatHistoryApi, getUserNameApi, getChatRoomListApi } from '../api/admin'

const route = useRoute()
const router = useRouter()

const userAccount = ref(route.query.userAccount || '')
const userName = ref('用户')
const userAvatar = ref(route.query.userAvatar || '')
const orderId = ref(route.query.orderId || '')
const orderNo = ref(route.query.orderNo || '')
const currentRoomId = ref('')

const messages = ref([])
const newMsg = ref('')
const msgBox = ref(null)
const typing = ref(false)
const showOrderDialog = ref(false)
const selectedOrder = ref(null)
const takingOrder = ref(false)

const staffName = ref('我')
const staffAvatar = ref('')

let ws = null
let isConnecting = false
let reconnectTimer = null
let heartbeatTimer = null

const startHeartbeat = () => {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'ping', timestamp: Date.now() }))
    }
  }, 30000)
}

const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

const clearReconnectTimer = () => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

const mergeMessages = (localMessages) => {
  if (!localMessages || localMessages.length === 0) return
  
  const existingTimes = new Set(messages.value.map(m => m.time))
  
  localMessages.forEach(msg => {
    if (!existingTimes.has(msg.time)) {
      messages.value.push(msg)
    }
  })
  
  messages.value.sort((a, b) => {
    const timeA = new Date(`2000-01-01 ${a.time}`)
    const timeB = new Date(`2000-01-01 ${b.time}`)
    return timeA - timeB
  })
}

onMounted(async () => {
  // 获取员工信息
  const staffInfoStr = localStorage.getItem('userInfo')
  let staffAccount = ''
  if (staffInfoStr) {
    try {
      const info = JSON.parse(staffInfoStr)
      staffAccount = info.account || ''
      staffName.value = info.username || info.account || '我'
      staffAvatar.value = info.avatar || ''
    } catch { /* ignore */ }
  }

  // 获取用户信息
  if (userAccount.value) {
    try {
      const res = await getUserNameApi(userAccount.value)
      if (res && res.data) {
        userName.value = res.data.username || res.data.account || '用户'
        userAvatar.value = res.data.avatar || userAvatar.value
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  // 从后端获取会话列表，找到正确的 roomId
  if (staffAccount && userAccount.value) {
    try {
      const res = await getChatRoomListApi(staffAccount)
      if (res && res.data && Array.isArray(res.data)) {
        const room = res.data.find(r => r.account === userAccount.value)
        if (room) {
          currentRoomId.value = room.roomId
        }
      }
    } catch (e) {
      console.warn('获取会话列表失败:', e)
    }
  }

  await loadHistoryMessages(currentRoomId.value, staffAccount)
  
  // 清除本地缓存，统一使用后端数据
  localStorage.removeItem(`chat_${staffAccount}_${userAccount.value}`)

  connectWs(staffAccount)
})

const loadHistoryMessages = async (roomId, staffAccount) => {
  if (!roomId) {
    console.warn('roomId 为空，跳过加载历史消息')
    return
  }
  try {
    const res = await getChatHistoryApi({
      room_id: roomId
    })
    if (res && res.data && res.data.length > 0) {
      messages.value = res.data.map(msg => {
        const isCurrentStaffSender = String(msg.senderId) === String(staffAccount)
        
        return {
          from: isCurrentStaffSender ? 'me' : 'user',
          text: msg.content,
          time: formatTime(new Date(msg.createdAt)),
          type: msg.msgType || 'text'
        }
      })
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
  }
}

const saveMessagesToBackend = async (senderId, content, msgType = 'text', attachUrl = '') => {
  try {
    await saveChatMsgApi({
      roomId: currentRoomId.value,
      senderId: senderId,
      senderType: 1,
      content: content,
      msgType: msgType,
      attachUrl: attachUrl
    })
  } catch (error) {
    console.error('保存消息失败:', error)
  }
}

const connectWs = (staffAccount) => {
  if (isConnecting) {
    console.log('WebSocket 正在连接中，跳过重复调用')
    return
  }
  
  if (ws && ws.readyState === WebSocket.OPEN) {
    console.log('WebSocket 已连接，无需重复连接')
    return
  }

  if (!staffAccount || !userAccount.value) {
    console.error('WebSocket 连接失败：缺少必要参数', { staffAccount, userAccount: userAccount.value })
    return
  }

  isConnecting = true
  
  if (ws) {
    ws.onclose = null
    ws.close()
    ws = null
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/chat?from=${encodeURIComponent(staffAccount)}&to=${encodeURIComponent(userAccount.value)}&role=staff`

  console.log('尝试连接 WebSocket:', wsUrl)

  try {
    ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      console.log('WebSocket 已连接')
      isConnecting = false
      startHeartbeat()
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'pong') {
          return
        }
        if (data.type === 'typing') {
          typing.value = true
          setTimeout(() => { typing.value = false }, 2000)
          return
        }
        
        // 判断消息发送者身份
        const messageSender = data.from || data.sender_id || ''
        const isFromMe = String(messageSender) === String(staffAccount)
        
        if (data.type === 'order') {
          const now = new Date()
          messages.value.push({
            from: isFromMe ? 'me' : 'user',
            type: 'order',
            orderData: data.orderData,
            time: formatTime(now)
          })
          nextTick(scrollToBottom)
        } else {
          // 普通消息：根据发送者身份决定显示位置
          messages.value.push({
            from: isFromMe ? 'me' : 'user',
            text: data.text || data.content || '',
            time: formatTime(new Date())
          })
          nextTick(scrollToBottom)
        }
      } catch (e) {
        console.warn('解析消息失败:', e)
        messages.value.push({
          from: 'user',
          text: event.data,
          time: formatTime(new Date())
        })
        nextTick(scrollToBottom)
      }
    }

    ws.onclose = (event) => {
      stopHeartbeat()
      console.log(`WebSocket 已断开 (code: ${event.code}, reason: ${event.reason})`)
      isConnecting = false
      
      clearReconnectTimer()
      
      if (event.code !== 1000) {
        console.log('3秒后重连')
        reconnectTimer = setTimeout(() => connectWs(staffAccount), 3000)
      }
    }

    ws.onerror = (error) => {
      console.error('WebSocket 连接错误:', error)
      isConnecting = false
    }
  } catch (e) {
    console.error('WebSocket 初始化失败:', e)
    isConnecting = false
    console.log('WebSocket 不可用，使用本地模式')
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
    if (userAccount.value && messages.value.length > 0) {
      const lastMsg = messages.value[messages.value.length - 1]
      if (lastMsg.type === 'order' && lastMsg.orderData.id === selectedOrder.value.id) {
        lastMsg.orderData.orderStatus = 1
      }
    }
    showOrderDialog.value = false

    const staffInfoStr = localStorage.getItem('userInfo')
    let currentStaffAccount = ''
    if (staffInfoStr) {
      try {
        currentStaffAccount = JSON.parse(staffInfoStr).account || ''
      } catch { /* ignore */ }
    }

    const now = new Date()
    const time = formatTime(now)
    const successMsg = {
      from: 'me',
      text: `已接单，订单号：${selectedOrder.value.orderNo || selectedOrder.value.id}`,
      time
    }
    messages.value.push(successMsg)
    updateChatUserList(currentStaffAccount, userAccount.value, successMsg.text, time)
    nextTick(scrollToBottom)

    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({
        from: currentStaffAccount,
        to: userAccount.value,
        text: successMsg.text,
        type: 'message',
        role: 'staff'
      }))
    }
  } catch {
    takingOrder.value = false
    ElMessage.error('接单失败，请重试')
  }
}

const sendMsg = async () => {
  const text = newMsg.value.trim()
  if (!text) return
  const now = new Date()
  const time = formatTime(now)
  const msg = { from: 'me', text, time }
  messages.value.push(msg)
  newMsg.value = ''
  nextTick(scrollToBottom)

  const staffInfoStr = localStorage.getItem('userInfo')
  let staffAccount = ''
  if (staffInfoStr) {
    try {
      staffAccount = JSON.parse(staffInfoStr).account || ''
    } catch { /* ignore */ }
  }
  await saveMessagesToBackend(staffAccount, text, 'text')

  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({
      from: staffAccount,
      to: userAccount.value,
      text,
      type: 'message',
      role: 'staff'
    }))
  }

  updateChatUserList(staffAccount, userAccount.value, text, time)
}

const updateChatUserList = (staffAccount, targetAccount, text, time) => {
  const key = `staff_chat_users`
  const stored = localStorage.getItem(key)
  let list = stored ? JSON.parse(stored) : []
  const idx = list.findIndex(u => u.account === targetAccount)
  if (idx >= 0) {
    list[idx].lastMsg = text
    list[idx].lastTime = time
    list[idx].lastTimestamp = new Date().toISOString()
  } else {
    list.push({
      account: targetAccount,
      name: userName.value,
      avatar: userAvatar.value,
      lastMsg: text,
      lastTime: time,
      lastTimestamp: new Date().toISOString(),
      unread: 0
    })
  }
  list.sort((a, b) => new Date(b.lastTimestamp) - new Date(a.lastTimestamp))
  localStorage.setItem(key, JSON.stringify(list))
}

const saveMessages = (staffAccount) => {
  localStorage.setItem(`chat_${staffAccount}_${userAccount.value}`, JSON.stringify(messages.value))
}

const scrollToBottom = () => {
  if (msgBox.value) {
    msgBox.value.scrollTop = msgBox.value.scrollHeight
  }
}

const formatTime = (date) => {
  if (!date || isNaN(date.getTime())) return '--:--'
  const h = date.getHours().toString().padStart(2, '0')
  const m = date.getMinutes().toString().padStart(2, '0')
  return `${h}:${m}`
}

onUnmounted(() => {
  stopHeartbeat()
  clearReconnectTimer()
  if (ws) {
    ws.onclose = null
    ws.close(1000, '页面关闭')
    ws = null
  }
})
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 20px;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.back-btn {
  font-size: 14px;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.chat-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chat-body {
  flex: 1;
  overflow: hidden;
  display: flex;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 80%;
}

.item-left {
  align-self: flex-start;
}

.item-right {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.msg-avatar {
  flex-shrink: 0;
}

.msg-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.msg-sender {
  font-size: 12px;
  color: #909399;
  padding: 0 4px;
}

.item-right .msg-sender {
  text-align: right;
}

.msg-bubble {
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}

.item-left .msg-bubble {
  background: #fff;
  color: #303133;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.item-right .msg-bubble {
  background: var(--accent);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 1px 3px rgba(255, 107, 107, 0.2);
}

.msg-time {
  font-size: 11px;
  color: #c0c4cc;
  padding: 0 4px;
}

.item-right .msg-time {
  text-align: right;
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

.typing-bubble {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #c0c4cc;
  animation: typing 1.4s infinite ease-in-out;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: translateY(0); }
  30% { opacity: 1; transform: translateY(-4px); }
}

.chat-footer {
  padding: 12px 20px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}

.chat-input-row {
  display: flex;
  gap: 12px;
  align-items: center;
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
</style>