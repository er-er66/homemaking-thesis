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
        <h4>服务项目</h4>
        <div class="service-type-tabs">
          <el-tag
            v-for="type in serviceTypes"
            :key="type.value"
            :type="selectedServiceType === type.value ? 'primary' : 'info'"
            :effect="selectedServiceType === type.value ? 'dark' : 'plain'"
            class="service-type-tab"
            @click="selectedServiceType = type.value; filterServiceItems()"
          >
            {{ type.label }}
          </el-tag>
        </div>
        <div v-loading="serviceItemsLoading" class="service-items-grid">
          <el-tag
            v-for="item in serviceItems"
            :key="item.id"
            :type="selectedServiceItem?.id === item.id ? 'success' : 'info'"
            :effect="selectedServiceItem?.id === item.id ? 'dark' : 'plain'"
            class="service-item-card"
            @click="selectServiceItem(item)"
          >
            {{ item.name }}
          </el-tag>
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

const serviceTypes = [
  { value: 0, label: '全部' },
  { value: 1, label: '保洁清洁类' },
  { value: 2, label: '家务保姆类' },
  { value: 3, label: '母婴护理类' },
  { value: 4, label: '老人/病患照护类' },
  { value: 5, label: '新兴细分家政服务' },
  { value: 6, label: '其他配套家政' }
]

const selectedServiceType = ref(0)
const serviceItems = ref([])
const selectedServiceItem = ref(null)
const serviceItemsLoading = ref(false)

const allServiceItems = [
  { id: 1, name: '日常保洁', serviceType: 1 },
  { id: 2, name: '深度清洁', serviceType: 1 },
  { id: 3, name: '开荒保洁', serviceType: 1 },
  { id: 4, name: '家电清洗', serviceType: 1 },
  { id: 5, name: '擦玻璃', serviceType: 1 },
  { id: 6, name: '地板打蜡', serviceType: 1 },
  { id: 7, name: '油烟机清洗', serviceType: 1 },
  { id: 8, name: '空调清洗', serviceType: 1 },
  { id: 9, name: '住家保姆', serviceType: 2 },
  { id: 10, name: '白班保姆', serviceType: 2 },
  { id: 11, name: '小时工', serviceType: 2 },
  { id: 12, name: '做饭阿姨', serviceType: 2 },
  { id: 13, name: '育儿嫂', serviceType: 3 },
  { id: 14, name: '月嫂', serviceType: 3 },
  { id: 15, name: '育婴师', serviceType: 3 },
  { id: 16, name: '催乳师', serviceType: 3 },
  { id: 17, name: '陪产护理', serviceType: 3 },
  { id: 18, name: '老人护理', serviceType: 4 },
  { id: 19, name: '病患护理', serviceType: 4 },
  { id: 20, name: '康复护理', serviceType: 4 },
  { id: 21, name: '陪诊服务', serviceType: 4 },
  { id: 22, name: '养老护理', serviceType: 4 },
  { id: 23, name: '整理收纳', serviceType: 5 },
  { id: 24, name: '搬家服务', serviceType: 5 },
  { id: 25, name: '甲醛治理', serviceType: 5 },
  { id: 26, name: '灭虫除螨', serviceType: 5 },
  { id: 27, name: '绿植养护', serviceType: 5 },
  { id: 28, name: '宠物护理', serviceType: 5 },
  { id: 29, name: '窗帘清洗', serviceType: 6 },
  { id: 30, name: '沙发清洗', serviceType: 6 },
  { id: 31, name: '地毯清洗', serviceType: 6 },
  { id: 32, name: '衣物的洗涤与整理', serviceType: 6 }
]

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
  fetchServiceItems()
})

const fetchServiceItems = async () => {
  serviceItemsLoading.value = true
  try {
    serviceItems.value = allServiceItems
  } catch (e) {
    console.error('获取服务项目列表失败:', e)
  }
  serviceItemsLoading.value = false
}

const filterServiceItems = () => {
  if (selectedServiceType.value === 0) {
    serviceItems.value = allServiceItems
  } else {
    serviceItems.value = allServiceItems.filter(item => item.serviceType === selectedServiceType.value)
  }
  selectedServiceItem.value = null
}

const selectServiceItem = (item) => {
  selectedServiceItem.value = item
  orderName.value = item.name
}

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
    
    ctx.fillStyle = '#FAF7F4'
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
  if (!selectedServiceItem.value) {
    ElMessage.warning('请选择服务项目')
    return
  }
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
    serviceType: selectedServiceType.value,
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
    // 支付密码一律交给后端校验（后端已改用 BCrypt 存储，hash 不会下发到前端）。
    // 这里只判断「是否已设置支付密码」：成功判定必须看 Result 的 code===200 且 data===true。
    // 注意 data 是布尔值，去比对 data 上的字段恒为 true，等于没校验。
    const checkRes = await checkPayPasswordApi(userAccount)
    if (!(checkRes && checkRes.code === 200 && checkRes.data === true)) {
      ElMessage.error('尚未设置支付密码，请先前往个人中心设置')
      return
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
    // 支付密码错误、订单校验失败等业务错误都在这里：拦截器已按后端 message 弹过提示
    // （并打了 __handled 标记），只有网络/超时之外真正没提示过的异常才需要兜底文案。
    if (!e || !e.__handled) {
      ElMessage.error('订单发布失败，请稍后重试')
    }
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.publish-order-page {
  min-height: 100vh;
  background: var(--surface-page);
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
  color: var(--text-primary);
}

.form-card {
  margin-bottom: 20px;
}

.form-card h4 {
  margin-bottom: 16px;
  color: var(--text-primary);
  font-size: 16px;
}

.service-type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.service-type-tab {
  cursor: pointer;
  padding: 8px 16px;
}

.service-items-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  min-height: 60px;
}

.service-item-card {
  cursor: pointer;
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
  border: 2px solid var(--border-base);
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--neutral-50);
}

.preset-cover-item:hover {
  border-color: var(--accent);
  transform: scale(1.05);
}

.preset-cover-item.active {
  border-color: var(--accent);
  box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.3);
}

.preset-cover-icon {
  font-size: 32px;
  line-height: 1;
}

.preset-cover-label {
  font-size: 12px;
  color: var(--text-secondary);
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
  border: 2px dashed var(--border-strong);
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
  border-color: var(--accent);
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
  color: var(--text-placeholder);
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
  color: var(--text-placeholder);
}

.amount-unit {
  margin-left: 8px;
  color: var(--text-secondary);
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
  border: 2px solid var(--border-base);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.addr-card:hover {
  border-color: var(--accent);
}

.addr-card.active {
  border-color: var(--accent);
  background: var(--color-danger-bg);
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
  border: 2px solid var(--border-strong);
  position: relative;
  transition: all 0.2s;
}

.addr-radio-dot.checked {
  border-color: var(--accent);
  background: var(--accent);
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
  color: var(--text-primary);
}

.addr-phone {
  color: var(--text-secondary);
  font-size: 14px;
}

.addr-tag,
.addr-default {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.addr-tag {
  background: var(--brand-50);
  color: var(--accent);
}

.addr-default {
  background: var(--color-warning-bg);
  color: var(--color-warning);
}

.addr-card-detail {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
}

.addr-tip {
  display: inline-block;
  margin-top: 12px;
  color: var(--accent);
  cursor: pointer;
  font-size: 14px;
}

.addr-tip:hover {
  text-decoration: underline;
}

.notice-section {
  margin: 24px 0;
  border: 1px solid var(--border-base);
  border-radius: 8px;
  overflow: hidden;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  background: var(--neutral-50);
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--text-primary);
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
  color: var(--text-secondary);
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
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.pay-amount {
  font-size: 32px;
  font-weight: bold;
  color: var(--accent);
  margin-bottom: 20px;
}

.pay-divider {
  height: 1px;
  background: var(--border-base);
  margin: 20px 0;
}

.pay-pwd-label {
  font-size: 14px;
  color: var(--text-secondary);
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
  border: 2px solid var(--border-base);
  border-radius: 8px;
  outline: none;
  transition: border-color 0.2s;
}

.pwd-dot:focus {
  border-color: var(--accent);
}
</style>