<template>
  <div class="company-layout" :class="{ 'is-embedded': isEmbedded }">
    <aside v-if="!isEmbedded" class="company-sidebar">
      <div class="company-logo">保险公司系统</div>
      <el-menu :default-active="activeMenu" router class="company-menu">
        <el-menu-item index="/company/dashboard">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/company/query">
          <el-icon><Search /></el-icon>
          <span>接口接入</span>
        </el-menu-item>
        <el-menu-item index="/company/recharge">
          <el-icon><Money /></el-icon>
          <span>提交充值申请</span>
        </el-menu-item>
        <el-menu-item index="/company/recharge-list">
          <el-icon><Document /></el-icon>
          <span>充值记录</span>
        </el-menu-item>
        <el-menu-item index="/company/query-log">
          <el-icon><Tickets /></el-icon>
          <span>查询记录</span>
        </el-menu-item>
        <el-menu-item index="/company/fee-flow">
          <el-icon><Wallet /></el-icon>
          <span>费用流水</span>
        </el-menu-item>
        <el-menu-item index="/company/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <section class="company-main">
      <header v-if="!isEmbedded" class="company-header">
        <div>
          <div class="company-title">{{ title }}</div>
          <div class="company-name">{{ companyName }}</div>
        </div>
        <el-button type="danger" plain @click="logout">退出登录</el-button>
      </header>
      <nav v-else class="embed-nav">
        <el-tabs :model-value="activeMenu" @tab-change="goTab">
          <el-tab-pane label="账户概览" name="/company/dashboard" />
          <el-tab-pane label="接口接入" name="/company/query" />
          <el-tab-pane label="充值申请" name="/company/recharge" />
          <el-tab-pane label="充值记录" name="/company/recharge-list" />
          <el-tab-pane label="查询记录" name="/company/query-log" />
          <el-tab-pane label="费用流水" name="/company/fee-flow" />
          <el-tab-pane label="资料信息" name="/company/profile" />
        </el-tabs>
      </nav>
      <main class="company-content">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup name="CompanyLayout">
import { getCompanyEmbedMode, getCompanyInfo, removeCompanyToken } from "@/utils/companyAuth"

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const embedMode = computed(() => getCompanyEmbedMode())
const isEmbedded = computed(() => ["iframe", "webview", "browser"].includes(embedMode.value))
const title = computed(() => route.meta.title || "保险公司门户")
const companyName = computed(() => {
  const info = getCompanyInfo()
  return info.companyName || "保险公司门户"
})

function goTab(path) {
  router.push(path)
}

function logout() {
  removeCompanyToken()
  router.replace("/company/login")
}
</script>

<style scoped>
.company-layout {
  min-height: 100vh;
  display: flex;
  background: #f4f7fb;
}

.company-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #101828;
  color: #fff;
}

.company-logo {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-size: 17px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.company-menu {
  border-right: 0;
  background: transparent;
}

.company-menu :deep(.el-menu-item) {
  color: #cbd5e1;
}

.company-menu :deep(.el-menu-item:hover),
.company-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: #1d4ed8;
}

.company-main {
  flex: 1;
  min-width: 0;
}

.company-header {
  height: 56px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.company-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.company-name {
  margin-top: 3px;
  font-size: 12px;
  color: #667085;
}

.company-content {
  min-height: calc(100vh - 56px);
  padding: 20px;
}

.is-embedded {
  min-height: 100vh;
  display: block;
  background: #f6f8fb;
}

.is-embedded .company-main {
  min-height: 100vh;
}

.embed-nav {
  height: 48px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.embed-nav :deep(.el-tabs__header) {
  margin: 0;
}

.embed-nav :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.is-embedded .company-content {
  min-height: calc(100vh - 48px);
  padding: 16px;
}
</style>
