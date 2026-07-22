<template>
  <div class="order-page">
    <div class="order-container">
      <el-link type="info" :underline="false" @click="router.back()" class="back-link">
        ← 返回
      </el-link>

      <h2>确认订单</h2>

      <el-card class="service-card">
        <div class="service-header">
          <span class="service-icon">{{ service.icon }}</span>
          <div class="service-info">
            <h3>{{ service.name }}</h3>
            <p>{{ service.description }}</p>
          </div>
          <el-tag type="danger" size="large">{{ service.price }}</el-tag>
        </div>
      </el-card>

      <el-card class="remark-card">
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

      <el-card class="remark-card">
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

      <el-card class="remark-card">
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
        class="pay-btn"
        :loading="paying"
        @click="handlePayClick"
      >
        立即支付 {{ service.price }}
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
        <div class="pay-amount">{{ service.price }}</div>
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { checkPayPasswordApi, getAddressListApi, createOrderApi } from '../api/admin'

const route = useRoute()
const router = useRouter()

const service = reactive({
  id: '',
  name: '',
  description: '',
  price: '',
  icon: ''
})

const remark = ref('')
const serviceTime = ref('')
const selectedAddrId = ref(null)
const addressList = ref([])
const addrLoading = ref(false)
const showNotice = ref(false)
const showPayDialog = ref(false)
const paying = ref(false)
const pwdDigits = ref(['', '', '', '', '', ''])
const pwdRefs = ref([])

onMounted(() => {
  const { id, name, description, price, icon } = route.query
  service.id = id || ''
  service.name = name || '未选择服务'
  service.description = description || ''
  service.price = price || '¥0'
  service.icon = icon || '🧹'
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
    const res = await getAddressListApi({ userAccount, targetId: userAccount })
    addressList.value = Array.isArray(res.data) ? res.data : []
    const defaultAddr = addressList.value.find(a => a.isDefault)
    if (defaultAddr) {
      selectedAddrId.value = defaultAddr.id
    }
  } catch {
    addressList.value = []
  } finally {
    addrLoading.value = false
  }
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const disabledHours = () => {
  const now = new Date()
  if (!serviceTime.value) return []
  const selectedDate = new Date(serviceTime.value)
  const isToday = selectedDate.toDateString() === now.toDateString()
  if (!isToday) return []
  const hours = []
  for (let i = 0; i < now.getHours(); i++) {
    hours.push(i)
  }
  return hours
}

const onPwdInput = (e, index) => {
  const val = e.target.value
  if (val && index < 5) {
    pwdRefs.value[index + 1]?.focus()
  }
}

const onPwdBackspace = (e, index) => {
  if (!pwdDigits.value[index] && index > 0) {
    pwdRefs.value[index - 1]?.focus()
  }
}

const onPwdFocus = (index) => {
  if (pwdDigits.value[index]) {
    pwdDigits.value[index] = ''
  }
}

const handlePayClick = async () => {
  if (!selectedAddrId.value) {
    ElMessage.warning('请选择服务地址')
    return
  }
  if (!serviceTime.value) {
    ElMessage.warning('请选择预约服务时间')
    return
  }
  const userInfoStr = localStorage.getItem('userInfo')
  let account = ''
  if (userInfoStr) {
    try {
      account = JSON.parse(userInfoStr).account || ''
    } catch { /* ignore */ }
  }

  try {
    const res = await checkPayPasswordApi(account)
    const hasPwd = res.data === true
    if (hasPwd) {
      showPayDialog.value = true
    } else {
      await ElMessageBox.confirm(
        '您尚未设置支付密码，请先前往个人中心设置支付密码后再进行支付。',
        '提示',
        {
          confirmButtonText: '去设置',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      router.push('/profile')
    }
  } catch {
    showPayDialog.value = true
  }
}

const handlePay = async () => {
  const pwd = pwdDigits.value.join('')
  if (pwd.length < 6) {
    ElMessage.warning('请输入6位支付密码')
    return
  }
  paying.value = true

  const userInfoStr = localStorage.getItem('userInfo')
  let userAccount = ''
  if (userInfoStr) {
    try {
      userAccount = JSON.parse(userInfoStr).account || ''
    } catch { /* ignore */ }
  }

  const selectedAddr = addressList.value.find(a => a.id === selectedAddrId.value)
  const serviceAddress = selectedAddr
    ? `${selectedAddr.province}${selectedAddr.city}${selectedAddr.district} ${selectedAddr.detailAddress}`
    : ''

  const orderAmount = parseFloat(service.price.replace(/[^0-9.]/g, ''))

  try {
    await createOrderApi({
      userAccount,
      staffAccount: '',
      serviceItem: service.name,
      serviceAddress,
      serviceTime: serviceTime.value,
      orderAmount,
      orderStatus: 0,
      payStatus: 1,
      payPassword: pwd,
      remark: remark.value
    })
    paying.value = false
    showPayDialog.value = false
    ElMessage.success('下单支付成功！')
    router.push('/')
  } catch {
    paying.value = false
    ElMessage.error('下单失败，请重试')
  }
}

const closePayDialog = () => {
  showPayDialog.value = false
  pwdDigits.value = ['', '', '', '', '', '']
}
</script>

<style scoped>
.order-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 30px 10px;
}

.order-container {
  max-width: 1000px;
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

.service-card {
  margin-bottom: 16px;
}

.service-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 4px 0;
}

.service-icon {
  font-size: 52px;
  flex-shrink: 0;
}

.service-info {
  flex: 1;
}

.service-info h3 {
  font-size: 20px;
  margin-bottom: 6px;
}

.service-info p {
  color: var(--text-light);
  font-size: 14px;
  line-height: 1.5;
}

.remark-card {
  margin-bottom: 16px;
}

.remark-card h4 {
  margin-bottom: 12px;
  font-size: 15px;
}

.address-select {
  min-height: 60px;
}

.addr-card-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.addr-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border: 2px solid #ebeef5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.addr-card:hover {
  border-color: #ffb0b0;
  background: #fffbfb;
}

.addr-card.active {
  border-color: var(--accent);
  background: #fff5f5;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.12);
}

.addr-card-left {
  padding-top: 2px;
}

.addr-radio-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.addr-radio-dot.checked {
  border-color: var(--accent);
  background: var(--accent);
}

.addr-radio-dot.checked::after {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff;
}

.addr-card-body {
  flex: 1;
  min-width: 0;
}

.addr-card-top {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

.addr-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.addr-phone {
  font-size: 13px;
  color: #909399;
}

.addr-tag {
  font-size: 11px;
  color: var(--accent);
  background: #fff0f0;
  padding: 1px 8px;
  border-radius: 3px;
  border: 1px solid #ffd2d2;
}

.addr-default {
  font-size: 11px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 1px 8px;
  border-radius: 3px;
  border: 1px solid #faecd8;
}

.addr-card-detail {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  word-break: break-all;
}

.addr-tip {
  display: inline-block;
  margin-top: 12px;
  font-size: 13px;
  color: var(--accent);
  cursor: pointer;
  text-decoration: underline;
}

.notice-section {
  margin-bottom: 24px;
}

.notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  cursor: pointer;
  user-select: none;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-h);
}

.notice-arrow {
  transition: transform 0.3s;
  font-size: 14px;
  color: var(--text-light);
}

.notice-arrow.open {
  transform: rotate(180deg);
}

.notice-body {
  overflow: hidden;
  max-height: 0;
  transition: max-height 0.3s ease;
}

.notice-body.open {
  max-height: 300px;
}

.notice-content {
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 6px;
}

.notice-content p {
  font-size: 13px;
  line-height: 2;
  color: var(--text-light);
}

.pay-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.pay-content {
  text-align: center;
  padding: 10px 0;
}

.pay-amount-label {
  font-size: 14px;
  color: var(--text-light);
  margin-bottom: 8px;
}

.pay-amount {
  font-size: 36px;
  font-weight: bold;
  color: var(--accent);
}

.pay-divider {
  height: 1px;
  background: #ebeef5;
  margin: 20px 0;
}

.pay-pwd-label {
  font-size: 14px;
  color: var(--text-light);
  margin-bottom: 16px;
}

.pay-pwd-inputs {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.pwd-dot {
  width: 44px;
  height: 44px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  text-align: center;
  font-size: 20px;
  outline: none;
  transition: border-color 0.2s;
}

.pwd-dot:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.2);
}
</style>