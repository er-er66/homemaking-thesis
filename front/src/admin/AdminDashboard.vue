<template>
  <div class="admin-page">
    <!-- 头部 -->
    <el-menu mode="horizontal" :ellipsis="false" class="admin-header">
      <div class="header-container">
        <div class="header-left">
          <img src="/src/assets/jiazen1.png" alt="家政服务" class="logo-img" />
          <span class="logo-text">家政服务 · 管理后台</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" :src="userAvatar">{{ userInitial }}</el-avatar>
              <span class="user-name">{{ userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/')">返回首页</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-menu>

    <!-- 主体 -->
    <div class="admin-body">
      <!-- 左侧菜单 -->
      <el-menu
        :default-active="activeMenu"
        class="admin-sidebar"
        @select="handleMenuSelect"
      >
        <el-menu-item index="orders">
          <el-icon><List /></el-icon>
          <span>用户查询</span>
        </el-menu-item>
        <el-menu-item index="orderList">
          <el-icon><Document /></el-icon>
          <span>用户下单列表</span>
        </el-menu-item>
        <el-menu-item index="staff">
          <el-icon><UserFilled /></el-icon>
          <span>员工管理</span>
        </el-menu-item>
        <el-menu-item v-if="isSuperAdmin" index="adminList">
          <el-icon><Setting /></el-icon>
          <span>管理员列表</span>
        </el-menu-item>
      </el-menu>

      <!-- 右侧内容 -->
      <div class="admin-content">
        <!-- 用户查询列表 -->
        <div v-if="activeMenu === 'orders'" class="content-panel">
          <h3>用户查询</h3>
          <el-form :model="userSearchForm" inline class="search-form">
            <el-form-item label="姓名">
              <el-input v-model="userSearchForm.name" placeholder="请输入姓名" clearable />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userSearchForm.phone" placeholder="请输入手机号" clearable />
            </el-form-item>
            <el-form-item label="创建时间">
              <el-date-picker
                v-model="userSearchForm.createTimeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchUserList">搜索</el-button>
              <el-button @click="resetUserSearch">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="userList" style="width: 100%" v-loading="userLoading" stripe>
            <el-table-column prop="username" label="用户名称" />
            <el-table-column prop="phone" label="手机号" />
            <el-table-column label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button
                  :type="row.status === 1 ? 'danger' : 'success'"
                  size="small"
                  @click.stop="toggleUserStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button type="primary" size="small" @click="goToDetail(row, 'user')">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 员工管理列表 -->
        <div v-if="activeMenu === 'staff'" class="content-panel">
          <h3>员工管理</h3>
          <el-form :model="staffSearchForm" inline class="search-form">
            <el-form-item label="姓名">
              <el-input v-model="staffSearchForm.name" placeholder="请输入姓名" clearable />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="staffSearchForm.phone" placeholder="请输入手机号" clearable />
            </el-form-item>
            <el-form-item label="创建时间">
              <el-date-picker
                v-model="staffSearchForm.createTimeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchStaffList">搜索</el-button>
              <el-button @click="resetStaffSearch">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="staffList" style="width: 100%" v-loading="staffLoading" stripe>
            <el-table-column label="员工姓名">
              <template #default="{ row }">
                {{ row.realName || row.username || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" />
            <el-table-column prop="skills" label="服务技能">
              <template #default="{ row }">
                <el-tag v-for="skill in parseSkills(row.skills)" :key="skill" size="small" class="skill-tag">
                  {{ skill }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button
                  :type="row.status === 1 ? 'danger' : 'success'"
                  size="small"
                  @click.stop="toggleStaffStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button type="primary" size="small" @click="goToDetail(row, 'staff')">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 用户下单列表 -->
        <div v-if="activeMenu === 'orderList'" class="content-panel">
          <h3>用户下单列表</h3>
          <el-tabs v-model="orderTab" @tab-change="handleOrderTabChange">
            <el-tab-pane label="全部订单" name="all">
              <el-form :model="orderSearchForm" inline class="search-form">
                <el-form-item label="订单编号">
                  <el-input v-model="orderSearchForm.orderNo" placeholder="请输入订单编号" clearable />
                </el-form-item>
                <el-form-item label="用户名">
                  <el-input v-model="orderSearchForm.username" placeholder="请输入用户名" clearable />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="orderSearchForm.phone" placeholder="请输入手机号" clearable />
                </el-form-item>
                <el-form-item label="下单时间">
                  <el-date-picker
                    v-model="orderSearchForm.createTimeRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="searchOrderList">搜索</el-button>
                  <el-button @click="resetOrderSearch">重置</el-button>
                </el-form-item>
              </el-form>
              <el-table :data="orderList" style="width: 100%" v-loading="orderLoading" stripe>
                <el-table-column prop="orderNo" label="订单编号" width="200" />
                <el-table-column prop="userAccount" label="用户账号" />
                <el-table-column prop="serviceItem" label="服务项目" />
                <el-table-column prop="orderAmount" label="订单金额" width="120">
                  <template #default="{ row }">
                    ¥{{ row.orderAmount }}
                  </template>
                </el-table-column>
                <el-table-column label="接单员工" width="120">
                  <template #default="{ row }">
                    {{ row.staffAccount && row.staffAccount !== 'null' ? row.staffAccount : '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="订单状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="orderStatusType(row)">
                      {{ orderStatusText(row) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="支付状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.payStatus === 1 ? 'success' : 'warning'">
                      {{ row.payStatus === 1 ? '已支付' : '未支付' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="下单时间" width="180" />
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button v-if="row.orderStatus === 0 && row.dispatchStatus === 0" type="primary" size="small" @click="showDispatchDialog(row)">
                      派单
                    </el-button>
                    <el-button v-if="row.orderStatus === 0 && row.dispatchStatus === 1" type="success" size="small" disabled>
                      已派单
                    </el-button>
                    <el-button v-if="row.orderStatus === 0 && row.dispatchStatus === 3" type="primary" size="small" @click="showDispatchDialog(row)">
                      派单
                    </el-button>
                    <el-button type="info" size="small" @click="showOrderDetail(row)">
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="未接单" name="unaccepted">
              <el-form :model="orderSearchForm" inline class="search-form">
                <el-form-item label="订单编号">
                  <el-input v-model="orderSearchForm.orderNo" placeholder="请输入订单编号" clearable />
                </el-form-item>
                <el-form-item label="用户名">
                  <el-input v-model="orderSearchForm.username" placeholder="请输入用户名" clearable />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="orderSearchForm.phone" placeholder="请输入手机号" clearable />
                </el-form-item>
                <el-form-item label="下单时间">
                  <el-date-picker
                    v-model="orderSearchForm.createTimeRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="searchOrderList">搜索</el-button>
                  <el-button @click="resetOrderSearch">重置</el-button>
                </el-form-item>
              </el-form>
              <el-table :data="orderList" style="width: 100%" v-loading="orderLoading" stripe>
                <el-table-column prop="orderNo" label="订单编号" width="200" />
                <el-table-column prop="userAccount" label="用户账号" />
                <el-table-column prop="serviceItem" label="服务项目" />
                <el-table-column prop="orderAmount" label="订单金额" width="120">
                  <template #default="{ row }">
                    ¥{{ row.orderAmount }}
                  </template>
                </el-table-column>
                <el-table-column label="订单状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="orderStatusType(row)">
                      {{ orderStatusText(row) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="支付状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.payStatus === 1 ? 'success' : 'warning'">
                      {{ row.payStatus === 1 ? '已支付' : '未支付' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="下单时间" width="180" />
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button v-if="row.dispatchStatus === 0 || row.dispatchStatus === 3" type="primary" size="small" @click="showDispatchDialog(row)">
                      派单
                    </el-button>
                    <el-button v-if="row.dispatchStatus === 1" type="success" size="small" disabled>
                      已派单
                    </el-button>
                    <el-button type="info" size="small" @click="showOrderDetail(row)">
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="已接单" name="accepted">
              <el-form :model="orderSearchForm" inline class="search-form">
                <el-form-item label="订单编号">
                  <el-input v-model="orderSearchForm.orderNo" placeholder="请输入订单编号" clearable />
                </el-form-item>
                <el-form-item label="用户名">
                  <el-input v-model="orderSearchForm.username" placeholder="请输入用户名" clearable />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="orderSearchForm.phone" placeholder="请输入手机号" clearable />
                </el-form-item>
                <el-form-item label="下单时间">
                  <el-date-picker
                    v-model="orderSearchForm.createTimeRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="searchOrderList">搜索</el-button>
                  <el-button @click="resetOrderSearch">重置</el-button>
                </el-form-item>
              </el-form>
              <el-table :data="orderList" style="width: 100%" v-loading="orderLoading" stripe>
                <el-table-column prop="orderNo" label="订单编号" width="200" />
                <el-table-column prop="userAccount" label="用户账号" />
                <el-table-column prop="serviceItem" label="服务项目" />
                <el-table-column prop="orderAmount" label="订单金额" width="120">
                  <template #default="{ row }">
                    ¥{{ row.orderAmount }}
                  </template>
                </el-table-column>
                <el-table-column label="接单员工" width="120">
                  <template #default="{ row }">
                    {{ row.staffAccount && row.staffAccount !== 'null' ? row.staffAccount : '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="订单状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="orderStatusType(row)">
                      {{ orderStatusText(row) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="支付状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.payStatus === 1 ? 'success' : 'warning'">
                      {{ row.payStatus === 1 ? '已支付' : '未支付' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="下单时间" width="180" />
                <el-table-column label="操作" width="120">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" @click="showOrderDetail(row)">
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 管理员列表 -->
        <div v-if="activeMenu === 'adminList'" class="content-panel">
          <h3>管理员列表</h3>
          <el-form :model="adminSearchForm" inline class="search-form">
            <el-form-item label="姓名">
              <el-input v-model="adminSearchForm.name" placeholder="请输入姓名" clearable />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="adminSearchForm.phone" placeholder="请输入手机号" clearable />
            </el-form-item>
            <el-form-item label="创建时间">
              <el-date-picker
                v-model="adminSearchForm.createTimeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchAdminList">搜索</el-button>
              <el-button @click="resetAdminSearch">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="adminList" style="width: 100%" v-loading="adminLoading" stripe>
            <el-table-column prop="username" label="管理员名称" />
            <el-table-column prop="phone" label="手机号" />
            <el-table-column label="角色">
              <template #default="{ row }">
                <el-tag type="danger">{{ String(row.role).startsWith('10') ? '超级管理员' : '普通管理员' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button
                  :type="row.status === 1 ? 'danger' : 'success'"
                  size="small"
                  @click.stop="toggleAdminStatus(row)"
                  :disabled="row.role === 'super_admin'"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button type="primary" size="small" @click="goToDetail(row, 'admin')">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="orderDetailVisible" title="订单详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border size="large" v-loading="orderDetailLoading">
        <el-descriptions-item label="订单编号" :span="2">
          {{ orderDetail.orderNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="用户账号">
          {{ orderDetail.userAccount || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="接单员工">
          {{ orderDetail.staffAccount || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="服务项目">
          {{ orderDetail.serviceItem || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">
          ¥{{ orderDetail.orderAmount || '0' }}
        </el-descriptions-item>
        <el-descriptions-item label="服务地址" :span="2">
          {{ orderDetail.serviceAddress || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="预约时间">
          {{ orderDetail.serviceTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">
          {{ orderDetail.createTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="orderStatusType(orderDetail.orderStatus)">
            {{ orderStatusText(orderDetail.orderStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="orderDetail.payStatus === 1 ? 'success' : 'warning'">
            {{ orderDetail.payStatus === 1 ? '已支付' : '未支付' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">
          {{ orderDetail.updateTime || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="orderDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 派单弹窗 -->
    <el-dialog v-model="dispatchVisible" title="派单" width="600px" destroy-on-close>
      <el-form :model="dispatchForm" label-width="80px">
        <el-form-item label="订单编号">
          <span>{{ dispatchForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="服务项目">
          <span>{{ dispatchForm.serviceItem }}</span>
        </el-form-item>
        <el-form-item label="选择员工" required>
          <el-select v-model="dispatchForm.staffAccount" placeholder="请选择员工" style="width: 100%" @change="handleStaffChange">
            <el-option
              v-for="emp in staffList"
              :key="emp.account"
              :label="`${emp.realName || emp.username} (${emp.account})`"
              :value="emp.account"
            >
              <span style="flex: 1">{{ emp.realName || emp.username }} ({{ emp.account }})</span>
              <el-button
                type="primary"
                size="small"
                @click.stop="showStaffDetail(emp)"
                style="margin-left: 8px"
              >
                详情
              </el-button>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" :loading="dispatchLoading" @click="handleDispatch">确认派单</el-button>
      </template>
    </el-dialog>

    <!-- 员工详情弹窗 -->
    <el-dialog v-model="staffDetailVisible" title="员工详情" width="600px" destroy-on-close>
      <el-descriptions :column="2" border size="large" v-loading="staffDetailLoading">
        <el-descriptions-item label="员工账号">
          {{ staffDetail.account || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          {{ staffDetail.realName || staffDetail.username || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ staffDetail.phone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ staffDetail.gender === 1 ? '男' : staffDetail.gender === 0 ? '女' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="年龄">
          {{ staffDetail.age || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="技能">
          <el-tag v-for="skill in parseSkills(staffDetail.skills)" :key="skill" size="small" style="margin-right: 4px">
            {{ skill }}
          </el-tag>
          <span v-if="!staffDetail.skills">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="工作经验">
          {{ staffDetail.workExperience || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="评分">
          {{ staffDetail.score || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="staffDetail.status === 1 ? 'success' : 'danger'">
            {{ staffDetail.status === 1 ? '在职' : '离职' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="staffDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, List, UserFilled, Document, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserListApi,
  getStaffListApi,
  getStaffDetailApi,
  toggleUserStatusApi,
  toggleStaffStatusApi,
  getOrderListApi,
  getOrderDetailApi,
  getAdminListApi,
  toggleAdminStatusApi,
  dispatchOrderApi
} from '../api/admin'

const router = useRouter()

const activeMenu = ref('orders')
const userList = ref([])
const staffList = ref([])
const userLoading = ref(false)
const staffLoading = ref(false)

const userSearchForm = ref({
  name: '',
  phone: '',
  createTimeRange: []
})

const staffSearchForm = ref({
  name: '',
  phone: '',
  createTimeRange: []
})

const orderList = ref([])
const orderLoading = ref(false)
const orderTab = ref('all')
const orderSearchForm = ref({
  orderNo: '',
  username: '',
  phone: '',
  createTimeRange: []
})

const orderDetailVisible = ref(false)
const orderDetail = ref({})
const orderDetailLoading = ref(false)

const dispatchVisible = ref(false)
const dispatchForm = ref({ orderId: '', orderNo: '', serviceItem: '', staffAccount: '' })
const dispatchLoading = ref(false)

const staffDetailVisible = ref(false)
const staffDetail = ref({})
const staffDetailLoading = ref(false)

const adminList = ref([])
const adminLoading = ref(false)
const adminSearchForm = ref({
  name: '',
  phone: '',
  createTimeRange: []
})

const userName = ref('')
const userAvatar = ref('')

const userInitial = computed(() => {
  return userName.value ? userName.value.charAt(0).toUpperCase() : 'A'
})

onMounted(() => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      userName.value = info.username || '管理员'
      userAvatar.value = info.avatar || ''
    } catch { /* ignore */ }
  }
  fetchUserList()
})

const isSuperAdmin = computed(() => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      return info.role === 'super_admin'
    } catch { return false }
  }
  return false
})

const parseSkills = (skills) => {
  if (!skills) return []
  if (Array.isArray(skills)) return skills
  return skills.split(',').map(s => s.trim()).filter(Boolean)
}

const handleMenuSelect = (index) => {
  activeMenu.value = index
  if (index === 'orders') {
    fetchUserList()
  } else if (index === 'staff') {
    fetchStaffList()
  } else if (index === 'orderList') {
    orderTab.value = 'all'
    fetchOrderList({})
  } else if (index === 'adminList') {
    fetchAdminList()
  }
}

const buildSearchParams = (form) => {
  const params = {}
  if (form.name) params.name = form.name
  if (form.phone) params.phone = form.phone
  if (form.orderNo) params.orderNo = form.orderNo
  if (form.username) params.username = form.username
  if (form.createTimeRange && form.createTimeRange.length === 2) {
    params.startTime = form.createTimeRange[0]
    params.endTime = form.createTimeRange[1]
  }
  return params
}

const fetchUserList = async (searchParams) => {
  userLoading.value = true
  try {
    const res = await getUserListApi(searchParams || {})
    const data = res.data || []
    userList.value = Array.isArray(data) ? data : (data.records || [data])
  } catch {
    userList.value = []
  } finally {
    userLoading.value = false
  }
}

const fetchStaffList = async (searchParams) => {
  staffLoading.value = true
  try {
    const res = await getStaffListApi(searchParams || {})
    const data = res.data || []
    staffList.value = Array.isArray(data) ? data : (data.records || [data])
  } catch {
    staffList.value = []
  } finally {
    staffLoading.value = false
  }
}

const searchUserList = () => {
  const params = buildSearchParams(userSearchForm.value)
  fetchUserList(params)
}

const resetUserSearch = () => {
  userSearchForm.value = { name: '', phone: '', createTimeRange: [] }
  fetchUserList()
}

const searchStaffList = () => {
  const params = buildSearchParams(staffSearchForm.value)
  fetchStaffList(params)
}

const resetStaffSearch = () => {
  staffSearchForm.value = { name: '', phone: '', createTimeRange: [] }
  fetchStaffList()
}

const orderStatusText = (row) => {
  const status = typeof row === 'object' ? row.orderStatus : row
  const dispatchStatus = typeof row === 'object' ? row.dispatchStatus : undefined
  
  if (status === 3) return '用户已取消'
  if (dispatchStatus === 3) return '已拒单'
  
  const map = { 0: '待接单', 1: '已接单', 2: '服务完成' }
  return map[status] ?? '未知'
}

const orderStatusType = (row) => {
  const status = typeof row === 'object' ? row.orderStatus : row
  const dispatchStatus = typeof row === 'object' ? row.dispatchStatus : undefined
  
  if (status === 3) return 'danger'
  if (dispatchStatus === 3) return 'warning'
  
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] ?? 'info'
}

const fetchOrderList = async (searchParams) => {
  orderLoading.value = true
  try {
    const res = await getOrderListApi(searchParams || {})
    let data = res.data || []
    let list = Array.isArray(data) ? data : (data.records || [data])
    
    // 前端过滤订单状态（兼容字符串和数字类型）
    if (searchParams?.orderStatus !== undefined) {
      const targetStatus = Number(searchParams.orderStatus)
      list = list.filter(item => Number(item.orderStatus) === targetStatus)
    }
    
    orderList.value = list
  } catch {
    orderList.value = []
  } finally {
    orderLoading.value = false
  }
}

const searchOrderList = () => {
  const params = buildSearchParams(orderSearchForm.value)
  if (orderTab.value === 'unaccepted') {
    params.orderStatus = 0
  } else if (orderTab.value === 'accepted') {
    params.orderStatus = 1
  }
  fetchOrderList(params)
}

const resetOrderSearch = () => {
  orderSearchForm.value = { orderNo: '', username: '', phone: '', createTimeRange: [] }
  const params = {}
  if (orderTab.value === 'unaccepted') {
    params.orderStatus = 0
  } else if (orderTab.value === 'accepted') {
    params.orderStatus = 1
  }
  fetchOrderList(params)
}

const showOrderDetail = async (row) => {
  orderDetailVisible.value = true
  orderDetailLoading.value = true
  try {
    const res = await getOrderDetailApi(row.id)
    orderDetail.value = res.data || {}
  } catch {
    orderDetail.value = {}
  } finally {
    orderDetailLoading.value = false
  }
}

const handleOrderTabChange = (tab) => {
  const params = buildSearchParams(orderSearchForm.value)
  if (tab === 'unaccepted') {
    params.orderStatus = 0
  } else if (tab === 'accepted') {
    params.orderStatus = 1
  }
  fetchOrderList(params)
}

const showDispatchDialog = async (row) => {
  dispatchForm.value = {
    orderId: row.id,
    orderNo: row.orderNo,
    serviceItem: row.serviceItem,
    staffAccount: ''
  }
  // 打开派单弹窗时加载员工列表
  await fetchStaffList()
  dispatchVisible.value = true
}

const handleDispatch = async () => {
  if (!dispatchForm.value.staffAccount) {
    ElMessage.warning('请选择员工')
    return
  }
  dispatchLoading.value = true
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    let dispatchAdminAccount = ''
    if (userInfoStr) {
      try {
        const info = JSON.parse(userInfoStr)
        dispatchAdminAccount = info.account || ''
      } catch { /* ignore */ }
    }
    
    const res = await dispatchOrderApi({
      id: dispatchForm.value.orderId,
      staffAccount: dispatchForm.value.staffAccount,
      dispatchAdminAccount
    })
    if (res.code === 200) {
      ElMessage.success('派单成功')
      dispatchVisible.value = false
      handleOrderTabChange(orderTab.value)
    } else {
      ElMessage.error(res.message || '派单失败')
    }
  } catch (e) {
    ElMessage.error('派单失败，请重试')
  } finally {
    dispatchLoading.value = false
  }
}

const handleStaffChange = () => {
  // 选择员工时的回调，可在此处做额外处理
}

const showStaffDetail = async (emp) => {
  staffDetailVisible.value = true
  staffDetailLoading.value = true
  try {
    const res = await getStaffDetailApi(emp.id || emp.account)
    staffDetail.value = res.data || emp
  } catch {
    staffDetail.value = emp
  } finally {
    staffDetailLoading.value = false
  }
}

const fetchAdminList = async (searchParams) => {
  adminLoading.value = true
  try {
    const res = await getAdminListApi(searchParams || {})
    const data = res.data || []
    adminList.value = Array.isArray(data) ? data : (data.records || [data])
  } catch {
    adminList.value = []
  } finally {
    adminLoading.value = false
  }
}

const searchAdminList = () => {
  const params = buildSearchParams(adminSearchForm.value)
  fetchAdminList(params)
}

const resetAdminSearch = () => {
  adminSearchForm.value = { name: '', phone: '', createTimeRange: [] }
  fetchAdminList()
}

const toggleAdminStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该管理员吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await toggleAdminStatusApi(row.id)
    if (res.code === 200) {
      row.status = row.status === 1 ? 0 : 1
      ElMessage.success(`${action}成功`)
    }
  } catch { /* 取消 */ }
}

const toggleUserStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await toggleUserStatusApi(row.id)
    if (res.code === 200) {
      row.status = row.status === 1 ? 0 : 1
      ElMessage.success(`${action}成功`)
    }
  } catch { /* 取消 */ }
}

const toggleStaffStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该员工吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await toggleStaffStatusApi(row.id)
    if (res.code === 200) {
      row.status = row.status === 1 ? 0 : 1
      ElMessage.success(`${action}成功`)
    }
  } catch { /* 取消 */ }
}

const goToDetail = (row, type) => {
  if (type === 'admin') {
    router.push(`/admin/admin/${row.id}`)
  } else {
    router.push(`/admin/${type === 'user' ? 'user' : 'emp'}/${row.id}`)
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-header {
  padding: 0 20px;
  flex-shrink: 0;
}

.header-container {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-img {
  width: 36px;
  height: 36px;
  border-radius: 6px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: var(--accent);
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 0;
}

.user-name {
  font-size: 14px;
  color: var(--text-h);
}

.admin-body {
  display: flex;
  flex: 1;
}

.admin-sidebar {
  width: 200px;
  flex-shrink: 0;
  border-right: 1px solid var(--el-border-color-light);
}

.admin-content {
  flex: 1;
  padding: 24px;
  background: #f5f7fa;
  overflow-y: auto;
}

.content-panel {
  background: white;
  padding: 24px;
  border-radius: 8px;
}

.content-panel h3 {
  margin-bottom: 20px;
  font-size: 20px;
}

.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.skill-tag {
  margin: 2px 4px 2px 0;
}
</style>