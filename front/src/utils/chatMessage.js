/**
 * 聊天消息统一处理
 *
 * 背景：订单详情卡片在前后端之间是一段约定好的文本 —— `Xiangqing` 前缀 + JSON。
 * 后端只按字符串存 content，是否渲染成卡片完全靠前端判断 msgType / 前缀。
 * 任何一处聊天页面漏了这层判断，消息就会退化成一长串 JSON 文本甩在气泡里，
 * 所以统一收敛到这里，不要在页面里各写一套。
 */

/** 订单详情消息的内容前缀（历史约定，勿改，改了旧数据就认不出来） */
export const ORDER_DETAIL_PREFIX = 'Xiangqing'

/** 会话列表里订单详情消息显示成什么样 */
export const ORDER_DETAIL_PREVIEW = '订单详情'

/** 把订单数据序列化成可发送/可入库的消息内容 */
export function buildOrderDetailContent(orderData) {
  return ORDER_DETAIL_PREFIX + JSON.stringify(orderData)
}

/**
 * 解析订单详情消息体
 * 兼容三种历史形态：带前缀的 JSON、纯 JSON、已经是对象
 * @returns {object|null} 解析不出来就返回 null（调用方据此回退成普通文本）
 */
export function parseOrderDetailPayload(raw) {
  if (!raw) return null
  let value = raw
  if (typeof value === 'string') {
    if (value.startsWith(ORDER_DETAIL_PREFIX)) {
      value = value.substring(ORDER_DETAIL_PREFIX.length)
    }
    try {
      value = JSON.parse(value)
    } catch {
      return null
    }
  }
  return value && typeof value === 'object' ? value : null
}

/**
 * 判断一条消息是不是订单详情卡片
 * 三重判断：msgType / type 字段，以及内容前缀兜底（后端不会返回 msgType，靠前缀认）
 * @returns {object|null} 是订单详情则返回订单数据，否则 null
 */
export function resolveOrderDetail(msgType, content, type) {
  if (msgType === 'order_detail' || type === 'order_detail') {
    return parseOrderDetailPayload(content)
  }
  if (typeof content === 'string' && content.startsWith(ORDER_DETAIL_PREFIX)) {
    return parseOrderDetailPayload(content)
  }
  return null
}

/** 会话列表摘要：订单详情不显示 JSON，显示「订单详情」 */
export function messagePreview(content, msgType) {
  return resolveOrderDetail(msgType, content) ? ORDER_DETAIL_PREVIEW : (content || '')
}

/** 把任意消息（后端历史记录 / 本地缓存 / WebSocket 推送）整形成渲染结构 */
export function normalizeChatMessage(msg, selfAccount) {
  const content = msg.content ?? msg.text ?? ''
  const orderData = resolveOrderDetail(msg.msgType, content, msg.type)
  const sender = msg.senderId ?? msg.sender_id ?? msg.from ?? ''
  const from = msg.from || (selfAccount && String(sender) === String(selfAccount) ? 'me' : 'user')
  return {
    from,
    type: orderData ? 'order_detail' : (msg.type || 'text'),
    msgType: orderData ? 'order_detail' : (msg.msgType || 'text'),
    // 订单详情不用文本渲染，避免任何分支把它当普通消息吐出来
    text: orderData ? '' : content,
    orderData,
    time: msg.time || formatClock(msg.createdAt || msg.created_at)
  }
}

/** HH:mm，聊天气泡上的时间 */
export function formatClock(date) {
  const d = date instanceof Date ? date : new Date(date)
  if (!date || isNaN(d.getTime())) return '--:--'
  const h = d.getHours().toString().padStart(2, '0')
  const m = d.getMinutes().toString().padStart(2, '0')
  return `${h}:${m}`
}

/**
 * 会话列表里的时间：后端给的是 ISO 时间串（2026-09-12T15:16:13），
 * 直接渲染会又长又难看；本地缓存里存的是已格式化好的 HH:mm。
 */
export function formatListTime(value) {
  if (!value) return ''
  const raw = String(value)
  if (/^\d{1,2}:\d{2}$/.test(raw)) return raw
  const d = new Date(raw)
  if (isNaN(d.getTime())) return raw
  const pad = (n) => n.toString().padStart(2, '0')
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
