<template>
  <div class="income-page">
    <header class="income-header">
      <el-icon class="back-icon" @click="router.back()"><ArrowLeft /></el-icon>
      <span class="header-title">我的收入</span>
    </header>

    <div class="income-cards" v-loading="loading">
      <div class="income-card total">
        <div class="label">累计收入</div>
        <div class="value">¥{{ money(amount.totalIncome) }}</div>
      </div>
      <div class="income-card pending">
        <div class="label">待结算</div>
        <div class="value">¥{{ money(amount.pendingAmount) }}</div>
      </div>
      <div class="income-card settled">
        <div class="label">已结算</div>
        <div class="value">¥{{ money(amount.settleAmount) }}</div>
      </div>
    </div>

    <div class="tip">
      每单按订单金额的 {{ (ratio * 100).toFixed(0) }}% 计入收入，服务完成满 {{ delayDays }} 天后自动结算
    </div>

    <div class="record-title">收入明细</div>
    <div class="record-list" v-loading="recordsLoading">
      <div v-for="r in records" :key="r.id" class="record-item">
        <div class="record-left">
          <div class="record-order">订单号：{{ r.orderNo || '-' }}</div>
          <div class="record-time">{{ fmtTime(r.createTime) }}</div>
        </div>
        <div class="record-right">
          <div class="record-amount">+¥{{ money(r.incomeAmount) }}</div>
          <el-tag size="small" :type="r.settleStatus === 1 ? 'success' : 'warning'">
            {{ r.settleStatus === 1 ? '已结算' : '待结算' }}
          </el-tag>
        </div>
      </div>
      <el-empty v-if="!recordsLoading && records.length === 0" description="暂无收入记录" />
    </div>

    <div class="table-pagination" v-if="total > pageSize">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="changePage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getMyStaffAmountApi, getStaffAmountRecordsApi, getStaffAmountConfigApi } from '../api/admin'

const router = useRouter()

const amount = ref({ totalIncome: 0, pendingAmount: 0, settleAmount: 0 })
const records = ref([])
const ratio = ref(0.8)
const delayDays = ref(7)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const recordsLoading = ref(false)

const staffAccount = () => {
  try {
    const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return info.account || ''
  } catch {
    return ''
  }
}

const loadAll = async () => {
  const acc = staffAccount()
  // /staff-amount/my 的 staffAccount 是 @RequestParam 必填，不传会 400
  if (!acc) return

  loading.value = true
  try {
    const res = await getMyStaffAmountApi(acc)
    amount.value = (res && res.data) || amount.value
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }

  getStaffAmountConfigApi()
    .then(r => {
      const data = (r && r.data) || {}
      ratio.value = Number(data.incomeRatio ?? 0.8)
      delayDays.value = Number(data.settleDelayDays ?? 7)
    })
    .catch(() => {})

  fetchRecords()
}

const fetchRecords = async () => {
  const acc = staffAccount()
  if (!acc) return

  recordsLoading.value = true
  try {
    // 带 pageNum + pageSize（两个都传才会走分页，返回 {records,total}）
    const res = await getStaffAmountRecordsApi({
      staffAccount: acc,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    const data = (res && res.data) || {}
    if (Array.isArray(data)) {
      records.value = data
      total.value = data.length
    } else {
      records.value = data.records || []
      total.value = Number(data.total) || 0
    }
  } catch {
    records.value = []
    total.value = 0
  } finally {
    recordsLoading.value = false
  }
}

const changePage = (p) => {
  pageNum.value = p
  fetchRecords()
}

const money = (v) => Number(v ?? 0).toFixed(2)

const fmtTime = (v) => {
  if (!v) return '-'
  return String(v).replace('T', ' ').slice(0, 19)
}

onMounted(loadAll)
</script>

<style scoped>
.income-page {
  min-height: 100vh;
  background: var(--surface-page);
  padding: 20px;
}

.income-header {
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 760px;
  margin: 0 auto 20px;
}

.back-icon {
  cursor: pointer;
  font-size: 18px;
  color: var(--text-secondary);
  transition: var(--transition-fast);
}

.back-icon:hover {
  color: var(--brand-500);
  transform: translateX(-2px);
}

.header-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.income-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  max-width: 760px;
  margin: 0 auto 16px;
}

.income-card {
  padding: 20px;
  border-radius: var(--radius-lg);
  background: var(--surface-card);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}

.income-card .label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.income-card .value {
  font-size: 24px;
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
}

/* 三张卡用色区分：累计=品牌色，待结算=警示，已结算=成功 */
.income-card.total .value {
  color: var(--brand-500);
}

.income-card.pending .value {
  color: var(--color-warning);
}

.income-card.settled .value {
  color: var(--color-success);
}

.tip {
  max-width: 760px;
  margin: 0 auto 24px;
  padding: 10px 14px;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  background: var(--brand-50);
  border-radius: var(--radius-md);
}

.record-title {
  max-width: 760px;
  margin: 0 auto 12px;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.record-list {
  max-width: 760px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 80px;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  background: var(--surface-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  transition: var(--transition-base);
}

.record-item:hover {
  border-color: var(--border-brand);
  box-shadow: var(--shadow-sm);
}

.record-order {
  font-size: var(--font-size-base);
  color: var(--text-primary);
  margin-bottom: 4px;
  word-break: break-all;
}

.record-time {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.record-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.record-amount {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-success);
}

.table-pagination {
  max-width: 760px;
  margin: 16px auto 0;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 640px) {
  .income-cards {
    grid-template-columns: 1fr;
  }
}
</style>
