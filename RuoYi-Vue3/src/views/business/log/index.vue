<template>
  <div class="app-container log-page">
    <el-radio-group v-model="viewMode" class="mode-switch" @change="handleModeChange">
      <el-radio-button label="all">按所有查询</el-radio-button>
      <el-radio-button label="company">按公司分组</el-radio-button>
    </el-radio-group>

    <template v-if="viewMode === 'all'">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="82px">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width: 220px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="查询类型" prop="queryType">
          <el-input v-model="queryParams.queryType" placeholder="请输入查询类型" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="结果状态" prop="resultStatus">
          <el-select v-model="queryParams.resultStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="查得" value="HIT" />
            <el-option label="未查得" value="NO_RESULT" />
            <el-option label="失败" value="FAILED" />
            <el-option label="部分" value="PARTIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="日志状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" value="0" />
            <el-option label="失败" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="请求时间">
          <el-date-picker
            v-model="dateRange"
            value-format="YYYY-MM-DD"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:log:export']">导出</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table v-loading="loading" :data="logList" border stripe>
        <el-table-column label="公司名称" align="center" prop="companyName" min-width="160" />
        <el-table-column label="查询类型" align="center" prop="queryType" width="150">
          <template #default="{ row }">{{ queryTypeText(row.queryType) }}</template>
        </el-table-column>
        <el-table-column label="结果状态" align="center" width="110">
          <template #default="{ row }">
            <el-tag :type="resultTagType(row.resultStatus)">{{ resultStatusText(row.resultStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="费用(元)" align="center" prop="fee" width="110">
          <template #default="{ row }">{{ formatMoney(row.fee) }}</template>
        </el-table-column>
        <el-table-column label="日志状态" align="center" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求IP" align="center" prop="requestIp" width="140" />
        <el-table-column label="请求时间" align="center" prop="requestTime" width="170" />
        <el-table-column label="备注" align="center" prop="remark" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" align="center" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="handleDetail(row)" v-hasPermi="['business:log:query']">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </template>

    <template v-else>
      <div class="company-toolbar">
        <el-form :model="companyQuery" :inline="true" label-width="82px">
          <el-form-item label="公司名称">
            <el-input v-model="companyQuery.companyName" placeholder="输入公司名称筛选" clearable style="width: 260px" @keyup.enter="getCompanyList" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="getCompanyList">查询公司</el-button>
            <el-button icon="Refresh" @click="resetCompanyQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-row :gutter="16">
        <el-col :span="7">
          <el-table
            v-loading="companyLoading"
            :data="companyList"
            border
            stripe
            highlight-current-row
            height="560"
            @current-change="handleCompanyChange"
          >
            <el-table-column label="公司名称" prop="companyName" min-width="160" show-overflow-tooltip />
            <el-table-column label="状态" width="82" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" align="center">
              <template #default="{ row }">
                <el-button link type="primary" icon="View" @click.stop="selectCompany(row)">日志</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination
            v-show="companyTotal > 0"
            :total="companyTotal"
            v-model:page="companyQuery.pageNum"
            v-model:limit="companyQuery.pageSize"
            @pagination="getCompanyList"
            small
          />
        </el-col>

        <el-col :span="17">
          <el-empty v-if="!selectedCompany.id" description="请选择左侧公司查看实时与非实时日志" />
          <template v-else>
            <el-descriptions :column="3" border class="company-summary">
              <el-descriptions-item label="公司名称">{{ selectedCompany.companyName }}</el-descriptions-item>
              <el-descriptions-item label="公司账号">{{ selectedCompany.username || '-' }}</el-descriptions-item>
              <el-descriptions-item label="公司状态">{{ selectedCompany.status === '0' ? '正常' : '停用' }}</el-descriptions-item>
            </el-descriptions>

            <el-tabs v-model="companyTab" class="mt16">
              <el-tab-pane label="实时查询日志" name="realtime">
                <el-table v-loading="companyLogLoading" :data="realtimeCompanyLogs" border stripe height="440">
                  <el-table-column label="查询类型" width="150">
                    <template #default="{ row }">{{ queryTypeText(row.queryType) }}</template>
                  </el-table-column>
                  <el-table-column label="查询参数" prop="queryParams" min-width="240" show-overflow-tooltip />
                  <el-table-column label="结果状态" width="110" align="center">
                    <template #default="{ row }">
                      <el-tag :type="resultTagType(row.resultStatus)">{{ resultStatusText(row.resultStatus) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="费用" width="100" align="center">
                    <template #default="{ row }">{{ formatMoney(row.fee) }}</template>
                  </el-table-column>
                  <el-table-column label="请求时间" prop="requestTime" width="170" />
                  <el-table-column label="操作" width="90" fixed="right" align="center">
                    <template #default="{ row }">
                      <el-button link type="primary" icon="View" @click="openLogDetail(row)">详情</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>

              <el-tab-pane label="非实时/精准延时日志" name="delayed">
                <el-table v-loading="companyLogLoading" :data="delayedLogs" border stripe height="440">
                  <el-table-column label="姓名" prop="patientName" width="120" />
                  <el-table-column label="身份证号" prop="idCard" width="190" show-overflow-tooltip />
                  <el-table-column label="查询状态" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag :type="row.queryStatus === 'QUERIED' ? 'success' : 'warning'">
                        {{ row.queryStatus === 'QUERIED' ? '已查询' : '处理中' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="上传状态" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag :type="row.uploadStatus === 'UPLOADED' ? 'success' : 'info'">
                        {{ row.uploadStatus === 'UPLOADED' ? '已上传' : '未上传' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="结果状态" width="110" align="center">
                    <template #default="{ row }">{{ resultStatusText(row.resultStatus) }}</template>
                  </el-table-column>
                  <el-table-column label="费用" width="100" align="center">
                    <template #default="{ row }">{{ formatMoney(row.fee) }}</template>
                  </el-table-column>
                  <el-table-column label="提交时间" prop="submitTime" width="170" />
                  <el-table-column label="处理时间" prop="handledTime" width="170" />
                  <el-table-column label="操作" width="100" fixed="right" align="center">
                    <template #default="{ row }">
                      <el-button link type="primary" icon="View" @click="openDelayedDetail(row)">结果</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </template>
        </el-col>
      </el-row>
    </template>

    <el-dialog title="查询日志详情" v-model="detailOpen" width="720px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="公司名称">{{ detail.companyName || selectedCompany.companyName }}</el-descriptions-item>
        <el-descriptions-item label="查询类型">{{ queryTypeText(detail.queryType) }}</el-descriptions-item>
        <el-descriptions-item label="结果状态">{{ resultStatusText(detail.resultStatus) }}</el-descriptions-item>
        <el-descriptions-item label="费用">{{ formatMoney(detail.fee) }}</el-descriptions-item>
        <el-descriptions-item label="日志状态">{{ detail.status === '0' ? '成功' : '失败' }}</el-descriptions-item>
        <el-descriptions-item label="请求时间">{{ detail.requestTime }}</el-descriptions-item>
        <el-descriptions-item label="请求IP">{{ detail.requestIp }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark }}</el-descriptions-item>
        <el-descriptions-item label="查询参数" :span="2">
          <pre class="json-block">{{ formatJson(detail.queryParams) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailOpen = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog title="非实时/精准延时结果" v-model="delayedDetailOpen" width="900px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="公司名称">{{ delayedDetail.companyNameSnapshot || selectedCompany.companyName }}</el-descriptions-item>
        <el-descriptions-item label="请求编号">{{ delayedDetail.requestNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ delayedDetail.patientName }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ delayedDetail.idCard }}</el-descriptions-item>
        <el-descriptions-item label="查询状态">{{ delayedDetail.queryStatus === 'QUERIED' ? '已查询' : '处理中' }}</el-descriptions-item>
        <el-descriptions-item label="上传状态">{{ delayedDetail.uploadStatus === 'UPLOADED' ? '已上传' : '未上传' }}</el-descriptions-item>
        <el-descriptions-item label="结果状态">{{ resultStatusText(delayedDetail.resultStatus) }}</el-descriptions-item>
        <el-descriptions-item label="结果说明">{{ delayedDetail.resultMessage }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="delayedDetail.results || []" border size="small" max-height="320" class="mt16">
        <el-table-column label="结果明细">
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

<script setup name="BusinessQueryLog">
import { listQueryLog, getQueryLog } from "@/api/business/queryLog"
import { listCompany } from "@/api/business/company"
import { getCompanyDelayedLogs, getDelayedQuery } from "@/api/business/delayedQuery"
import { addDateRange } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const viewMode = ref("all")
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const logList = ref([])
const dateRange = ref([])
const detailOpen = ref(false)
const detail = ref({})

const companyLoading = ref(false)
const companyLogLoading = ref(false)
const companyList = ref([])
const companyTotal = ref(0)
const selectedCompany = reactive({})
const companyTab = ref("realtime")
const realtimeLogs = ref([])
const delayedLogs = ref([])
const delayedDetailOpen = ref(false)
const delayedDetail = ref({})

const realtimeCompanyLogs = computed(() => realtimeLogs.value.filter(item => item.queryType !== "delayed_precise"))

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  companyName: undefined,
  queryType: undefined,
  resultStatus: undefined,
  status: undefined
})

const companyQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  companyName: undefined
})

function handleModeChange() {
  if (viewMode.value === "all") {
    getList()
  } else {
    getCompanyList()
  }
}

function getList() {
  loading.value = true
  listQueryLog(addDateRange(queryParams, dateRange.value)).then(res => {
    logList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => { loading.value = false })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleDetail(row) {
  getQueryLog(row.id).then(res => {
    detail.value = res.data || {}
    detailOpen.value = true
  })
}

function handleExport() {
  proxy.download("business/log/export", addDateRange(queryParams, dateRange.value), `query_log_${new Date().getTime()}.xlsx`)
}

function getCompanyList() {
  companyLoading.value = true
  listCompany(companyQuery).then(res => {
    companyList.value = res.rows || []
    companyTotal.value = res.total || 0
  }).finally(() => {
    companyLoading.value = false
  })
}

function resetCompanyQuery() {
  companyQuery.companyName = undefined
  companyQuery.pageNum = 1
  getCompanyList()
}

function handleCompanyChange(row) {
  if (row) {
    selectCompany(row)
  }
}

function selectCompany(row) {
  Object.assign(selectedCompany, row || {})
  companyTab.value = "realtime"
  getCompanyLogs()
}

function getCompanyLogs() {
  if (!selectedCompany.id) return
  companyLogLoading.value = true
  getCompanyDelayedLogs(selectedCompany.id).then(res => {
    realtimeLogs.value = res.data?.realtimeLogs || []
    delayedLogs.value = res.data?.delayedLogs || []
  }).finally(() => {
    companyLogLoading.value = false
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

function queryTypeText(type) {
  if (type === "delayed_precise") return "精准延时查询"
  if (type === "medical") return "实时医疗查询"
  return type || "-"
}

function resultStatusText(status) {
  if (status === "HIT") return "查得"
  if (status === "NO_RESULT") return "未查得"
  if (status === "FAILED") return "失败"
  if (status === "PARTIAL") return "部分"
  return status || "-"
}

function resultTagType(status) {
  if (status === "HIT") return "success"
  if (status === "NO_RESULT") return "info"
  if (status === "FAILED") return "danger"
  if (status === "PARTIAL") return "warning"
  return "info"
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
.mode-switch {
  margin-bottom: 18px;
}

.company-toolbar {
  padding: 2px 0 10px;
}

.company-summary {
  margin-bottom: 12px;
}

.mt16 {
  margin-top: 16px;
}

.json-block {
  max-height: 260px;
  margin: 0;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
