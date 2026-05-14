<template>
  <div class="query-page">
    <section class="doc-hero">
      <div>
        <span class="eyebrow">API ACCESS</span>
        <h2>医疗信息查询接口接入</h2>
        <p>保险公司在自有系统中按签名协议调用接口，本页面只提供接入参数、价目和调试辅助。</p>
      </div>
      <div class="doc-actions">
        <el-button type="primary" plain icon="Tickets" @click="goQueryLog">查询记录</el-button>
        <el-button type="primary" plain icon="Wallet" @click="goFeeFlow">费用流水</el-button>
      </div>
    </section>

    <section class="access-card">
      <div class="access-item">
        <span>AppKey</span>
        <code>{{ company.appKey || "-" }}</code>
      </div>
      <div class="access-item">
        <span>请求地址</span>
        <code>{{ endpoint }}</code>
      </div>
      <div class="access-item">
        <span>密钥说明</span>
        <strong>AppSecret 不在门户页面展示</strong>
      </div>
    </section>

    <section class="doc-grid">
      <article class="doc-card">
        <h3>请求头</h3>
        <el-table :data="headers" border>
          <el-table-column label="Header" prop="name" width="160" />
          <el-table-column label="说明" prop="desc" />
        </el-table>
      </article>
      <article class="doc-card">
        <h3>查询类型与价格</h3>
        <el-table :data="queryTypes" border empty-text="暂无可用查询类型">
          <el-table-column label="查询类型" prop="queryType" width="150" />
          <el-table-column label="名称" prop="queryName" />
          <el-table-column label="单价" width="130">
            <template #default="scope">{{ formatMoney(scope.row.fee) }} 元/次</template>
          </el-table-column>
        </el-table>
      </article>
    </section>

    <section class="sample-grid">
      <article class="doc-card">
        <h3>请求示例</h3>
        <pre>{{ requestExample }}</pre>
      </article>
      <article class="doc-card">
        <h3>响应示例</h3>
        <pre>{{ responseExample }}</pre>
      </article>
    </section>

    <section class="helper-card">
      <div class="helper-title">
        <div>
          <span class="eyebrow">POSTMAN HELPER</span>
          <h3>签名调试助手</h3>
        </div>
        <p>AppSecret 仅用于本页临时生成签名，不保存、不回传。</p>
      </div>
      <el-form label-width="100px">
        <el-form-item label="AppSecret">
          <el-input
            v-model="signForm.appSecret"
            show-password
            placeholder="请输入平台交付的 AppSecret"
          />
        </el-form-item>
        <el-form-item label="请求 Body">
          <el-input v-model="signForm.body" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Key" @click="generateSignature">生成 Headers</el-button>
          <el-button icon="CopyDocument" :disabled="!postmanHeaders" @click="copyText(postmanHeaders)">复制 Headers</el-button>
          <el-button icon="CopyDocument" @click="copyText(signForm.body)">复制 Body</el-button>
        </el-form-item>
      </el-form>
      <pre v-if="postmanHeaders" class="headers-output">{{ postmanHeaders }}</pre>
    </section>
  </div>
</template>

<script setup name="CompanyQuery">
import { getCompanyProfile, listMedicalQueryTypes } from "@/api/business/portal"
import { ElMessage } from "element-plus"

const router = useRouter()
const company = ref({})
const queryTypes = ref([])
const endpoint = `${window.location.origin}/open/api/medical/query`
const postmanHeaders = ref("")
const signForm = reactive({
  appSecret: "",
  body: `{"queryType":"SURGERY","name":"张三","idCard":"430102199001011234"}`
})
const headers = [
  { name: "X-App-Key", desc: "保险公司 AppKey" },
  { name: "X-Timestamp", desc: "当前毫秒时间戳" },
  { name: "X-Nonce", desc: "随机字符串，避免重放" },
  { name: "X-Sign", desc: "使用 AppSecret 生成的请求签名" }
]
const requestExample = `POST /open/api/medical/query
Content-Type: application/json

{
  "queryType": "OUTPATIENT",
  "name": "张三",
  "idCard": "430***********1234"
}`
const responseExample = `{
  "code": 200,
  "msg": "success",
  "data": {
    "queryType": "OUTPATIENT",
    "name": "张*",
    "idCard": "430***********1234",
    "result": {
      "riskLevel": "LOW",
      "summary": "近一年无重大疾病记录"
    }
  }
}`

function loadProfile() {
  getCompanyProfile().then(res => {
    company.value = res.data || {}
  })
}

function loadQueryTypes() {
  listMedicalQueryTypes().then(res => {
    queryTypes.value = res.data || []
  })
}

function formatMoney(val) {
  if (!val) return "0.00"
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",")
}

async function generateSignature() {
  if (!company.value.appKey) {
    ElMessage.warning("请先登录保险公司端")
    return
  }
  if (!signForm.appSecret) {
    ElMessage.warning("请输入 AppSecret")
    return
  }
  if (!signForm.body) {
    ElMessage.warning("请输入请求 Body")
    return
  }

  const timestamp = Date.now().toString()
  const nonce = randomNonce()
  const sign = await sha256(company.value.appKey + timestamp + nonce + signForm.body + signForm.appSecret.trim())
  postmanHeaders.value = [
    "Content-Type: application/json",
    `X-App-Key: ${company.value.appKey}`,
    `X-Timestamp: ${timestamp}`,
    `X-Nonce: ${nonce}`,
    `X-Sign: ${sign}`
  ].join("\n")
}

async function copyText(text) {
  if (!text) return
  await navigator.clipboard.writeText(text)
  ElMessage.success("已复制")
}

async function sha256(value) {
  const bytes = new TextEncoder().encode(value)
  const hash = await crypto.subtle.digest("SHA-256", bytes)
  return Array.from(new Uint8Array(hash)).map(byte => byte.toString(16).padStart(2, "0")).join("")
}

function randomNonce() {
  if (crypto.randomUUID) {
    return crypto.randomUUID().replace(/-/g, "")
  }
  return `${Date.now()}${Math.random().toString(16).slice(2)}`
}

function goQueryLog() {
  router.push("/company/query-log")
}

function goFeeFlow() {
  router.push("/company/fee-flow")
}

loadProfile()
loadQueryTypes()
</script>

<style scoped>
.query-page {
  display: grid;
  gap: 18px;
}

.doc-hero,
.access-card,
.doc-card,
.helper-card {
  border: 1px solid rgba(16, 32, 47, 0.08);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 10px 28px rgba(12, 36, 48, 0.06);
}

.doc-hero {
  min-height: 148px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(15, 118, 110, 0.11), transparent 48%),
    #ffffff;
}

.eyebrow {
  color: #0f766e;
  font-size: 12px;
  font-weight: 800;
}

.doc-hero h2,
.helper-title h3 {
  margin: 8px 0;
  color: #10202f;
  font-size: 26px;
  letter-spacing: 0;
}

.doc-hero p,
.helper-title p {
  margin: 0;
  color: #667781;
  line-height: 1.7;
}

.doc-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.access-card {
  display: grid;
  grid-template-columns: 1.2fr 1.8fr 1fr;
}

.access-item {
  min-width: 0;
  padding: 18px;
  border-right: 1px solid rgba(16, 32, 47, 0.08);
}

.access-item:last-child {
  border-right: 0;
}

.access-item span {
  display: block;
  margin-bottom: 10px;
  color: #6f7e86;
  font-size: 12px;
  font-weight: 700;
}

.access-item code,
.access-item strong {
  display: block;
  overflow: hidden;
  color: #10202f;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-grid,
.sample-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.doc-card {
  min-width: 0;
  padding: 18px;
}

.doc-card h3 {
  margin: 0 0 14px;
  color: #10202f;
  font-size: 16px;
  font-weight: 800;
}

.helper-card {
  padding: 20px;
}

.helper-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.headers-output {
  min-height: 140px;
  margin-top: 8px;
}

pre {
  min-height: 220px;
  margin: 0;
  padding: 14px;
  overflow: auto;
  border: 1px solid rgba(142, 228, 216, 0.16);
  border-radius: 8px;
  background: #0c2430;
  color: #e5e7eb;
  line-height: 1.6;
}

@media (max-width: 1080px) {
  .doc-hero,
  .helper-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .access-card,
  .doc-grid,
  .sample-grid {
    grid-template-columns: 1fr;
  }

  .access-item {
    border-right: 0;
    border-bottom: 1px solid rgba(16, 32, 47, 0.08);
  }

  .access-item:last-child {
    border-bottom: 0;
  }
}
</style>
