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
              <el-radio value="001">普通用户</el-radio>
              <el-radio value="002">家政人员</el-radio>
            </el-radio-group></el-form-item>

          <div class="form-options">
            <el-checkbox v-model="form.remember" label="记住我" size="small" />
            <el-link type="primary" :underline="false">忘记密码?</el-link>
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
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginApi } from '../api/admin'

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

onMounted(() => {
  const saveUser = localStorage.getItem('loginUser')
  if (saveUser) {
    const userInfo = JSON.parse(saveUser)
    form.account = userInfo.account || ''
    form.password = userInfo.password || ''
    form.remember = true
  }
})

const parseRole = (code) => {
  const str = String(code || '')
  if (str.charAt(0) === '1') return 'super_admin'
  if (str.charAt(0) === '0') {
    if (str.charAt(1) === '1') return 'admin'
    if (str.charAt(1) === '2') return 'staff'
    if (str.charAt(1) === '3') return 'user'
  }
  return 'user'
}

const handleLogin = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    // 判断输入的是手机号还是账号
    const isPhone = /^1\d{10}$/.test(form.account)
    const res = await loginApi({
      account: isPhone ? '' : form.account,
      phone: isPhone ? form.account : '',
      password: form.password,
      role: form.role || '000'
    })

    if (res.code === 200) {
      const data = res.data || {}
      console.log('后端返回 data:', JSON.stringify(data, null, 2))

      if (form.remember) {
        localStorage.setItem('loginUser', JSON.stringify({ account: form.account, password: form.password }))
      } else {
        localStorage.removeItem('loginUser')
      }

      const token = data.token || 'token-' + Date.now()
      localStorage.setItem('token', token)

      const roleCode = data.role || data.roleCode || data.code || ''
      const userRole = parseRole(roleCode)

      const userInfo = {
        username: data.realName || data.username || data.name || form.account,
        account: data.account || form.account,
        avatar: data.avatar || '',
        role: userRole,
        roleCode: roleCode
      }
      console.log('保存 userInfo:', JSON.stringify(userInfo, null, 2))
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      ElMessage.success('登录成功')
      if (userInfo.role === 'super_admin' || userInfo.role === 'admin') {
        router.replace('/admin/dashboard')
      } else {
        router.replace('/')
      }
    }
  } catch (error) {
    console.error('请求失败:', error)
  }
  loading.value = false
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
</style>