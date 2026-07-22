import { reactive } from 'vue'

const merchantStore = reactive({
  merchants: [],
  initialized: false
})

export const getMerchants = () => merchantStore.merchants

export const setMerchants = (list) => {
  merchantStore.merchants = list.map(item => ({
    id: item.id || item.merchantId || item.merchant_id,
    name: item.name || item.merchantName || item.merchant_name || '商家',
    avatar: item.avatar || item.merchantAvatar || item.merchant_avatar || '',
    lastMsg: item.lastMsg || item.last_msg || '',
    time: item.time || item.last_time || '',
    unread: item.unread || item.unreadCount || item.unread_count || 0
  }))
  merchantStore.initialized = true
}

export const getTotalUnread = () => {
  return merchantStore.merchants.reduce((sum, m) => sum + (m.unread || 0), 0)
}

export const addMerchant = (merchant) => {
  const exists = merchantStore.merchants.find(m => m.id === merchant.id)
  if (!exists) {
    merchantStore.merchants.push({
      id: merchant.id,
      name: merchant.name || '商家',
      avatar: merchant.avatar || '',
      lastMsg: '',
      time: '',
      unread: 0
    })
  }
}

export const recvMsgFromMerchant = ({ merchantId, merchantName, merchantAvatar, text }) => {
  let merchant = merchantStore.merchants.find(m => m.id === merchantId)
  if (!merchant) {
    merchant = {
      id: merchantId,
      name: merchantName || '商家',
      avatar: merchantAvatar || '',
      lastMsg: '',
      time: '',
      unread: 0
    }
    merchantStore.merchants.unshift(merchant)
  }
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  merchant.lastMsg = text
  merchant.time = time
  merchant.unread = (merchant.unread || 0) + 1
}

export const initUnreadFromApi = (unreadList) => {
  if (!unreadList || !Array.isArray(unreadList)) return
  
  unreadList.forEach(item => {
    const merchant = merchantStore.merchants.find(m => m.id === item.merchantId || item.merchant_id)
    if (merchant) {
      merchant.unread = item.unreadCount || item.unread_count || 0
    } else {
      merchantStore.merchants.unshift({
        id: item.merchantId || item.merchant_id,
        name: item.merchantName || item.merchant_name || '商家',
        avatar: item.merchantAvatar || item.merchant_avatar || '',
        lastMsg: item.lastMsg || item.last_msg || '',
        time: item.time || item.last_time || '',
        unread: item.unreadCount || item.unread_count || 0
      })
    }
  })
}

export const clearUnread = (merchantId) => {
  const merchant = merchantStore.merchants.find(m => m.id === merchantId)
  if (merchant) {
    merchant.unread = 0
  }
}

export const updateLastMsg = (merchantId, text, time) => {
  const merchant = merchantStore.merchants.find(m => m.id === merchantId)
  if (merchant) {
    merchant.lastMsg = text
    merchant.time = time
  }
}

export default merchantStore