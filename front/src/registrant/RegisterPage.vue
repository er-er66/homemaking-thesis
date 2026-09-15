<template>
  <div class="register-page">
    <div class="register-left">
      <div class="left-content">
        <div class="logo">
          <img src="/src/assets/jiazen1.png" alt="家政服务" />
          <span>家政服务</span>
        </div>
        <h2>创建您的账号</h2>
        <p>注册即享专业家政服务，让生活更轻松</p>
      </div>
    </div>

    <div class="register-right">
      <div class="register-form-wrapper">
        <el-link type="info" :underline="false" @click="router.push('/')" class="back-home">
          ← 返回首页
        </el-link>
        <h3>注册账号</h3>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="handleRegister"
        >
          <el-form-item label="用户名称" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名称"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender" class="gender-group">
              <el-radio value="0">男</el-radio>
              <el-radio value="1">女</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="头像">
            <el-upload
              ref="uploadRef"
              class="avatar-uploader"
              :show-file-list="false"
              :http-request="handleUpload"
              :before-upload="beforeUpload"
              :on-change="handleChange"
              accept="image/*"
            >
              <div class="avatar-upload-trigger">
                <el-avatar
                  :size="80"
                  :src="avatarPreview"
                  class="avatar-preview"
                  shape="square"
                >
                  <el-icon :size="32"><Plus /></el-icon>
                </el-avatar>
                <div class="avatar-hint">{{ uploading ? '上传中...' : (avatarPreview ? '点击更换头像' : '点击上传头像') }}</div>
              </div>
            </el-upload>
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item label="验证码" prop="code">
            <div class="code-input-group">
              <el-input
                v-model="form.code"
                placeholder="请输入验证码"
                size="large"
                clearable
                autocomplete="one-time-code"
              />
              <el-button
                size="large"
                :disabled="countdown > 0"
                @click="sendCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码(至少6位)"
              size="large"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item label="注册身份">
            <el-radio-group v-model="form.role" class="role-group">
              <el-radio value="01">普通用户</el-radio>
              <el-radio value="02">家政人员</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item v-if="form.role === '02'" label="服务技能" prop="skills">
            <el-checkbox-group v-model="form.skills" class="skills-group">
              <el-checkbox v-for="skill in skillOptions" :key="skill" :value="skill" :label="skill" />
            </el-checkbox-group>
          </el-form-item>

          <el-form-item prop="agreed">
            <el-checkbox v-model="form.agreed">
              <span>我已阅读并同意<el-link type="primary" :underline="false">《用户协议》</el-link>和<el-link type="primary" :underline="false">《隐私政策》</el-link></span>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="btn-block"
              :loading="loading"
              @click="handleRegister"
            >
              {{ loading ? '注册中...' : '注册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <el-divider />

        <div class="register-footer">
          <span>已有账号?</span>
          <el-link type="primary" :underline="false" @click="router.push('/login')">立即登录</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { registerApi, sendCodeApi, uploadAvatarApi, getPackageListApi } from '../api/admin'

const router = useRouter()
const formRef = ref(null)

const form = reactive({
  username: '',
  gender: '',
  avatar: '',
  phone: '',
  code: '',
  password: '',
  confirmPassword: '',
 role: '01',
  skills: [],
  agreed: false
})

const uploading = ref(false)
const avatarPreview = ref('')
const skillOptions = ref([])

const fetchSkills = async () => {
  if (skillOptions.value.length > 0) return
  try {
    // 0 = 上架，只取在售套餐（原来传 1 是下架，属于笔误，后端目前忽略该参数才没暴露）
    const res = await getPackageListApi({ status: 0 })
    if (res && res.data && Array.isArray(res.data)) {
      skillOptions.value = res.data.map(pkg => pkg.package_name)
    }
  } catch (e) {
    console.warn('获取技能列表失败:', e)
  }
}

const handleRoleChange = (role) => {
  if (role === '02') {
    fetchSkills()
  }
}

onMounted(() => {
  if (form.role === '02') {
    fetchSkills()
  }
})

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleChange = (uploadFile) => {
  if (uploadFile.raw) {
    avatarPreview.value = URL.createObjectURL(uploadFile.raw)
  }
}

const handleUpload = async (options) => {
  uploading.value = true
  const formData = new FormData()
  formData.append('avatar', options.file)
  try {
    const res = await uploadAvatarApi(formData)
    if (res.code === 200) {
      const avatarUrl = typeof res.data === 'string' ? res.data : (res.data?.url || '')
      form.avatar = avatarUrl
      avatarPreview.value = avatarUrl
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error('上传失败:', error)
  } finally {
    uploading.value = false
  }
}

const validateConfirmPassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入确认密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名称', trigger: 'blur' },
    { min: 2, message: '用户名称至少2个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreed: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback(new Error('请同意用户协议和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const loading = ref(false)
const countdown = ref(0)

const sendCode = async () => {
  if (!form.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入有效的手机号')
    return
  }

  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)

  try {
    const res = await sendCodeApi(form.phone)
    const code = res.data || res.message || ''
    form.code = String(code)
    ElMessageBox.alert(
      `您的验证码是：<strong style="font-size:24px;color:#e74c3c;letter-spacing:4px;">${code}</strong>`,
      '模拟获取验证码',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '我知道了',
        type: 'success'
      }
    )
  } catch {
    countdown.value = 0
  }
}

const handleRegister = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await registerApi({
      username: form.username,
      gender: form.gender,
      avatar: form.avatar,
      phone: form.phone,
      code: form.code,
      password: form.password,
      confirmPassword: form.confirmPassword,
      role: form.role,
      skills: form.skills
    })
    ElMessage.success('注册成功!')
    router.replace('/login')
  } catch (error) {
    // 错误已在拦截器中处理
  }
  loading.value = false
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
}

.register-left {
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

.register-right {
  flex: 1;
  background: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.register-form-wrapper {
  width: 100%;
  max-width: 420px;
}

.register-form-wrapper h3 {
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

.code-input-group {
  display: flex;
  gap: 10px;
}

.code-input-group .el-input {
  flex: 1;
}

.btn-block {
  width: 100%;
}

.role-group {
  width: 100%;
}

.skills-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: var(--text-light);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.avatar-uploader {
  display: block;
}

.avatar-upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.avatar-preview {
  border: 2px dashed var(--el-border-color);
  border-radius: 8px;
  transition: border-color 0.3s;
}

.avatar-preview:hover {
  border-color: var(--el-color-primary);
}

.avatar-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 768px) {
  .register-page {
    flex-direction: column;
  }

  .register-left {
    padding: 60px 20px;
  }

  .register-right {
    padding: 40px 20px;
  }
}
</style>