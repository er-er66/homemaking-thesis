<template>
  <div class="login-page">
    <div class="login-left auth-brand">
      <!-- 装饰层：点阵 / 光晕 / 涟漪 / 光斑 / 房屋水印 / 扫光，全部纯 CSS，不参与交互 -->
      <div class="auth-deco" aria-hidden="true">
        <span class="auth-deco__grid"></span>
        <span class="auth-deco__glow auth-deco__glow--a"></span>
        <span class="auth-deco__glow auth-deco__glow--b"></span>
        <span class="auth-deco__glow auth-deco__glow--c"></span>
        <span class="auth-deco__ripple auth-deco__ripple--1"></span>
        <span class="auth-deco__ripple auth-deco__ripple--2"></span>
        <span class="auth-orb auth-orb--1"></span>
        <span class="auth-orb auth-orb--2"></span>
        <span class="auth-orb auth-orb--3"></span>
        <svg class="auth-deco__house" viewBox="0 0 200 200" fill="none">
          <path
            d="M100 24 184 96v74a10 10 0 0 1-10 10H26a10 10 0 0 1-10-10V96L100 24Z"
            stroke="rgba(255,255,255,.18)"
            stroke-width="3"
            stroke-linejoin="round"
          />
          <path
            d="M76 180v-46a12 12 0 0 1 12-12h24a12 12 0 0 1 12 12v46"
            stroke="rgba(255,255,255,.18)"
            stroke-width="3"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <span class="auth-deco__sheen"></span>
      </div>

      <div class="auth-content">
        <div class="auth-logo">
          <img src="/src/assets/logo.svg" alt="家政服务" />
          <span>家政服务</span>
        </div>

        <h2 class="auth-title">欢迎回来</h2>
        <div class="auth-rule" aria-hidden="true"><i></i></div>
        <p class="auth-subtitle">登录您的账号，继续享受专业家政服务</p>

        <ul class="auth-features">
          <li class="auth-feature"><i class="auth-feature__icon">🧹</i>专业保洁</li>
          <li class="auth-feature"><i class="auth-feature__icon">🛡️</i>持证上岗</li>
          <li class="auth-feature"><i class="auth-feature__icon">⏱️</i>准时上门</li>
        </ul>

        <div class="auth-stats">
          <div class="auth-stat">
            <strong>10万+</strong>
            <span>服务家庭</span>
          </div>
          <div class="auth-stat">
            <strong>98%</strong>
            <span>好评率</span>
          </div>
          <div class="auth-stat">
            <strong>2000+</strong>
            <span>认证人员</span>
          </div>
        </div>
      </div>

      <p class="auth-foot">用心服务每一个家 · 让生活更轻松</p>
    </div>

    <div class="login-right">
      <div class="login-form-wrapper">
        <el-link type="info" :underline="false" @click="router.push('/')" class="back-home">
          ← 返回首页
        </el-link>
        <h3>欢迎登录</h3>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="handleLogin"
        >
          <el-form-item label="账号/手机号" prop="account">
            <el-input
              v-model="form.account"
              placeholder="请输入账号或手机号"
              size="large"
              clearable
            />
            
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item label="登录身份">
            <el-radio-group v-model="form.role" class="role-group">
              <el-radio value="000">管理员</el-radio>
              <el-radio value="001">普通用户</el-radio>
              <el-radio value="002">家政人员</el-radio>
            </el-radio-group>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="form.remember" label="记住我" size="small" />
            <el-link type="primary" :underline="false" @click="showResetDialog = true">忘记密码?</el-link>
          </div>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="btn-block"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <el-divider />

        <div class="login-footer">
          <span>还没有账号?</span>
          <el-link type="primary" :underline="false" @click="router.push('/register')">立即注册</el-link>
        </div>
      </div>
    </div>

    <!-- 忘记密码弹窗 -->
    <el-dialog
      v-model="showResetDialog"
      title="重置密码"
      width="420px"
      :close-on-click-modal="false"
      class="reset-dialog"
    >
      <el-form
        ref="resetFormRef"
        :model="resetForm"
        :rules="resetRules"
        label-position="top"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="resetForm.phone"
            placeholder="请输入注册手机号"
            size="large"
            clearable
          />
        </el-form-item>

     

        <el-form-item label="验证码" prop="code">
          <div class="code-row">
            <el-input
              v-model="resetForm.code"
              placeholder="请输入验证码"
              size="large"
              maxlength="6"
              clearable
              autocomplete="off"
            />
            <el-button
              type="primary"
              size="large"
              :disabled="codeCountdown > 0"
              :loading="sendingCode"
              @click="handleSendCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            size="large"
            show-password
            clearable
            autocomplete="new-password"
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="resetForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            size="large"
            show-password
            clearable
            autocomplete="new-password"
          />
        </el-form-item>   <el-form-item label="重置身份" prop="role">
          <el-radio-group v-model="resetForm.role">
            <el-radio value="001">管理员</el-radio>
            <el-radio value="002">家政人员</el-radio>
            <el-radio value="003">普通用户</el-radio>
          </el-radio-group>
          <div class="role-tip">因一个手机号可以注册不同身份，所以请选择重置密码的身份</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showResetDialog = false">取消</el-button>
        <el-button type="primary" :loading="resetLoading" @click="handleResetPassword">
          确认重置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginApi, sendResetCodeApi, resetPasswordApi } from '../api/admin'

const router = useRouter()
const formRef = ref(null)

const form = reactive({
  account: '',
  password: '',
  role: '',
  remember: false
})

const rules = {
  account: [
    { required: true, message: '请输入账号或手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const loading = ref(false)

// 重置密码相关
const showResetDialog = ref(false)
const resetFormRef = ref(null)
const resetForm = reactive({
  phone: '',
  role: '001',
  code: '',
  newPassword: '',
  confirmPassword: ''
})
const resetRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择重置身份', trigger: 'change' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
const sendingCode = ref(false)
const codeCountdown = ref(0)
const resetLoading = ref(false)
let countdownTimer = null

onMounted(() => {
  const saveUser = localStorage.getItem('loginUser')
  if (saveUser) {
    const userInfo = JSON.parse(saveUser)
    form.account = userInfo.account || ''
    form.password = userInfo.password || ''
    form.remember = true
  }
})

const handleLogin = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const isPhone = /^1\d{10}$/.test(form.account)
    const res = await loginApi({
      account: isPhone ? '' : form.account,
      phone: isPhone ? form.account : '',
      password: form.password,
      role: form.role || '001'
    })

    if (res && res.data) {
      const data = res.data

      if (form.remember) {
        localStorage.setItem('loginUser', JSON.stringify({ account: form.account, password: form.password }))
      } else {
        localStorage.removeItem('loginUser')
      }

      const token = data.token || ''
      localStorage.setItem('token', token)

      const roleCode = data.roleCode || ''

      const userInfo = {
        username: data.username || form.account,
        account: data.account || form.account,
        avatar: data.avatar || '',
        roleCode: roleCode,
        phone: data.phone || ''
      }
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      ElMessage.success('登录成功')

      if (roleCode === '10' || roleCode === '01') {
        router.replace('/admin/dashboard')
      } else if (roleCode === '002' || roleCode === '02') {
        router.replace('/staff/home')
      } else {
        router.replace('/')
      }
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('请求失败:', error)
    ElMessage.error(error.response?.data?.message || '登录失败，请重试')
  }
  loading.value = false
}

const handleSendCode = async () => {
  if (!resetForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  if (!/^1\d{10}$/.test(resetForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  sendingCode.value = true
  try {
    const res = await sendResetCodeApi(resetForm.phone)
    if (res && res.data) {
      const code = res.data
      resetForm.code = String(code)
      ElMessage.success(`验证码已发送：${code}`)

      codeCountdown.value = 60
      countdownTimer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
          clearInterval(countdownTimer)
          countdownTimer = null
        }
      }, 1000)
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.response?.data?.message || '发送验证码失败')
  }
  sendingCode.value = false
}

const handleResetPassword = async () => {
  if (!resetFormRef.value) return
  try {
    await resetFormRef.value.validate()
  } catch {
    return
  }

  resetLoading.value = true
  try {
    const res = await resetPasswordApi({
      phone: resetForm.phone,
      role: resetForm.role,
      code: resetForm.code,
      newPassword: resetForm.newPassword
    })

    if (res && (res.code === 200 || res.success)) {
      ElMessage.success('密码重置成功，请使用新密码登录')
      showResetDialog.value = false
      resetForm.phone = ''
      resetForm.role = '001'
      resetForm.code = ''
      resetForm.newPassword = ''
      resetForm.confirmPassword = ''
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
      codeCountdown.value = 0
    } else {
      ElMessage.error(res.message || '重置失败')
    }
  } catch (error) {
    console.error('重置密码失败:', error)
    ElMessage.error(error.response?.data?.message || '重置密码失败')
  }
  resetLoading.value = false
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background-color: var(--surface-page);
}

/* ---------- 左侧品牌区 ----------
   全部视觉样式在 src/styles/auth-brand.css（登录/注册共用）。
   这里只保留 .login-left 这个钩子类，供下方移动端媒体查询做布局覆盖。 */

/* ---------- 右侧表单区 ---------- */
.login-right {
  position: relative;
  overflow: hidden;
  flex: 1;
  background-color: var(--surface-card);
  /* 右上角一点极淡的品牌色，避免整块纯白显得空 */
  background-image: radial-gradient(circle at 85% 12%, rgba(var(--brand-rgb), .07) 0%, rgba(var(--brand-rgb), 0) 45%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-form-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  animation: appFadeInUp var(--duration-slow) var(--ease-out) 80ms both;
}

.login-form-wrapper h3 {
  font-size: 30px;
  font-weight: var(--font-weight-bold);
  letter-spacing: 1px;
  margin-bottom: 32px;
  text-align: center;
}

.back-home {
  display: inline-block;
  margin-bottom: 20px;
  font-size: var(--font-size-base);
  transition: var(--transition-fast);
}

.back-home:hover {
  transform: translateX(-3px);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.role-group {
  width: 100%;
  display: flex;
  gap: var(--space-5);
  flex-wrap: wrap;
}

.btn-block {
  width: 100%;
  height: 46px;
  font-size: var(--font-size-md);
  letter-spacing: 3px;
  border-radius: var(--radius-md);
}

.login-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: var(--space-5);
  font-size: var(--font-size-base);
  color: var(--text-secondary);
}

.code-row {
  display: flex;
  gap: 12px;
}

.code-row .el-input {
  flex: 1;
}

.code-row .el-button {
  flex-shrink: 0;
  width: 130px;
}

.role-tip {
  margin-top: 4px;
  font-size: var(--font-size-xs);
  line-height: var(--leading-snug);
  color: var(--text-secondary);
}

.reset-dialog .el-form-item {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .login-page {
    flex-direction: column;
  }

  .login-left {
    flex: none;
    min-height: 280px;
    padding: 56px 20px;
  }

  .login-left h2 {
    font-size: 32px;
  }

  .login-right {
    flex: 1;
    padding: 40px 20px;
  }
}
</style>