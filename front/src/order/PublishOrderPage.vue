<template>
  <div class="publish-order-page">
    <div class="order-container">
      <el-link type="info" :underline="false" @click="router.back()" class="back-link">
        ← 返回
      </el-link>

      <h2>发布订单</h2>

      <el-card class="form-card">
        <h4>订单封面</h4>
        <div class="cover-section">
          <div class="preset-covers">
            <div
              v-for="(cover, index) in presetCovers"
              :key="index"
              class="preset-cover-item"
              :class="{ active: selectedPresetIndex === index }"
              @click="selectPresetCover(index)"
            >
              <div class="preset-cover-icon">{{ cover.icon }}</div>
              <div class="preset-cover-label">{{ cover.label }}</div>
              <div v-if="selectedPresetIndex === index" class="preset-cover-check">✓</div>
            </div>
          </div>
          <div class="cover-upload">
            <el-upload
              class="cover-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeCoverUpload"
              :http-request="handleCoverUpload"
            >
              <img v-if="coverUrl" :src="coverUrl" class="cover-image" />
              <div v-if="!coverUrl" class="cover-placeholder">
                <el-icon :size="40"><Plus /></el-icon>
                <span>点击上传自定义封面</span>
              </div>
            </el-upload>
            <p class="upload-tip">支持 jpg、png 格式，大小不超过 2MB</p>
          </div>
        </div>
      </el-card>

      <el-card class="form-card">
        <h4>订单名称</h4>
        <el-input
          v-model="orderName"
          placeholder="请输入订单名称，如：家庭保洁、月嫂服务等"
          maxlength="50"
          show-word-count
        />
      </el-card>

      <el-card class="form-card">
        <h4>订单简介</h4>
        <el-input
          v-model="orderDesc"
          type="textarea"
          :rows="4"
          placeholder="请输入订单简介，描述您需要的服务内容..."
          maxlength="200"
          show-word-count
        />
      </el-card>

      <el-card class="form-card">
        <h4>订单金额</h4>
        <el-input-number
          v-model="orderAmount"
          :min="0"
          :precision="2"
          :step="10"
          placeholder="请输入订单金额"
          style="width: 100%"
        />
        <span class="amount-unit">元</span>
      </el-card>

      <el-card class="form-card">
        <h4>选择服务地址</h4>
        <div v-loading="addrLoading" class="address-select">
          <div v-if="addressList.length > 0" class="addr-card-list">
            <div
              v-for="addr in addressList"
              :key="addr.id"
              class="addr-card"
              :class="{ active: selectedAddrId === addr.id }"
              @click="selectedAddrId = addr.id"
            >
              <div class="addr-card-left">
                <div class="addr-radio-dot" :class="{ checked: selectedAddrId === addr.id }"></div>
              </div>
              <div class="addr-card-body">
                <div class="addr-card-top">
                  <span class="addr-name">{{ addr.consigneeName }}</span>
                  <span class="addr-phone">{{ addr.consigneePhone }}</span>
                  <span class="addr-tag" v-if="addr.label">{{ addr.label }}</span>
                  <span class="addr-default" v-if="addr.isDefault">默认</span>
                </div>
                <div class="addr-card-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detailAddress }}</div>
              </div>
            </div>
          </div>
          <el-empty v-if="!addrLoading && addressList.length === 0" description="暂无保存的地址" />
          <span class="addr-tip" @click="router.push('/profile')">前往个人中心添加地址 →</span>
        </div>
      </el-card>

      <el-card class="form-card">
        <h4>预约服务时间</h4>
        <el-date-picker
          v-model="serviceTime"
          type="datetime"
          placeholder="请选择预约时间"
          :disabled-date="disabledDate"
          :disabled-hours="disabledHours"
          style="width:100%"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-card>

      <el-card class="form-card">
        <h4>订单备注</h4>
        <el-input
          v-model="remark"
          type="textarea"
          :rows="4"
          placeholder="请输入备注信息，如：小区门禁、宠物情况、重点清洁区域等..."
          maxlength="200"
          show-word-count
        />
      </el-card>

      <div class="notice-section">
        <div class="notice-header" @click="showNotice = !showNotice">
          <span class="notice-title">
            <el-icon><WarningFilled /></el-icon>
            订单注意事项
          </span>
          <el-icon class="notice-arrow" :class="{ open: showNotice }">
            <ArrowDown />
          </el-icon>
        </div>
        <div class="notice-body" :class="{ open: showNotice }">
          <div class="notice-content">
            <p>1. 请确保服务期间家中有人，如需取消请提前2小时联系客服。</p>
            <p>2. 服务人员会携带专业工具上门，如有特殊需求请提前备注。</p>
            <p>3. 服务完成后请当场验收，如有问题及时反馈。</p>
            <p>4. 贵重物品请自行妥善保管，建议服务前收纳好。</p>
            <p>5. 如需开具发票，请在下单时备注发票信息。</p>
            <p>6. 预约时间前30分钟可免费取消，超时将收取20%手续费。</p>
          </div>
        </div>
      </div>

      <el-button
        type="primary"
        size="large"
        class="submit-btn"
        :loading="submitting"
        @click="handleSubmit"
      >
        立即发布订单
      </el-button>
    </div>

    <el-dialog
      v-model="showPayDialog"
      title="确认支付"
      width="380px"
      :close-on-click-modal="false"
      center
      class="pay-dialog"
    >
      <div class="pay-content">
        <div class="pay-amount-label">支付金额</div>
        <div class="pay-amount">¥{{ orderAmount }}</div>
        <div class="pay-divider"></div>
        <div class="pay-pwd-label">请输入支付密码</div>
        <div class="pay-pwd-inputs">
          <input
            v-for="(_, i) in 6"
            :key="i"
            :ref="el => pwdRefs[i] = el"
            v-model="pwdDigits[i]"
            type="password"
            maxlength="1"
            class="pwd-dot"
            @input="onPwdInput($event, i)"
            @keydown.backspace="onPwdBackspace($event, i)"
            @focus="onPwdFocus(i)"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="closePayDialog">取消</el-button>
        <el-button
          type="primary"
          :disabled="pwdDigits.join('').length < 6"
          :loading="paying"
          @click="handlePay"
        >
          确认支付
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, WarningFilled, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { checkPayPasswordApi, getAddressListApi, createOrderApi, uploadPackageCoverApi } from '../api/admin'

const router = useRouter()

const presetCovers = [
  { icon: '🧹', label: '保洁' },
  { icon: '✨', label: '清洁' },
  { icon: '🔧', label: '维修' },
  { icon: '👶', label: '月嫂' }
]

const presetCoverUrls = ref({})

const coverUrl = ref('')
const coverFile = ref(null)
const selectedPresetIndex = ref(-1)
const isPresetCover = ref(false)
const orderName = ref('')
const orderDesc = ref('')
const orderAmount = ref(0)
const remark = ref('')
const serviceTime = ref('')
const selectedAddrId = ref(null)
const addressList = ref([])
const addrLoading = ref(false)
const showNotice = ref(false)
const showPayDialog = ref(false)
const submitting = ref(false)
const paying = ref(false)
const pwdDigits = ref(['', '', '', '', '', ''])
const pwdRefs = ref([])
const pendingOrderData = ref(null)

onMounted(() => {
  fetchAddressList()
})

const fetchAddressList = async () => {
  const userInfoStr = localStorage.getItem('userInfo')
  let userAccount = ''
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      userAccount = info.account || ''
    } catch { /* ignore */ }
  }
  if (!userAccount) return

  addrLoading.value = true
  try {
    const res = await getAddressListApi({ userAccount: userAccount })
    if (res && res.data && Array.isArray(res.data)) {
      addressList.value = res.data
      const defaultAddr = res.data.find(a => a.isDefault)
      if (defaultAddr) {
        selectedAddrId.value = defaultAddr.id
      } else if (res.data.length > 0) {
        selectedAddrId.value = res.data[0].id
      }
    }
  } catch (e) {
    console.error('获取地址列表失败:', e)
  }
  addrLoading.value = false
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const disabledHours = () => {
  const now = new Date()
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const selectedDate = new Date(serviceTime.value)
  
  if (selectedDate.toDateString() === today.toDateString()) {
    const currentHour = now.getHours()
    const hours = []
    for (let i = 0; i < currentHour; i++) {
      hours.push(i)
    }
    return hours
  }
  return []
}

const beforeCoverUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleCoverUpload = async (options) => {
  const file = options.file
  coverFile.value = file
  isPresetCover.value = false
  
  const formData = new FormData()
  formData.append('cover', file)
  
  try {
    const res = await uploadPackageCoverApi(formData)
    if (res && res.data && res.data.url) {
      coverUrl.value = res.data.url
      ElMessage.success('封面上传成功')
    }
  } catch (e) {
    console.error('封面上传失败:', e)
    ElMessage.error('封面上传失败')
  }
}

const selectPresetCover = async (index) => {
  selectedPresetIndex.value = index
  isPresetCover.value = true
  
  const cover = presetCovers[index]
  
  if (presetCoverUrls.value[index]) {
    coverUrl.value = presetCoverUrls.value[index]
    return
  }
  
  try {
    const blob = await emojiToImageBlob(cover.icon, cover.label)
    const file = new File([blob], `preset-${cover.label}.png`, { type: 'image/png' })
    
    const formData = new FormData()
    formData.append('cover', file)
    
    const res = await uploadPackageCoverApi(formData)
    if (res && res.data && res.data.url) {
      presetCoverUrls.value[index] = res.data.url
      coverUrl.value = res.data.url
      ElMessage.success('封面已上传')
    }
  } catch (e) {
    console.error('预设封面上传失败:', e)
    ElMessage.error('封面上传失败')
  }
}

const emojiToImageBlob = (emoji, label) => {
  return new Promise((resolve) => {
    const canvas = document.createElement('canvas')
    canvas.width = 200
    canvas.height = 200
    const ctx = canvas.getContext('2d')
    
    ctx.fillStyle = '#f5f7fa'
    ctx.fillRect(0, 0, 200, 200)
    
    ctx.font = '80px serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(emoji, 100, 75)
    
    ctx.font = '24px sans-serif'
    ctx.fillStyle = '#606266'
    ctx.fillText(label, 100, 150)
    
    canvas.toBlob((blob) => {
      resolve(blob)
    }, 'image/png')
  })
}

const handleSubmit = async () => {
  if (!orderName.value.trim()) {
    ElMessage.warning('请输入订单名称')
    return
  }
  if (!orderDesc.value.trim()) {
    ElMessage.warning('请输入订单简介')
    return
  }
  if (!orderAmount.value || orderAmount.value <= 0) {
    ElMessage.warning('请输入订单金额')
    return
  }
  if (!selectedAddrId.value) {
    ElMessage.warning('请选择服务地址')
    return
  }
  if (!serviceTime.value) {
    ElMessage.warning('请选择预约服务时间')
    return
  }

  const selectedAddr = addressList.value.find(a => a.id === selectedAddrId.value)
  if (!selectedAddr) {
    ElMessage.warning('请选择有效的服务地址')
    return
  }

  const serviceAddress = `${selectedAddr.province}${selectedAddr.city}${selectedAddr.district}${selectedAddr.detailAddress}`

  pendingOrderData.value = {
    serviceItem: orderName.value,
    serviceAddress: serviceAddress,
    serviceTime: serviceTime.value,
    orderAmount: orderAmount.value,
    remark: remark.value,
    coverUrl: coverUrl.value,
    orderStatus: 0,
    payStatus: 1
  }

  showPayDialog.value = true
}

const closePayDialog = () => {
  showPayDialog.value = false
  pwdDigits.value = ['', '', '', '', '', '']
}

const onPwdInput = (event, index) => {
  const val = event.target.value
  if (val && index < 5) {
    pwdRefs.value[index + 1]?.focus()
  }
}

const onPwdBackspace = (event, index) => {
  if (!pwdDigits.value[index] && index > 0) {
    pwdRefs.value[index - 1]?.focus()
  }
}

const onPwdFocus = (index) => {
  if (pwdDigits.value[index]) {
    pwdDigits.value[index] = ''
  }
}

const handlePay = async () => {
  const payPassword = pwdDigits.value.join('')
  if (payPassword.length < 6) {
    ElMessage.warning('请输入完整的6位支付密码')
    return
  }

  const userInfoStr = localStorage.getItem('userInfo')
  let userAccount = ''
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      userAccount = info.account || ''
    } catch { /* ignore */ }
  }

  if (!userAccount) {
    ElMessage.error('用户信息异常，请重新登录')
    return
  }

  paying.value = true
  try {
    const checkRes = await checkPayPasswordApi(userAccount)
    if (checkRes && checkRes.data && checkRes.data.hasPassword) {
      if (checkRes.data.password !== payPassword) {
        ElMessage.error('支付密码错误')
        paying.value = false
        return
      }
    }

    const orderData = {
      ...pendingOrderData.value,
      userAccount: userAccount,
      payPassword: payPassword
    }

    const res = await createOrderApi(orderData)
    if (res && res.code === 200) {
      ElMessage.success('订单发布成功')
      closePayDialog()
      router.push('/profile')
    } else {
      ElMessage.error(res.msg || '订单发布失败')
    }
  } catch (e) {
    console.error('订单发布失败:', e)
    ElMessage.error('订单发布失败，请稍后重试')
  }
  paying.value = false
}
</script>

<style scoped>
.publish-order-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.order-container {
  max-width: 800px;
  margin: 0 auto;
}

.back-link {
  margin-bottom: 20px;
  font-size: 16px;
}

h2 {
  margin-bottom: 24px;
  color: #333;
}

.form-card {
  margin-bottom: 20px;
}

.form-card h4 {
  margin-bottom: 16px;
  color: #333;
  font-size: 16px;
}

.cover-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cover-section {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.preset-covers {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.preset-cover-item {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 2px solid #e5e7eb;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
}

.preset-cover-item:hover {
  border-color: #ff6b6b;
  transform: scale(1.05);
}

.preset-cover-item.active {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.3);
}

.preset-cover-icon {
  font-size: 32px;
  line-height: 1;
}

.preset-cover-label {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.preset-cover-check {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 107, 107, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  font-weight: bold;
}

.cover-uploader {
  width: 200px;
  height: 200px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-uploader:hover {
  border-color: #ff6b6b;
}

.cover-uploader :deep(.el-upload) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  gap: 8px;
  text-align: center;
}

.cover-placeholder :deep(.el-icon) {
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-placeholder span {
  font-size: 14px;
}

.upload-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #999;
}

.amount-unit {
  margin-left: 8px;
  color: #666;
}

.address-select {
  position: relative;
}

.addr-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.addr-card {
  display: flex;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.addr-card:hover {
  border-color: #ff6b6b;
}

.addr-card.active {
  border-color: #ff6b6b;
  background: #fff5f5;
}

.addr-card-left {
  display: flex;
  align-items: center;
  margin-right: 12px;
}

.addr-radio-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #dcdfe6;
  position: relative;
  transition: all 0.2s;
}

.addr-radio-dot.checked {
  border-color: #ff6b6b;
  background: #ff6b6b;
}

.addr-radio-dot.checked::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fff;
}

.addr-card-body {
  flex: 1;
}

.addr-card-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.addr-name {
  font-weight: 600;
  color: #333;
}

.addr-phone {
  color: #666;
  font-size: 14px;
}

.addr-tag,
.addr-default {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.addr-tag {
  background: #fff0f0;
  color: #ff6b6b;
}

.addr-default {
  background: #fff7e6;
  color: #fa8c16;
}

.addr-card-detail {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.addr-tip {
  display: inline-block;
  margin-top: 12px;
  color: #ff6b6b;
  cursor: pointer;
  font-size: 14px;
}

.addr-tip:hover {
  text-decoration: underline;
}

.notice-section {
  margin: 24px 0;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  background: #f9fafb;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #333;
}

.notice-arrow {
  transition: transform 0.3s;
}

.notice-arrow.open {
  transform: rotate(180deg);
}

.notice-body {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s;
}

.notice-body.open {
  max-height: 500px;
}

.notice-content {
  padding: 16px;
  background: #fff;
}

.notice-content p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.submit-btn {
  width: 100%;
  margin-top: 24px;
}

.pay-dialog :deep(.el-dialog__header) {
  text-align: center;
  padding-bottom: 0;
}

.pay-content {
  text-align: center;
  padding: 20px 0;
}

.pay-amount-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.pay-amount {
  font-size: 32px;
  font-weight: bold;
  color: #ff6b6b;
  margin-bottom: 20px;
}

.pay-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 20px 0;
}

.pay-pwd-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.pay-pwd-inputs {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.pwd-dot {
  width: 40px;
  height: 40px;
  text-align: center;
  font-size: 20px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  outline: none;
  transition: border-color 0.2s;
}

.pwd-dot:focus {
  border-color: #ff6b6b;
}
</style>