<template>
  <div class="company-layout">
    <aside class="company-sidebar">
      <div class="company-logo">保险公司系统</div>
      <el-menu :default-active="activeMenu" router class="company-menu">
        <el-menu-item index="/company/dashboard">
          <el-icon><House /></el-icon>
          <span>首页</span>
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
        <el-menu-item index="/company/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <section class="company-main">
      <header class="company-header">
        <div>
          <div class="company-title">{{ title }}</div>
          <div class="company-name">{{ companyName }}</div>
        </div>
        <el-button type="danger" plain @click="logout">退出登录</el-button>
      </header>
      <main class="company-content">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup name="CompanyLayout">
import { removeCompanyToken } from "@/utils/companyAuth"

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const title = computed(() => route.meta.title || "保险公司门户")
const companyName = computed(() => {
  const info = JSON.parse(localStorage.getItem("companyInfo") || "{}")
  return info.companyName || "保险公司门户"
})

function logout() {
  removeCompanyToken()
  localStorage.removeItem("companyToken")
  localStorage.removeItem("companyInfo")
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
</style>
