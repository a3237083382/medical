<template>
  <div class="app-container">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span>接口接入</span>
          <div>
            <el-button type="primary" plain icon="Tickets" @click="goQueryLog">查询记录</el-button>
            <el-button type="primary" plain icon="Wallet" @click="goFeeFlow">费用流水</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="AppKey">{{ company.appKey || "-" }}</el-descriptions-item>
        <el-descriptions-item label="请求地址">{{ endpoint }}</el-descriptions-item>
        <el-descriptions-item label="密钥说明">AppSecret 不在门户页面展示</el-descriptions-item>
      </el-descriptions>

      <el-row :gutter="20" class="doc-row">
        <el-col :xs="24" :lg="12">
          <section class="doc-section">
            <h3>请求头</h3>
            <el-table :data="headers" border>
              <el-table-column label="Header" prop="name" width="160" />
              <el-table-column label="说明" prop="desc" />
            </el-table>
          </section>
        </el-col>
        <el-col :xs="24" :lg="12">
          <section class="doc-section">
            <h3>查询类型与价格</h3>
            <el-table :data="queryTypes" border empty-text="暂无可用查询类型">
              <el-table-column label="查询类型" prop="queryType" width="140" />
              <el-table-column label="名称" prop="queryName" />
              <el-table-column label="单价" width="120">
                <template #default="scope">{{ formatMoney(scope.row.fee) }} 元/次</template>
              </el-table-column>
            </el-table>
          </section>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="doc-row">
        <el-col :xs="24" :lg="12">
          <section class="doc-section">
            <h3>请求示例</h3>
            <pre>{{ requestExample }}</pre>
          </section>
        </el-col>
        <el-col :xs="24" :lg="12">
          <section class="doc-section">
            <h3>响应示例</h3>
            <pre>{{ responseExample }}</pre>
          </section>
        </el-col>
      </el-row>

      <section class="doc-section helper-card">
        <div class="helper-title">
          <h3>Postman 调试助手</h3>
          <span>AppSecret 仅用于本页临时生成签名，不保存、不回传。</span>
        </div>
        <el-form label-width="100px">
          <el-form-item label="AppSecret">
            <el-input
              v-model="signForm.appSecret"
              show-password
              placeholder="请输入管理员交付的 AppSecret"
            />
          </el-form-item>
          <el-form-item label="请求 Body">
            <el-input v-model="signForm.body" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Key" @click="generateSignature">生成 Postman Headers</el-button>
            <el-button icon="CopyDocument" :disabled="!postmanHeaders" @click="copyText(postmanHeaders)">复制 Headers</el-button>
            <el-button icon="CopyDocument" @click="copyText(signForm.body)">复制 Body</el-button>
          </el-form-item>
        </el-form>
        <pre v-if="postmanHeaders" class="headers-output">{{ postmanHeaders }}</pre>
      </section>

      <el-alert
        title="POST /open/api/medical/query"
        type="info"
        :closable="false"
        description="调用方需使用 AppSecret 在服务端生成签名，门户仅展示 AppKey、接口地址和接入参数。"
      />
    </el-card>
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
    ElMessage.warning("请先登录保险公司门户")
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
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.doc-row {
  margin-top: 20px;
}

.doc-section h3 {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.helper-card {
  margin-top: 20px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
}

.helper-title {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
}

.helper-title h3 {
  margin-bottom: 0;
}

.helper-title span {
  color: #6b7280;
  font-size: 13px;
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
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  background: #0f172a;
  color: #e5e7eb;
  line-height: 1.6;
}
</style>
