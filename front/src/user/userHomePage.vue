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
          <img src="/src/assets/logo.svg" alt="家政服务" class="logo-img" />
          <span class="logo-text">家政服务</span>
        </div>
        <div class="header-center">
          <el-menu-item v-if="isAdmin" index="admin" @click="router.push('/admin/dashboard')">
            后台管理
          </el-menu-item>
          <el-menu-item index="services">
            <a href="#services">服务项目</a>
          </el-menu-item>
          <el-menu-item index="about">
            <a href="#about">关于我们</a>
          </el-menu-item>
          <el-menu-item index="contact">
            <a href="#contact">联系我们</a>
          </el-menu-item>
          <el-menu-item v-if="!isAdmin" index="merchant" @click="router.push('/merchant')">
            商家消息
            <el-badge
              v-if="totalUnread > 0"
              :value="totalUnread > 99 ? '99+' : totalUnread"
              class="merchant-badge"
            />
          </el-menu-item>
        </div>
        <div class="header-right">
          <template v-if="isLoggedIn && !isAdmin">
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
          <template v-else-if="isLoggedIn && isAdmin">
            <el-button type="danger" @click="handleLogout">退出登录</el-button>
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
      <div class="service-type-tabs">
        <el-tag
          v-for="type in serviceTypes"
          :key="type.value"
          :type="selectedServiceType === type.value ? 'primary' : 'info'"
          :effect="selectedServiceType === type.value ? 'dark' : 'plain'"
          class="service-type-tab"
          @click="changeType(type.value)"
        >
          {{ type.label }}
        </el-tag>
      </div>
      <div v-loading="listLoading" class="service-list-wrap">
        <el-row :gutter="20" justify="center">
          <el-col :xs="24" :sm="12" :md="8" v-for="service in services" :key="service.id">
            <el-card shadow="hover" class="service-card" @click="goToOrder(service)">
              <div class="service-image">
                <img v-if="service.image" :src="service.image" :alt="service.name" />
                <div v-else class="service-icon">{{ service.icon }}</div>
              </div>
              <h3>{{ service.name }}</h3>
              <p>{{ service.description }}</p>
              <el-tag type="danger" size="large">{{ service.price }}</el-tag>
            </el-card>
          </el-col>
        </el-row>
        <el-empty
          v-if="!listLoading && services.length === 0"
          description="该分类下暂无服务项目"
          :image-size="80"
        />
      </div>
      <div class="service-pagination" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="pageNum"
          hide-on-single-page
          @current-change="handlePageChange"
        />
      </div>
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
import { ref, computed, onMounted, nextTick } from 'vue'
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
const services = ref([])        // 当前页展示的数据
const allServices = ref([])     // 后端未分页时的本地全量缓存
const total = ref(0)            // 记录总数（分页器用）
const pageNum = ref(1)
const pageSize = ref(9)
const listLoading = ref(false)
// true = 后端返回了分页结构，翻页/切分类都要重新请求；false = 本地切片
const useServerPaging = ref(false)

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

const userInitial = computed(() => {
  return userName.value ? userName.value.charAt(0).toUpperCase() : 'U'
})

const totalUnread = computed(() => getTotalUnread())

const ICON_POOL = ['🧹', '✨', '🔧', '👶', '🍳', '⏰']

// 后端套餐 -> 页面展示结构
const mapPackage = (pkg, index = 0) => {
  const desc = pkg.packageDesc || ''
  return {
    id: pkg.id,
    name: pkg.packageName,
    description: desc.length > 15 ? desc.slice(0, 15) + '...' : desc,
    price: `¥${pkg.packagePrice}${pkg.unitText || ''}`,
    image: (pkg.packageImg || '').replace(/`/g, ''),
    icon: ICON_POOL[index % ICON_POOL.length],
    serviceType: pkg.serviceType || 0
  }
}

// 本地分页兜底：后端还没做分页时，先按分类过滤再切片
const applyLocalPage = () => {
  const filtered = selectedServiceType.value === 0
    ? allServices.value
    : allServices.value.filter(item => item.serviceType === selectedServiceType.value)
  total.value = filtered.length
  const start = (pageNum.value - 1) * pageSize.value
  services.value = filtered.slice(start, start + pageSize.value)
}

const fetchPackages = async () => {
  listLoading.value = true
  try {
    const params = {
      // 0 = 上架（1 是下架）。之前传 1 是错的，只是后端没认这个参数才没暴露
      status: 0,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    // 分类 0 = 全部，不传 serviceType，交给后端查全量
    if (selectedServiceType.value !== 0) {
      params.serviceType = selectedServiceType.value
    }

    const res = await getPackageListApi(params)
    const data = res && res.data

    // 情况一：后端已分页（{ records|list: [], total }），直接用后端那一页
    const rows = data && (data.records || data.list)
    if (Array.isArray(rows)) {
      useServerPaging.value = true
      services.value = rows.map(mapPackage)
      total.value = Number(data.total ?? data.totalCount ?? rows.length) || 0
      return
    }

    // 情况二：后端仍返回全量数组，缓存下来本地切片（后端改好后无需再动前端）
    if (Array.isArray(data)) {
      useServerPaging.value = false
      allServices.value = data.map(mapPackage)
      applyLocalPage()
      return
    }

    throw new Error('套餐列表数据格式异常')
  } catch (e) {
    console.warn('获取套餐列表失败，使用内置数据兜底:', e)
    useServerPaging.value = false
    allServices.value = getDefaultServices()
    applyLocalPage()
  } finally {
    listLoading.value = false
  }
}

const getDefaultServices = () => {
  return [
    { id: 1, name: '日常钟点保洁', description: '全屋除尘、拖地、擦台面', price: '¥50/小时', icon: '🧹', serviceType: 1 },
    { id: 2, name: '深度保洁', description: '清理卫生死角、油污、水垢', price: '¥200起', icon: '✨', serviceType: 1 },
    { id: 3, name: '新房开荒保洁', description: '装修后精细清洁', price: '¥300起', icon: '🧹', serviceType: 1 },
    { id: 4, name: '擦玻璃', description: '专业玻璃清洁服务', price: '¥100起', icon: '✨', serviceType: 1 },
    { id: 5, name: '油烟机清洗', description: '深度拆洗油烟机', price: '¥80起', icon: '🔧', serviceType: 1 },
    { id: 6, name: '空调清洗', description: '挂机/柜机清洗', price: '¥60起', icon: '🔧', serviceType: 1 },
    { id: 7, name: '地板打蜡', description: '木质地板保养护理', price: '¥150起', icon: '✨', serviceType: 1 },
    { id: 8, name: '除甲醛', description: '专业甲醛检测治理', price: '¥500起', icon: '🌿', serviceType: 1 },
    { id: 9, name: '商业保洁', description: '办公室、门店清洁', price: '¥200起', icon: '🏢', serviceType: 1 },
    { id: 10, name: '住家保姆', description: '做饭、洗衣、打扫收纳', price: '¥5000起', icon: '👩‍🍳', serviceType: 2 },
    { id: 11, name: '白班保姆', description: '固定时段家务服务', price: '¥3500起', icon: '👩‍🍳', serviceType: 2 },
    { id: 12, name: '钟点家务', description: '做饭+简单家务', price: '¥40/小时', icon: '⏰', serviceType: 2 },
    { id: 13, name: '月嫂', description: '产妇护理+新生儿照护', price: '¥12000起', icon: '👶', serviceType: 3 },
    { id: 14, name: '育婴师', description: '0-3岁宝宝照料早教', price: '¥8000起', icon: '👶', serviceType: 3 },
    { id: 15, name: '催乳师', description: '专业通乳按摩服务', price: '¥300起', icon: '💆', serviceType: 3 },
    { id: 16, name: '陪产护理', description: '医院陪产护理服务', price: '¥300/天', icon: '🏥', serviceType: 3 },
    { id: 17, name: '养老陪护', description: '居家老人日常照料', price: '¥4000起', icon: '👴', serviceType: 4 },
    { id: 18, name: '病患护理', description: '住院/居家病人看护', price: '¥300/天', icon: '🏥', serviceType: 4 },
    { id: 19, name: '医院护工', description: '专业陪护服务', price: '¥280/天', icon: '🏥', serviceType: 4 },
    { id: 20, name: '康复护理', description: '术后康复护理', price: '¥350/天', icon: '💪', serviceType: 4 },
    { id: 21, name: '整理收纳师', description: '全屋空间规划收纳', price: '¥200起', icon: '📦', serviceType: 5 },
    { id: 22, name: '搬家服务', description: '打包搬运一站式服务', price: '¥300起', icon: '🚚', serviceType: 5 },
    { id: 23, name: '宠物家政', description: '上门喂养遛狗服务', price: '¥50起', icon: '🐕', serviceType: 5 },
    { id: 24, name: '高端家庭管家', description: '统筹家务管理服务', price: '¥10000起', icon: '🎩', serviceType: 5 },
    { id: 25, name: '家电维修', description: '各种家电故障维修', price: '¥50起', icon: '🔧', serviceType: 6 },
    { id: 26, name: '管道疏通', description: '厨房卫生间管道疏通', price: '¥80起', icon: '🔧', serviceType: 6 },
    { id: 27, name: '上门做饭', description: '私厨上门烹饪服务', price: '¥150起', icon: '🍳', serviceType: 6 },
    { id: 28, name: '绿植养护', description: '室内绿植定期养护', price: '¥100起', icon: '🌿', serviceType: 6 }
  ]
}

// 切换分类：回到第 1 页。后端分页时重新请求，本地分页时只需重切片
const changeType = (value) => {
  if (selectedServiceType.value === value) return
  selectedServiceType.value = value
  pageNum.value = 1
  if (useServerPaging.value) {
    fetchPackages()
  } else {
    applyLocalPage()
  }
}

// 翻页
const handlePageChange = (page) => {
  pageNum.value = page
  if (useServerPaging.value) {
    fetchPackages()
  } else {
    applyLocalPage()
  }
  // 已经在视口上方才滚回去，避免页面莫名跳动
  nextTick(() => {
    const el = document.getElementById('services')
    if (el && el.getBoundingClientRect().top < 0) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  })
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
      const role = userInfo.role || userInfo.roleCode || ''
      isAdmin.value = role.startsWith('10') || role.startsWith('01') || role === 'super_admin' || role === 'admin'
      if (userInfo.roleCode === '002' || userInfo.roleCode === '02' || userInfo.role === 'staff') {
        router.replace('/staff/home')
      } else {
        // 普通用户和管理员：获取未读消息（管理员不自动跳转）
        if (!isAdmin.value) {
          fetchUnreadMessages(userInfo.account)
        }
      }
    } catch {
      isLoggedIn.value = false
    }
  } else {
    isLoggedIn.value = false
  }
  // 未登录也要展示服务项目，所以放在登录分支外面，只请求一次
  fetchPackages()
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
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再预约')
    router.push('/login')
    return
  }
  router.push('/publish-order')
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
      icon: service.icon,
      // 套餐图片也要带过去，否则确认订单页只能退回图标，和首页展示不一致
      image: service.image || ''
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
  background-color: var(--surface-page);
  padding-bottom: var(--space-8);
}

/* ---------- 吸顶导航：玻璃拟态 ---------- */
.header-menu {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0 20px;
  background-color: rgba(255, 255, 255, .86);
  backdrop-filter: blur(14px) saturate(180%);
  -webkit-backdrop-filter: blur(14px) saturate(180%);
  border-bottom: 1px solid var(--border-light);
  box-shadow: var(--shadow-xs);
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
  /* 圆角已画在 SVG 里，这里不要再加 border-radius，否则会把徽标边角削掉 */
  flex-shrink: 0;
  filter: drop-shadow(0 2px 6px rgba(var(--brand-rgb), .22));
}

.logo-text {
  font-size: 20px;
  font-weight: var(--font-weight-bold);
  letter-spacing: .5px;
  background-image: var(--gradient-brand);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.header-center {
  display: flex;
  flex: 1;
  border-bottom: none !important;
}

.header-center .el-menu-item {
  border-bottom: none !important;
  border-radius: var(--radius-md);
  font-weight: var(--font-weight-medium);
  transition: var(--transition-base);
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
  padding: 4px 10px;
  border-radius: var(--radius-full);
  transition: var(--transition-base);
}

.user-info:hover {
  background: var(--brand-50);
}

.user-avatar {
  flex-shrink: 0;
}

.user-name {
  font-size: var(--font-size-base);
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ---------- 横幅：渐变 + 双光晕装饰 ---------- */
.banner {
  position: relative;
  overflow: hidden;
  background: var(--gradient-brand);
  padding: 96px 20px 104px;
  text-align: center;
  margin-bottom: var(--space-8);
}

.banner::before {
  content: '';
  position: absolute;
  top: -180px;
  right: -110px;
  width: 520px;
  height: 520px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, .34) 0%, rgba(255, 255, 255, 0) 70%);
  pointer-events: none;
}

.banner::after {
  content: '';
  position: absolute;
  bottom: -210px;
  left: -130px;
  width: 560px;
  height: 560px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(122, 44, 14, .28) 0%, rgba(122, 44, 14, 0) 70%);
  pointer-events: none;
}

.banner-container {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
}

.banner-content {
  color: #fff;
  animation: appFadeInUp var(--duration-slow) var(--ease-out) both;
}

.banner-content h1 {
  font-size: 52px;
  font-weight: var(--font-weight-bold);
  margin-bottom: 20px;
  color: #fff;
  letter-spacing: 3px;
  text-shadow: 0 4px 24px rgba(122, 44, 14, .25);
}

.banner-content p {
  font-size: var(--font-size-lg);
  margin-bottom: 34px;
  color: rgba(255, 255, 255, .92);
  letter-spacing: .6px;
  line-height: var(--leading-relaxed);
}

/* ---------- 区块统一卡片化 ---------- */
.services,
.about,
.contact {
  max-width: 1200px;
  margin: 0 auto var(--space-6);
  padding: 64px 40px;
  background-color: var(--surface-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
}

.services {
  /* 翻页后 scrollIntoView 不会被吸顶导航挡住 */
  scroll-margin-top: 84px;
}

/* 加载/空状态时别让区块塌掉 */
.service-list-wrap {
  min-height: 220px;
}

.service-pagination {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

.section-title {
  position: relative;
  text-align: center;
  margin-bottom: 44px;
  padding-bottom: 16px;
  font-size: 32px;
  font-weight: var(--font-weight-bold);
  letter-spacing: 1px;
}

/* 标题下方的渐变短横线 */
.section-title::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 56px;
  height: 4px;
  border-radius: var(--radius-full);
  background-image: var(--gradient-brand);
}

/* ---------- 服务类型筛选：胶囊标签 ---------- */
.service-type-tabs {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  margin-bottom: 36px;
}

.service-type-tab {
  cursor: pointer;
  height: auto;
  padding: 10px 20px;
  font-size: 15px;
  border-radius: var(--radius-full);
  transition: var(--transition-base);
}

.service-type-tab:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

/* 选中态（el-tag type=primary effect=dark）加品牌光晕 */
.service-type-tab.el-tag--primary {
  border-color: transparent;
  box-shadow: var(--shadow-brand-soft);
}

/* ---------- 服务卡片 ---------- */
.service-card {
  text-align: center;
  margin-bottom: 20px;
  cursor: pointer;
  transition: var(--transition-base);
  height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 16px;
  box-sizing: border-box;
  overflow: hidden !important;
  border-radius: var(--radius-lg);
}

.service-card :deep(.el-card__body) {
  overflow: hidden !important;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0;
}

.service-card:hover {
  transform: translateY(-6px);
  border-color: var(--border-brand);
  box-shadow: var(--shadow-lg);
}

.service-image {
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--brand-50);
  margin: 0 auto 12px;
  box-shadow: inset 0 0 0 1px var(--border-light);
}

.service-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--duration-slow) var(--ease-out);
}

/* 悬停时图片轻微放大，卡片更有生命力 */
.service-card:hover .service-image img {
  transform: scale(1.06);
}

.service-icon {
  font-size: 40px;
  line-height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  text-align: center;
}

.service-card h3 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  margin: 0 0 8px;
  flex-shrink: 0;
}

.service-card p {
  color: var(--text-secondary);
  margin: 0 0 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.5;
  font-size: var(--font-size-base);
  flex-shrink: 0;
}

.service-card .el-tag {
  flex-shrink: 0;
  margin-top: auto;
}

/* ---------- 关于我们 ---------- */
.about {
  background-image: linear-gradient(135deg, var(--brand-50) 0%, var(--neutral-100) 100%);
}

.about .about-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.about-content p {
  font-size: var(--font-size-md);
  line-height: var(--leading-relaxed);
  color: var(--text-secondary);
  max-width: 800px;
  margin: 0 auto 40px;
}

/* ---------- 联系我们 ---------- */
.contact-item {
  display: flex;
  align-items: center;
  gap: 15px;
  justify-content: center;
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  transition: var(--transition-base);
}

.contact-item:hover {
  background-color: var(--brand-50);
}

.contact-item p {
  color: var(--text-secondary);
  font-size: var(--font-size-base);
}

.contact-item strong {
  font-size: var(--font-size-md);
  color: var(--text-primary);
  font-weight: var(--font-weight-semibold);
}

/* ---------- 页脚 ---------- */
.footer {
  background-image: linear-gradient(135deg, var(--neutral-900) 0%, #3D2E22 100%);
  padding: 36px 20px;
  text-align: center;
  border-top: 3px solid transparent;
  border-image: var(--gradient-brand) 1;
}

.footer p {
  color: rgba(255, 255, 255, .66);
  font-size: var(--font-size-base);
  letter-spacing: .4px;
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

  .banner {
    padding: 64px 20px 72px;
  }

  .banner-content h1 {
    font-size: 32px;
    letter-spacing: 2px;
  }

  .banner-content p {
    font-size: var(--font-size-base);
  }

  .services,
  .about,
  .contact {
    padding: 40px 20px;
    border-radius: var(--radius-lg);
  }

  .section-title {
    font-size: 26px;
    margin-bottom: 32px;
  }
}
</style>