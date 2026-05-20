<template>
  <div class="company-dashboard">
    <section class="hero-panel">
      <div class="hero-copy">
        <span class="eyebrow">账户概览</span>
        <h2>{{ company.companyName || "保险公司门户" }}</h2>
        <p>用于医疗信息查询、余额核对、充值申请和调用留痕查看。</p>
      </div>
      <div class="balance-tile">
        <span>当前余额</span>
        <strong>{{ formatMoney(company.balance) }}</strong>
        <small>元</small>
      </div>
    </section>

    <section class="meta-grid">
      <div class="meta-card">
        <span>余额更新时间</span>
        <strong>{{ formatDateTime(company.balanceUpdateTime) }}</strong>
      </div>
      <div class="meta-card">
        <span>下次更新</span>
        <strong>{{ nextUpdateTime }}</strong>
      </div>
      <div class="meta-card">
        <span>结算周期</span>
        <strong>{{ company.billingCycleDays || "-" }} 天</strong>
      </div>
    </section>

    <section class="work-grid">
      <button class="action-card" @click="goRecharge">
        <span>充值申请</span>
        <strong>提交公对公转账上账申请</strong>
      </button>
      <button class="action-card" @click="goQueryLog">
        <span>查询记录</span>
        <strong>核对每一次接口调用留痕</strong>
      </button>
      <button class="action-card" @click="goRechargeList">
        <span>充值记录</span>
        <strong>查看申请和审核结果</strong>
      </button>
      <button class="action-card" @click="goProfile">
        <span>资料信息</span>
        <strong>维护联系人和账号密码</strong>
      </button>
    </section>

    <section v-if="!isEmbedded" class="key-strip">
      <div>
        <span>AppKey</span>
        <code>{{ company.appKeyMasked || "未生成" }}</code>
      </div>
      <div class="key-actions">
        <el-button type="primary" plain icon="Plus" :loading="generatingKey" @click="requestNewAppKey">
          {{ company.hasAppKey ? "换发 AppKey" : "新增 AppKey" }}
        </el-button>
      </div>
    </section>

    <el-dialog v-model="keyDialogOpen" title="AppKey 已生成" width="560px" append-to-body @closed="clearGeneratedKey">
      <el-alert
        title="请立即复制并保存到保险公司系统中。关闭弹窗后，门户不再展示完整 AppKey。"
        type="warning"
        :closable="false"
        show-icon
      />
      <div class="generated-key">
        <span>完整 AppKey</span>
        <code>{{ generatedAppKey }}</code>
      </div>
      <template #footer>
        <el-button type="primary" icon="CopyDocument" @click="copyGeneratedAppKey">复制 AppKey</el-button>
        <el-button @click="keyDialogOpen = false">我已保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CompanyDashboard">
import { getCompanyProfile, regenerateCompanyAppKey } from "@/api/business/portal"
import { getCompanyEmbedMode, getCompanyInfo, setCompanyInfo } from "@/utils/companyAuth"
import { ElMessage, ElMessageBox } from "element-plus"

const router = useRouter()
const company = ref(getCompanyInfo())
const nextUpdateTime = ref("-")
const generatingKey = ref(false)
const keyDialogOpen = ref(false)
const generatedAppKey = ref("")
const isEmbedded = computed(() => ["iframe", "webview", "browser"].includes(getCompanyEmbedMode()))

function calcNextUpdate() {
  const info = company.value
  if (info.balanceUpdateTime && info.billingCycleDays) {
    const d = new Date(info.balanceUpdateTime)
    d.setDate(d.getDate() + info.billingCycleDays)
    nextUpdateTime.value = formatDateTime(d)
  }
}

calcNextUpdate()

function loadProfile() {
  getCompanyProfile().then(res => {
    company.value = res.data || {}
    if (isEmbedded.value) {
      delete company.value.appKey
      setCompanyInfo(company.value)
    } else {
      localStorage.setItem("companyInfo", JSON.stringify(company.value))
    }
    calcNextUpdate()
  })
}

function formatMoney(val) {
  if (!val) return "0.00"
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",")
}

function formatDateTime(val) {
  if (!val) return "-"
  const d = new Date(val)
  if (Number.isNaN(d.getTime())) return "-"
  const pad = n => String(n).padStart(2, "0")
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function requestNewAppKey() {
  const message = company.value.hasAppKey
    ? "换发后旧 AppKey 会立即失效，保险公司系统需要改用新的 AppKey。确认继续？"
    : "确认为当前保险公司生成 AppKey？完整 AppKey 只会在生成成功后展示一次。"
  ElMessageBox.confirm(message, company.value.hasAppKey ? "换发 AppKey" : "新增 AppKey", {
    confirmButtonText: "确认",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    generatingKey.value = true
    return regenerateCompanyAppKey()
  }).then(res => {
    const data = res.data || {}
    generatedAppKey.value = data.appKey || ""
    company.value = Object.assign({}, company.value, {
      hasAppKey: true,
      appKeyMasked: data.appKeyMasked || "已生成"
    })
    localStorage.setItem("companyInfo", JSON.stringify(company.value))
    keyDialogOpen.value = true
    loadProfile()
  }).finally(() => {
    generatingKey.value = false
  }).catch(() => {})
}

async function copyGeneratedAppKey() {
  if (!generatedAppKey.value) return
  await navigator.clipboard.writeText(generatedAppKey.value)
  ElMessage.success("AppKey 已复制")
}

function clearGeneratedKey() {
  generatedAppKey.value = ""
}

function goRecharge() { router.push("/company/recharge") }
function goRechargeList() { router.push("/company/recharge-list") }
function goQueryLog() { router.push("/company/query-log") }
function goProfile() { router.push("/company/profile") }

loadProfile()
</script>

<style scoped>
.company-dashboard {
  display: grid;
  gap: 18px;
}

.hero-panel {
  min-height: 230px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
  align-items: stretch;
  padding: 28px;
  border: 1px solid rgba(16, 32, 47, 0.08);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(12, 36, 48, 0.98), rgba(20, 76, 89, 0.94)),
    linear-gradient(90deg, transparent 0, rgba(255, 255, 255, 0.08) 1px, transparent 1px);
  color: #f7fbfc;
  box-shadow: 0 18px 50px rgba(12, 36, 48, 0.14);
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.eyebrow {
  color: #8ee4d8;
  font-size: 12px;
  font-weight: 800;
}

.hero-copy h2 {
  margin: 12px 0;
  font-size: 34px;
  line-height: 1.2;
  letter-spacing: 0;
}

.hero-copy p {
  max-width: 560px;
  margin: 0;
  color: #c8d9de;
  line-height: 1.8;
}

.balance-tile {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px;
  border: 1px solid rgba(142, 228, 216, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.08);
}

.balance-tile span,
.meta-card span,
.action-card span,
.key-strip span {
  color: #6f7e86;
  font-size: 12px;
  font-weight: 700;
}

.balance-tile span {
  color: #b8d7d9;
}

.balance-tile strong {
  margin-top: 10px;
  color: #ffffff;
  font-size: 42px;
  line-height: 1;
  letter-spacing: 0;
}

.balance-tile small {
  margin-top: 10px;
  color: #8ee4d8;
  font-weight: 700;
}

.meta-grid,
.work-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.meta-card,
.action-card,
.key-strip {
  border: 1px solid rgba(16, 32, 47, 0.08);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 10px 28px rgba(12, 36, 48, 0.06);
}

.meta-card {
  min-height: 88px;
  padding: 18px;
}

.meta-card strong {
  display: block;
  margin-top: 10px;
  color: #10202f;
  font-size: 16px;
}

.work-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.action-card {
  min-height: 132px;
  padding: 18px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.action-card:hover {
  transform: translateY(-2px);
  border-color: rgba(15, 118, 110, 0.28);
  box-shadow: 0 16px 34px rgba(12, 36, 48, 0.12);
}

.action-card strong {
  display: block;
  margin-top: 16px;
  color: #10202f;
  font-size: 16px;
  line-height: 1.55;
}

.key-strip {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  padding: 16px 18px;
}

.key-strip code {
  display: block;
  margin-top: 8px;
  overflow: hidden;
  color: #10202f;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.key-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.generated-key {
  margin-top: 16px;
  padding: 14px;
  border: 1px solid rgba(16, 32, 47, 0.08);
  border-radius: 8px;
  background: #f8fafc;
}

.generated-key span {
  display: block;
  margin-bottom: 8px;
  color: #667781;
  font-size: 12px;
  font-weight: 700;
}

.generated-key code {
  display: block;
  color: #10202f;
  font-weight: 800;
  overflow-wrap: anywhere;
}

@media (max-width: 1100px) {
  .hero-panel,
  .meta-grid,
  .work-grid {
    grid-template-columns: 1fr;
  }

  .key-strip {
    align-items: flex-start;
    flex-direction: column;
  }

  .key-actions {
    justify-content: flex-start;
  }
}
</style>
