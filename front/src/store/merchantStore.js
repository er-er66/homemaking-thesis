import { reactive } from 'vue'
import { messagePreview } from '../utils/chatMessage'

const merchantStore = reactive({
  merchants: [],
  initialized: false
})

export const getMerchants = () => merchantStore.merchants

const firstNonEmpty = (...values) => {
  for (const value of values) {
    if (value !== undefined && value !== null && value !== '') return value
  }
  return ''
}

const nowTime = () => {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
}

/**
 * 把后端会话对象（/chat/rooms）或本地临时对象归一成统一的会话结构。
 *
 * 关键点：会话的 id 优先使用 roomId。后端会话列表返回的是 roomId（如 `用户账号_对方账号`），
 * 而 WebSocket 推送过来的 merchantId 其实是发送者 account，两者如果不归一，
 * 同一个人就会被当成两个会话，左侧商家列表就会凭空多出一条记录。
 */
export const normalizeMerchant = (item) => {
  if (typeof item === 'string') {
    return {
      id: item,
      roomId: '',
      account: item,
      name: item,
      avatar: '',
      lastMsg: '',
      time: '',
      unread: 0,
      type: ''
    }
  }

  const source = item || {}
  const roomId = firstNonEmpty(source.roomId, source.room_id)
  let account = firstNonEmpty(source.account, source.merchantAccount, source.merchant_account)

  // 后端脏数据：room_id 就是对方账号（缺了 `账号_对方账号` 的后半段），此时 account 为空。
  // 从 roomId 反推账号，否则这条会话既没名称也没账号，只能显示成「商家」。
  if (!account && roomId && !roomId.includes('_')) {
    account = roomId
  }
  // 形如 `a_b` 的 roomId 取非自己的那一半（自己是谁由调用方保证不了，这里只取一个候选）
  if (!account && roomId && roomId.includes('_')) {
    account = roomId.split('_')[0]
  }

  const name = firstNonEmpty(
    source.name,
    source.merchantName,
    source.merchant_name,
    source.roomName,
    source.room_name,
    account
  )

  return {
    id: firstNonEmpty(source.id, roomId, account, source.merchantId, source.merchant_id),
    roomId,
    account,
    // 没有真名时用账号兜底，别落成「商家」——前端 displayName 会渲染成「账号」
    name: name || roomId || '商家',
    avatar: firstNonEmpty(source.avatar, source.merchantAvatar, source.merchant_avatar),
    // 会话列表只显示摘要：订单详情消息的原文是 "Xiangqing{...}"，
    // 直接塞进列表会是一大串 JSON，这里统一转成「订单详情」
    lastMsg: messagePreview(firstNonEmpty(source.lastMsg, source.last_msg)),
    time: firstNonEmpty(source.time, source.lastTime, source.last_time, source.lastMsgTime, source.last_msg_time),
    unread: source.unread || source.unreadCount || source.unread_count || 0,
    type: firstNonEmpty(source.type, source.roomType, source.room_type)
  }
}

/**
 * 标准化 roomId：将 "a_b" 和 "b_a" 统一为排序后的格式
 */
const normalizeRoomId = (roomId) => {
  if (!roomId || !roomId.includes('_')) return roomId
  return roomId.split('_').sort().join('_')
}

/**
 * 兼容 roomId / id / account 三种写法查找同一个会话。
 * 先用 roomId、id 精确匹配（包括标准化后的 roomId），匹配不到再用对方账号匹配，避免重复创建会话条目。
 */
export const findMerchant = ({ id, roomId, account } = {}) => {
  const strongKeys = [id, roomId].filter(value => value !== undefined && value !== null && value !== '')
  
  if (strongKeys.length > 0) {
    // 先尝试精确匹配
    const hit = merchantStore.merchants.find(m =>
      strongKeys.includes(m.id) || (m.roomId && strongKeys.includes(m.roomId))
    )
    if (hit) return hit
    
    // 再尝试标准化 roomId 匹配（处理 "a_b" vs "b_a" 的情况）
    const normalizedKeys = strongKeys.map(k => normalizeRoomId(k)).filter(k => k)
    if (normalizedKeys.length > 0) {
      const normalizedHit = merchantStore.merchants.find(m => {
        const normalizedId = normalizeRoomId(m.id)
        const normalizedRoomId = normalizeRoomId(m.roomId)
        return normalizedKeys.includes(normalizedId) || 
               (normalizedRoomId && normalizedKeys.includes(normalizedRoomId))
      })
      if (normalizedHit) {
        console.log('[merchantStore] findMerchant 通过标准化匹配找到:', { 
          input: { id, roomId, account }, 
          found: { id: normalizedHit.id, accountId: normalizedHit.account, roomId: normalizedHit.roomId }
        })
        return normalizedHit
      }
    }
  }

  if (account) {
    return merchantStore.merchants.find(m => m.account === account || m.id === account) || null
  }

  return null
}

const mergeMerchant = (target, incoming) => {
  if (!target || !incoming) return target
  if (incoming.roomId && !target.roomId) target.roomId = incoming.roomId
  if (incoming.account && !target.account) target.account = incoming.account
  if (incoming.avatar && !target.avatar) target.avatar = incoming.avatar
  const targetNameIsFallback =
    !target.name || target.name === '商家' || target.name === target.account || target.name === target.id
  if (incoming.name && targetNameIsFallback && incoming.name !== incoming.account) {
    target.name = incoming.name
  }
  return target
}

export const setMerchants = (list) => {
  if (!Array.isArray(list)) return
  
  const normalizedList = list
    .map(item => normalizeMerchant(item))
    .filter(merchant => merchant.id)
  
  // 同一账号可能对应多条后端记录（脏数据：room_id 就是账号本身，且没有 room_id 的另一条才是真会话）。
  // 去重时不能简单「保留第一条」——第一条往往是没名字没头像的那条，
  // 应该保留信息更全的一条（有真名 > 有头像 > 有 roomId「账号_对方」格式）。
  const scoreOf = (m) => {
    let score = 0
    if (m.name && m.name !== '商家' && m.name !== m.account) score += 4
    if (m.avatar) score += 2
    // `账号_对方账号` 才是真实房间号，`账号` 单独一个词是脏数据
    if (m.roomId && m.roomId.includes('_')) score += 1
    return score
  }

  const byKey = new Map()
  const order = []

  for (const merchant of normalizedList) {
    // 生成唯一标识：优先用 account，其次用 id/roomId
    let key = merchant.account || merchant.id || merchant.roomId || ''
    // 标准化 roomId 格式（处理 "a_b" 和 "b_a" 视为相同的情况）
    if (key.includes('_')) {
      key = key.split('_').sort().join('_')
    }
    if (!key) {
      console.log('[merchantStore] setMerchants 丢弃无标识项:', merchant)
      continue
    }

    const existed = byKey.get(key)
    if (!existed) {
      byKey.set(key, merchant)
      order.push(key)
      continue
    }

    // 已有同 key 记录：留下信息更全的那条，并把另一条的字段并过去
    const winner = scoreOf(merchant) > scoreOf(existed) ? merchant : existed
    const loser = winner === merchant ? existed : merchant
    if (!winner.avatar && loser.avatar) winner.avatar = loser.avatar
    if (!winner.roomId && loser.roomId) winner.roomId = loser.roomId
    if (!winner.account && loser.account) winner.account = loser.account
    if (!winner.lastMsg && loser.lastMsg) winner.lastMsg = loser.lastMsg
    console.log('[merchantStore] setMerchants 合并重复:', {
      key,
      kept: { name: winner.name, account: winner.account, roomId: winner.roomId, score: scoreOf(winner) },
      dropped: { name: loser.name, account: loser.account, roomId: loser.roomId, score: scoreOf(loser) }
    })
    byKey.set(key, winner)
  }

  const deduplicated = order.map(k => byKey.get(k))
  
  console.log('[merchantStore] setMerchants: 原始', normalizedList.length, '条，去重后', deduplicated.length, '条')
  
  merchantStore.merchants = deduplicated
  merchantStore.initialized = true
}

export const getTotalUnread = () => {
  return merchantStore.merchants.reduce((sum, m) => sum + (m.unread || 0), 0)
}

export const addMerchant = (merchant) => {
  const normalized = normalizeMerchant(merchant)
  if (!normalized.id) return null

  const existing = findMerchant({
    id: normalized.id,
    roomId: normalized.roomId,
    account: normalized.account
  })
  if (existing) {
    mergeMerchant(existing, normalized)
    if (!existing.roomId && normalized.roomId) existing.roomId = normalized.roomId
    return existing
  }

  merchantStore.merchants.unshift(normalized)
  return normalized
}

/**
 * 收到对方消息时更新左侧会话列表。
 *
 * roomId 存在时以 roomId 作为会话 id（与 /chat/rooms 返回的会话一一对应），
 * 只有对方账号时退化成账号，并且先按 roomId/id/account 三种键查找已有会话，
 * 找到就更新，找不到才新增——不会每来一条消息就多出一个"商家"。
 */
export const recvMsgFromMerchant = ({ merchantId, merchantName, merchantAvatar, text, roomId }) => {
  const incoming = normalizeMerchant({
    roomId,
    account: merchantId,
    name: merchantName,
    avatar: merchantAvatar
  })
  if (!incoming.id) {
    incoming.id = firstNonEmpty(roomId, merchantId)
  }
  if (!incoming.id) return null

  let merchant = findMerchant({ id: roomId, roomId, account: merchantId })
  if (merchant) {
    mergeMerchant(merchant, incoming)
  } else {
    incoming.name = firstNonEmpty(merchantName, incoming.name, merchantId, '商家')
    merchant = addMerchant(incoming)
  }
  if (!merchant) return null

  merchant.lastMsg = messagePreview(text)
  merchant.time = nowTime()
  merchant.unread = (merchant.unread || 0) + 1
  return merchant
}

export const initUnreadFromApi = (unreadList) => {
  if (!unreadList || !Array.isArray(unreadList)) return

  unreadList.forEach(item => {
    const merchantId = item.merchantId || item.merchant_id
    const roomId = item.roomId || item.room_id
    let merchant = findMerchant({ id: roomId, roomId, account: merchantId })
    if (!merchant) {
      merchant = addMerchant({
        roomId,
        account: merchantId,
        name: item.merchantName || item.merchant_name,
        avatar: item.merchantAvatar || item.merchant_avatar
      })
    }
    if (merchant) {
      merchant.unread = item.unreadCount || item.unread_count || 0
      if (!merchant.lastMsg) {
        merchant.lastMsg = messagePreview(item.lastMsg || item.last_msg)
      }
      if (!merchant.time) {
        merchant.time = item.time || item.last_time || ''
      }
    }
  })
}

export const clearUnread = (merchantId) => {
  const merchant = findMerchant({ id: merchantId, account: merchantId })
  if (merchant) {
    merchant.unread = 0
  }
}

export const updateLastMsg = (merchantId, text, time) => {
  const merchant = findMerchant({ id: merchantId, account: merchantId })
  if (merchant) {
    merchant.lastMsg = text
    merchant.time = time
  }
}

export default merchantStore