<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <div slot="header"><span>账户余额</span></div>
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
          <div slot="header">
            <span>快速操作</span>
          </div>
          <div style="padding:20px">
            <el-button type="primary" icon="el-icon-money" size="medium" @click="goRecharge">提交充值申请</el-button>
            <el-button type="success" icon="el-icon-document" size="medium" style="margin-left:15px" @click="goLogs">查询记录</el-button>
            <el-button type="warning" icon="el-icon-s-order" size="medium" style="margin-left:15px" @click="goRechargeList">充值记录</el-button>
          </div>
        </el-card>
        <el-card shadow="hover" style="margin-top:20px">
          <div slot="header"><span>公司信息</span></div>
          <div style="font-size:13px;color:#666;padding:10px">
            <p><b>公司名称：</b>{{ company.companyName }}</p>
            <p class="app-key-line">
              <b>AppKey：</b><span>{{ company.appKey || '-' }}</span>
              <el-button
                v-if="company.appKey"
                type="text"
                icon="el-icon-document-copy"
                @click="copyAppKey"
              >复制</el-button>
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "CompanyDashboard",
  data() {
    return {
      company: JSON.parse(localStorage.getItem("companyInfo") || "{}"),
      nextUpdateTime: "-",
    };
  },
  created() {
    this.calcNextUpdate();
  },
  methods: {
    calcNextUpdate() {
      const info = this.company;
      if (info.balanceUpdateTime && info.billingCycleDays) {
        const d = new Date(info.balanceUpdateTime);
        d.setDate(d.getDate() + info.billingCycleDays);
        this.nextUpdateTime = d.toLocaleString("zh-CN", { hour12: false });
      }
    },
    formatMoney(val) {
      if (!val) return "0.00";
      return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    },
    copyAppKey() {
      const value = this.company.appKey || "";
      if (!value) return;
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(value).then(() => {
          this.$message.success("AppKey 已复制");
        });
        return;
      }
      const input = document.createElement("textarea");
      input.value = value;
      document.body.appendChild(input);
      input.select();
      document.execCommand("copy");
      document.body.removeChild(input);
      this.$message.success("AppKey 已复制");
    },
    goRecharge() { this.$router.push("/company/recharge"); },
    goLogs() { this.$router.push("/company/logs"); },
    goRechargeList() { this.$router.push("/company/recharge-list"); },
  },
};
</script>

<style scoped>
.app-key-line {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.app-key-line span {
  overflow-wrap: anywhere;
}
</style>
