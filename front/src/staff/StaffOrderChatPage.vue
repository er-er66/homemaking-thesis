<template>
  <div class="order-chat-page">
    <div class="header">
      <el-link type="info" :underline="false" @click="router.back()" class="back-link">
        ← 返回
      </el-link>
      <h2>订单与聊天</h2>
    </div>

    <div class="body">
      <div class="order-panel">
        <div class="order-title">订单详情</div>
        <div v-loading="loading" class="order-detail" v-if="orderData">
          <div class="order-info-card">
            <div class="info-row">
              <span class="label">订单号</span>
              <span class="value">{{ orderData.orderNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">服务项目</span>
              <span class="value">{{ orderData.serviceItem }}</span>
            </div>
            <div class="info-row">
              <span class="label">服务地址</span>
              <span class="value">{{ orderData.serviceAddress }}</span>
            </div>
            <div class="info-row">
              <span class="label">预约时间</span>
              <span class="value">{{ orderData.serviceTime }}</span>
            </div>
            <div class="info-row">
              <span class="label">订单金额</span>
              <span class="value price">¥{{ orderData.orderAmount }}</span>
            </div>
            <div class="info-row" v-if="orderData.remark">
              <span class="label">备注</span>
              <span class="value">{{ orderData.remark }}</span>
            </div>
            <div class="info-row">
              <span class="label">下单用户</span>
              <span class="value">{{ orderData.userAccount }}</span>
            </div>
            <div class="info-row">
              <span class="label">状态</span>
              <el-tag size="small" :type="orderData.orderStatus === 0 ? 'warning' : 'success'">
                {{ orderData.orderStatus === 0 ? '待接单' : '已接单' }}
              </el-tag>
            </div>
          </div>

          <div class="action-buttons">
            <el-button 
              v-if="orderData.orderStatus === 0" 
              type="primary" 
              size="large" 
              :loading="takingOrder" 
              @click="handleTakeOrder"
            >
              接单
            </el-button>
            <el-button 
              type="success" 
              size="large" 
              :loading="sendingOrder" 
              @click="handleSendOrderDetail"
            >
              发送订单详情
            </el-button>
          </div>
        </div>
        <el-empty v-else description="加载中..." />
      </div>

      <div class="chat-panel">
        <div class="chat-title">
          <el-avatar :size="36" :src="userAvatar" shape="square">{{ userName.charAt(0) }}</el-avatar>
          <span>{{ userName }}</span>
        </div>
        <div class="chat-messages" ref="msgBox">
          <template v-for="(msg, idx) in messages" :key="idx">
            <div v-if="msg.msgType === 'order_detail'" :class="['chat-msg', msg.from === 'me' ? 'item-right' : 'item-left']" :style="msg.from === 'me' ? (msg.msgType === 'order_detail' ? orderDetailStyleRight : {}) : (msg.msgType === 'order_detail' ? orderDetailStyleLeft : {})">
              <div class="order-card" :class="msg.from === 'me' ? 'sent-order-card-right' : 'sent-order-card-left'" :style="msg.from === 'me' ? cardStyleRight : cardStyleLeft">
                <div class="order-card-header" :style="msg.from === 'me' ? headerStyleRight : headerStyleLeft">
                  <el-icon size="18" color="white"><Document /></el-icon>
                  <span class="order-card-title" style="color: white">订单详情</span>
                  <el-tag size="small">
                    {{ (msg.orderData?.orderStatus || msg.orderData?.orderStatus === 0) === 0 ? '待接单' : '已接单' }}
                  </el-tag>
                </div>
                <div class="order-card-body" style="padding: 12px">
                  <div class="order-card-row" v-if="msg.orderData?.orderNo">
                    <span class="order-label">订单号</span>
                    <span class="order-value">{{ msg.orderData.orderNo }}</span>
                  </div>
                  <div class="order-card-row" v-if="msg.orderData?.serviceItem">
                    <span class="order-label">服务项目</span>
                    <span class="order-value">{{ msg.orderData.serviceItem }}</span>
                  </div>
                  <div class="order-card-row" v-if="msg.orderData?.serviceAddress">
                    <span class="order-label">服务地址</span>
                    <span class="order-value">{{ msg.orderData.serviceAddress }}</span>
                  </div>
                  <div class="order-card-row" v-if="msg.orderData?.serviceTime">
                    <span class="order-label">预约时间</span>
                    <span class="order-value">{{ msg.orderData.serviceTime }}</span>
                  </div>
                  <div class="order-card-row" v-if="msg.orderData?.orderAmount">
                    <span class="order-label">订单金额</span>
                    <span class="order-amount">¥{{ msg.orderData.orderAmount }}</span>
                  </div>
                </div>
              </div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
            <div v-else-if="msg.type === 'order'" class="chat-msg item-left">
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
                    <span class="order-label">预约时间</span>
                    <span class="order-value">{{ msg.orderData.serviceTime }}</span>
                  </div>
                  <div class="order-card-row">
                    <span class="order-label">金额</span>
                    <span class="order-amount">¥{{ msg.orderData.orderAmount }}</span>
                  </div>
                </div>
              </div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
            <div
              v-else
              :class="['chat-item', msg.from === 'me' ? 'item-right' : 'item-left']"
            >
              <div class="msg-bubble">{{ msg.text }}</div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
          </template>
          <div v-if="typing" class="chat-item item-left">
            <div class="msg-bubble typing-bubble">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>
        <div class="chat-footer">
          <div class="chat-input-row">
            <el-input v-model="newMsg" placeholder="输入消息..." @keyup.enter="sendMsg" />
            <el-button type="primary" @click="sendMsg">发送</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="showOrderDialog"
      title="订单详情"
      width="480px"
      :close-on-click-modal="false"
    >
      <div class="order-detail-pop" v-if="selectedOrder">
        <div class="info-row">
          <span class="label">订单号</span>
          <span class="value">{{ selectedOrder.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="label">服务项目</span>
          <span class="value">{{ selectedOrder.serviceItem }}</span>
        </div>
        <div class="info-row">
          <span class="label">服务地址</span>
          <span class="value">{{ selectedOrder.serviceAddress }}</span>
        </div>
        <div class="info-row">
          <span class="label">预约时间</span>
          <span class="value">{{ selectedOrder.serviceTime }}</span>
        </div>
        <div class="info-row">
          <span class="label">订单金额</span>
          <span class="value price">¥{{ selectedOrder.orderAmount }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showOrderDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getOrderDetailApi, takeOrderApi, markChatReadApi, saveChatMsgApi } from '../api/admin'
import { normalizeChatMessage, resolveOrderDetail, buildOrderDetailContent } from '../utils/chatMessage'

const route = useRoute()
const router = useRouter()

const orderId = ref(route.query.orderId || '')
const userAccount = ref(route.query.userAccount || '')
const userName = ref(route.query.userName || '用户')
const userAvatar = ref(route.query.userAvatar || '')

const loading = ref(true)
const takingOrder = ref(false)
const sendingOrder = ref(false)
const orderData = ref(null)
const messages = ref([])
const newMsg = ref('')
const msgBox = ref(null)

const orderDetailStyleLeft = {
  display: 'flex',
  justifyContent: 'flex-start'
}

const orderDetailStyleRight = {
  display: 'flex',
  justifyContent: 'flex-end'
}

const cardStyleLeft = {
  background: 'linear-gradient(135deg, var(--brand-50) 0%, var(--neutral-50) 100%)',
  border: '1px solid var(--border-brand)',
  borderRadius: '12px',
  padding: '0',
  minWidth: '300px',
  maxWidth: '90%'
}

const cardStyleRight = {
  background: 'linear-gradient(135deg, var(--color-success-bg) 0%, var(--brand-50) 100%)',
  border: '1px solid #C1E3D3',
  borderRadius: '12px',
  padding: '0',
  minWidth: '300px',
  maxWidth: '90%'
}

const headerStyleLeft = {
  background: 'var(--gradient-brand)',
  padding: '10px 12px',
  borderRadius: '12px 12px 0 0',
  display: 'flex',
  alignItems: 'center',
  gap: '8px',
  margin: '0'
}

const headerStyleRight = {
  background: 'linear-gradient(90deg, #2FA36B, #52C08A)',
  padding: '10px 12px',
  borderRadius: '12px 12px 0 0',
  display: 'flex',
  alignItems: 'center',
  gap: '8px',
  margin: '0'
}

const typing = ref(false)
const showOrderDialog = ref(false)
const selectedOrder = ref(null)

let ws = null

onMounted(() => {
  fetchOrderDetail()
  connectWs()
})

const fetchOrderDetail = async () => {
  if (!orderId.value) {
    loading.value = false
    return
  }
  try {
    const res = await getOrderDetailApi(orderId.value)
    if (res && res.data) {
      orderData.value = res.data
    }
  } catch {
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

const connectWs = () => {
  const staffInfoStr = localStorage.getItem('userInfo')
  let staffAccount = ''
  if (staffInfoStr) {
    try {
      staffAccount = JSON.parse(staffInfoStr).account || ''
    } catch { /* ignore */ }
  }

  const saved = localStorage.getItem(`chat_${staffAccount}_${userAccount.value}`)
  if (saved) {
    try {
      // 老缓存里的订单详情只有文本没有 msgType，统一走 normalize 补成卡片结构
      messages.value = JSON.parse(saved).map(msg => normalizeChatMessage(msg, staffAccount))
    } catch { /* ignore */ }
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/chat?from=${staffAccount}&to=${userAccount.value}&role=staff`
  try {
    ws = new WebSocket(wsUrl)
    ws.onopen = () => console.log('WebSocket 已连接')
    ws.onmessage = (event) => {
      const staffInfoStr = localStorage.getItem('userInfo')
      let staffAccount = ''
      if (staffInfoStr) {
        try {
          staffAccount = JSON.parse(staffInfoStr).account || ''
        } catch { /* ignore */ }
      }
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'typing') {
          typing.value = true
          setTimeout(() => { typing.value = false }, 2000)
        } else if (data.type === 'order') {
          const now = new Date()
          messages.value.push({
            from: 'user',
            type: 'order',
            orderData: data.orderData,
            time: formatTime(now)
          })
          saveMessages(staffAccount)
          nextTick(scrollToBottom)
        } else if (resolveOrderDetail(data.msgType, data.text, data.type)) {
          // 订单详情卡片：优先看 msgType，后端推送里没有时靠 "Xiangqing" 前缀识别
          messages.value.push({
            from: 'user',
            msgType: 'order_detail',
            orderData: resolveOrderDetail(data.msgType, data.text, data.type),
            text: '',
            time: formatTime(new Date())
          })
          saveMessages(staffAccount)
          nextTick(scrollToBottom)
        } else {
          messages.value.push({
            from: 'user',
            text: data.text || data.content || '',
            time: formatTime(new Date())
          })
          saveMessages(staffAccount)
          nextTick(scrollToBottom)
        }
      } catch {
        messages.value.push({
          from: 'user',
          text: event.data,
          time: formatTime(new Date())
        })
        saveMessages(staffAccount)
        nextTick(scrollToBottom)
      }
    }
    ws.onclose = () => {
      console.log('WebSocket 断开，3秒后重连')
      setTimeout(connectWs, 3000)
    }
  } catch (e) {
    console.error('WebSocket connect error', e)
  }
}

const showOrderDetail = (order) => {
  selectedOrder.value = order
  showOrderDialog.value = true
}

const handleTakeOrder = async () => {
  const userInfoStr = localStorage.getItem('userInfo')
  let staffAccount = ''
  if (userInfoStr) {
    try {
      staffAccount = JSON.parse(userInfoStr).account || ''
    } catch { /* ignore */ }
  }
  if (!staffAccount) {
    ElMessage.error('请先登录')
    return
  }

  takingOrder.value = true
  try {
    await takeOrderApi({
      id: orderData.value.id,
      staffAccount
    })
    takingOrder.value = false
    ElMessage.success('接单成功')
    orderData.value.orderStatus = 1

    const now = new Date()
    const time = formatTime(now)
    const successMsg = {
      from: 'me',
      text: `我已接单，订单号：${orderData.value.orderNo}`,
      time
    }
    messages.value.push(successMsg)
    updateChatUserList(staffAccount, userAccount.value, successMsg.text, time)
    nextTick(scrollToBottom)

    const roomId = `${userAccount.value}_${staffAccount}`

    // 后端 ChatWebSocketHandler 收到 type='message' 会自行入库，
    // 这里再调 saveChatMsgApi 会造成同一条消息入库两次，所以 WS 可用时不重复保存
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({
        from: staffAccount,
        to: userAccount.value,
        text: successMsg.text,
        type: 'message',
        role: 'staff',
        merchantId: staffAccount,
        // 后端 WebSocket 只认 camelCase 的 roomId，用 room_id 会导致转发给对方的房间号为空
        roomId: roomId
      }))
    } else {
      await saveChatMsgApi({
        roomId: roomId,
        senderId: staffAccount,
        senderType: 1,
        content: successMsg.text,
        msgType: 'text',
        attachUrl: ''
      }).catch(error => console.error('保存消息失败:', error))
    }

    await markChatReadApi({
      room_id: roomId,
      userId: staffAccount
    })

    if (orderData.value && !messages.value.some(m => m.type === 'order' && m.orderData.id === orderData.value.id)) {
      messages.value.unshift({
        from: 'user',
        type: 'order',
        orderData: orderData.value,
        time: time
      })
    }
  } catch {
    takingOrder.value = false
    ElMessage.error('接单失败')
  }
}

const handleSendOrderDetail = async () => {
  if (!orderData.value) return

  const staffInfoStr = localStorage.getItem('userInfo')
  let staffAccount = ''
  if (staffInfoStr) {
    try {
      staffAccount = JSON.parse(staffInfoStr).account || ''
    } catch { /* ignore */ }
  }

  if (!staffAccount) {
    ElMessage.error('请先登录')
    return
  }

  sendingOrder.value = true

  const orderDetailData = {
    orderNo: orderData.value.orderNo,
    serviceItem: orderData.value.serviceItem,
    serviceAddress: orderData.value.serviceAddress,
    serviceTime: orderData.value.serviceTime,
    orderAmount: orderData.value.orderAmount,
    orderStatus: orderData.value.orderStatus
  }
  const roomId = `${userAccount.value}_${staffAccount}`
  const orderDetailContent = buildOrderDetailContent(orderDetailData)

  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({
      from: staffAccount,
      to: userAccount.value,
      text: orderDetailContent,
      type: 'order_detail',
      role: 'staff',
      merchantId: staffAccount,
      roomId: roomId
    }))
  }

  // 注意：后端 ChatWebSocketHandler 只对 type='message' 入库，order_detail 不落库，
  // 所以这里必须保留 saveChatMsgApi，否则订单详情卡片不会进历史记录。
  try {
    await saveChatMsgApi({
      roomId: roomId,
      senderId: staffAccount,
      senderType: 1,
      content: orderDetailContent,
      msgType: 'order_detail',
      attachUrl: ''
    })

    await markChatReadApi({
      room_id: roomId,
      userId: staffAccount
    })

    const now = new Date()
    const time = formatTime(now)
    const newMsg = {
      from: 'me',
      text: '',
      msgType: 'order_detail',
      orderData: orderDetailData,
      time
    }
    messages.value.push(newMsg)
    saveMessages(staffAccount)
    updateChatUserList(staffAccount, userAccount.value, '订单详情', time)
    nextTick(scrollToBottom)

    ElMessage.success('订单详情已发送')
  } catch (error) {
    console.error('发送订单详情失败:', error)
    ElMessage.error('发送失败')
  } finally {
    sendingOrder.value = false
  }
}

const sendMsg = async () => {
  const text = newMsg.value.trim()
  if (!text) return

  const staffInfoStr = localStorage.getItem('userInfo')
  let staffAccount = ''
  if (staffInfoStr) {
    try {
      staffAccount = JSON.parse(staffInfoStr).account || ''
    } catch { /* ignore */ }
  }

  const now = new Date()
  const time = formatTime(now)
  const msg = { from: 'me', text, time }
  messages.value.push(msg)
  newMsg.value = ''
  nextTick(scrollToBottom)
  saveMessages(staffAccount)

  const roomId = `${userAccount.value}_${staffAccount}`

  // 同上：WS 可达时由后端入库，避免双写产生重复消息
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({
      from: staffAccount,
      to: userAccount.value,
      text,
      type: 'message',
      role: 'staff',
      merchantId: staffAccount,
      roomId: roomId
    }))
  } else {
    try {
      await saveChatMsgApi({
        roomId: roomId,
        senderId: staffAccount,
        senderType: 1,
        content: text,
        msgType: 'text',
        attachUrl: ''
      })
    } catch (error) {
      console.error('保存消息失败:', error)
    }
  }

  await markChatReadApi({
    room_id: roomId,
    userId: staffAccount
  })

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
  const h = date.getHours().toString().padStart(2, '0')
  const m = date.getMinutes().toString().padStart(2, '0')
  return `${h}:${m}`
}

onUnmounted(() => {
  if (ws) ws.close()
})
</script>

<style scoped>
.order-chat-page {
  min-height: 100vh;
  background: var(--surface-page);
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-light);
}

.header h2 {
  margin: 0;
  font-size: 20px;
  margin-top: 8px;
}

.body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.order-panel {
  width: 380px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid var(--border-light);
  padding: 20px;
  overflow-y: auto;
}

.order-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.order-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-info-card {
  background: var(--neutral-50);
  border-radius: 8px;
  padding: 20px;
}

.info-row {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px dashed var(--border-base);
  align-items: center;
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  width: 90px;
  flex-shrink: 0;
  font-size: 14px;
  color: var(--text-secondary);
  text-align: left;
}

.value {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
}

.value.price {
  font-size: 18px;
  font-weight: 600;
  color: var(--accent);
}

.action-buttons {
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--surface-page);
  overflow: hidden;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  background: #fff;
  border-bottom: 1px solid var(--border-light);
  font-size: 16px;
  font-weight: 500;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-item {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.chat-msg {
  display: flex;
  flex-direction: column;
  max-width: 80%;
}

.item-left {
  align-items: flex-start;
}

.item-right {
  align-items: flex-end;
  align-self: flex-end;
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
  color: var(--text-primary);
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
  color: var(--text-disabled);
  margin-top: 4px;
  padding: 0 4px;
}

.order-card {
  background: white;
  border-radius: 8px;
  padding: 12px 14px;
  min-width: 280px;
  max-width: 100%;
  border: 1px solid var(--border-light);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.2s;
}

.sent-order-card {
  background: linear-gradient(135deg, var(--brand-50) 0%, var(--neutral-50) 100%) !important;
  border: 1px solid var(--border-brand) !important;
  cursor: default !important;
}

.chat-msg.item-left .sent-order-card .order-card-header {
  background: var(--gradient-brand) !important;
  margin: -12px -14px 8px -14px !important;
  padding: 10px 14px !important;
  border-radius: 8px 8px 0 0 !important;
  border-bottom: none !important;
}

.chat-msg.item-left .sent-order-card .order-card-title {
  color: white !important;
}

.chat-msg.item-left .sent-order-card .el-tag {
  background: rgba(255, 255, 255, 0.9) !important;
  border: none !important;
}

.chat-msg.item-right .sent-order-card {
  background: linear-gradient(135deg, var(--color-success-bg) 0%, var(--brand-50) 100%) !important;
  border-color: #C1E3D3 !important;
}

.chat-msg.item-right .sent-order-card .order-card-header {
  background: linear-gradient(90deg, #2FA36B, #52C08A) !important;
  margin: -12px -14px 8px -14px !important;
  padding: 10px 14px !important;
  border-radius: 8px 8px 0 0 !important;
  border-bottom: none !important;
}

.chat-msg.item-right .sent-order-card .order-card-title {
  color: white !important;
}

.chat-msg.item-right .sent-order-card .el-tag {
  background: rgba(255, 255, 255, 0.9) !important;
  border: none !important;
}

.chat-msg .sent-order-card .order-card-header .el-icon {
  color: white !important;
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
  border-bottom: 1px solid var(--border-light);
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
  background: var(--text-disabled);
  animation: typing 1.4s infinite ease-in-out;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: translateY(0); }
  30% { opacity: 1; transform: translateY(-4px); }
}

.chat-footer {
  padding: 14px 24px;
  background: #fff;
  border-top: 1px solid var(--border-light);
  flex-shrink: 0;
}

.chat-input-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.order-detail-pop {
  padding: 10px 0;
}
</style>