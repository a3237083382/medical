<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="查询类型" prop="queryType">
        <el-input v-model="queryParams.queryType" placeholder="请输入查询类型" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
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
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
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
      <el-table-column label="查询类型" align="center" prop="queryType" width="140" />
      <el-table-column label="费用(元)" align="center" prop="fee" width="110">
        <template #default="{ row }">{{ formatMoney(row.fee) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="90">
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

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="查询日志详情" v-model="detailOpen" width="640px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="公司名称">{{ detail.companyName }}</el-descriptions-item>
        <el-descriptions-item label="查询类型">{{ detail.queryType }}</el-descriptions-item>
        <el-descriptions-item label="费用">{{ formatMoney(detail.fee) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status === '0' ? '成功' : '失败' }}</el-descriptions-item>
        <el-descriptions-item label="请求IP">{{ detail.requestIp }}</el-descriptions-item>
        <el-descriptions-item label="请求时间">{{ detail.requestTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
        <el-descriptions-item label="查询参数" :span="2">
          <pre class="json-block">{{ formatJson(detail.queryParams) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessQueryLog">
import { listQueryLog, getQueryLog } from "@/api/business/queryLog"
import { addDateRange } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const logList = ref([])
const dateRange = ref([])
const detailOpen = ref(false)
const detail = ref({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  companyName: undefined,
  queryType: undefined,
  status: undefined
})

function getList() {
  loading.value = true
  listQueryLog(addDateRange(queryParams, dateRange.value)).then(res => {
    logList.value = res.rows
    total.value = res.total
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

function formatMoney(val) {
  if (!val) return "0.00"
  return Number(val).toFixed(2)
}

function formatJson(value) {
  if (!value) return ""
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch (e) {
    return value
  }
}

getList()
</script>

<style scoped>
.json-block {
  max-height: 220px;
  margin: 0;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
