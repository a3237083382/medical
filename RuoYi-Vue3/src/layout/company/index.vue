<template>
  <div class="company-layout" :class="{ 'is-embedded': isEmbedded }">
    <aside v-if="!isEmbedded" class="company-sidebar">
      <div class="company-logo">
        <span class="logo-mark">湘</span>
        <span>
          <strong>医疗信息接口台</strong>
          <small>INSURER WORKBENCH</small>
        </span>
      </div>
      <el-menu :default-active="activeMenu" router class="company-menu">
        <el-menu-item index="/company/dashboard">
          <el-icon><House /></el-icon>
          <span>账户概览</span>
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
          <div class="company-name">{{ companyName }} · 医疗数据查询服务</div>
        </div>
        <div class="header-actions">
          <span class="service-pill">医疗查询服务已启用</span>
          <el-button type="primary" plain @click="logout">退出登录</el-button>
        </div>
      </header>
      <nav v-else class="embed-nav">
        <el-tabs :model-value="activeMenu" @tab-change="goTab">
          <el-tab-pane label="账户概览" name="/company/dashboard" />
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
  background:
    linear-gradient(135deg, rgba(34, 184, 167, 0.09) 0%, transparent 32%),
    linear-gradient(160deg, #edf3f7 0%, #f7f9fb 48%, #eef5f3 100%);
  color: #10202f;
}

.company-sidebar {
  width: 248px;
  flex-shrink: 0;
  background: #0c2430;
  color: #e8f1f3;
  box-shadow: 10px 0 30px rgba(12, 36, 48, 0.12);
}

.company-logo {
  min-height: 78px;
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 0 22px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-mark {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #22b8a7;
  color: #052027;
  font-weight: 800;
}

.company-logo strong {
  display: block;
  font-size: 16px;
  letter-spacing: 0;
}

.company-logo small {
  display: block;
  margin-top: 4px;
  color: #89a9b0;
  font-size: 10px;
  letter-spacing: 0;
}

.company-menu {
  border-right: 0;
  background: transparent;
  padding: 14px 12px;
}

.company-menu :deep(.el-menu-item) {
  height: 44px;
  margin: 4px 0;
  border-radius: 8px;
  color: #c5d7db;
}

.company-menu :deep(.el-icon) {
  color: #7fb7bd;
}

.company-menu :deep(.el-menu-item:hover),
.company-menu :deep(.el-menu-item.is-active) {
  color: #ffffff;
  background: #174656;
}

.company-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #22d3c5;
}

.company-main {
  flex: 1;
  min-width: 0;
}

.company-header {
  height: 72px;
  padding: 0 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.82);
  border-bottom: 1px solid rgba(147, 163, 171, 0.24);
  backdrop-filter: blur(14px);
}

.company-title {
  font-size: 20px;
  font-weight: 700;
  color: #10202f;
}

.company-name {
  margin-top: 5px;
  font-size: 12px;
  color: #62717a;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.service-pill {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border: 1px solid rgba(34, 184, 167, 0.28);
  border-radius: 999px;
  background: rgba(34, 184, 167, 0.1);
  color: #0f766e;
  font-size: 12px;
  font-weight: 600;
}

.company-content {
  min-height: calc(100vh - 72px);
  padding: 24px;
}

.is-embedded {
  min-height: 100vh;
  display: block;
  background: #f5f8fa;
}

.is-embedded .company-main {
  min-height: 100vh;
}

.embed-nav {
  min-height: 52px;
  padding: 0 18px;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid rgba(147, 163, 171, 0.24);
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

@media (max-width: 960px) {
  .company-layout {
    display: block;
  }

  .company-sidebar {
    width: 100%;
  }

  .company-menu {
    display: flex;
    overflow-x: auto;
  }

  .company-menu :deep(.el-menu-item) {
    flex-shrink: 0;
  }

  .company-header {
    height: auto;
    min-height: 72px;
    gap: 14px;
    align-items: flex-start;
    padding: 18px;
    flex-direction: column;
  }

  .company-content {
    padding: 16px;
  }
}
</style>
