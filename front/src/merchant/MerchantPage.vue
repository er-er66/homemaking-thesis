<template>
  <div class="merchant-page">
    <div class="merchant-header">
      <el-link type="info" :underline="false" @click="router.push('/')" class="back-link">
        ← 返回
      </el-link>
      <h2>商家消息</h2>
    </div>

    <div :class="['merchant-body', { 'has-chat': currentMerchant.id }]" v-loading="loading">
      <div class="merchant-sidebar">
        <div class="sidebar-title">商家列表</div>
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
                  <span class="merchant-name">{{ item.name }}</span>
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
          <span class="chat-title">{{ currentMerchant.name }}</span>
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
                <div class="msg-time">{{ msg.time }}</div>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { getMerchants, clearUnread, updateLastMsg, recvMsgFromMerchant, setMerchants } from '../store/merchantStore'
import { getOrderDetailApi, takeOrderApi, getChatHistoryApi, getMerchantListApi, getUserNameApi, saveChatMsgApi } from '../api/admin'

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

const connectWebSocket = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  let userId = ''
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      userId = info.account || ''
    } catch { /* ignore */ }
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = import.meta.env.VITE_WS_HOST || window.location.host
  const wsUrl = `${protocol}//${host}/ws/chat?userId=${userId}&role=merchant`
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
    console.log('WebSocket 连接关闭，代码:', event.code)
    if (event.code !== 1000) {
      setTimeout(() => {
        console.log('尝试重新连接...')
        connectWebSocket()
      }, 3000)
    }
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
      chatMessages.value.push({ from: 'merchant', text: message, time })
      updateLastMsg(merchantId, message, time)
      nextTick(scrollToBottom)
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
      const res = await getMerchantListApi(userId)
      if (res && res.data && Array.isArray(res.data)) {
        const merchantList = res.data.map(merchantId => ({
          id: merchantId,
          name: String(merchantId),
          avatar: '',
          lastMsg: '',
          time: '',
          unread: 0
        }))
        setMerchants(merchantList)
      }
    } catch (e) {
      console.warn('获取商家列表失败:', e)
    }
  }
  
  connectWebSocket()
  setTimeout(() => {
    loading.value = false
  }, 300)
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

  const userInfoStr = localStorage.getItem('userInfo')
  let myId = ''
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      myId = info.account || ''
    } catch { /* ignore */ }
  }

  chatMessages.value = []
  try {
    const res = await getChatHistoryApi({
      sender_id: myId,
      receiver_id: merchant.id
    })
    if (res && res.data && Array.isArray(res.data)) {
      res.data.forEach(msg => {
        const isMe = String(msg.senderId) === String(myId)
        chatMessages.value.push({
          from: isMe ? 'me' : 'merchant',
          type: msg.msgType || 'text',
          text: msg.content,
          orderData: msg.orderData,
          time: formatTime(new Date(msg.createdAt))
        })
      })
    }
  } catch {
    console.warn('获取历史消息失败')
  }

  nextTick(scrollToBottom)
}

const sendMsg = async () => {
  const text = newMsg.value.trim()
  if (!text) return
  
  const userInfoStr = localStorage.getItem('userInfo')
  let myId = ''
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      myId = info.account || ''
    } catch { /* ignore */ }
  }
  
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  chatMessages.value.push({ from: 'me', text, time })
  updateLastMsg(currentMerchant.value.id, text, time)
  newMsg.value = ''
  nextTick(scrollToBottom)

  try {
    await saveChatMsgApi({
      senderId: myId,
      receiverId: currentMerchant.value.id,
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
}

.merchant-header h2 {
  font-size: 18px;
  margin-top: 4px;
  margin-bottom: 0;
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
</style>