<template>
  <div class="admin-page">
    <!-- 头部 -->
    <el-menu mode="horizontal" :ellipsis="false" class="admin-header">
      <div class="header-container">
        <div class="header-left">
          <img src="/src/assets/logo.svg" alt="家政服务" class="logo-img" />
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
                <el-dropdown-item @click="router.push('/admin/profile')">个人中心</el-dropdown-item>
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
        <el-menu-item index="package">
          <el-icon><ShoppingCart /></el-icon>
          <span>套餐管理</span>
        </el-menu-item>
        <el-menu-item index="messages">
          <el-icon><ChatDotRound /></el-icon>
          <span>消息列表</span>
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
            <el-table-column prop="account" label="用户账号" />
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
          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="userTotal"
              :page-size="userPageSize"
              :page-sizes="PAGE_SIZE_OPTIONS"
              :current-page="userPageNum"
              @current-change="userChangePage"
              @size-change="userChangeSize"
            />
          </div>
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
          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="staffTotal"
              :page-size="staffPageSize"
              :page-sizes="PAGE_SIZE_OPTIONS"
              :current-page="staffPageNum"
              @current-change="staffChangePage"
              @size-change="staffChangeSize"
            />
          </div>
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
                <el-table-column label="订单状态" width="130"  centent>
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

                         <el-button type="info" size="small" @click="showOrderDetail(row)">
                      详情
                    </el-button>
                    <el-button v-if="row.orderStatus !== 1 && row.orderStatus !== 2" type="primary" size="small" @click="showDispatchDialog(row)">
                      派单
                    </el-button>
               
                  </template>
                </el-table-column>
              </el-table>
              <div class="table-pagination">
                <el-pagination
                  background
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="orderTotal"
                  :page-size="orderPageSize"
                  :page-sizes="PAGE_SIZE_OPTIONS"
                  :current-page="orderPageNum"
                  @current-change="orderChangePage"
                  @size-change="orderChangeSize"
                />
              </div>
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
                    <el-button v-if="row.orderStatus !== 1 && row.orderStatus !== 2" type="primary" size="small" @click="showDispatchDialog(row)">
                      派单
                    </el-button>
                    <el-button type="info" size="small" @click="showOrderDetail(row)">
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="table-pagination">
                <el-pagination
                  background
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="orderTotal"
                  :page-size="orderPageSize"
                  :page-sizes="PAGE_SIZE_OPTIONS"
                  :current-page="orderPageNum"
                  @current-change="orderChangePage"
                  @size-change="orderChangeSize"
                />
              </div>
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
              <div class="table-pagination">
                <el-pagination
                  background
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="orderTotal"
                  :page-size="orderPageSize"
                  :page-sizes="PAGE_SIZE_OPTIONS"
                  :current-page="orderPageNum"
                  @current-change="orderChangePage"
                  @size-change="orderChangeSize"
                />
              </div>
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
          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="adminTotal"
              :page-size="adminPageSize"
              :page-sizes="PAGE_SIZE_OPTIONS"
              :current-page="adminPageNum"
              @current-change="adminChangePage"
              @size-change="adminChangeSize"
            />
          </div>
        </div>

        <!-- 套餐管理列表 -->
        <div v-if="activeMenu === 'package'" class="content-panel">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h3 style="margin: 0;">套餐管理</h3>
            <el-button type="primary" @click="showPackageDialog()">新增套餐</el-button>
          </div>
          <el-form :model="packageSearchForm" inline class="search-form">
            <el-form-item label="套餐名称">
              <el-input v-model="packageSearchForm.packageName" placeholder="请输入套餐名称" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="packageSearchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
                <el-option label="上架" :value="0" />
                <el-option label="下架" :value="1" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchPackageList">搜索</el-button>
              <el-button @click="resetPackageSearch">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="packageList" style="width: 100%" v-loading="packageLoading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="封面" width="100">
              <template #default="{ row }">
                <img v-if="row.packageImg" :src="row.packageImg" class="table-cover-img" />
                <span v-else class="no-cover">暂无封面</span>
              </template>
            </el-table-column>
            <el-table-column prop="packageName" label="套餐名称" width="150" />
            <el-table-column prop="packagePrice" label="套餐价格" width="120">
              <template #default="{ row }">
                ¥{{ row.packagePrice }}
              </template>
            </el-table-column>
            <el-table-column prop="packageDesc" label="套餐简介" show-overflow-tooltip />
            <el-table-column prop="unitText" label="计价单位" width="100" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'danger'">
                  {{ row.status === 0 ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column label="有效期" width="120">
              <template #default="{ row }">
                {{ row.expireStatus === 0 ? '永久' : '限时' }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="showPackageDialog(row)">修改</el-button>
                <el-button
                  :type="row.status === 0 ? 'warning' : 'success'"
                  size="small"
                  @click="togglePackageStatus(row)"
                >
                  {{ row.status === 0 ? '下架' : '上架' }}
                </el-button>
                <el-button type="danger" size="small" @click="deletePackage(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="packageTotal"
              :page-size="packagePageSize"
              :page-sizes="PAGE_SIZE_OPTIONS"
              :current-page="packagePageNum"
              @current-change="packageChangePage"
              @size-change="packageChangeSize"
            />
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-if="activeMenu === 'messages'" class="content-panel">
          <h3>消息列表</h3>
          <el-tabs v-model="messageTab" @tab-change="handleMessageTabChange">
            <el-tab-pane label="用户消息" name="user">
              <el-table :data="userMessageList" style="width: 100%" v-loading="userMessageLoading" stripe>
                <el-table-column label="用户账号" width="150">
                  <template #default="{ row }">
                    {{ row.userAccount || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="用户名称" width="120">
                  <template #default="{ row }">
                    {{ row.userName || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="最新消息" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.lastMessage || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="消息时间" width="180">
                  <template #default="{ row }">
                    {{ row.lastTime || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" @click="openUserChat(row)">
                      查看消息
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="!userMessageLoading && userMessageList.length === 0" description="暂无用户消息" />
              <div class="table-pagination">
                <el-pagination
                  background
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="userMessageTotal"
                  :page-size="userMessagePageSize"
                  :page-sizes="PAGE_SIZE_OPTIONS"
                  :current-page="userMessagePageNum"
                  @current-change="userMessageChangePage"
                  @size-change="userMessageChangeSize"
                />
              </div>
            </el-tab-pane>
            <el-tab-pane label="家政人员消息" name="staff">
              <el-table :data="staffMessageList" style="width: 100%" v-loading="staffMessageLoading" stripe>
                <el-table-column label="员工账号" width="150">
                  <template #default="{ row }">
                    {{ row.staffAccount || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="家政人员名称" width="120">
                  <template #default="{ row }">
                    {{ row.staffName || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="最新消息" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.lastMessage || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="消息时间" width="180">
                  <template #default="{ row }">
                    {{ row.lastTime || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" @click="openStaffChat(row)">
                      查看消息
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="!staffMessageLoading && staffMessageList.length === 0" description="暂无家政人员消息" />
              <div class="table-pagination">
                <el-pagination
                  background
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="staffMessageTotal"
                  :page-size="staffMessagePageSize"
                  :page-sizes="PAGE_SIZE_OPTIONS"
                  :current-page="staffMessagePageNum"
                  @current-change="staffMessageChangePage"
                  @size-change="staffMessageChangeSize"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
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

    <!-- 套餐新增/修改弹窗 -->
    <el-dialog v-model="packageDialogVisible" :title="packageDialogTitle" width="600px" destroy-on-close>
      <el-form :model="packageForm" label-width="100px" ref="packageFormRef" :rules="packageRules">
        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="packageForm.packageName" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="套餐价格" prop="packagePrice">
          <el-input-number v-model="packageForm.packagePrice" :min="0" :precision="2" :step="0.01" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="套餐简介" prop="packageDesc">
          <el-input v-model="packageForm.packageDesc" type="textarea" :rows="3" placeholder="请输入套餐简介、包含服务内容" />
        </el-form-item>
        <el-form-item label="套餐封面" prop="packageImg">
          <div class="cover-selector">
            <div class="preset-icons">
              <div
                v-for="icon in presetIcons.filter(i => i && i.trim())"
                :key="icon"
                :class="['preset-icon-item', { active: selectedPresetIcon === icon }]"
                @click="selectPresetIcon(icon)"
              >
                <span class="icon-emoji">{{ icon }}</span>
              </div>
            </div>
            <div class="cover-upload-area">
              <el-upload
                class="package-cover-uploader"
                :show-file-list="false"
                :http-request="handlePackageCoverUpload"
                :before-upload="beforePackageCoverUpload"
                accept="image/*"
              >
                <img v-if="packageCoverPreview" :src="packageCoverPreview" class="package-cover-preview" />
                <el-icon v-else class="package-cover-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-hint">支持 jpg/png 格式，不超过 2MB</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="计价单位" prop="unitText">
          <el-select v-model="packageForm.unitText" placeholder="请选择计价单位" style="width: 100%;">
            <el-option label="/小时" value="/小时" />
            <el-option label="/次" value="/次" />
            <el-option label="/台" value="/台" />
            <el-option label="/月" value="/月" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务类别" prop="serviceType">
          <el-select v-model="packageForm.serviceType" placeholder="请选择服务类别" style="width: 100%;">
            <el-option
              v-for="item in serviceTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序权重" prop="sort">
          <el-input-number v-model="packageForm.sort" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="有效期类型" prop="expireStatus">
          <el-radio-group v-model="packageForm.expireStatus">
            <el-radio :label="0">永久上架</el-radio>
            <el-radio :label="1">限时上架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上架时间" v-if="packageForm.expireStatus === 1" prop="expireStartTime">
          <el-date-picker
            v-model="packageForm.expireStartTime"
            type="datetime"
            placeholder="选择上架起始时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="到期时间" v-if="packageForm.expireStatus === 1" prop="expireEndTime">
          <el-date-picker
            v-model="packageForm.expireEndTime"
            type="datetime"
            placeholder="选择到期结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="packageForm.status">
            <el-radio :label="0">上架</el-radio>
            <el-radio :label="1">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="packageDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="packageSubmitLoading" @click="submitPackage">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, List, UserFilled, Document, Setting, ShoppingCart, Plus, ChatDotRound } from '@element-plus/icons-vue'
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
  dispatchOrderApi,
  getPackageListApi,
  createPackageApi,
  updatePackageApi,
  deletePackageApi,
  togglePackageStatusApi,
  uploadPackageCoverApi,
  getChatHistoryApi,
  getChatRoomListApi
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

const packageList = ref([])
const packageLoading = ref(false)
const packageSearchForm = ref({
  packageName: '',
  status: undefined
})
const packageDialogVisible = ref(false)
const packageDialogTitle = ref('新增套餐')
const packageFormRef = ref(null)
const packageSubmitLoading = ref(false)
const packageForm = ref({
  id: null,
  packageName: '',
  packagePrice: 0,
  packageDesc: '',
  packageImg: '',
  unitText: '/次',
  serviceType: 1,
  status: 0,
  sort: 0,
  expireStatus: 0,
  expireStartTime: '',
  expireEndTime: ''
})

const serviceTypeOptions = [
  { value: 1, label: '保洁清洁类' },
  { value: 2, label: '家务保姆类' },
  { value: 3, label: '母婴护理类' },
  { value: 4, label: '老人/病患照护类' },
  { value: 5, label: '新兴细分家政服务' },
  { value: 6, label: '其他配套家政' }
]

const packageRules = {
  packageName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  packagePrice: [{ required: true, message: '请输入套餐价格', trigger: 'blur' }],
  unitText: [{ required: true, message: '请选择计价单位', trigger: 'change' }]
}

const packageCoverPreview = ref('')

const presetIcons = ref(['🧹', '✨', '🔧', '️📕 ' , '👩🍼', '⏰'])
const selectedPresetIcon = ref('')

const messageTab = ref('user')
const userMessageList = ref([])
const userMessageLoading = ref(false)
const staffMessageList = ref([])
const staffMessageLoading = ref(false)

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
      const role = info.role || info.roleCode || ''
      return role.startsWith('10') || role === 'super_admin'
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
  } else if (index === 'package') {
    fetchPackageList()
  } else if (index === 'messages') {
    fetchUserMessageList()
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

/* ==================== 分页公共逻辑 ====================
 * 项目里 6 个列表（用户查询 / 用户下单列表 / 员工管理 / 管理员列表 / 套餐管理 / 消息列表）
 * 都用同一套模式：
 *   1. 请求带上 pageNum / pageSize
 *   2. 后端返回数组            → 前端本地切片（后端分页上线前后都能用，前端不用再改）
 *      后端返回 { records, total } → 直接用后端那一页
 *   3. 搜索 / 切 Tab / 翻页 都要重置到第 1 页
 * 所以抽成 createPager()，各列表只声明自己的 ref 和请求函数即可。
 */
const DEFAULT_PAGE_SIZE = 10

const isPageResult = (data) => {
  if (!data || Array.isArray(data) || typeof data !== 'object') return false
  return Array.isArray(data.records || data.list)
}

/**
 * @param {object}   listRef     列表数据 ref
 * @param {object}   loadingRef  加载状态 ref
 * @param {function} requestFn   请求函数，形如 (params) => api(params)
 * @param {object}   options     { pageSize, clientFilter, clientSort }
 *   clientFilter: 后端不支持的筛选条件，在前端兜底过滤（如订单状态）
 *   clientSort:   前端兜底排序，接收数组返回新数组。后端分页模式下排序由后端负责
 */
const createPager = (listRef, loadingRef, requestFn, options = {}) => {
  const pageNum = ref(1)
  const pageSize = ref(options.pageSize || DEFAULT_PAGE_SIZE)
  const total = ref(0)
  // 后端是否已支持分页；未支持时走本地切片
  const serverPaging = ref(false)
  // 本地分页时的全量数据
  let allRows = []
  let lastParams = {}

  const clientFilter = options.clientFilter || null
  const clientSort = options.clientSort || null

  const applyLocal = () => {
    let rows = clientFilter ? clientFilter(allRows) : allRows
    if (clientSort) rows = clientSort(rows)
    total.value = rows.length
    const start = (pageNum.value - 1) * pageSize.value
    listRef.value = rows.slice(start, start + pageSize.value)
  }

  const fetchPage = async (params = {}) => {
    lastParams = params
    loadingRef.value = true
    try {
      const res = await requestFn({
        ...params,
        pageNum: pageNum.value,
        pageSize: pageSize.value
      })
      const data = (res && res.data) ?? []

      if (isPageResult(data)) {
        serverPaging.value = true
        listRef.value = data.records || data.list || []
        total.value = Number(data.total ?? data.totalCount ?? listRef.value.length) || 0
      } else if (Array.isArray(data)) {
        serverPaging.value = false
        allRows = data
        applyLocal()
      } else {
        listRef.value = []
        total.value = 0
      }
    } catch {
      listRef.value = []
      total.value = 0
    } finally {
      loadingRef.value = false
    }
  }

  /**
   * 拉取全量数据并本地分页，返回全量数组（不写 listRef）。
   * 供需要「先拿全量再自行映射」的场景使用，如消息列表要 join 会话记录。
   * 索引拉取失败时返回 []。
   */
  const fetchAll = async (fullApi) => {
    loadingRef.value = true
    try {
      const res = await (fullApi || requestFn)({})
      const data = (res && res.data) ?? []
      allRows = Array.isArray(data) ? data : (data.records || data.list || [])
    } catch {
      allRows = []
    } finally {
      loadingRef.value = false
    }
    serverPaging.value = false
    pageNum.value = 1
    return allRows
  }

  // 搜索 / 切 Tab：重置到第 1 页再查
  const search = (params = {}) => {
    pageNum.value = 1
    return fetchPage(params)
  }

  // 翻页：后端分页重新请求，本地分页只重切片
  const changePage = (page) => {
    pageNum.value = page
    if (serverPaging.value) {
      fetchPage(lastParams)
    } else {
      applyLocal()
    }
  }

  /**
   * 切换「每页条数」：必须回到第 1 页。
   * 否则第 3 页切成每页 50 条时会因为越界变成空白页。
   */
  const changePageSize = (size) => {
    pageSize.value = Number(size) || DEFAULT_PAGE_SIZE
    pageNum.value = 1
    if (serverPaging.value) {
      fetchPage(lastParams)
    } else {
      applyLocal()
    }
  }

  // 重新加载当前页（增删改后刷新用）
  const reload = () => fetchPage(lastParams)

  /**
   * 本地分页模式下，从已缓存的 allRows 重新切片。
   * 用于外部改了 allRows 之后（一般配合 fetchAll）刷新表格。
   */
  const applyFromCache = () => {
    serverPaging.value = false
    applyLocal()
  }

  return {
    pageNum, pageSize, total, serverPaging,
    fetchPage, fetchAll, search, changePage, changePageSize, reload,
    applyFromCache,
    getAllRows: () => allRows
  }
}

/* ==================== 各列表分页实例 ==================== */

// 每页条数可选项，最小 5 条
const PAGE_SIZE_OPTIONS = [5, 10, 20, 50]

// 订单当前 Tab 对应的状态筛选值（undefined = 全部）
const pendingOrderStatus = ref(undefined)

const userPager = createPager(userList, userLoading, getUserListApi)
const staffPager = createPager(staffList, staffLoading, getStaffListApi)
// 订单状态后端暂不认（原来就是前端过滤），用 clientFilter 兜底
const orderPager = createPager(orderList, orderLoading, getOrderListApi, {
  clientFilter: (rows) => {
    if (pendingOrderStatus.value === undefined || pendingOrderStatus.value === null) return rows
    return rows.filter(item => Number(item.orderStatus) === Number(pendingOrderStatus.value))
  }
})
const adminPager = createPager(adminList, adminLoading, getAdminListApi)
// 套餐按 id 升序展示（后端分页上线后由后端 ORDER BY id ASC）
const packagePager = createPager(packageList, packageLoading, getPackageListApi, {
  clientSort: (rows) => [...rows].sort((a, b) => Number(a.id || 0) - Number(b.id || 0))
})
const userMessagePager = createPager(userMessageList, userMessageLoading, getUserListApi)
const staffMessagePager = createPager(staffMessageList, staffMessageLoading, getStaffListApi)

// 模板里用扁平名字，避免写成 userPager.total.value 这种容易出错的链路
const userTotal = userPager.total
const userPageNum = userPager.pageNum
const userPageSize = userPager.pageSize
const userChangePage = userPager.changePage
const userChangeSize = userPager.changePageSize

const staffTotal = staffPager.total
const staffPageNum = staffPager.pageNum
const staffPageSize = staffPager.pageSize
const staffChangePage = staffPager.changePage
const staffChangeSize = staffPager.changePageSize

const orderTotal = orderPager.total
const orderPageNum = orderPager.pageNum
const orderPageSize = orderPager.pageSize
const orderChangePage = orderPager.changePage
const orderChangeSize = orderPager.changePageSize

const adminTotal = adminPager.total
const adminPageNum = adminPager.pageNum
const adminPageSize = adminPager.pageSize
const adminChangePage = adminPager.changePage
const adminChangeSize = adminPager.changePageSize

const packageTotal = packagePager.total
const packagePageNum = packagePager.pageNum
const packagePageSize = packagePager.pageSize
const packageChangePage = packagePager.changePage
const packageChangeSize = packagePager.changePageSize

const userMessageTotal = userMessagePager.total
const userMessagePageNum = userMessagePager.pageNum
const userMessagePageSize = userMessagePager.pageSize
// userMessageChangePage / userMessageChangeSize 定义在 fetchUserMessageList 附近
// （消息列表展示的是映射后的行，翻页/切条数要重建映射，不能走 pager 的通用切片）

const staffMessageTotal = staffMessagePager.total
const staffMessagePageNum = staffMessagePager.pageNum
const staffMessagePageSize = staffMessagePager.pageSize
// staffMessageChangePage / staffMessageChangeSize 同上

const fetchUserList = async (searchParams) => {
  const params = searchParams !== undefined ? searchParams : buildSearchParams(userSearchForm.value)
  return userPager.search(params || {})
}

const fetchStaffList = async (searchParams) => {
  const params = searchParams !== undefined ? searchParams : buildSearchParams(staffSearchForm.value)
  // 未传条件的调用（如派单弹窗）需要全量数据，不带分页参数
  if (searchParams === undefined) {
    staffLoading.value = true
    try {
      const res = await getStaffListApi({})
      const data = res.data || []
      staffList.value = Array.isArray(data) ? data : (data.records || [])
    } catch {
      staffList.value = []
    } finally {
      staffLoading.value = false
    }
    return
  }
  return staffPager.search(params || {})
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
  if (status === 4) return '家政人员已取消'
  if (dispatchStatus === 3) return '已拒单'
  
  const map = { 0: '待接单', 1: '已接单', 2: '服务完成' }
  return map[status] ?? '未知'
}

const orderStatusType = (row) => {
  const status = typeof row === 'object' ? row.orderStatus : row
  const dispatchStatus = typeof row === 'object' ? row.dispatchStatus : undefined
  
  if (status === 3) return 'danger'
  if (status === 4) return 'rgb(247, 137, 137)'
  if (dispatchStatus === 3) return 'warning'
  
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] ?? 'info'
}

const fetchOrderList = async (searchParams) => {
  // 订单状态后端目前不认该参数（原来就是前端 filter），
  // 这里同步到 pendingOrderStatus，由 orderPager 的 clientFilter 兜底过滤
  pendingOrderStatus.value = searchParams?.orderStatus
  return orderPager.search(searchParams || {})
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
  const params = searchParams !== undefined ? searchParams : buildSearchParams(adminSearchForm.value)
  return adminPager.search(params || {})
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
  window.location.href = '/'
}

const fetchPackageList = async (searchParams) => {
  // 管理端不传 status 时不过滤，上/下架套餐都要能看到（与搜索框的显式选择区分开）
  const params = {}
  if (searchParams && searchParams.packageName) params.packageName = searchParams.packageName
  if (searchParams && searchParams.status !== undefined && searchParams.status !== '') params.status = searchParams.status
  return packagePager.search(params)
}

const searchPackageList = () => {
  const params = {}
  if (packageSearchForm.value.packageName) params.packageName = packageSearchForm.value.packageName
  if (packageSearchForm.value.status !== undefined && packageSearchForm.value.status !== '') params.status = packageSearchForm.value.status
  fetchPackageList(params)
}

const resetPackageSearch = () => {
  packageSearchForm.value = { packageName: '', status: undefined }
  fetchPackageList()
}

const showPackageDialog = (row) => {
  selectedPresetIcon.value = ''
  if (row && row.id) {
    packageDialogTitle.value = '修改套餐'
    packageForm.value = {
      id: row.id,
      packageName: row.packageName || '',
      packagePrice: row.packagePrice || 0,
      packageDesc: row.packageDesc || '',
      packageImg: row.packageImg || '',
      unitText: row.unitText || '/次',
      serviceType: row.serviceType || 1,
      status: row.status !== undefined ? row.status : 0,
      sort: row.sort || 0,
      expireStatus: row.expireStatus !== undefined ? row.expireStatus : 0,
      expireStartTime: row.expireStartTime || '',
      expireEndTime: row.expireEndTime || ''
    }
    packageCoverPreview.value = row.packageImg || ''
  } else {
    packageDialogTitle.value = '新增套餐'
    packageForm.value = {
      id: null,
      packageName: '',
      packagePrice: 0,
      packageDesc: '',
      packageImg: '',
      unitText: '/次',
      serviceType: 1,
      status: 0,
      sort: 0,
      expireStatus: 0,
      expireStartTime: '',
      expireEndTime: ''
    }
    packageCoverPreview.value = ''
  }
  packageDialogVisible.value = true
}

const submitPackage = async () => {
  if (!packageFormRef.value) return
  await packageFormRef.value.validate(async (valid) => {
    if (!valid) return
    packageSubmitLoading.value = true
    try {
      let res
      if (packageForm.value.id) {
        res = await updatePackageApi(packageForm.value)
      } else {
        res = await createPackageApi(packageForm.value)
      }
      if (res.code === 200) {
        ElMessage.success(packageForm.value.id ? '修改成功' : '新增成功')
        packageDialogVisible.value = false
        fetchPackageList()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch {
      ElMessage.error('操作失败，请重试')
    } finally {
      packageSubmitLoading.value = false
    }
  })
}

const togglePackageStatus = async (row) => {
  const action = row.status === 0 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${action}该套餐吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await togglePackageStatusApi(row.id)
    if (res.code === 200) {
      row.status = row.status === 0 ? 1 : 0
      ElMessage.success(`${action}成功`)
    } else {
      ElMessage.error(res.message || `${action}失败`)
    }
  } catch { /* 取消 */ }
}

const deletePackage = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除套餐"${row.packageName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deletePackageApi(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchPackageList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch { /* 取消 */ }
}

const beforePackageCoverUpload = (file) => {
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

const handlePackageCoverUpload = async (options) => {
  const formData = new FormData()
  formData.append('cover', options.file)
  try {
    const res = await uploadPackageCoverApi(formData)
    if (res.code === 200) {
      const coverUrl = typeof res.data === 'string' ? res.data : (res.data?.url || '')
      packageForm.value.packageImg = coverUrl
      packageCoverPreview.value = coverUrl
      selectedPresetIcon.value = ''
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch {
    ElMessage.error('上传失败，请重试')
  }
}

const selectPresetIcon = async (icon) => {
  if (!icon) return
  selectedPresetIcon.value = icon
  try {
    const canvas = document.createElement('canvas')
    canvas.width = 200
    canvas.height = 200
    const ctx = canvas.getContext('2d')
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, 200, 200)
    ctx.font = '120px serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(icon, 100, 100)
    const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/png'))
    const formData = new FormData()
    formData.append('cover', blob, 'icon.png')
    const res = await uploadPackageCoverApi(formData)
    if (res.code === 200) {
      const coverUrl = typeof res.data === 'string' ? res.data : (res.data?.url || '')
      packageForm.value.packageImg = coverUrl
      packageCoverPreview.value = coverUrl
      ElMessage.success('图标已上传')
    } else {
      ElMessage.error(res.message || '图标上传失败')
    }
  } catch {
    ElMessage.error('图标上传失败，请重试')
  }
}

const handleMessageTabChange = (tab) => {
  if (tab === 'user') {
    userMessagePager.pageNum.value = 1
    fetchUserMessageList()
  } else {
    staffMessagePager.pageNum.value = 1
    fetchStaffMessageList()
  }
}

const getAdminAccount = () => {
  try {
    const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return info.account || ''
  } catch { return '' }
}

/**
 * 消息列表的公共实现：拉一页「用户/员工」，再与该管理员的会话列表匹配出最新消息。
 * 会话列表（/chat/rooms）是一次性全量返回的，所以匹配不受分页影响。
 * @param {object} pager    对应的 pager 实例（负责分页与 total）
 * @param {object} listApi  拉取用户/员工列表的接口
 */
const buildMessageRows = async (pager, loadingRef, rowsRef, listApi, itemKey) => {
  loadingRef.value = true
  try {
    const allRows = await pager.fetchAll(listApi)
    const adminAccount = getAdminAccount()
    let rooms = []
    if (adminAccount) {
      try {
        const roomRes = await getChatRoomListApi(adminAccount)
        rooms = (roomRes && roomRes.data) || []
      } catch { rooms = [] }
    }

    const mapped = allRows.map(item => {
      const account = item.account || item.username || ''
      const room = rooms.find(r => r.account === account)
      return {
        [itemKey.account]: account,
        [itemKey.name]: item.realName || item.username || '-',
        lastMessage: room ? (room.lastMsg || '') : '',
        lastTime: room ? (room.lastTime || '') : (item.createTime || '')
      }
    })
    // 映射后的数组长度与源数据一致，直接本地切片
    pager.total.value = mapped.length
    const start = (pager.pageNum.value - 1) * pager.pageSize.value
    rowsRef.value = mapped.slice(start, start + pager.pageSize.value)
  } catch {
    rowsRef.value = []
    pager.total.value = 0
  } finally {
    loadingRef.value = false
  }
}

const fetchUserMessageList = () =>
  buildMessageRows(userMessagePager, userMessageLoading, userMessageList, getUserListApi, {
    account: 'userAccount', name: 'userName'
  })

const fetchStaffMessageList = () =>
  buildMessageRows(staffMessagePager, staffMessageLoading, staffMessageList, getStaffListApi, {
    account: 'staffAccount', name: 'staffName'
  })

// 消息列表展示的是映射后的行，翻页 / 切每页条数都不能走 pager 的通用切片，要重建映射后再切
const changeMessagePage = (pager, fetcher) => (page) => {
  pager.pageNum.value = page
  fetcher()
}
const changeMessagePageSize = (pager, fetcher) => (size) => {
  pager.pageSize.value = Number(size) || DEFAULT_PAGE_SIZE
  pager.pageNum.value = 1
  fetcher()
}

const userMessageChangePage = changeMessagePage(userMessagePager, fetchUserMessageList)
const userMessageChangeSize = changeMessagePageSize(userMessagePager, fetchUserMessageList)
const staffMessageChangePage = changeMessagePage(staffMessagePager, fetchStaffMessageList)
const staffMessageChangeSize = changeMessagePageSize(staffMessagePager, fetchStaffMessageList)

const openUserChat = (row) => {
  router.push({
    path: '/merchant',
    query: { merchantId: row.userAccount }
  })
}

const openStaffChat = (row) => {
  router.push({
    path: '/merchant',
    query: { merchantId: row.staffAccount }
  })
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
  /* 圆角已画在 SVG 里，这里不要再加 border-radius */
  flex-shrink: 0;
  filter: drop-shadow(0 2px 6px rgba(var(--brand-rgb), .22));
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
  background: var(--surface-page);
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
  background: var(--neutral-50);
  border-radius: 6px;
}

.skill-tag {
  margin: 2px 4px 2px 0;
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.cover-selector {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.preset-icons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.preset-icon-item {
  width: 60px;
  height: 60px;
  border: 2px solid var(--border-base);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.preset-icon-item:hover {
  border-color: var(--el-color-primary);
  transform: scale(1.05);
}

.preset-icon-item.active {
  border-color: var(--el-color-primary);
  background: var(--brand-50);
  box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.2);
}

.icon-emoji {
  font-size: 32px;
  line-height: 1;
}

.cover-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.package-cover-uploader {
  width: 148px;
  height: 148px;
  border: 1px dashed var(--border-strong);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.package-cover-uploader:hover {
  border-color: var(--el-color-primary);
}

.package-cover-preview {
  width: 148px;
  height: 148px;
  object-fit: cover;
  display: block;
}

.package-cover-uploader-icon {
  font-size: 28px;
  color: var(--text-secondary);
}

.upload-hint {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 4px;
}

.table-cover-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.no-cover {
  font-size: 12px;
  color: var(--text-placeholder);
}
</style>