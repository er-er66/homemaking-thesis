<template>
  <div class="detail-page">
    <div class="detail-header">
      <el-button @click="router.back()" :icon="ArrowLeft" text>← 返回</el-button>
      <h2>{{ type === 'user' ? '用户详情' : type === 'staff' ? '员工详情' : '管理员详情' }}</h2>
    </div>

    <div class="detail-card" v-loading="loading">
      <div class="detail-avatar-section">
        <el-avatar :size="100" :src="detail.avatar" shape="square">
          {{ detailInitial }}
        </el-avatar>
        <h3>{{ detail.realName || detail.username || '未知' }}</h3>
        <el-tag :type="detail.status === 1 ? 'success' : 'danger'" size="large">
          {{ detail.status === 1 ? '正常' : '已禁用' }}
        </el-tag>
      </div>

      <el-divider />

      <el-descriptions :column="2" border size="large">
        <el-descriptions-item label="账号">
          {{ detail.username || detail.account || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ detail.phone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ detail.gender === 1 ? '女' : detail.gender === 0 ? '男' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ detail.createTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="type === 'staff'" label="服务技能" :span="2">
          <el-tag v-for="skill in parseSkills(detail.skills)" :key="skill" class="skill-tag">
            {{ skill }}
          </el-tag>
          <span v-if="!parseSkills(detail.skills).length">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="账号状态" :span="2">
          <el-tag :type="detail.status === 1 ? 'success' : 'danger'">
            {{ detail.status === 1 ? '正常使用' : '已被禁用' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getUserDetailApi, getStaffDetailApi, getAdminDetailApi } from '../api/admin'

const route = useRoute()
const router = useRouter()

const type = computed(() => {
  if (route.path.includes('/admin/user')) return 'user'
  if (route.path.includes('/admin/emp')) return 'staff'
  return 'admin'
})
const detail = ref({})
const loading = ref(false)

const detailInitial = computed(() => {
  const name = detail.value.realName || detail.value.username || '?'
  return name.charAt(0).toUpperCase()
})

const parseSkills = (skills) => {
  if (!skills) return []
  if (Array.isArray(skills)) return skills
  return skills.split(',').map(s => s.trim()).filter(Boolean)
}

onMounted(() => {
  fetchDetail()
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const apiMap = {
      user: getUserDetailApi,
      staff: getStaffDetailApi,
      admin: getAdminDetailApi
    }
    const api = apiMap[type.value]
    const res = await api(id)
    detail.value = res.data || {}
  } catch {
    detail.value = {}
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 24px;
}

.detail-header {
  max-width: 900px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.detail-header h2 {
  font-size: 22px;
}

.detail-card {
  max-width: 900px;
  margin: 0 auto;
  background: white;
  padding: 32px;
  border-radius: 8px;
}

.detail-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.detail-avatar-section h3 {
  font-size: 24px;
}

.skill-tag {
  margin: 2px 4px 2px 0;
}
</style>