<template>
  <div class="admin-profile-page">
    <div class="profile-container">
      <el-link type="info" :underline="false" @click="router.push('/admin/dashboard')" class="back-link">
        ← 返回管理后台
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
            <h3>
              {{ userName }}
              <el-icon class="edit-name-icon" @click="showNameEditDialog = true"><Edit /></el-icon>
            </h3>
            <p>{{ userPhone }}</p>
          </div>
        </div>
      </el-card>

      <el-card class="menu-card">
        <div class="menu-item" @click="togglePwdPanel">
          <div class="menu-left">
            <el-icon size="20" color="#409eff"><Lock /></el-icon>
            <span>修改登录密码</span>
          </div>
          <el-icon class="menu-arrow" :class="{ open: activePanel === 'pwd' }"><ArrowRight /></el-icon>
        </div>
        <div class="menu-panel" :class="{ open: activePanel === 'pwd' }">
          <el-form :model="pwdForm" label-position="top" size="large">
            <el-form-item label="旧密码">
              <el-input v-model="pwdForm.oldPwd" type="password" placeholder="请输入旧密码" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPwd" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="pwdForm.confirmPwd" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-button type="primary" :loading="pwdLoading" @click="handleChangePwd">
              确认修改
            </el-button>
          </el-form>
        </div>
      </el-card>

      <el-card class="menu-card">
        <div class="menu-item" @click="activePanel = activePanel === 'phone' ? '' : 'phone'">
          <div class="menu-left">
            <el-icon size="20" color="#67c23a"><Phone /></el-icon>
            <span>更换手机号</span>
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
            <el-button type="primary" :loading="phoneLoading" @click="handleChangePhone">确认更换</el-button>
          </el-form>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="showNameEditDialog" title="修改用户名" width="360px" destroy-on-close>
      <el-form :model="nameForm" label-position="top" size="large">
        <el-form-item label="新用户名">
          <el-input v-model="nameForm.newName" placeholder="请输入新用户名（至少2个字符）" maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNameEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="nameLoading" @click="handleChangeName">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, ArrowRight, Phone, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadAvatarApi, sendCodeForUserApi, changeAdminPasswordApi, changeAdminPhoneApi, changeAdminNameApi } from '../api/admin'

const router = useRouter()

const activePanel = ref('')
const avatar = ref('')
const userName = ref('')
const userPhone = ref('')
const userAccount = ref('')

const userInitial = computed(() => {
  return userName.value ? userName.value.charAt(0).toUpperCase() : 'A'
})

const pwdForm = ref({ oldPwd: '', newPwd: '', confirmPwd: '' })
const pwdLoading = ref(false)

const phoneForm = ref({ newPhone: '', code: '' })
const phoneLoading = ref(false)
const phoneCountdown = ref(0)

const nameForm = ref({ newName: '' })
const nameLoading = ref(false)
const showNameEditDialog = ref(false)
const userRoleCode = ref('')

onMounted(() => {
  loadUserInfo()
})

const loadUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      userName.value = info.username || info.account || '管理员'
      avatar.value = info.avatar || ''
      userPhone.value = info.phone || info.account || ''
      userAccount.value = info.account || ''
      userRoleCode.value = info.roleCode || ''
    } catch { /* ignore */ }
  }
}

const updateUserInfo = (updates) => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      Object.assign(info, updates)
      localStorage.setItem('userInfo', JSON.stringify(info))
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

const togglePwdPanel = () => {
  if (activePanel.value === 'pwd') {
    activePanel.value = ''
    return
  }
  activePanel.value = 'pwd'
  pwdForm.value = { oldPwd: '', newPwd: '', confirmPwd: '' }
}

const handleChangePwd = async () => {
  const { oldPwd, newPwd, confirmPwd } = pwdForm.value
  if (!oldPwd) return ElMessage.warning('请输入旧密码')
  if (!newPwd || newPwd.length < 6) return ElMessage.warning('新密码至少6位')
  if (newPwd !== confirmPwd) return ElMessage.warning('两次密码不一致')
  pwdLoading.value = true
  try {
    const res = await changeAdminPasswordApi({ account: userAccount.value, oldPwd, newPwd })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      pwdForm.value = { oldPwd: '', newPwd: '', confirmPwd: '' }
      activePanel.value = ''
    } else {
      ElMessage.error(res.message || '修改失败')
    }
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
    const res = await changeAdminPhoneApi({ account: userAccount.value, newPhone, code })
    if (res.code === 200) {
      userPhone.value = newPhone
      updateUserInfo({ phone: newPhone })
      ElMessage.success('手机号更换成功')
      phoneForm.value = { newPhone: '', code: '' }
      activePanel.value = ''
    } else {
      ElMessage.error(res.message || '更换失败')
    }
  } catch {
    ElMessage.error('更换失败，请重试')
  } finally {
    phoneLoading.value = false
  }
}

const sendPhoneCode = async () => {
  const { newPhone } = phoneForm.value
  if (!newPhone) return ElMessage.warning('请先输入新手机号')
  if (!/^1[3-9]\d{9}$/.test(newPhone)) return ElMessage.warning('请输入有效的手机号')
  try {
    const res = await sendCodeForUserApi(newPhone)
    phoneCountdown.value = 60
    const timer = setInterval(() => {
      phoneCountdown.value--
      if (phoneCountdown.value <= 0) clearInterval(timer)
    }, 1000)
    const code = res.data || res.message || ''
    phoneForm.value.code = String(code)
    ElMessage.success('验证码已发送')
  } catch {
    ElMessage.error('发送验证码失败')
  }
}

const handleChangeName = async () => {
  const { newName } = nameForm.value
  if (!newName || !newName.trim()) return ElMessage.warning('请输入新用户名')
  if (newName.trim().length < 2) return ElMessage.warning('用户名至少2个字符')
  nameLoading.value = true
  try {
    await changeAdminNameApi({ account: userAccount.value, role:userRoleCode.value,newName: newName.trim() })
    userName.value = newName.trim()
    updateUserInfo({ username: newName.trim() })
    ElMessage.success('用户名修改成功')
    nameForm.value = { newName: '' }
    showNameEditDialog.value = false
  } catch {
    ElMessage.error('修改失败，请重试')
  } finally {
    nameLoading.value = false
  }
}
</script>

<style scoped>
.admin-profile-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.profile-container {
  max-width: 600px;
  margin: 0 auto;
}

.back-link {
  margin-bottom: 20px;
  font-size: 14px;
}

h2 {
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
}

.profile-card {
  margin-bottom: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-uploader {
  cursor: pointer;
  position: relative;
}

.change-text {
  display: block;
  text-align: center;
  font-size: 12px;
  color: #409eff;
  margin-top: 4px;
}

.user-base h3 {
  margin: 0 0 4px;
  font-size: 18px;
}

.user-base p {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.edit-name-icon {
  cursor: pointer;
  margin-left: 8px;
  color: #409eff;
  vertical-align: middle;
}

.edit-name-icon:hover {
  color: #66b1ff;
}

.menu-card {
  margin-bottom: 16px;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  cursor: pointer;
  transition: color 0.2s;
}

.menu-item:hover {
  color: #409eff;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
}

.menu-arrow {
  transition: transform 0.3s;
  color: #999;
}

.menu-arrow.open {
  transform: rotate(90deg);
}

.menu-panel {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.menu-panel.open {
  max-height: 500px;
}

.code-row {
  display: flex;
  gap: 10px;
}

.code-row .el-input {
  flex: 1;
}
</style>