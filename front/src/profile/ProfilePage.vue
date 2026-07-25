<template>
  <div class="profile-page">
    <div class="profile-container">
      <el-link type="info" :underline="false" @click="router.push('/')" class="back-link">
        ← 返回首页
      </el-link>

      <h2>个人中心</h2>

      <el-card class="profile-card">
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
          >
            <el-avatar :size="80" :src="avatar" shape="square">
              {{ userInitial }}
            </el-avatar>
            <span class="change-text">更换头像</span>
          </el-upload>
          <div class="user-base">
            <h3>{{ userName }}</h3>
            <p>{{ userPhone }}</p>
          </div>
        </div>
      </el-card>

      <el-card class="menu-card">
        <div class="menu-item" @click="togglePwdPanel">
          <div class="menu-left">
            <el-icon size="20" color="#409eff"><Lock /></el-icon>
            <span>{{ hasPayPassword ? '修改支付密码' : '设置支付密码' }}</span>
          </div>
          <el-icon class="menu-arrow" :class="{ open: activePanel === 'pwd' }"><ArrowRight /></el-icon>
        </div>
        <div class="menu-panel" :class="{ open: activePanel === 'pwd' }">
          <el-form :model="pwdForm" label-position="top" size="large">
            <el-form-item v-if="hasPayPassword" label="旧密码">
              <el-input v-model="pwdForm.oldPwd" type="password" placeholder="请输入旧支付密码" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPwd" type="password" placeholder="请输入新支付密码（6位）" show-password maxlength="6" />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="pwdForm.confirmPwd" type="password" placeholder="请再次输入新密码" show-password maxlength="6" />
            </el-form-item>
            <el-button type="primary" :loading="pwdLoading" @click="handleChangePwd">
              {{ hasPayPassword ? '确认修改' : '设置密码' }}
            </el-button>
          </el-form>
        </div>
      </el-card>

      <el-card class="menu-card">
        <div class="menu-item" @click="activePanel = activePanel === 'phone' ? '' : 'phone'">
          <div class="menu-left">
            <el-icon size="20" color="#67c23a"><Phone /></el-icon>
            <span>换绑手机号</span>
          </div>
          <el-icon class="menu-arrow" :class="{ open: activePanel === 'phone' }"><ArrowRight /></el-icon>
        </div>
        <div class="menu-panel" :class="{ open: activePanel === 'phone' }">
          <el-form :model="phoneForm" label-position="top" size="large">
            <el-form-item label="新手机号">
              <el-input v-model="phoneForm.newPhone" placeholder="请输入新手机号" />
            </el-form-item>
            <el-form-item label="验证码">
              <div class="code-row">
                <el-input v-model="phoneForm.code" placeholder="请输入验证码" maxlength="6" />
                <el-button :disabled="phoneCountdown > 0" @click="sendPhoneCode">
                  {{ phoneCountdown > 0 ? `${phoneCountdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-button type="primary" :loading="phoneLoading" @click="handleChangePhone">确认换绑</el-button>
          </el-form>
        </div>
      </el-card>

      <el-card class="menu-card">
        <div class="menu-item" @click="activePanel = activePanel === 'orders' ? '' : 'orders'">
          <div class="menu-left">
            <el-icon size="20" color="#e6a23c"><List /></el-icon>
            <span>{{ userRole === 'staff' ? '最近接单' : '历史订单' }}</span>
          </div>
          <el-icon class="menu-arrow" :class="{ open: activePanel === 'orders' }"><ArrowRight /></el-icon>
        </div>
        <div class="menu-panel" :class="{ open: activePanel === 'orders' }">
          <div class="order-tabs">
            <el-radio-group v-model="orderFilter" @change="fetchOrders">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="pending">待接单</el-radio-button>
              <el-radio-button value="progress">进行中</el-radio-button>
              <el-radio-button value="completed">已完成</el-radio-button>
              <el-radio-button value="cancelled">已取消</el-radio-button>
            </el-radio-group>
          </div>
          <div class="order-scroll-container">
            <div v-loading="orderLoading" class="order-list">
              <div v-for="order in orderList" :key="order.id" class="order-item">
                <div class="order-header">
                  <span class="order-id">订单号：{{ order.id }}</span>
                  <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
                </div>
                <div class="order-body">
                  <div class="order-cover">
                    <img v-if="order.coverUrl" :src="order.coverUrl" :alt="order.serviceItem" />
                    <div v-else class="order-cover-placeholder">{{ getOrderIcon(order.serviceItem) }}</div>
                  </div>
                  <div class="order-info">
                    <h4 class="order-name">{{ order.serviceItem }}</h4>
                    <p class="order-address">{{ order.serviceAddress }}</p>
                    <p class="order-time-slot">预约时间：{{ formatDateTime(order.serviceTime) }}</p>
                    <p v-if="order.remark" class="order-remark">备注：{{ order.remark }}</p>
                  </div>
                  <div class="order-right">
                    <div class="order-price">¥{{ order.orderAmount }}</div>
                    <el-tag :type="getOrderStatusType(order)" size="small">
                      {{ getOrderStatusText(order) }}
                    </el-tag>
                    <div class="order-actions">
                      <el-button size="small" text type="primary" @click="viewOrderDetail(order)">
                        查看详情
                      </el-button>
                      <el-button v-if="canContactMerchant(order)" size="small" text type="success" @click="contactMerchant(order)">
                        联系商家
                      </el-button>
                      <el-button v-if="canCancelOrder(order)" size="small" text type="danger" @click="cancelOrder(order.id)">
                        取消订单
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
              <el-empty v-if="!orderLoading && orderList.length === 0" :description="getEmptyDescription()" />
            </div>
          </div>
        </div>
      </el-card>
    <el-card class="menu-card">
        <div class="menu-item" @click="toggleAddressPanel">
          <div class="menu-left">
            <el-icon size="20" color="#f56c6c"><MapLocation /></el-icon>
            <span>服务地址</span>
          </div>
          <el-icon class="menu-arrow" :class="{ open: activePanel === 'address' }"><ArrowRight /></el-icon>
        </div>
        <div class="menu-panel" :class="{ open: activePanel === 'address' }">
          <div v-loading="addrLoading" class="address-list">
            <div v-for="addr in addressList" :key="addr.id" class="address-item">
              <div class="addr-info">
                <el-tag size="small" :type="addr.isDefault ? 'danger' : 'info'">
                  {{ addr.label || '地址' }}
                </el-tag>
                <span v-if="addr.isDefault" class="default-tag">默认</span>
                <p class="addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detailAddress }}</p>
                <p class="addr-contact">{{ addr.consigneeName }} {{ addr.consigneePhone }}</p>
              </div>
              <div class="addr-actions">
                <el-button size="small" text type="primary" @click="editAddress(addr)">编辑</el-button>
                <el-button size="small" text type="danger" @click="handleDeleteAddress(addr.id)">删除</el-button>
              </div>
            </div>
            <el-empty v-if="!addrLoading && addressList.length === 0" description="暂无服务地址" />
            <el-button type="primary" plain class="add-addr-btn" @click="editAddress(null)">添加地址</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="addrDialogVisible" :title="editingAddr ? '编辑地址' : '添加地址'" width="460px" destroy-on-close>
      <el-form :model="addrForm" label-position="top" size="large">
        <el-form-item label="地址标签">
          <el-select v-model="addrForm.label" placeholder="请选择标签" style="width:100%">
            <el-option label="家" value="家" />
            <el-option label="公司" value="公司" />
            <el-option label="学校" value="学校" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="收货人姓名">
          <el-input v-model="addrForm.consigneeName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="收货人手机号">
          <el-input v-model="addrForm.consigneePhone" placeholder="请输入收货人手机号" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="省份">
              <el-select v-model="addrForm.province" placeholder="省份" style="width:100%" @change="onProvinceChange">
                <el-option v-for="p in provinces" :key="p.name" :label="p.name" :value="p.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市">
              <el-select v-model="addrForm.city" placeholder="城市" style="width:100%" :disabled="!addrForm.province" @change="onCityChange">
                <el-option v-for="c in cities" :key="c.name" :label="c.name" :value="c.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区县">
              <el-select v-model="addrForm.district" placeholder="区县" style="width:100%" :disabled="!addrForm.city">
                <el-option v-for="d in districts" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址">
          <el-input v-model="addrForm.detailAddress" placeholder="请输入街道、小区、门牌号" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addrForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addrDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addrSaving" @click="handleSaveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, ArrowRight, Phone, List, MapLocation } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { uploadAvatarApi, checkPayPasswordApi, setPayPasswordApi, updatePayPasswordApi, sendCodeForUserApi, sendCodeForStaffApi, changePhoneApi, getAddressListApi, saveAddressApi, updateAddressApi, deleteAddressApi, getOrderListApi } from '../api/admin'
import regionData from '../data/china-region'

const router = useRouter()

const activePanel = ref('')
const avatar = ref('')
const userName = ref('')
const userPhone = ref('')

const userInitial = computed(() => {
  return userName.value ? userName.value.charAt(0).toUpperCase() : 'U'
})

const pwdForm = ref({ oldPwd: '', newPwd: '', confirmPwd: '' })
const pwdLoading = ref(false)
const hasPayPassword = ref(false)
const pwdChecking = ref(false)

const userRole = ref('')
const userRoleCode = ref('')
const isStaff = computed(() => userRole.value === 'staff' || userRoleCode.value === '002')
const userAccount = ref('')
const userId = ref('')

const phoneForm = ref({ newPhone: '', code: '' })
const phoneLoading = ref(false)
const phoneCountdown = ref(0)

const orderList = ref([])
const orderLoading = ref(false)
const orderFilter = ref('all')

const addressList = ref([])
const addrLoading = ref(false)
const addrDialogVisible = ref(false)
const addrSaving = ref(false)
const editingAddr = ref(null)
const addrForm = ref({ label: '家', consigneeName: '', consigneePhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: false })

const provinces = regionData
const cities = computed(() => {
  if (!addrForm.value.province) return []
  const province = regionData.find(p => p.name === addrForm.value.province)
  return province ? province.cities : []
})
const districts = computed(() => {
  if (!addrForm.value.city) return []
  const province = regionData.find(p => p.name === addrForm.value.province)
  if (!province) return []
  const city = province.cities.find(c => c.name === addrForm.value.city)
  return city ? city.districts : []
})

const onProvinceChange = () => {
  addrForm.value.city = ''
  addrForm.value.district = ''
}

const onCityChange = () => {
  addrForm.value.district = ''
}

onMounted(() => {
  loadUserInfo()
  fetchOrders()
})

const loadUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      userName.value = info.username || info.account || '用户'
      avatar.value = info.avatar || ''
      userPhone.value = info.phone || info.account || ''
      userRole.value = info.role || ''
      userRoleCode.value = info.roleCode || ''
      userAccount.value = info.account || ''
      userId.value = info.id || ''
    } catch { /* ignore */ }
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

const handleUpload = async (options) => {
  const formData = new FormData()
  formData.append('avatar', options.file)
  try {
    const res = await uploadAvatarApi(formData)
    const url = typeof res.data === 'string' ? res.data : (res.data?.url || '')
    avatar.value = url
    updateUserInfo({ avatar: url })
    ElMessage.success('头像更新成功')
  } catch {
    ElMessage.error('上传失败')
  }
}

const togglePwdPanel = async () => {
  if (activePanel.value === 'pwd') {
    activePanel.value = ''
    return
  }
  pwdChecking.value = true
  activePanel.value = 'pwd'
  try {
    const res = await checkPayPasswordApi(userAccount.value)
    hasPayPassword.value = res.data === true
    if (hasPayPassword.value) {
      pwdForm.value = { oldPwd: '', newPwd: '', confirmPwd: '' }
    }
  } catch {
    hasPayPassword.value = false
  } finally {
    pwdChecking.value = false
  }
}

const handleChangePwd = async () => {
  const { oldPwd, newPwd, confirmPwd } = pwdForm.value
  if (hasPayPassword.value && !oldPwd) return ElMessage.warning('请输入旧密码')
  if (!newPwd || newPwd.length < 6) return ElMessage.warning('新密码至少6位')
  if (newPwd !== confirmPwd) return ElMessage.warning('两次密码不一致')
  pwdLoading.value = true
  try {
    if (hasPayPassword.value) {
      await updatePayPasswordApi({ account: userAccount.value, oldPwd, newPwd })
    } else {
      await setPayPasswordApi({ account: userAccount.value, newPwd })
    }
    hasPayPassword.value = true
    ElMessage.success(hasPayPassword.value ? '支付密码修改成功' : '支付密码设置成功')
    pwdForm.value = { oldPwd: '', newPwd: '', confirmPwd: '' }
    activePanel.value = ''
  } catch {
    ElMessage.error('操作失败，请重试')
  } finally {
    pwdLoading.value = false
  }
}

const handleChangePhone = async () => {
  const { newPhone, code } = phoneForm.value
  if (!newPhone) return ElMessage.warning('请输入新手机号')
  if (!/^1[3-9]\d{9}$/.test(newPhone)) return ElMessage.warning('请输入有效的手机号')
  if (!code || code.length < 6) return ElMessage.warning('请输入6位验证码')
  phoneLoading.value = true
  try {
    await changePhoneApi({ account: userAccount.value, role: userRole.value, newPhone, code })
    userPhone.value = newPhone
    updateUserInfo({ phone: newPhone })
    ElMessage.success('手机号换绑成功')
    phoneForm.value = { newPhone: '', code: '' }
    activePanel.value = ''
  } catch {
    ElMessage.error('换绑失败，请重试')
  } finally {
    phoneLoading.value = false
  }
}

const sendPhoneCode = async () => {
  const { newPhone } = phoneForm.value
  if (!newPhone) return ElMessage.warning('请先输入新手机号')
  if (!/^1[3-9]\d{9}$/.test(newPhone)) return ElMessage.warning('请输入有效的手机号')
  try {
    let res
    if (userRole.value === 'staff') {
      res = await sendCodeForStaffApi(newPhone)
    } else {
      res = await sendCodeForUserApi(newPhone)
    }
    phoneCountdown.value = 60
    const timer = setInterval(() => {
      phoneCountdown.value--
      if (phoneCountdown.value <= 0) clearInterval(timer)
    }, 1000)
    const code = res.data || res.message || ''
    phoneForm.value.code = String(code)
    ElMessageBox.alert(`验证码：${code}`, '验证码', {
      confirmButtonText: '知道了',
      type: 'success'
    })
  } catch {
    ElMessage.error('发送验证码失败')
  }
}

const fetchOrders = async () => {
  orderLoading.value = true
  try {
    const params = userRole.value === 'staff'
      ? { staffAccount: userAccount.value }
      : { userAccount: userAccount.value }
    
    if (orderFilter.value !== 'all') {
      const statusMap = { pending: 0, progress: 1, completed: 2, cancelled: 3 }
      params.orderStatus = statusMap[orderFilter.value]
    }
    
    const res = await getOrderListApi(params)
    orderList.value = Array.isArray(res.data) ? res.data : []
  } catch {
    orderList.value = []
  } finally {
    orderLoading.value = false
  }
}

const formatDateTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const getOrderIcon = (serviceName) => {
  if (!serviceName) return '📦'
  if (serviceName.includes('保洁') || serviceName.includes('清洁')) return '🧹'
  if (serviceName.includes('维修')) return ''
  if (serviceName.includes('月嫂') || serviceName.includes('保姆')) return '👶'
  if (serviceName.includes('搬家')) return '🚚'
  return ''
}

const getOrderStatusText = (order) => {
  const status = order.orderStatus ?? order.status
  switch (status) {
    case 0: return '待接单'
    case 1: return '进行中'
    case 2: return '已完成'
    case 3: return '已取消'
    default: return '未知'
  }
}

const getOrderStatusType = (order) => {
  const status = order.orderStatus ?? order.status
  switch (status) {
    case 0: return 'warning'
    case 1: return 'primary'
    case 2: return 'success'
    case 3: return 'info'
    default: return 'info'
  }
}

const canCancelOrder = (order) => {
  const status = order.orderStatus ?? order.status
  return status === 0 || status === 1
}

const canContactMerchant = (order) => {
  const status = order.orderStatus ?? order.status
  return status === 1 && order.staffAccount
}

const contactMerchant = (order) => {
  if (!order.staffAccount) {
    ElMessage.warning('暂无可联系的商家')
    return
  }
  router.push({
    path: '/merchant',
    query: { merchantId: order.staffAccount }
  })
}

const getEmptyDescription = () => {
  if (userRole.value === 'staff') return '暂无接单记录'
  const filterText = { all: '暂无订单', pending: '暂无待接单', progress: '暂无进行中', completed: '暂无已完成', cancelled: '暂无已取消' }
  return filterText[orderFilter.value] || '暂无订单'
}

const viewOrderDetail = (order) => {
  router.push({ path: '/order', query: { id: order.id } })
}

const cancelOrder = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch { /* cancelled */ }
}

const toggleAddressPanel = () => {
  if (activePanel.value === 'address') {
    activePanel.value = ''
    return
  }
  activePanel.value = 'address'
  fetchAddressList()
}

const fetchAddressList = async () => {
  addrLoading.value = true
  try {
    const res = await getAddressListApi({ userAccount: userAccount.value, targetId: userAccount.value })
    addressList.value = Array.isArray(res.data) ? res.data : []
  } catch {
    addressList.value = []
  } finally {
    addrLoading.value = false
  }
}

const editAddress = (addr) => {
  editingAddr.value = addr
  if (addr) {
    addrForm.value = { ...addr }
  } else {
    addrForm.value = { label: '家', consigneeName: '', consigneePhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: false }
  }
  addrDialogVisible.value = true
}

const handleSaveAddress = async () => {
  const { label, consigneeName, consigneePhone, province, city, district, detailAddress, isDefault } = addrForm.value
  if (!consigneeName) return ElMessage.warning('请输入收货人姓名')
  if (!consigneePhone) return ElMessage.warning('请输入收货人手机号')
  if (!/^1[3-9]\d{9}$/.test(consigneePhone)) return ElMessage.warning('请输入正确的手机号')
  if (!detailAddress) return ElMessage.warning('请输入详细地址')
  addrSaving.value = true
  try {
    const params = { userAccount: userAccount.value, targetId: userAccount.value, label, consigneeName, consigneePhone, province, city, district, detailAddress, isDefault: isDefault ? 1 : 0 }
    if (editingAddr.value) {
      params.id = editingAddr.value.id
      await updateAddressApi(params)
    } else {
      await saveAddressApi(params)
    }
    ElMessage.success(editingAddr.value ? '地址修改成功' : '地址添加成功')
    addrDialogVisible.value = false
    editingAddr.value = null
    fetchAddressList()
  } catch {
    ElMessage.error('操作失败，请重试')
  } finally {
    addrSaving.value = false
  }
}

const handleDeleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', { type: 'warning' })
    await deleteAddressApi(id)
    ElMessage.success('删除成功')
    fetchAddressList()
  } catch { /* 取消删除或接口失败 */ }
}

const updateUserInfo = (patch) => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      Object.assign(info, patch)
      localStorage.setItem('userInfo', JSON.stringify(info))
    } catch { /* ignore */ }
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 40px 20px;
}

.profile-container {
  max-width: 600px;
  margin: 0 auto;
}

.back-link {
  margin-bottom: 16px;
  display: inline-block;
}

h2 {
  font-size: 24px;
  margin-bottom: 24px;
}

.profile-card {
  margin-bottom: 16px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.change-text {
  font-size: 12px;
  color: var(--accent);
}

.user-base h3 {
  font-size: 20px;
  margin-bottom: 4px;
}

.user-base p {
  color: var(--text-light);
  font-size: 14px;
}

.menu-card {
  margin-bottom: 12px;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 0;
  cursor: pointer;
  user-select: none;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  color: var(--text-h);
}

.menu-arrow {
  transition: transform 0.3s;
  color: var(--text-light);
}

.menu-arrow.open {
  transform: rotate(90deg);
}

.menu-panel {
  overflow: hidden;
  max-height: 0;
  transition: max-height 0.3s ease;
  padding: 0 8px;
}

.menu-panel.open {
  max-height: 2000px;
  padding-top: 16px;
  overflow: visible;
}

.order-scroll-container {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 4px;
}

.order-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.order-scroll-container::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 3px;
}

.order-scroll-container::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.order-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.code-row {
  display: flex;
  gap: 10px;
}

.code-row .el-input {
  flex: 1;
}

.address-list {
  padding-top: 16px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px;
  margin-bottom: 10px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
}

.addr-info {
  flex: 1;
}

.addr-info .el-tag {
  margin-right: 8px;
}

.default-tag {
  font-size: 12px;
  color: #f56c6c;
  margin-left: 4px;
}

.addr-detail {
  margin: 6px 0 4px;
  font-size: 14px;
  color: #303133;
}

.addr-contact {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.addr-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: 12px;
  white-space: nowrap;
}

.add-addr-btn {
  width: 100%;
  margin-top: 8px;
}

.order-tabs {
  margin-bottom: 16px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 4px;
}

.order-list::-webkit-scrollbar {
  width: 6px;
}

.order-list::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 3px;
}

.order-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.order-list::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.order-item {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fff;
  transition: box-shadow 0.2s;
}

.order-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: #f9fafb;
  border-bottom: 1px solid #ebeef5;
}

.order-id {
  font-size: 13px;
  color: #909399;
}

.order-time {
  font-size: 12px;
  color: #c0c4cc;
}

.order-body {
  display: flex;
  padding: 16px;
  gap: 16px;
}

.order-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.order-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  font-size: 36px;
  border-radius: 8px;
}

.order-info {
  flex: 1;
  min-width: 0;
}

.order-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px;
}

.order-address {
  font-size: 13px;
  color: #606266;
  margin: 0 0 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.order-time-slot {
  font-size: 12px;
  color: #909399;
  margin: 0 0 4px;
}

.order-remark {
  font-size: 12px;
  color: #e6a23c;
  margin: 0;
}

.order-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  min-width: 100px;
}

.order-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
}

.order-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
</style>