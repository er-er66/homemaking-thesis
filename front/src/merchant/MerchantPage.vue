﻿<template>
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
                {{ displayName(item).charAt(0) }}
              </el-avatar>
              <div class="merchant-info">
                <div class="merchant-top">
                  <span class="merchant-name" :title="displayName(item)">{{ sidebarName(item) }}<span v-if="item.type === 'cs'" class="cs-tag">（客服）</span></span>
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
          <el-avatar :size="36" :src="peerAvatar" shape="square">{{ peerInitial }}</el-avatar>
          <span class="chat-title" :title="displayName(currentMerchant)">{{ displayName(currentMerchant) }}<span v-if="currentMerchant.type === 'cs'" class="cs-tag">（客服）</span></span>
        </div>
        <div class="chat-body">
          <div class="chat-messages" ref="msgBox">
            <template v-for="(msg, idx) in chatMessages" :key="idx">
              <div v-if="msg.msgType === 'order_detail'" :class="['chat-msg', msg.from === 'me' ? 'msg-right' : 'msg-left']">
                <div class="msg-avatar" v-if="msg.from !== 'me'">
                  <el-avatar :size="36" :src="peerAvatar" shape="square">{{ peerInitial }}</el-avatar>
                </div>
                <div class="msg-content">
                  <div class="order-card" :style="msg.from === 'me' ? cardStyleMerchantRight : cardStyleMerchantLeft">
                    <div class="order-card-header" :style="msg.from === 'me' ? headerStyleMerchantRight : headerStyleMerchantLeft">
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
                <div class="msg-avatar" v-if="msg.from === 'me'">
                  <el-avatar :size="36" :src="myAvatar" shape="square">{{ myInitial }}</el-avatar>
                </div>
              </div>
              <div v-else-if="msg.type === 'order'" class="chat-msg msg-left">
                <div class="msg-avatar">
                  <el-avatar :size="36" :src="peerAvatar" shape="square">{{ peerInitial }}</el-avatar>
                </div>
                <div class="msg-content">
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
              </div>
              <div
                v-else
                :class="['chat-msg', msg.from === 'me' ? 'msg-right' : 'msg-left']"
              >
                <div class="msg-avatar" v-if="msg.from !== 'me'">
                  <el-avatar :size="36" :src="peerAvatar" shape="square">{{ peerInitial }}</el-avatar>
                </div>
                <div class="msg-content">
                  <div class="msg-bubble">{{ msg.text }}</div>
                  <div class="msg-status-row">
                    <span class="msg-time">{{ msg.time }}</span>
                    <span v-if="msg.from === 'merchant'" class="msg-read-status">
                      {{ msg.isRead ? '已读' : '未读' }}
                    </span>
                  </div>
                </div>
                <div class="msg-avatar" v-if="msg.from === 'me'">
                  <el-avatar :size="36" :src="myAvatar" shape="square">{{ myInitial }}</el-avatar>
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
import { getMerchants, clearUnread, updateLastMsg, recvMsgFromMerchant, setMerchants, addMerchant, findMerchant } from '../store/merchantStore'
import { getOrderDetailApi, takeOrderApi, getChatHistoryApi, markChatReadApi, getChatRoomListApi } from '../api/admin'
import { parseOrderDetailPayload, resolveOrderDetail } from '../utils/chatMessage'
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const currentMerchant = ref({})
const currentRoomId = ref('')
const newMsg = ref('')
const chatMessages = ref([])
const processedMessageIds = new Set()
const MAX_PROCESSED_IDS = 200

// 聊天头像：自己（右侧）取登录信息，对方（左侧）取当前会话对象
const myAvatar = ref('')
const myName = ref('')
const myInitial = computed(() => (myName.value || '').charAt(0) || '我')
const peerAvatar = computed(() => currentMerchant.value.avatar || '')
const peerInitial = computed(() => (currentMerchant.value.name || '').charAt(0) || '商')

const cardStyleMerchantLeft = {
  background: 'linear-gradient(135deg, var(--brand-50) 0%, var(--neutral-50) 100%)',
  border: '1px solid var(--border-brand)',
  borderRadius: '12px',
  padding: '0',
  minWidth: '300px',
  maxWidth: '90%'
}

const cardStyleMerchantRight = {
  background: 'linear-gradient(135deg, var(--color-success-bg) 0%, var(--brand-50) 100%)',
  border: '1px solid #C1E3D3',
  borderRadius: '12px',
  padding: '0',
  minWidth: '300px',
  maxWidth: '90%'
}

const headerStyleMerchantLeft = {
  background: 'var(--gradient-brand)',
  padding: '10px 12px',
  borderRadius: '12px 12px 0 0',
  display: 'flex',
  alignItems: 'center',
  gap: '8px',
  margin: '0'
}

const headerStyleMerchantRight = {
  background: 'linear-gradient(90deg, #2FA36B, #52C08A)',
  padding: '10px 12px',
  borderRadius: '12px 12px 0 0',
  display: 'flex',
  alignItems: 'center',
  gap: '8px',
  margin: '0'
}
const msgBox = ref(null)
let ws = null

const showOrderDialog = ref(false)
const selectedOrder = ref(null)
const takingOrder = ref(false)

const merchantList = computed(() => getMerchants())

/**
 * 会话名称统一展示成「名称（账号）」，名称缺失或与账号相同时只显示账号。
 * 管理员从「消息列表」点进来时只有账号可靠，这样至少能认出是谁。
 */
const displayName = (merchant) => {
  if (!merchant) return ''
  const account = String(merchant.account || '').trim()
  const name = String(merchant.name || '').trim()
  if (!name || name === account) return account || name || '未知会话'
  return account ? `${name}（${account}）` : name
}

// 侧栏的一行：去掉「（客服）」后缀，避免和「（账号）」两个括号堆在一起
const sidebarName = (merchant) => displayName(merchant).replace(/（客服）/g, '')

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
      Promise.resolve(handleIncomingMessage(data)).catch(e => {
        console.error('处理消息失败:', e)
      })
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

// 消息去重：生成唯一标识符并检查是否已处理过
const isMessageDuplicate = (msgKey) => {
  if (processedMessageIds.has(msgKey)) {
    console.log('[MerchantPage] 检测到重复消息，已忽略:', msgKey)
    return true
  }
  processedMessageIds.add(msgKey)
  if (processedMessageIds.size > MAX_PROCESSED_IDS) {
    const iterator = processedMessageIds.values()
    const first = iterator.next().value
    processedMessageIds.delete(first)
  }
  return false
}

// 解析订单详情消息体统一走公共工具（见 src/utils/chatMessage.js），
// 避免各处聊天页各写一套解析逻辑、漏掉前缀判断就退化成 JSON 文本

let refreshTimer = null

// 会话列表以后端为准，用于拿到对方真实昵称/头像和未读数
const refreshMerchantList = async () => {
  const myId = getMyId()
  if (!myId) return
  try {
    const res = await getChatRoomListApi(myId)
    if (res && Array.isArray(res.data)) {
      setMerchants(res.data)
      const currentKey = currentMerchant.value.roomId || currentMerchant.value.id
      const mine = findMerchant({ id: currentKey, roomId: currentKey, account: currentMerchant.value.account })
      if (mine) currentMerchant.value = mine
    }
  } catch (e) {
    console.warn('[MerchantPage] 刷新会话列表失败:', e)
  }
}

// 新会话刚创建时后端可能还没写完，稍后再刷新一次列表
const scheduleMerchantListRefresh = (delay = 800) => {
  if (refreshTimer) return
  refreshTimer = setTimeout(() => {
    refreshTimer = null
    refreshMerchantList()
  }, delay)
}

// 后端房间号约定是 `用户账号_家政人员/管理员账号`。
// 已经有 roomId 就直接复用；只有对方账号时按自己的角色拼接，
// 否则同一段对话两侧会拼出顺序相反的 roomId，后端就会当成两个会话。
const buildRoomId = (merchant, myId) => {
  if (!merchant) return ''
  if (merchant.roomId) return merchant.roomId
  const peer = String(merchant.id || '')
  if (!peer) return ''
  if (peer.includes('_')) return peer
  return isStaff.value
    ? [peer, myId].filter(Boolean).join('_')
    : [myId, peer].filter(Boolean).join('_')
}

// 只有对方账号时，先在后端会话列表里找已存在的房间，避免房间号顺序不同建出重复会话
const resolveRoomId = async (merchant, myId) => {
  const fallback = buildRoomId(merchant, myId)
  if (!merchant || merchant.roomId || String(merchant.id || '').includes('_')) {
    return fallback
  }
  try {
    const res = await getChatRoomListApi(myId)
    const rooms = res && Array.isArray(res.data) ? res.data : []
    const room = rooms.find(r =>
      r.roomId === merchant.id ||
      r.account === merchant.id ||
      (merchant.account && r.account === merchant.account)
    )
    if (room && room.roomId) {
      merchant.roomId = room.roomId
      if (!merchant.account) merchant.account = room.account || ''
      if (room.name && (!merchant.name || merchant.name === merchant.id)) merchant.name = room.name
      return room.roomId
    }
  } catch (e) {
    console.warn('[MerchantPage] 获取会话房间失败:', e)
  }
  return fallback
}

// 当前会话必须在左侧列表里有对应条目，否则消息只出现在聊天区、列表和会话对不上
const ensureCurrentInList = async (roomId) => {
  const current = currentMerchant.value
  if (!current || (!current.id && !current.account)) return null

  const key = roomId || current.roomId || (String(current.id || '').includes('_') ? current.id : '')
  const existing = findMerchant({ id: key, roomId: key, account: current.account || current.id })
  if (existing) {
    currentMerchant.value = existing
    return existing
  }
  // 还没有真实房间号时不占位，等收到带 roomId 的消息或后端会话列表返回后再展示
  if (!key || !key.includes('_')) return null

  const created = addMerchant({
    id: key,
    roomId: key,
    account: current.account || '',
    name: current.name,
    avatar: current.avatar
  })
  if (created) currentMerchant.value = created
  scheduleMerchantListRefresh()
  return created
}

const handleIncomingMessage = async (data) => {
  const myId = getMyId()
  const from = data.from || data.senderId || data.sender || ''
  const peerAccount = data.merchantId || data.to || from
  const incomingRoomId = data.roomId || data.room_id || ''
  const type = data.type || (data.msgType === 'order_detail' ? 'order_detail' : 'message')
  const message = data.message || data.text || data.content || ''
  const orderData = data.orderData
  const merchantName = data.merchantName || data.name || data.senderName || ''
  const merchantAvatar = data.merchantAvatar || data.avatar || ''

  console.log('[MerchantPage] 收到完整消息数据:', JSON.stringify(data))
  console.log('[MerchantPage] 解析后的消息:', { type, from, peerAccount, roomId: incomingRoomId, myId })

  // 忽略自己发送的消息（避免重复显示）
  if (from && from === myId) {
    console.log('[MerchantPage] 忽略自己发送的消息')
    return
  }

  if (!peerAccount && !incomingRoomId) {
    console.warn('[MerchantPage] 消息缺少发送者信息，尝试使用备用字段:', data)
    // 尝试从其他可能的字段获取标识
    const fallbackId = data.userId || data.user_id || data.account || ''
    if (!fallbackId) {
      console.error('[MerchantPage] 无法确定消息发送者，忽略消息:', data)
      return
    }
    // 动态更新 peerAccount
    data._fallbackPeerAccount = fallbackId
  }

  // 使用 fallbackId 如果原始 peerAccount 为空
  const effectivePeerAccount = peerAccount || data._fallbackPeerAccount || ''
  
  // 后端推送的 merchantId 是发送者 account，会话列表里的 id 却是 roomId，
  // 这里统一按 roomId / id / account 三种键定位，找不到才新增，避免重复建会话
  const existing = findMerchant({ id: incomingRoomId, roomId: incomingRoomId, account: effectivePeerAccount })
  const resolvedRoomId = existing ? buildRoomId(existing, myId) : incomingRoomId
  console.log('[MerchantPage] 会话匹配:', {
    existingId: existing && existing.id,
    resolvedRoomId,
    currentId: currentMerchant.value.id,
    currentRoomId: currentRoomId.value,
    effectivePeerAccount
  })

  const current = currentMerchant.value || {}
  const isCurrent = Boolean(current.id || current.account) && (
    (resolvedRoomId && currentRoomId.value && resolvedRoomId === currentRoomId.value) ||
    (incomingRoomId && (current.id === incomingRoomId || current.roomId === incomingRoomId)) ||
    (effectivePeerAccount && (current.account === effectivePeerAccount || current.id === effectivePeerAccount))
  )

  if (!isCurrent) {
    // 不是当前聊天：只更新左侧列表（命中已有会话就更新，没有才新增），不切换聊天
    console.log('[MerchantPage] 不是当前聊天会话，更新列表, effectivePeerAccount:', effectivePeerAccount)
    const listText = type === 'order_detail'
      ? '订单详情'
      : type === 'order'
        ? '[订单] ' + ((orderData && orderData.serviceItem) || '新订单')
        : message
    
    const addResult = recvMsgFromMerchant({
      merchantId: effectivePeerAccount,
      merchantName: merchantName || effectivePeerAccount,
      merchantAvatar,
      text: listText,
      roomId: incomingRoomId
    })
    
    console.log('[MerchantPage] recvMsgFromMerchant 返回结果:', addResult)
    console.log('[MerchantPage] 当前 merchantList 长度:', merchantList.value.length)
    
    if (!existing && !addResult) {
      console.warn('[MerchantPage] 会话添加失败，尝试强制添加')
      // 强制添加：如果正常流程失败，直接调用 addMerchant
      const forcedMerchant = addMerchant({
        id: incomingRoomId || effectivePeerAccount,
        roomId: incomingRoomId,
        account: effectivePeerAccount,
        name: merchantName || effectivePeerAccount || '未知用户',
        avatar: merchantAvatar,
        lastMsg: listText,
        time: new Date().toTimeString().slice(0, 5),
        unread: 1
      })
      console.log('[MerchantPage] 强制添加结果:', forcedMerchant)
    }
    
    if (!existing) {
      scheduleMerchantListRefresh()
    }
    return
  }

  await ensureCurrentInList(incomingRoomId)

  const time = formatTime(new Date())
  const msgKey = `${type}:${effectivePeerAccount}:${message || JSON.stringify(orderData || '')}:${time}`
  
  if (isMessageDuplicate(msgKey)) {
    return
  }
  
  if (type === 'order_detail') {
    chatMessages.value.push({
      from: 'merchant',
      msgType: 'order_detail',
      orderData: parseOrderDetailPayload(message) || orderData || {},
      text: message,
      time
    })
    updateLastMsg(incomingRoomId || effectivePeerAccount, '订单详情', time)
  } else if (type === 'order' && orderData) {
    chatMessages.value.push({ from: 'merchant', type: 'order', orderData, time })
    updateLastMsg(incomingRoomId || effectivePeerAccount, '[订单] ' + (orderData.serviceItem || '新订单'), time)
  } else if (message) {
    chatMessages.value.push({ from: 'merchant', text: message, time, isRead: true })
    updateLastMsg(incomingRoomId || effectivePeerAccount, message, time)
  } else {
    console.log('[MerchantPage] 消息条件不满足:', data)
    return
  }

  nextTick(scrollToBottom)

  const room = currentRoomId.value || resolvedRoomId
  if (room) {
    markChatReadApi({ room_id: room, userId: myId }).catch(() => {})
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
      from: staffAccount,
      to: currentMerchant.value.account || currentMerchant.value.id,
      merchantId: staffAccount,
      roomId: currentRoomId.value || undefined,
      message: `商家已接单，订单号：${selectedOrder.value.orderNo || selectedOrder.value.id}`,
      text: `商家已接单，订单号：${selectedOrder.value.orderNo || selectedOrder.value.id}`
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
      // 自己的头像/昵称用于聊天里右侧气泡
      myAvatar.value = info.avatar || ''
      myName.value = info.username || info.account || ''
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
      // targetMerchantId 是「对方账号」，匹配要按 account 优先。
      // 不能只比 id：脏数据里可能存在 room_id 恰好等于该账号、但 account/name 全空的条目
      // （如 room_id='10241065' 而不是 '10241065_admin'），先比 id 会命中空条目 ⇒ 标题退化成「商家」。
      const list = merchantList.value
      const targetMerchant =
        list.find(m => m.account === targetMerchantId) ||
        list.find(m => m.id === targetMerchantId || m.roomId === targetMerchantId)

      console.log('[MerchantPage] targetMerchant:', targetMerchant)

      if (targetMerchant) {
        await openChat(targetMerchant)
        // 命中的条目本身可能没有名称（后端会话记录不全），再补一次
        await enrichMerchantName(targetMerchant)
      } else {
        // 商家不在本地列表（常见于从未聊过、后端还没建会话）：用账号占位，
        // 再按账号去后端会话记录里补真实姓名，否则标题只能显示一串账号
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
        await enrichMerchantName(tempMerchant)
        await ensureCurrentInList()
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
  if (refreshTimer) {
    clearTimeout(refreshTimer)
    refreshTimer = null
  }
  if (ws) {
    ws.close(1000, '页面离开')
  }
})

/**
 * 会话只有账号、没有真实姓名时，去本地列表和后端会话记录里补一次。
 * 补到就更新 currentMerchant，标题立刻从「账号」变成「姓名（账号）」。
 */
/** 名称有效：非空、不等于账号、也不是「商家」这种兜底值 */
const hasRealName = (name, account) => {
  const n = String(name || '').trim()
  if (!n) return false
  if (n === '商家') return false
  if (account && n === String(account)) return false
  return true
}

const enrichMerchantName = async (merchant) => {
  if (!merchant) return
  const account = merchant.account || merchant.id
  if (!account) return
  if (hasRealName(merchant.name, account)) return

  try {
    const myId = getMyId()
    const res = await getChatRoomListApi(myId)
    const rooms = res && Array.isArray(res.data) ? res.data : []

    // 一个账号可能对应多条会话记录（历史脏数据：room_id 就是账号本身）。
    // 必须挑出**真正有名称**的那条，否则补全又补了个空。
    const candidates = rooms.filter(r =>
      r.account === account ||
      r.roomId === merchant.id ||
      r.roomId === merchant.roomId ||
      // 兜底：room_id 形如「账号_对方账号」，也能认出来
      String(r.roomId || '').split('_').includes(String(account))
    )
    const room = candidates.find(r => hasRealName(r.name, account)) || candidates[0]

    if (room) {
      if (hasRealName(room.name, account)) merchant.name = room.name
      if (!merchant.avatar && room.avatar) merchant.avatar = room.avatar
      if (!merchant.roomId && room.roomId) merchant.roomId = room.roomId
    }

    // 回填到本地 store，侧栏那一行也会跟着更新
    const local = findMerchant({ id: merchant.id, roomId: merchant.roomId, account })
    if (local && hasRealName(merchant.name, account) && !hasRealName(local.name, account)) {
      local.name = merchant.name
      if (!local.avatar) local.avatar = merchant.avatar || ''
      if (!local.account) local.account = account
    }

    if (currentMerchant.value.id === merchant.id) {
      currentMerchant.value = { ...currentMerchant.value, ...merchant }
    }
  } catch (e) {
    console.warn('[MerchantPage] 补全会话名称失败:', e)
  }
}

const openChat = async (merchant) => {
  if (!merchant || !merchant.id) {
    ElMessage.error('商家信息不完整')
    return
  }

  const myId = getMyId()
  // 会话房间号：优先复用后端会话列表里的 roomId，只有对方账号时先去后端找已有房间
  const roomId = await resolveRoomId(merchant, myId)

  currentMerchant.value = merchant
  currentRoomId.value = roomId
  clearUnread(merchant.id)

  chatMessages.value = []
  if (roomId) {
    try {
      const res = await getChatHistoryApi({
        room_id: roomId
      })
      if (res && res.data && Array.isArray(res.data)) {
        res.data.forEach(msg => {
          const isMe = String(msg.senderId) === String(myId)
          const msgType = msg.msgType || 'text'
          // 订单详情要按卡片渲染：优先看 msg_type，早期只有文本、没有 msg_type 的记录
          // 靠 "Xiangqing" 前缀兜底识别，否则会显示成一串 JSON
          const orderData = resolveOrderDetail(msgType, msg.content)
          chatMessages.value.push({
            from: isMe ? 'me' : 'merchant',
            type: orderData ? 'order_detail' : msgType,
            msgType: orderData ? 'order_detail' : 'text',
            text: orderData ? '' : msg.content,
            orderData: orderData || msg.orderData,
            time: formatTime(new Date(msg.createdAt)),
            isRead: msg.isRead === true || msg.isRead === 1 || msg.isRead === '1'
          })
        })
      }
    } catch {
      console.warn('获取历史消息失败')
    }

    // 使用正确的 roomId 标记已读
    markChatReadApi({ room_id: roomId, userId: myId }).catch(() => {})
  }

  nextTick(scrollToBottom)

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
  if (!text || !currentMerchant.value.id) return

  const myId = getMyId()
  const roomId = currentRoomId.value || await resolveRoomId(currentMerchant.value, myId)
  currentRoomId.value = roomId

  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  chatMessages.value.push({ from: 'me', text, time })
  updateLastMsg(currentMerchant.value.id, text, time)
  newMsg.value = ''
  nextTick(scrollToBottom)

  // 只通过 WebSocket 发送消息，让后端处理保存逻辑（避免重复插入 DB）
  const success = sendMessage({
    type: 'message',
    from: myId,           // 发送者 account
    to: currentMerchant.value.account || currentMerchant.value.id,  // 接收者 account（不是 roomId）
    merchantId: myId,     // 发送者 account
    message: text,
    text: text,
    roomId: roomId
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
  background: var(--surface-page);
}

.merchant-header {
  flex-shrink: 0;
  background: white;
  padding: 12px 20px;
  border-bottom: 1px solid var(--border-light);
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
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  padding: 16px 20px 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-h);
  border-bottom: 1px solid var(--border-light);
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
  border-bottom: 1px solid var(--neutral-100);
}

.merchant-item:hover {
  background: var(--surface-page);
}

.merchant-item.active {
  background: var(--brand-50);
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
  /* 名称+账号可能较长，允许收缩并省略，别把右侧时间挤出容器 */
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  background: var(--surface-page);
}

.chat-header {
  padding: 14px 20px;
  background: white;
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 10px;
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
  /* 横向排：头像 + 内容列。不要用 row-reverse —— 模板里「自己发的」头像
     本来就写在内容之后，反转后头像会跑到左边。 */
  flex-direction: row;
  align-items: flex-start;
  gap: 10px;
}

.msg-left {
  justify-content: flex-start;
}

.msg-right {
  justify-content: flex-end;
}

.msg-avatar {
  flex-shrink: 0;
}

.msg-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
  min-width: 0;
}

.msg-left .msg-content {
  align-items: flex-start;
}

.msg-right .msg-content {
  align-items: flex-end;
}

.msg-bubble {
  max-width: 100%;
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
  color: var(--text-secondary);
}

.msg-left .msg-read-status {
  color: var(--text-secondary);
}

.msg-right .msg-read-status {
  display: none;
}
.chat-input-row {
  display: flex;
  gap: 10px;
  padding: 14px 20px;
  background: white;
  border-top: 1px solid var(--border-light);
  flex-shrink: 0;
}

.merchant-chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-page);
}

.order-card {
  background: white;
  border-radius: 8px;
  padding: 12px 14px;
  min-width: 280px;
  max-width: 80%;
  border: 1px solid var(--border-light);
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
  border-top: 1px solid var(--border-light);
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
  border: 2px solid var(--border-light);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.cs-item:hover {
  border-color: var(--brand-200);
  background: var(--color-danger-bg);
}

.cs-item.active {
  border-color: var(--accent);
  background: var(--color-danger-bg);
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.12);
}

.cs-info {
  flex: 1;
  min-width: 0;
}

.cs-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.cs-account {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}
</style>