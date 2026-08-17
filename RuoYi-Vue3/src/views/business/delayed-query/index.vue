<template>
  <div class="app-container delayed-query-page">
    <el-tabs v-model="activeView" class="view-tabs" @tab-change="handleViewChange">
      <el-tab-pane label="人员请求" name="requests" />
      <el-tab-pane label="批次处理" name="batches" />
    </el-tabs>

    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch && activeView === 'requests'" label-width="76px">
      <el-form-item label="请求编号" prop="requestNo">
        <el-input v-model="queryParams.requestNo" placeholder="请输入请求编号" clearable style="width: 210px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width: 190px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="姓名" prop="patientName">
        <el-input v-model="queryParams.patientName" placeholder="请输入姓名" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="处理状态" prop="processStatus">
        <el-select v-model="queryParams.processStatus" placeholder="全部" clearable style="width: 140px">
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="失败" value="FAILED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-form-item>
      <el-form-item label="提交时间">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form ref="batchQueryRef" :model="batchQueryParams" :inline="true" v-show="showSearch && activeView === 'batches'" label-width="76px">
      <el-form-item label="批次编号" prop="batchNo">
        <el-input v-model="batchQueryParams.batchNo" placeholder="请输入批次编号" clearable style="width: 220px" @keyup.enter="handleBatchQuery" />
      </el-form-item>
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="batchQueryParams.companyName" placeholder="请输入公司名称" clearable style="width: 190px" @keyup.enter="handleBatchQuery" />
      </el-form-item>
      <el-form-item label="批次状态" prop="batchStatus">
        <el-select v-model="batchQueryParams.batchStatus" placeholder="全部" clearable style="width: 150px">
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="部分失败" value="PARTIAL_FAILED" />
          <el-option label="失败" value="FAILED" />
          <el-option label="部分取消" value="PARTIAL_CANCELLED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-form-item>
      <el-form-item label="提交时间">
        <el-date-picker v-model="batchDateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleBatchQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetBatchQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="refreshCurrentView" />
    </el-row>

    <el-table v-if="activeView === 'requests'" v-loading="loading" :data="requestList" border stripe>
      <el-table-column label="请求编号" prop="requestNo" min-width="220" show-overflow-tooltip />
      <el-table-column label="公司名称" prop="companyName" min-width="160" show-overflow-tooltip />
      <el-table-column label="人员" min-width="190">
        <template #default="{ row }">
          <div>{{ row.patientName }}</div>
          <div class="secondary-text">{{ maskIdCard(row.idCard) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="处理状态" width="100" align="center">
        <template #default="{ row }"><el-tag :type="processTag(row.processStatus)">{{ processLabel(row.processStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="结果状态" width="100" align="center">
        <template #default="{ row }">{{ resultLabel(row.resultStatus) }}</template>
      </el-table-column>
      <el-table-column label="费用(元)" width="100" align="right">
        <template #default="{ row }">{{ formatMoney(row.feeSnapshot) }}</template>
      </el-table-column>
      <el-table-column label="提交时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="190" fixed="right" align="center">
        <template #default="{ row }">
          <el-button v-if="row.processStatus === 'PENDING'" link type="primary" icon="VideoPlay" @click="handleStart(row)" v-hasPermi="['business:delayed-query:start']">开始处理</el-button>
          <el-button link type="primary" icon="View" @click="openDetail(row)" v-hasPermi="['business:delayed-query:query']">{{ row.processStatus === 'PROCESSING' ? '处理结果' : '详情' }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-if="activeView === 'requests' && total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-table v-if="activeView === 'batches'" v-loading="batchLoading" :data="batchList" border stripe>
      <el-table-column label="批次编号" prop="batchNo" min-width="220" show-overflow-tooltip />
      <el-table-column label="公司名称" prop="companyName" min-width="170" show-overflow-tooltip />
      <el-table-column label="批次状态" width="110" align="center">
        <template #default="{ row }"><el-tag :type="batchTag(row.batchStatus)">{{ batchLabel(row.batchStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="进度" min-width="210">
        <template #default="{ row }">
          <div>完成 {{ row.completedCount || 0 }} / {{ row.totalCount || 0 }}</div>
          <div class="secondary-text">待处理 {{ row.pendingCount || 0 }} · 处理中 {{ row.processingCount || 0 }} · 取消 {{ row.cancelledCount || 0 }}</div>
        </template>
      </el-table-column>
      <el-table-column label="结果" min-width="150">
        <template #default="{ row }">查得 {{ row.hitCount || 0 }} · 未查得 {{ row.noResultCount || 0 }} · 失败 {{ row.failedCount || 0 }}</template>
      </el-table-column>
      <el-table-column label="费用(元)" width="110" align="right">
        <template #default="{ row }">{{ formatMoney(row.totalFee) }}</template>
      </el-table-column>
      <el-table-column label="提交时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="110" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="openBatchDetail(row)" v-hasPermi="['business:delayed-query:query']">人员明细</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-if="activeView === 'batches' && batchTotal > 0" :total="batchTotal" v-model:page="batchQueryParams.pageNum" v-model:limit="batchQueryParams.pageSize" @pagination="getBatchList" />

    <el-dialog v-model="batchDetailOpen" title="精准延时批次详情" width="90%" top="5vh" append-to-body destroy-on-close>
      <div v-loading="batchDetailLoading" class="batch-detail-content">
        <el-descriptions :column="4" border class="batch-summary">
          <el-descriptions-item label="批次编号" :span="2">{{ currentBatch.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="公司">{{ currentBatch.companyName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ batchLabel(currentBatch.batchStatus) }}</el-descriptions-item>
          <el-descriptions-item label="总人数">{{ currentBatch.totalCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="待处理 / 处理中">{{ currentBatch.pendingCount || 0 }} / {{ currentBatch.processingCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="完成 / 失败">{{ currentBatch.completedCount || 0 }} / {{ currentBatch.failedCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="取消 / 费用">{{ currentBatch.cancelledCount || 0 }} / {{ formatMoney(currentBatch.totalFee) }} 元</el-descriptions-item>
        </el-descriptions>

        <el-table :data="batchItems" border stripe height="460">
          <el-table-column label="序号" prop="rowNo" width="70" align="center" />
          <el-table-column label="请求编号" prop="requestNo" min-width="210" show-overflow-tooltip />
          <el-table-column label="人员" min-width="190">
            <template #default="{ row }"><div>{{ row.patientName }}</div><div class="secondary-text">{{ maskIdCard(row.idCard) }}</div></template>
          </el-table-column>
          <el-table-column label="来源" width="90" align="center">
            <template #default="{ row }">{{ row.reusedFlag === '1' ? '复用' : '新建' }}</template>
          </el-table-column>
          <el-table-column label="处理状态" width="100" align="center">
            <template #default="{ row }"><el-tag :type="processTag(row.processStatus)">{{ processLabel(row.processStatus) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="结果状态" width="100" align="center">
            <template #default="{ row }">{{ resultLabel(row.resultStatus) }}</template>
          </el-table-column>
          <el-table-column label="费用(元)" width="100" align="right">
            <template #default="{ row }">{{ formatMoney(row.feeSnapshot) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="190" fixed="right" align="center">
            <template #default="{ row }">
              <el-button v-if="row.processStatus === 'PENDING'" link type="primary" icon="VideoPlay" @click="startBatchMember(row)" v-hasPermi="['business:delayed-query:start']">开始处理</el-button>
              <el-button v-if="row.processStatus !== 'CANCELLED'" link type="primary" icon="View" @click="openBatchMember(row)" v-hasPermi="['business:delayed-query:query']">{{ row.processStatus === 'PROCESSING' ? '处理结果' : '详情' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer><el-button @click="batchDetailOpen = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailOpen" :title="detailTitle" width="92%" top="4vh" append-to-body destroy-on-close>
      <div v-loading="detailLoading" class="detail-content">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="请求编号" :span="2">{{ currentRequest.requestNo }}</el-descriptions-item>
          <el-descriptions-item label="公司">{{ currentRequest.companyName }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentRequest.createTime }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentRequest.patientName }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentRequest.idCard }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">{{ processLabel(currentRequest.processStatus) }}</el-descriptions-item>
          <el-descriptions-item label="结果状态">{{ resultLabel(currentRequest.resultStatus) }}</el-descriptions-item>
        </el-descriptions>

        <template v-if="currentRequest.processStatus === 'PENDING'">
          <el-empty description="请求尚未开始处理">
            <el-button type="primary" icon="VideoPlay" @click="startFromDetail" v-hasPermi="['business:delayed-query:start']">开始处理</el-button>
          </el-empty>
        </template>

        <template v-else-if="canShowEditor">
          <div class="result-toolbar">
            <div class="toolbar-group">
              <el-upload :show-file-list="false" :http-request="handleExcelImport" accept=".xlsx,.xls" v-hasPermi="['business:delayed-query:edit']">
                <el-button icon="Upload">导入 Excel</el-button>
              </el-upload>
              <el-button icon="Plus" @click="addColumn">新增字段</el-button>
              <el-button icon="Plus" @click="addRow">新增记录</el-button>
            </div>
            <el-alert v-if="currentRequest.uploadStatus === 'UPLOADED'" title="已上传结果只能修改内容，不能改变结果状态和计费" type="warning" :closable="false" show-icon />
          </div>

          <el-form label-width="90px" class="result-form">
            <el-form-item label="结果状态" required>
              <el-select v-model="resultForm.resultStatus" :disabled="currentRequest.uploadStatus === 'UPLOADED'" style="width: 220px">
                <el-option label="查得" value="HIT" />
                <el-option label="未查得" value="NO_RESULT" />
                <el-option label="仅提示" value="HINT_ONLY" />
              </el-select>
            </el-form-item>
            <el-form-item label="结果说明">
              <el-input v-model="resultForm.resultSummary" type="textarea" :rows="2" maxlength="1000" show-word-limit />
            </el-form-item>
            <el-form-item v-if="currentRequest.uploadStatus === 'UPLOADED'" label="修改说明" required>
              <el-input v-model="resultForm.updateReason" placeholder="说明本次修正内容" maxlength="500" show-word-limit />
            </el-form-item>
          </el-form>

          <div class="dynamic-table-wrap">
            <el-table :data="resultRecords" border height="360">
              <el-table-column type="index" label="#" width="56" fixed="left" />
              <el-table-column v-for="column in resultColumns" :key="column.field" :min-width="180">
                <template #header>
                  <div class="column-header">
                    <el-input v-model="column.label" size="small" maxlength="50" />
                    <el-button link type="danger" icon="Delete" title="删除字段" @click="removeColumn(column.field)" />
                  </div>
                </template>
                <template #default="{ row }"><el-input v-model="row[column.field]" /></template>
              </el-table-column>
              <el-table-column label="操作" width="76" fixed="right" align="center">
                <template #default="{ $index }"><el-button link type="danger" icon="Delete" title="删除记录" @click="removeRow($index)" /></template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关闭</el-button>
          <el-button v-if="currentRequest.processStatus === 'PROCESSING'" icon="Document" @click="saveDraft" v-hasPermi="['business:delayed-query:edit']">保存草稿</el-button>
          <el-button v-if="currentRequest.processStatus === 'PROCESSING'" type="primary" icon="UploadFilled" @click="completeResult" v-hasPermi="['business:delayed-query:complete']">上传完毕</el-button>
          <el-button v-if="currentRequest.uploadStatus === 'UPLOADED'" type="primary" icon="Check" @click="saveCorrection" v-hasPermi="['business:delayed-query:update']">保存修改</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DelayedMedicalQuery">
import { addDateRange } from '@/utils/ruoyi'
import {
  completeDelayedQuery,
  getDelayedQuery,
  getDelayedQueryBatch,
  importDelayedResult,
  listDelayedQuery,
  listDelayedQueryBatch,
  saveDelayedDraft,
  startDelayedQuery,
  updateDelayedResult
} from '@/api/business/delayedQuery'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const activeView = ref('requests')
const showSearch = ref(true)
const total = ref(0)
const requestList = ref([])
const dateRange = ref([])
const batchLoading = ref(false)
const batchTotal = ref(0)
const batchList = ref([])
const batchDateRange = ref([])
const batchDetailOpen = ref(false)
const batchDetailLoading = ref(false)
const currentBatchId = ref()
const currentBatch = ref({})
const batchItems = ref([])
const detailOpen = ref(false)
const detailLoading = ref(false)
const currentId = ref()
const currentRequest = ref({})
const resultColumns = ref([])
const resultRecords = ref([])
const resultForm = reactive({ resultStatus: 'HIT', resultSummary: '', updateReason: '' })

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  requestNo: undefined,
  companyName: undefined,
  patientName: undefined,
  processStatus: undefined
})

const batchQueryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  batchNo: undefined,
  companyName: undefined,
  batchStatus: undefined
})

const detailTitle = computed(() => currentRequest.value.processStatus === 'PROCESSING' ? '处理精准延时结果' : '精准延时请求详情')
const canShowEditor = computed(() => currentRequest.value.processStatus === 'PROCESSING' || currentRequest.value.uploadStatus === 'UPLOADED')

function getList() {
  loading.value = true
  listDelayedQuery(addDateRange(queryParams, dateRange.value)).then(res => {
    requestList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => { loading.value = false })
}

function getBatchList() {
  batchLoading.value = true
  listDelayedQueryBatch(addDateRange(batchQueryParams, batchDateRange.value)).then(res => {
    batchList.value = res.rows || []
    batchTotal.value = res.total || 0
  }).finally(() => { batchLoading.value = false })
}

function handleViewChange(name) {
  if (name === 'batches') getBatchList()
}

function refreshCurrentView() {
  activeView.value === 'batches' ? getBatchList() : getList()
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleBatchQuery() {
  batchQueryParams.pageNum = 1
  getBatchList()
}

function resetBatchQuery() {
  batchDateRange.value = []
  proxy.resetForm('batchQueryRef')
  handleBatchQuery()
}

function openBatchDetail(row) {
  currentBatchId.value = row.id
  batchDetailOpen.value = true
  loadBatchDetail(row.id)
}

function loadBatchDetail(id) {
  batchDetailLoading.value = true
  getDelayedQueryBatch(id).then(res => {
    const detail = res.data || {}
    currentBatch.value = detail.batch || {}
    batchItems.value = detail.items || []
  }).finally(() => { batchDetailLoading.value = false })
}

function refreshBatchDetailIfOpen() {
  if (batchDetailOpen.value && currentBatchId.value) loadBatchDetail(currentBatchId.value)
  if (activeView.value === 'batches') getBatchList()
}

function startBatchMember(row) {
  proxy.$modal.confirm(`确认开始处理 ${row.patientName} 的精准延时请求？`).then(() => startDelayedQuery(row.requestId)).then(() => {
    proxy.$modal.msgSuccess('已开始处理')
    refreshBatchDetailIfOpen()
    openDetail({ id: row.requestId })
  }).catch(() => {})
}

function openBatchMember(row) {
  openDetail({ id: row.requestId })
}

function handleStart(row) {
  proxy.$modal.confirm(`确认开始处理 ${row.patientName} 的精准延时请求？`).then(() => startDelayedQuery(row.id)).then(() => {
    proxy.$modal.msgSuccess('已开始处理')
    getList()
    openDetail(row)
  }).catch(() => {})
}

function startFromDetail() {
  startDelayedQuery(currentId.value).then(() => {
    proxy.$modal.msgSuccess('已开始处理')
    loadDetail(currentId.value)
    getList()
    refreshBatchDetailIfOpen()
  })
}

function openDetail(row) {
  currentId.value = row.id
  detailOpen.value = true
  loadDetail(row.id)
}

function loadDetail(id) {
  detailLoading.value = true
  getDelayedQuery(id).then(res => {
    const detail = res.data || {}
    currentRequest.value = detail.request || {}
    applyResult(detail)
  }).finally(() => { detailLoading.value = false })
}

function applyResult(detail) {
  resultColumns.value = Array.isArray(detail.columnSchema) ? detail.columnSchema.map((column, index) => ({
    field: column.field || `c${index + 1}`,
    label: column.label || `字段${index + 1}`,
    order: index
  })) : [{ field: 'c1', label: '结果', order: 0 }]
  const records = detail.data && Array.isArray(detail.data.records) ? detail.data.records : []
  resultRecords.value = records.map(record => ({ ...record }))
  resultForm.resultStatus = currentRequest.value.resultStatus || 'HIT'
  resultForm.resultSummary = detail.resultSummary || ''
  resultForm.updateReason = ''
}

function handleExcelImport(options) {
  detailLoading.value = true
  importDelayedResult(currentId.value, options.file).then(res => {
    const preview = res.data || {}
    applyResult({ columnSchema: preview.columnSchema, data: preview.data, resultSummary: preview.resultSummary })
    resultForm.resultStatus = preview.resultStatus || 'HIT'
    proxy.$modal.msgSuccess('Excel 已解析，请确认后保存')
  }).finally(() => { detailLoading.value = false })
}

function addColumn() {
  const field = `c${Date.now()}`
  resultColumns.value.push({ field, label: `字段${resultColumns.value.length + 1}`, order: resultColumns.value.length })
  resultRecords.value.forEach(row => { row[field] = '' })
}

function removeColumn(field) {
  if (resultColumns.value.length <= 1) {
    proxy.$modal.msgWarning('至少保留一个结果字段')
    return
  }
  resultColumns.value = resultColumns.value.filter(column => column.field !== field)
  resultRecords.value.forEach(row => { delete row[field] })
}

function addRow() {
  const row = {}
  resultColumns.value.forEach(column => { row[column.field] = '' })
  resultRecords.value.push(row)
}

function removeRow(index) {
  resultRecords.value.splice(index, 1)
}

function payload() {
  return {
    resultStatus: resultForm.resultStatus,
    columnSchema: resultColumns.value.map((column, index) => ({ ...column, order: index })),
    data: { records: resultRecords.value },
    resultSummary: resultForm.resultSummary,
    updateReason: resultForm.updateReason
  }
}

function validateEditor(requireReason = false) {
  if (!resultColumns.value.length || resultColumns.value.some(column => !column.label.trim())) {
    proxy.$modal.msgWarning('结果字段名称不能为空')
    return false
  }
  if (requireReason && !resultForm.updateReason.trim()) {
    proxy.$modal.msgWarning('请填写修改说明')
    return false
  }
  return true
}

function saveDraft() {
  if (!validateEditor()) return
  saveDelayedDraft(currentId.value, payload()).then(() => {
    proxy.$modal.msgSuccess('草稿已保存')
    loadDetail(currentId.value)
  })
}

function completeResult() {
  if (!validateEditor()) return
  proxy.$modal.confirm('上传完毕后保险公司即可查看结果，并立即执行计费判断。确认继续？').then(() => completeDelayedQuery(currentId.value, payload())).then(() => {
    proxy.$modal.msgSuccess('结果已上传')
    loadDetail(currentId.value)
    getList()
    refreshBatchDetailIfOpen()
  }).catch(() => {})
}

function saveCorrection() {
  if (!validateEditor(true)) return
  updateDelayedResult(currentId.value, payload()).then(() => {
    proxy.$modal.msgSuccess('结果内容已修改')
    loadDetail(currentId.value)
    getList()
    refreshBatchDetailIfOpen()
  })
}

function processLabel(status) {
  return { PENDING: '待处理', PROCESSING: '处理中', COMPLETED: '已完成', FAILED: '失败', CANCELLED: '已取消' }[status] || '-'
}

function processTag(status) {
  return { PENDING: 'info', PROCESSING: 'warning', COMPLETED: 'success', FAILED: 'danger', CANCELLED: 'info' }[status] || 'info'
}

function resultLabel(status) {
  return { HIT: '查得', NO_RESULT: '未查得', HINT_ONLY: '仅提示', FAILED: '失败', CANCELLED: '已取消' }[status] || '-'
}

function batchLabel(status) {
  return {
    PENDING: '待处理', PROCESSING: '处理中', COMPLETED: '已完成', PARTIAL_FAILED: '部分失败',
    FAILED: '失败', PARTIAL_CANCELLED: '部分取消', CANCELLED: '已取消'
  }[status] || '-'
}

function batchTag(status) {
  return {
    PENDING: 'info', PROCESSING: 'warning', COMPLETED: 'success', PARTIAL_FAILED: 'danger',
    FAILED: 'danger', PARTIAL_CANCELLED: 'warning', CANCELLED: 'info'
  }[status] || 'info'
}

function maskIdCard(value) {
  if (!value || value.length < 8) return value || '-'
  return `${value.slice(0, 3)}${'*'.repeat(value.length - 7)}${value.slice(-4)}`
}

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}

getList()
</script>

<style scoped>
.secondary-text { margin-top: 3px; color: var(--el-text-color-secondary); font-size: 12px; }
.view-tabs { margin-bottom: 14px; }
.batch-detail-content { min-height: 300px; }
.batch-summary { margin-bottom: 18px; }
.detail-content { min-height: 300px; }
.result-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin: 20px 0 16px; }
.toolbar-group { display: flex; align-items: center; gap: 8px; flex: none; }
.result-toolbar :deep(.el-alert) { max-width: 520px; }
.result-form { max-width: 980px; }
.dynamic-table-wrap { width: 100%; overflow: hidden; }
.column-header { display: flex; align-items: center; gap: 4px; }
@media (max-width: 900px) {
  .result-toolbar { align-items: stretch; flex-direction: column; }
  .toolbar-group { flex-wrap: wrap; }
}
</style>
