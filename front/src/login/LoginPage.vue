<template>
  <div class="login-page">
    <div class="login-left">
      <div class="left-content">
        <div class="logo">
          <img src="/src/assets/jiazen1.png" alt="家政服务" />
          <span>家政服务</span>
        </div>
        <h2>欢迎回来</h2>
        <p>登录您的账号，继续享受专业家政服务</p>
      </div>
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
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, var(--accent) 0%, #ff8e8e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.left-content {
  text-align: center;
  color: white;
}

.left-content h2 {
  font-size: 36px;
  color: white;
  margin-bottom: 16px;
}

.left-content p {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.85);
}

.login-right {
  flex: 1;
  background: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-form-wrapper {
  width: 100%;
  max-width: 420px;
}

.login-form-wrapper h3 {
  font-size: 28px;
  margin-bottom: 32px;
  text-align: center;
}

.back-home {
  margin-bottom: 20px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 28px;
  font-weight: bold;
  color: white;
  margin-bottom: 32px;
}

.logo img {
  width: 48px;
  height: 48px;
  border-radius: 8px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.role-group {
  width: 100%;
}

.btn-block {
  width: 100%;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: var(--text-light);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

@media (max-width: 768px) {
  .login-page {
    flex-direction: column;
  }

  .login-left {
    padding: 60px 20px;
  }

  .login-right {
    padding: 40px 20px;
  }
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
  font-size: 12px;
  color: var(--text-light);
  margin-top: 4px;
}

.reset-dialog .el-form-item {
  margin-bottom: 20px;
}
</style>