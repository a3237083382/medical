<template>
  <div class="app-container">
    <el-page-header @back="goBack">
      <template #content>
        <span>公司查询日志</span>
      </template>
    </el-page-header>

    <el-descriptions :column="3" border class="mt16">
      <el-descriptions-item label="公司ID">{{ companyId }}</el-descriptions-item>
      <el-descriptions-item label="公司名称">{{ companyName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="数据类型">实时查询 / 精准延时查询</el-descriptions-item>
    </el-descriptions>

    <el-tabs v-model="activeTab" class="mt16">
      <el-tab-pane label="实时查询日志" name="realtime">
        <el-table v-loading="loading" :data="realtimeLogs" border stripe>
          <el-table-column label="查询类型" prop="queryType" width="140" />
          <el-table-column label="姓名" prop="patientName" width="120" />
          <el-table-column label="查询参数" prop="queryParams" min-width="220" show-overflow-tooltip />
          <el-table-column label="查询结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.resultStatus === 'HIT' ? 'success' : row.resultStatus === 'NO_RESULT' ? 'info' : 'warning'">
                {{ row.resultStatus || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="费用" width="110">
            <template #default="{ row }">{{ formatMoney(row.fee) }}</template>
          </el-table-column>
          <el-table-column label="时间" prop="requestTime" width="170" />
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" icon="View" @click="openLogDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="精准延时日志" name="delayed">
        <el-table v-loading="loading" :data="delayedLogs" border stripe>
          <el-table-column label="姓名" prop="patientName" width="120" />
          <el-table-column label="身份证号" prop="idCard" width="190" />
          <el-table-column label="查询状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.queryStatus === 'QUERIED' ? 'success' : 'warning'">{{ row.queryStatus === 'QUERIED' ? '已查询' : '处理中' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="结果上传" width="100">
            <template #default="{ row }">
              <el-tag :type="row.uploadStatus === 'UPLOADED' ? 'success' : 'info'">{{ row.uploadStatus === 'UPLOADED' ? '已上传' : '未上传' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="结果状态" prop="resultStatus" width="110" />
          <el-table-column label="费用" width="110">
            <template #default="{ row }">{{ formatMoney(row.fee) }}</template>
          </el-table-column>
          <el-table-column label="提交时间" prop="submitTime" width="170" />
          <el-table-column label="处理时间" prop="handledTime" width="170" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" icon="View" @click="openDelayedDetail(row)">结果</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog title="日志详情" v-model="detailOpen" width="760px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="查询类型">{{ detail.queryType }}</el-descriptions-item>
        <el-descriptions-item label="查询结果">{{ detail.resultStatus }}</el-descriptions-item>
        <el-descriptions-item label="费用">{{ formatMoney(detail.fee) }}</el-descriptions-item>
        <el-descriptions-item label="请求时间">{{ detail.requestTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="json-block">{{ formatJson(detail.queryParams) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailOpen = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog title="精准延时结果" v-model="delayedDetailOpen" width="860px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ delayedDetail.patientName }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ delayedDetail.idCard }}</el-descriptions-item>
        <el-descriptions-item label="查询状态">{{ delayedDetail.queryStatus }}</el-descriptions-item>
        <el-descriptions-item label="结果上传">{{ delayedDetail.uploadStatus }}</el-descriptions-item>
        <el-descriptions-item label="结果状态">{{ delayedDetail.resultStatus }}</el-descriptions-item>
        <el-descriptions-item label="结果说明">{{ delayedDetail.resultMessage }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="delayedDetail.results || []" border size="small" max-height="280" class="mt16">
        <el-table-column label="明细">
          <template #default="{ row }">
            <pre class="json-block">{{ formatJson(row.rawJson) }}</pre>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="delayedDetailOpen = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CompanyLogs">
import { getCompanyDelayedLogs, getDelayedQuery } from "@/api/business/delayedQuery"

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const activeTab = ref("realtime")
const realtimeLogs = ref([])
const delayedLogs = ref([])
const companyId = computed(() => route.params.companyId)
const companyName = computed(() => route.query.companyName)
const detailOpen = ref(false)
const detail = ref({})
const delayedDetailOpen = ref(false)
const delayedDetail = ref({})

function getList() {
  loading.value = true
  getCompanyDelayedLogs(companyId.value).then(res => {
    realtimeLogs.value = res.data?.realtimeLogs || []
    delayedLogs.value = res.data?.delayedLogs || []
  }).finally(() => {
    loading.value = false
  })
}

function openLogDetail(row) {
  detail.value = row || {}
  detailOpen.value = true
}

function openDelayedDetail(row) {
  getDelayedQuery(row.id).then(res => {
    delayedDetail.value = res.data || row || {}
    delayedDetailOpen.value = true
  })
}

function goBack() {
  router.back()
}

function formatMoney(val) {
  if (val === null || val === undefined || val === "") return "0.00"
  return Number(val).toFixed(2)
}

function formatJson(value) {
  if (!value) return ""
  if (typeof value === "string") {
    try {
      return JSON.stringify(JSON.parse(value), null, 2)
    } catch (e) {
      return value
    }
  }
  return JSON.stringify(value, null, 2)
}

getList()
</script>

<style scoped>
.mt16 {
  margin-top: 16px;
}

.json-block {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
