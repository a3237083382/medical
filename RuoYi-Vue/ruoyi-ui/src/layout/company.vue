<template>
  <div class="company-layout">
    <aside class="company-sidebar">
      <div class="brand">
        <div class="brand-mark">湘</div>
        <div>
          <div class="brand-title">医疗信息接口台</div>
          <div class="brand-sub">INSURER WORKBENCH</div>
        </div>
      </div>
      <nav class="company-menu">
        <router-link to="/company/dashboard">公司首页</router-link>
        <router-link to="/company/logs">查询记录</router-link>
        <router-link to="/company/monthly-bill">月度对账</router-link>
      </nav>
    </aside>
    <main class="company-main">
      <header class="company-header">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p>{{ companyName }}</p>
        </div>
        <el-button size="small" plain @click="logout">退出登录</el-button>
      </header>
      <router-view />
    </main>
  </div>
</template>

<script>
export default {
  name: 'CompanyLayout',
  computed: {
    pageTitle() {
      return this.$route.meta.title || '公司首页'
    },
    companyName() {
      const info = JSON.parse(localStorage.getItem('companyInfo') || sessionStorage.getItem('companyInfo') || '{}')
      return info.companyName || '医疗数据查询服务'
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('companyToken')
      localStorage.removeItem('companyInfo')
      sessionStorage.removeItem('companyToken')
      sessionStorage.removeItem('companyInfo')
      this.$router.replace('/company/login')
    }
  }
}
</script>

<style scoped>
.company-layout {
  min-height: 100vh;
  display: flex;
  background: #f5f8fb;
}
.company-sidebar {
  width: 240px;
  background: #0d2c36;
  color: #d8f2f4;
  padding: 22px 18px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 22px;
  border-bottom: 1px solid rgba(255,255,255,.1);
}
.brand-mark {
  width: 42px;
  height: 42px;
  line-height: 42px;
  text-align: center;
  border-radius: 8px;
  background: #24c5b6;
  color: #06262e;
  font-weight: 700;
}
.brand-title {
  font-size: 16px;
  font-weight: 700;
}
.brand-sub {
  margin-top: 6px;
  font-size: 11px;
  color: #86b9c3;
}
.company-menu {
  margin-top: 20px;
  display: grid;
  gap: 8px;
}
.company-menu a {
  color: #c7dde2;
  padding: 12px 14px;
  border-radius: 6px;
  text-decoration: none;
}
.company-menu a.router-link-active {
  background: rgba(36,197,182,.16);
  color: #fff;
}
.company-main {
  flex: 1;
  min-width: 0;
}
.company-header {
  height: 76px;
  padding: 0 26px;
  background: #fff;
  border-bottom: 1px solid #edf1f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.company-header h1 {
  margin: 0;
  color: #102030;
  font-size: 22px;
}
.company-header p {
  margin: 8px 0 0;
  color: #7b8b99;
}
</style>
