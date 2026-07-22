<template>
  <div class="home-page">
    <!-- 头部导航 -->
    <el-menu
      mode="horizontal"
      :ellipsis="false"
      class="header-menu"
    >
      <div class="header-container">
        <div class="header-left">
          <img src="/src/assets/jiazen1.png" alt="家政服务" class="logo-img" />
          <span class="logo-text">家政服务</span>
        </div>
        <div class="header-center">
          <el-menu-item index="services">
            <a href="#services">服务项目</a>
          </el-menu-item>
          <el-menu-item index="about">
            <a href="#about">关于我们</a>
          </el-menu-item>
          <el-menu-item index="contact">
            <a href="#contact">联系我们</a>
          </el-menu-item>
          <el-menu-item index="merchant" @click="router.push('/merchant')">
            商家消息
            <el-badge
              v-if="totalUnread > 0"
              :value="totalUnread > 99 ? '99+' : totalUnread"
              class="merchant-badge"
            />
          </el-menu-item>
        </div>
        <div class="header-right">
          <template v-if="isLoggedIn">
            <el-dropdown trigger="click">
              <span class="user-info">
                <el-avatar :size="32" :src="userAvatar" class="user-avatar">
                  {{ userInitial }}
                </el-avatar>
                <span class="user-name">{{ userName }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goToProfile">个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="goToLogin">登录</el-button>
            <el-button type="primary" @click="goToRegister">注册</el-button>
          </template>
        </div>
      </div>
    </el-menu>

    <!-- 横幅区域 -->
    <section class="banner">
      <div class="banner-container">
        <div class="banner-content">
          <h1>专业家政服务</h1>
          <p>为您提供优质、专业、贴心的家政服务，让您的生活更美好</p>
          <el-button type="primary" size="large" round @click="bookService">立即预约</el-button>
        </div>
      </div>
    </section>

    <!-- 服务项目 -->
    <section id="services" class="services">
      <h2 class="section-title">服务项目</h2>
      <el-row :gutter="20" justify="center">
        <el-col :xs="24" :sm="12" :md="8" v-for="service in services" :key="service.id">
          <el-card shadow="hover" class="service-card" @click="goToOrder(service)">
            <div class="service-icon">{{ service.icon }}</div>
            <h3>{{ service.name }}</h3>
            <p>{{ service.description }}</p>
            <el-tag type="danger" size="large">{{ service.price }}</el-tag>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 关于我们 -->
    <section id="about" class="about">
      <h2 class="section-title">关于我们</h2>
      <div class="about-content">
        <p>我们是一家专业的家政服务公司，拥有多年的服务经验和一支专业的服务团队。我们致力于为每一位客户提供优质、高效、贴心的家政服务。</p>
        <el-row :gutter="40" justify="center">
          <el-col :span="8">
            <el-statistic title="服务满意度" :value="100" suffix="%" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="服务客户" :value="5000" suffix="+" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="全天候服务" :value="24" suffix="h" />
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 联系我们 -->
    <section id="contact" class="contact">
      <h2 class="section-title">联系我们</h2>
      <el-row :gutter="40" justify="center">
        <el-col :span="6">
          <div class="contact-item">
            <el-icon :size="32" color="var(--accent)"><Phone /></el-icon>
            <div>
              <p>服务热线</p>
              <strong>400-888-8888</strong>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="contact-item">
            <el-icon :size="32" color="var(--accent)"><Message /></el-icon>
            <div>
              <p>电子邮箱</p>
              <strong>service@homemaking.com</strong>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="contact-item">
            <el-icon :size="32" color="var(--accent)"><Location /></el-icon>
            <div>
              <p>服务地址</p>
              <strong>北京市朝阳区XX街道XX号</strong>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <p>© 2024 家政服务. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Phone, Message, Location, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getTotalUnread, initUnreadFromApi, setMerchants } from '../store/merchantStore'
import { getUnreadMessagesApi, getMerchantListApi, getPackageListApi } from '../api/admin'

const router = useRouter()

const isLoggedIn = ref(false)
const isAdmin = ref(false)
const userName = ref('')
const userAvatar = ref('')
const services = ref([])

const userInitial = computed(() => {
  return userName.value ? userName.value.charAt(0).toUpperCase() : 'U'
})

const totalUnread = computed(() => getTotalUnread())

const fetchPackages = async () => {
  try {
    const res = await getPackageListApi({ status: 1 })
    if (res && res.data && Array.isArray(res.data)) {
      services.value = res.data.map((pkg, index) => ({
        id: pkg.id,
        name: pkg.package_name,
        description: pkg.package_desc || '',
        price: `¥${pkg.package_price}${pkg.unit_text || ''}`,
        icon: ['🧹', '✨', '🔧', '👶', '👩‍🍼', '⏰'][index % 6]
      }))
    }
  } catch (e) {
    console.warn('获取套餐列表失败:', e)
  }
}

const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  if (token && userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      isLoggedIn.value = true
      userName.value = userInfo.username || userInfo.account || '用户'
      userAvatar.value = userInfo.avatar || ''
      isAdmin.value = userInfo.role === 'super_admin'
      if (isAdmin.value) {
        router.replace('/admin/dashboard')
      } else if (userInfo.role === 'staff' || userInfo.roleCode === '002') {
        router.replace('/staff/home')
      } else {
        // 普通用户：获取未读消息
        fetchUnreadMessages(userInfo.account)
      }
    } catch {
      isLoggedIn.value = false
    }
  } else {
    isLoggedIn.value = false
  }
}

const fetchUnreadMessages = async (userId) => {
  if (!userId) return
  try {
    const res = await getMerchantListApi(userId)
    if (res && res.data && Array.isArray(res.data)) {
      setMerchants(res.data)
    }
  } catch (e) {
    console.warn('获取商家列表失败:', e)
  }
}

onMounted(() => {
  checkLoginStatus()
  fetchPackages()
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  isLoggedIn.value = false
  isAdmin.value = false
  userName.value = ''
  userAvatar.value = ''
  ElMessage.success('已退出登录')
  router.push('/')
}

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}

const bookService = () => {
  router.push('/login')
}

const goToOrder = (service) => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再下单')
    router.push('/login')
    return
  }
  router.push({
    path: '/order',
    query: {
      id: service.id,
      name: service.name,
      description: service.description,
      price: service.price,
      icon: service.icon
    }
  })
}

const goToProfile = () => {
  router.push('/profile')
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}

.header-menu {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0 20px;
}

.header-container {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-right: 40px;
}

.logo-img {
  width: 36px;
  height: 36px;
  border-radius: 6px;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: var(--accent);
}

.header-center {
  display: flex;
  flex: 1;
  border-bottom: none !important;
}

.header-center .el-menu-item {
  border-bottom: none !important;
}

.header-right {
  display: flex;
  gap: 10px;
  margin-left: auto;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f5f5;
}

.user-avatar {
  flex-shrink: 0;
}

.user-name {
  font-size: 14px;
  color: var(--text-h, #333);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 12px;
  color: #999;
}

.banner {
  background: linear-gradient(135deg, var(--accent) 0%, #ff8e8e 100%);
  padding: 80px 20px;
  text-align: center;
}

.banner-container {
  max-width: 1200px;
  margin: 0 auto;
}

.banner-content {
  color: white;
}

.banner-content h1 {
  font-size: 48px;
  margin-bottom: 20px;
  color: white;
}

.banner-content p {
  font-size: 18px;
  margin-bottom: 30px;
  color: rgba(255, 255, 255, 0.9);
}

.services {
  padding: 60px 20px;
  background: var(--bg-white);
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  margin-bottom: 40px;
  font-size: 32px;
}

.service-card {
  text-align: center;
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s;
}

.service-card:hover {
  transform: translateY(-4px);
}

.service-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.service-card h3 {
  font-size: 20px;
  margin-bottom: 10px;
}

.service-card p {
  color: var(--text-light);
  margin-bottom: 15px;
}

.about {
  padding: 60px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.about .about-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.about-content p {
  font-size: 16px;
  line-height: 180%;
  color: var(--text-light);
  max-width: 800px;
  margin: 0 auto 40px;
}

.contact {
  padding: 60px 20px;
  background: var(--bg-white);
  max-width: 1200px;
  margin: 0 auto;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 15px;
  justify-content: center;
}

.contact-item p {
  color: var(--text-light);
  font-size: 14px;
}

.contact-item strong {
  font-size: 16px;
  color: var(--text-h);
}

.footer {
  background: var(--primary);
  padding: 30px 20px;
  text-align: center;
}

.footer p {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.merchant-badge {
  margin-left: 4px;
}

@media (max-width: 768px) {
  .header-container {
    flex-wrap: wrap;
    justify-content: center;
    gap: 10px;
  }

  .header-left {
    margin-right: 0;
  }

  .banner-content h1 {
    font-size: 32px;
  }
}
</style>