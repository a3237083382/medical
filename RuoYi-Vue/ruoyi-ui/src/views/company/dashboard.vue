<template>
  <div class="app-container company-home">
    <el-card shadow="never" class="service-hero">
      <div>
        <div class="eyebrow">医疗数据查询服务</div>
        <h2>{{ company.companyName || '保险公司' }}</h2>
        <p>用于医疗信息查询、接口调用留痕、月度对账和账号资料维护。</p>
      </div>
      <el-tag :type="statusTagType" effect="plain">{{ statusText }}</el-tag>
    </el-card>

    <el-row :gutter="16" class="mt16">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="metric-title">本月使用情况</div>
          <el-progress :percentage="usagePercent" :status="progressStatus" />
          <div class="metric-sub">{{ company.billingMonth || '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="metric-title">预计剩余可用次数</div>
          <div class="metric-value">{{ estimatedRemaining }}</div>
          <div class="metric-sub">按当前公司接口标准估算</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="metric-title">下次对账周期</div>
          <div class="metric-value">{{ nextMonth }}</div>
          <div class="metric-sub">自然月汇总生成</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :span="8">
        <el-card shadow="hover" class="quick-card" @click.native="goLogs">
          <i class="el-icon-document"></i>
          <div>查询记录</div>
          <span>核对每一次接口调用留痕</span>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="quick-card" @click.native="goMonthlyBill">
          <i class="el-icon-s-order"></i>
          <div>月度对账</div>
          <span>按月份查看汇总和明细</span>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="quick-card">
          <i class="el-icon-user"></i>
          <div>资料信息</div>
          <span>维护联系人和账号密码</span>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mt16">
      <div slot="header"><span>接入信息</span></div>
      <p class="app-key-line">
        <b>AppKey：</b><span>{{ company.appKeyMasked || maskAppKey(company.appKey) }}</span>
        <el-button v-if="company.appKey" type="text" icon="el-icon-document-copy" @click="copyAppKey">复制</el-button>
        <el-button v-if="company.hasAppKey || company.appKey" type="text" icon="el-icon-refresh" @click="changeAppKey">换发 AppKey</el-button>
        <span v-if="company.appKey" class="app-key-tip">新 AppKey 仅本次显示，请及时保存。</span>
      </p>
    </el-card>
  </div>
</template>

<script>
import { getProfile, regenerateAppKey } from '@/api/business/portal'

export default {
  name: 'CompanyDashboard',
  data() {
    return {
      company: JSON.parse(localStorage.getItem('companyInfo') || sessionStorage.getItem('companyInfo') || '{}')
    }
  },
  computed: {
    usagePercent() {
      return Number(this.company.usagePercent || 0)
    },
    statusText() {
      if (this.company.serviceStatus === 'LIMIT_REACHED') return '本月服务额度已达上限'
      if (this.company.serviceStatus === 'NEAR_LIMIT') return '本月使用接近上限'
      return '医疗查询服务已启用'
    },
    statusTagType() {
      if (this.company.serviceStatus === 'LIMIT_REACHED') return 'danger'
      if (this.company.serviceStatus === 'NEAR_LIMIT') return 'warning'
      return 'success'
    },
    progressStatus() {
      if (this.company.serviceStatus === 'LIMIT_REACHED') return 'exception'
      if (this.company.serviceStatus === 'NEAR_LIMIT') return 'warning'
      return 'success'
    },
    estimatedRemaining() {
      const budget = Number(this.company.monthlyBudget || 0)
      const used = Number(this.company.usedAmount || 0) + Number(this.company.reservedAmount || 0)
      if (!budget || budget <= used) return 0
      return Math.floor((budget - used) / 20)
    },
    nextMonth() {
      const date = new Date()
      date.setMonth(date.getMonth() + 1, 1)
      return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0')
    }
  },
  created() {
    this.loadProfile()
  },
  methods: {
    loadProfile() {
      getProfile().then(res => {
        this.company = Object.assign({}, this.company, res.data || {})
      })
    },
    copyAppKey() {
      const value = this.company.appKey || ''
      if (!value) return
      const input = document.createElement('textarea')
      input.value = value
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
      this.$message.success('AppKey 已复制')
    },
    changeAppKey() {
      this.$confirm('换发后旧 AppKey 将不能继续调用接口，是否确认换发？', '系统提示', {
        confirmButtonText: '确认换发',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return regenerateAppKey()
      }).then(res => {
        this.company = Object.assign({}, this.company, res.data || {}, { hasAppKey: true })
        localStorage.setItem('companyInfo', JSON.stringify(this.company))
        this.$message.success('AppKey 已换发，请保存新的完整 AppKey')
      }).catch(() => {})
    },
    goLogs() { this.$router.push('/company/logs') },
    goMonthlyBill() { this.$router.push('/company/monthly-bill') },
    maskAppKey(value) {
      if (!value) return '-'
      if (value.length <= 8) return value.slice(0, 2) + '****' + value.slice(-2)
      return value.slice(0, 4) + '****' + value.slice(-4)
    }
  }
}
</script>

<style scoped>
.company-home {
  background: #f5f8fb;
  min-height: calc(100vh - 84px);
}
.service-hero {
  background: #0f3b46;
  color: #fff;
}
.service-hero ::v-deep .el-card__body {
  min-height: 160px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.eyebrow {
  color: #60e0cf;
  font-size: 13px;
}
.service-hero h2 {
  margin: 12px 0;
  font-size: 32px;
}
.service-hero p {
  margin: 0;
  color: #d6e6ea;
}
.mt16 {
  margin-top: 16px;
}
.metric-title {
  color: #6b7a88;
  margin-bottom: 14px;
}
.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #102030;
}
.metric-sub {
  color: #8b98a5;
  margin-top: 10px;
  font-size: 12px;
}
.quick-card {
  cursor: pointer;
  min-height: 130px;
}
.quick-card i {
  font-size: 24px;
  color: #0b9f94;
}
.quick-card div {
  margin: 14px 0 8px;
  font-weight: 700;
}
.quick-card span {
  color: #6b7a88;
  font-size: 13px;
}
.app-key-line {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.app-key-tip {
  color: #8b98a5;
  font-size: 12px;
}
</style>
