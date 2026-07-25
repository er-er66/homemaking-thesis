import request from './request'

export const loginApi = (data) => {
  return request({
    url: '/login',
    method: 'post',
    data
  })
}

export const registerApi = (data) => {
  return request({
    url: '/register',
    method: 'post',
    data
  })
}

export const sendCodeApi = (phone) => {
  return request({
    url: '/send-code',
    method: 'post',
    data: { phone }
  })
}

export const getUserInfoApi = () => {
  return request({
    url: '/user-info',
    method: 'get'
  })
}

export const logoutApi = () => {
  return request({
    url: '/logout',
    method: 'post'
  })
}

export const uploadAvatarApi = (formData) => {
  return request({
    url: '/upload-avatar',
    method: 'post',
    data: formData
  })
}

export const uploadPackageCoverApi = (formData) => {
  return request({
    url: '/upload-package-cover',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const getUserListApi = (params) => {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

export const getStaffListApi = (params) => {
  return request({
    url: '/emp',
    method: 'get',
    params
  })
}

export const getUserDetailApi = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}
export const getUserNameApi = (account) => {
  return request({
    url: `/user/account/${account}`,
    method: 'get'
  })
}
export const getStaffDetailApi = (id) => {
  return request({
    url: `/emp/${id}`,
    method: 'get'
  })
}

export const toggleUserStatusApi = (id) => {
  return request({
    url: `/user/${id}/status`,
    method: 'put'
  })
}

export const toggleStaffStatusApi = (id) => {
  return request({
    url: `/emp/${id}/status`,
    method: 'put'
  })
}

export const getOrderListApi = (params) => {
  return request({
    url: '/order',
    method: 'get',
    params
  })
}

export const getOrderDetailApi = (id) => {
  return request({
    url: `/order/${id}`,
    method: 'get'
  })
}

export const getAdminListApi = (params) => {
  return request({
    url: '/admins',
    method: 'get',
    params
  })
}

export const toggleAdminStatusApi = (id) => {
  return request({
    url: `/admin/${id}/status`,
    method: 'put'
  })
}

export const getAdminDetailApi = (id) => {
  return request({
    url: `/admin/${id}`,
    method: 'get'
  })
}

export const dispatchOrderApi = (data) => {
  return request({
    url: '/order/dispatch',
    method: 'post',
    data
  })
}

export const rejectOrderApi = (data) => {
  return request({
    url: '/order/reject',
    method: 'post',
    data
  })
}

export const checkPayPasswordApi = (account) => {
  return request({
    url: '/user/pay-password/check',
    method: 'get',
    params: { account }
  })
}

export const setPayPasswordApi = (params) => {
  return request({
    url: '/user/pay-password/set',
    method: 'post',
    params
  })
}

export const updatePayPasswordApi = (params) => {
  return request({
    url: '/user/pay-password/update',
    method: 'put',
    params
  })
}

export const sendCodeForUserApi = (phone) => {
  return request({
    url: '/user/send-code',
    method: 'post',
    params: { phone }
  })
}

export const sendCodeForStaffApi = (phone) => {
  return request({
    url: '/emp/send-code',
    method: 'post',
    params: { phone }
  })
}

export const changePhoneApi = (params) => {
  return request({
    url: '/user/change-phone',
    method: 'post',
    params
  })
}



export const saveAddressApi = (data) => {
  return request({
    url: '/user/address/save',
    method: 'post',
    data
  })
}

export const updateAddressApi = (data) => {
  return request({
    url: '/user/address/update',
    method: 'post',
    data
  })
}

export const deleteAddressApi = (id) => {
  return request({
    url: '/user/address/delete',
    method: 'post',
    params: { id }
  })
}

export const getAddressListApi = (params) => {
  return request({
    url: '/user/address/list',
    method: 'get',
    params
  })
}

export const createOrderApi = (data) => {
  return request({
    url: '/order/create',
    method: 'post',
    data
  })
}

export const takeOrderApi = (data) => {
  return request({
    url: '/order/take',
    method: 'post',
    data
  })
}

export const getChatHistoryApi = (params) => {
  return request({
    url: '/chat/history',
    method: 'get',
    params
  })
}

export const saveChatMsgApi = (data) => {
  return request({
    url: '/chat/send',
    method: 'post',
    data
  })
}

export const getUnreadMessagesApi = (userId) => {
  return request({
    url: '/chat/unread',
    method: 'get',
    params: { userId }
  })
}
export const getChatRoomListApi = (userId) => {
  return request({
    url: '/chat/rooms',
    method: 'get',
    params: { userId }
  })
}
export const getMerchantListApi = (userId) => {
  return request({
    url: '/chat/merchants',
    method: 'get',
    params: { userId }
  })
}

export const markChatReadApi = (params) => {
  return request({
    url: '/chat/read',
    method: 'post',
    params
  })
}

export const getPackageListApi = (params) => {
  return request({
    url: '/package/list',
    method: 'get',
    params
  })
}

export const createPackageApi = (data) => {
  return request({
    url: '/package/create',
    method: 'post',
    data
  })
}

export const updatePackageApi = (data) => {
  return request({
    url: '/package/update',
    method: 'put',
    data
  })
}

export const deletePackageApi = (id) => {
  return request({
    url: `/package/delete/${id}`,
    method: 'post'
  })
}

export const togglePackageStatusApi = (id) => {
  return request({
    url: `/package/${id}/status`,
    method: 'put'
  })
}

export const changeAdminPasswordApi = (data) => {
  return request({
    url: '/admin/change-password',
    method: 'post',
    data
  })
}

export const changeAdminPhoneApi = (data) => {
  return request({
    url: '/admin/change-phone',
    method: 'post',
    data
  })
}