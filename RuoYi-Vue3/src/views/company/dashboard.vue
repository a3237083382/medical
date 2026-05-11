<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>账户余额</span></template>
          <div style="text-align:center;padding:20px 0">
            <div style="font-size:36px;font-weight:bold;color:#409EFF">{{ formatMoney(company.balance) }}</div>
            <div style="font-size:12px;color:#999;margin-top:10px">元</div>
          </div>
          <el-divider />
          <div style="font-size:13px;color:#666">
            <p>余额更新时间：{{ company.balanceUpdateTime || '-' }}</p>
            <p>下次更新：{{ nextUpdateTime }}</p>
            <p>结算周期：{{ company.billingCycleDays }}天</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span>快速操作</span></template>
          <div style="padding:20px">
            <el-button type="primary" icon="Money" size="default" @click="goRecharge">提交充值申请</el-button>
            <el-button type="success" icon="Document" size="default" style="margin-left:15px" @click="goRechargeList">充值记录</el-button>
          </div>
        </el-card>
        <el-card shadow="hover" style="margin-top:20px">
          <template #header><span>公司信息</span></template>
          <div style="font-size:13px;color:#666;padding:10px">
            <p><b>公司名称：</b>{{ company.companyName }}</p>
            <p><b>AppKey：</b>{{ company.appKey || '-' }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="CompanyDashboard">
const router = useRouter()
const company = ref(JSON.parse(localStorage.getItem("companyInfo") || "{}"))
const nextUpdateTime = ref("-")

function calcNextUpdate() {
  const info = company.value
  if (info.balanceUpdateTime && info.billingCycleDays) {
    const d = new Date(info.balanceUpdateTime)
    d.setDate(d.getDate() + info.billingCycleDays)
    nextUpdateTime.value = d.toLocaleString("zh-CN", { hour12: false })
  }
}

calcNextUpdate()

function formatMoney(val) {
  if (!val) return "0.00"
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",")
}

function goRecharge() { router.push("/company/recharge") }
function goRechargeList() { router.push("/company/recharge-list") }
</script>
