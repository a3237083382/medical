<template>
  <div class="app-container delayed-query-page">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
      label-width="82px"
      class="query-panel"
    >
      <el-form-item label="身份证号" prop="idCard">
        <el-input
          v-model="queryParams.idCard"
          placeholder="请输入身份证号"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="姓名" prop="patientName">
        <el-input
          v-model="queryParams.patientName"
          placeholder="请输入姓名"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="公司名称" prop="companyNameSnapshot">
        <el-input
          v-model="queryParams.companyNameSnapshot"
          placeholder="请输入公司名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="查询状态" prop="queryStatus">
        <el-select v-model="queryParams.queryStatus" placeholder="全部" clearable style="width: 130px">
          <el-option label="未查询" value="PENDING" />
          <el-option label="已查询" value="QUERIED" />
        </el-select>
      </el-form-item>
      <el-form-item label="上传状态" prop="uploadStatus">
        <el-select v-model="queryParams.uploadStatus" placeholder="全部" clearable style="width: 130px">
          <el-option label="未上传" value="NOT_UPLOADED" />
          <el-option label="已上传" value="UPLOADED" />
        </el-select>
      </el-form-item>
      <el-form-item label="提交时间">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 260px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 action-row">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Upload" :disabled="!currentId" @click="openProcess('upload')">上传结果</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Check" :disabled="!currentId" @click="openProcess('complete')">上传完毕</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Edit" :disabled="!currentId || currentUploadStatus !== 'UPLOADED'" @click="openProcess('update')">修改结果</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      v-loading="loading"
      :data="requestList"
      border
      stripe
      highlight-current-row
      @current-change="handleCurrentChange"
    >
      <el-table-column label="公司名称" align="center" prop="companyNameSnapshot" min-width="160" />
      <el-table-column label="姓名" align="center" prop="patientName" width="120" />
      <el-table-column label="身份证号" align="center" prop="idCard" width="210" show-overflow-tooltip />
      <el-table-column label="查询状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="row.queryStatus === 'QUERIED' ? 'success' : 'warning'">
            {{ queryStatusText(row.queryStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结果上传" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="row.uploadStatus === 'UPLOADED' ? 'success' : 'info'">
            {{ uploadStatusText(row.uploadStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结果状态" align="center" width="110">
        <template #default="{ row }">{{ resultStatusText(row.resultStatus) }}</template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="submitTime" width="170" />
      <el-table-column label="处理人" align="center" prop="handlerName" width="110" />
      <el-table-column label="处理时间" align="center" prop="handledTime" width="170" />
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button v-if="row.uploadStatus !== 'UPLOADED'" link type="warning" icon="Upload" @click="openProcessForRow(row, 'upload')">上传结果</el-button>
          <el-button v-else link type="info" icon="Edit" @click="openProcessForRow(row, 'update')">修改结果</el-button>
          <el-button v-if="row.uploadStatus !== 'UPLOADED'" link type="success" icon="Check" @click="openProcessForRow(row, 'complete')">上传完毕</el-button>
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

    <el-dialog :title="dialogTitle" v-model="dialogOpen" width="1280px" append-to-body destroy-on-close class="delayed-editor-dialog">
      <div class="patient-summary">
        <div>
          <span>保险公司</span>
          <strong>{{ currentRow.companyNameSnapshot || '-' }}</strong>
        </div>
        <div>
          <span>姓名</span>
          <strong>{{ currentRow.patientName || '-' }}</strong>
        </div>
        <div>
          <span>身份证号</span>
          <strong>{{ currentRow.idCard || '-' }}</strong>
        </div>
        <div>
          <span>请求编号</span>
          <strong>{{ currentRow.requestNo || '-' }}</strong>
        </div>
      </div>

      <el-form :model="form" ref="formRef" label-width="108px" class="result-form">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="查询状态">
              <el-input :model-value="queryStatusText(currentRow.queryStatus)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="上传状态">
              <el-input :model-value="uploadStatusText(currentRow.uploadStatus)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="结果状态">
              <el-select v-model="form.resultStatus" :disabled="readonly" placeholder="请选择结果状态" style="width: 100%">
                <el-option label="查得" value="HIT" />
                <el-option label="未查得" value="NO_RESULT" />
                <el-option label="有提示无明细" value="PARTIAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="本次费用">
              <el-input :model-value="formatMoney(currentRow.fee)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结果说明">
              <el-input
                v-model="form.resultMessage"
                :disabled="readonly"
                type="textarea"
                :rows="2"
                placeholder="未查得、部分结果或需要提示保险公司时填写"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="mode === 'update'">
            <el-form-item label="修改说明">
              <el-input v-model="form.modifyReason" type="textarea" :rows="2" placeholder="请输入修改原因" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="import-strip">
        <div class="import-title">
          <strong>结果明细</strong>
          <span>Excel 导入和手动新增的每一行都可以直接修改</span>
        </div>
        <el-space>
          <el-upload :auto-upload="false" :show-file-list="false" :on-change="handleExcelChange" :disabled="readonly">
            <el-button type="warning" icon="FolderOpened" :disabled="readonly">导入Excel文件内容</el-button>
          </el-upload>
          <el-button type="primary" plain icon="Plus" :disabled="readonly" @click="addRow">新增一行</el-button>
        </el-space>
      </div>

      <el-table :data="form.results" border size="small" max-height="420" class="detail-table">
        <el-table-column label="医院名称" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.hospitalName" :disabled="readonly" placeholder="医院名称" />
          </template>
        </el-table-column>
        <el-table-column label="就诊时间" min-width="190">
          <template #default="{ row }">
            <el-input v-model="row.visitTime" :disabled="readonly" placeholder="就诊时间" />
          </template>
        </el-table-column>
        <el-table-column label="就诊类型" min-width="120">
          <template #default="{ row }">
            <el-input v-model="row.visitType" :disabled="readonly" placeholder="门诊/住院" />
          </template>
        </el-table-column>
        <el-table-column label="医嘱" min-width="130">
          <template #default="{ row }">
            <el-input v-model="row.medicalAdvice" :disabled="readonly" placeholder="医嘱" />
          </template>
        </el-table-column>
        <el-table-column label="诊断结果" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.diagnosisResult" :disabled="readonly" placeholder="诊断结果" />
          </template>
        </el-table-column>
        <el-table-column label="是否报销" width="110">
          <template #default="{ row }">
            <el-switch
              v-model="row.reimbursed"
              :disabled="readonly"
              active-text="是"
              inactive-text="否"
              inline-prompt
            />
          </template>
        </el-table-column>
        <el-table-column label="医保区划" min-width="130">
          <template #default="{ row }">
            <el-input v-model="row.medicalArea" :disabled="readonly" placeholder="医保区划" />
          </template>
        </el-table-column>
        <el-table-column label="险种类型" min-width="130">
          <template #default="{ row }">
            <el-input v-model="row.insuranceType" :disabled="readonly" placeholder="险种类型" />
          </template>
        </el-table-column>
        <el-table-column label="人员类型" min-width="130">
          <template #default="{ row }">
            <el-input v-model="row.personType" :disabled="readonly" placeholder="人员类型" />
          </template>
        </el-table-column>
        <el-table-column label="本次参保日期" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.currentInsuranceDate" :disabled="readonly" placeholder="本次参保日期" />
          </template>
        </el-table-column>
        <el-table-column label="暂停参保日期" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.suspendedInsuranceDate" :disabled="readonly" placeholder="暂停参保日期" />
          </template>
        </el-table-column>
        <el-table-column label="首次参保年月" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.firstInsuranceMonth" :disabled="readonly" placeholder="首次参保年月" />
          </template>
        </el-table-column>
        <el-table-column label="医保参保单位" min-width="160">
          <template #default="{ row }">
            <el-input v-model="row.insuranceUnit" :disabled="readonly" placeholder="医保参保单位" />
          </template>
        </el-table-column>
        <el-table-column label="其他信息" min-width="180">
          <template #default="{ row }">
            <el-input v-model="row.extraText" :disabled="readonly" type="textarea" :rows="1" placeholder="其他字段 JSON" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row, $index }">
            <el-button link type="primary" icon="DocumentCopy" :disabled="readonly" @click="copyRow(row)">复制</el-button>
            <el-button link type="danger" icon="Delete" :disabled="readonly" @click="removeRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="dialogOpen = false">返回列表</el-button>
        <el-button v-if="!readonly && mode !== 'update'" type="primary" icon="DocumentChecked" @click="handleSave">确定保存</el-button>
        <el-button v-if="!readonly && mode !== 'update'" type="success" icon="Finished" @click="handleComplete">上传完毕</el-button>
        <el-button v-if="mode === 'update'" type="warning" icon="Edit" @click="handleUpdateResult">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessDelayedQuery">
import {
  completeDelayedQuery,
  getDelayedQuery,
  importDelayedQueryExcel,
  listDelayedQuery,
  saveDelayedQuery,
  updateDelayedQueryResult
} from "@/api/business/delayedQuery"
import { addDateRange } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const requestList = ref([])
const dateRange = ref([])
const dialogOpen = ref(false)
const dialogTitle = ref("结果处理")
const mode = ref("save")
const currentId = ref(null)
const currentUploadStatus = ref("")
const currentRow = reactive({})

const readonly = computed(() => mode.value === "detail")

const detailFields = [
  { prop: "hospitalName", output: "医院名称", aliases: ["医院名称", "医院", "医院名", "hospital", "hospitalName"] },
  { prop: "visitTime", output: "就诊时间", aliases: ["就诊时间", "就诊日期", "visitTime", "visitDate"] },
  { prop: "visitType", output: "就诊类型", aliases: ["就诊类型", "就诊类别", "visitType"] },
  { prop: "medicalAdvice", output: "医嘱", aliases: ["医嘱", "医生", "医师", "doctor", "doctorName", "medicalAdvice"] },
  { prop: "diagnosisResult", output: "诊断结果", aliases: ["诊断结果", "诊断", "diagnosis", "diagnosisResult"] },
  { prop: "reimbursed", output: "是否报销", aliases: ["是否报销", "报销", "reimbursed"] },
  { prop: "medicalArea", output: "医保区划", aliases: ["医保区划", "医保区", "medicalArea"] },
  { prop: "insuranceType", output: "险种类型", aliases: ["险种类型", "险种", "insuranceType"] },
  { prop: "personType", output: "人员类型", aliases: ["人员类型", "personType"] },
  { prop: "personalInsuranceDate", output: "个人参保日期", aliases: ["个人参保日期", "personalInsuranceDate"] },
  { prop: "currentInsuranceDate", output: "本次参保日期", aliases: ["本次参保日期", "currentInsuranceDate"] },
  { prop: "suspendedInsuranceDate", output: "暂停参保日期", aliases: ["暂停参保日期", "suspendedInsuranceDate"] },
  { prop: "firstInsuranceMonth", output: "首次参保年月", aliases: ["首次参保年月", "首次参保日期", "firstInsuranceMonth"] },
  { prop: "insuranceUnit", output: "医保参保单位", aliases: ["医保参保单位", "参保单位", "insuranceUnit"] }
]

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  companyNameSnapshot: undefined,
  patientName: undefined,
  idCard: undefined,
  queryStatus: undefined,
  uploadStatus: undefined
})

const form = reactive({
  resultStatus: "HIT",
  resultMessage: undefined,
  modifyReason: undefined,
  results: []
})

function getList() {
  loading.value = true
  listDelayedQuery(addDateRange(queryParams, dateRange.value)).then(res => {
    requestList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => {
    loading.value = false
  })
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

function handleCurrentChange(row) {
  if (!row) return
  currentId.value = row.id
  currentUploadStatus.value = row.uploadStatus || ""
  Object.assign(currentRow, row)
}

function handleDetail(row) {
  handleCurrentChange(row)
  openEditor("detail")
}

function openProcess(type) {
  if (!currentId.value) {
    proxy.$modal.msgWarning("请先选择一条记录")
    return
  }
  openEditor(type)
}

function openProcessForRow(row, type) {
  handleCurrentChange(row)
  openProcess(type)
}

function openEditor(type) {
  dialogTitle.value = type === "detail" ? "精准延时查询详情" : type === "update" ? "修改精准延时查询结果" : "精准延时查询结果上传"
  mode.value = type
  getDelayedQuery(currentId.value).then(res => {
    Object.assign(currentRow, res.data || {})
    currentUploadStatus.value = currentRow.uploadStatus || ""
    resetEditor()
    form.resultStatus = currentRow.resultStatus || "HIT"
    form.resultMessage = currentRow.resultMessage
    form.results = normalizeResults(currentRow.results || [])
    dialogOpen.value = true
  })
}

function resetEditor() {
  form.resultStatus = "HIT"
  form.resultMessage = undefined
  form.modifyReason = undefined
  form.results = []
}

function addRow() {
  form.results.push(emptyDetailRow())
}

function copyRow(row) {
  form.results.push({ ...row })
}

function removeRow(index) {
  form.results.splice(index, 1)
}

function emptyDetailRow() {
  return detailFields.reduce((row, field) => {
    row[field.prop] = field.prop === "reimbursed" ? false : ""
    return row
  }, { extraText: "" })
}

function normalizeResults(rows) {
  return (rows || []).map(item => rawJsonToRow(item.rawJson || JSON.stringify(item)))
}

function rawJsonToRow(rawJson) {
  const row = emptyDetailRow()
  const raw = parseJsonObject(rawJson)
  const usedKeys = new Set()
  detailFields.forEach(field => {
    const key = field.aliases.find(alias => raw[alias] !== undefined && raw[alias] !== null)
    if (key) {
      row[field.prop] = field.prop === "reimbursed" ? normalizeBoolean(raw[key]) : String(raw[key])
      usedKeys.add(key)
    }
  })
  const extra = {}
  Object.keys(raw).forEach(key => {
    if (!usedKeys.has(key)) {
      extra[key] = raw[key]
    }
  })
  row.extraText = Object.keys(extra).length ? JSON.stringify(extra) : ""
  return row
}

function parseJsonObject(rawJson) {
  if (!rawJson) return {}
  if (typeof rawJson === "object") return rawJson
  try {
    const parsed = JSON.parse(rawJson)
    return parsed && typeof parsed === "object" && !Array.isArray(parsed) ? parsed : {}
  } catch (e) {
    return { 原始内容: String(rawJson) }
  }
}

function normalizeBoolean(value) {
  return value === true || value === "true" || value === "1" || value === "是" || value === "已报销"
}

function buildPayload() {
  return {
    resultStatus: form.resultStatus || currentRow.resultStatus || "HIT",
    resultMessage: form.resultMessage,
    modifyReason: form.modifyReason,
    results: form.results.map(rowToPayload).filter(Boolean)
  }
}

function rowToPayload(row) {
  const data = {}
  detailFields.forEach(field => {
    const value = row[field.prop]
    if (value !== undefined && value !== null && value !== "") {
      data[field.output] = field.prop === "reimbursed" ? (value ? "是" : "否") : value
    }
  })
  Object.assign(data, parseExtra(row.extraText))
  if (Object.keys(data).length === 0) {
    return null
  }
  return { rawJson: JSON.stringify(data) }
}

function parseExtra(extraText) {
  if (!extraText || !extraText.trim()) return {}
  try {
    const parsed = JSON.parse(extraText)
    return parsed && typeof parsed === "object" && !Array.isArray(parsed) ? parsed : { 其他信息: extraText }
  } catch (e) {
    return { 其他信息: extraText }
  }
}

function validateFinalPayload(payload) {
  if (payload.resultStatus === "HIT" && payload.results.length === 0) {
    proxy.$modal.msgWarning("查得结果请至少填写一条明细")
    return false
  }
  if (payload.resultStatus !== "HIT" && !payload.resultMessage) {
    proxy.$modal.msgWarning("未查得或有提示无明细时请填写结果说明")
    return false
  }
  return true
}

function handleSave() {
  if (!currentId.value) {
    proxy.$modal.msgWarning("请先在列表中选择一条请求")
    return
  }
  saveDelayedQuery(currentId.value, buildPayload()).then(() => {
    proxy.$modal.msgSuccess("草稿已保存")
    dialogOpen.value = false
    getList()
  })
}

function handleComplete() {
  if (!currentId.value) {
    proxy.$modal.msgWarning("请先在列表中选择一条请求")
    return
  }
  const payload = buildPayload()
  if (!validateFinalPayload(payload)) return
  completeDelayedQuery(currentId.value, payload).then(() => {
    proxy.$modal.msgSuccess("已完成上传")
    dialogOpen.value = false
    getList()
  })
}

function handleUpdateResult() {
  if (!currentId.value) {
    proxy.$modal.msgWarning("请先在列表中选择一条请求")
    return
  }
  const payload = buildPayload()
  if (!validateFinalPayload(payload)) return
  updateDelayedQueryResult(currentId.value, payload).then(() => {
    proxy.$modal.msgSuccess("结果已更新")
    dialogOpen.value = false
    getList()
  })
}

function handleExcelChange(file) {
  if (!currentId.value) {
    proxy.$modal.msgWarning("请先选择一条请求")
    return false
  }
  importDelayedQueryExcel(currentId.value, file.raw).then(res => {
    proxy.$modal.msgSuccess("Excel 已导入，可继续修改明细")
    Object.assign(currentRow, res.data || currentRow)
    form.resultStatus = currentRow.resultStatus || form.resultStatus || "HIT"
    form.resultMessage = currentRow.resultMessage
    form.results = normalizeResults(currentRow.results || [])
    getList()
  })
  return false
}

function queryStatusText(status) {
  if (status === "QUERIED") return "已查询"
  if (status === "PENDING") return "未查询"
  return status || "-"
}

function uploadStatusText(status) {
  if (status === "UPLOADED") return "已上传"
  if (status === "NOT_UPLOADED") return "未上传"
  return status || "-"
}

function resultStatusText(status) {
  if (status === "HIT") return "查得"
  if (status === "NO_RESULT") return "未查得"
  if (status === "PARTIAL") return "有提示无明细"
  return status || "-"
}

function formatMoney(val) {
  if (val === null || val === undefined || val === "") return "0.00"
  return Number(val).toFixed(2)
}

getList()
</script>

<style scoped>
.delayed-query-page :deep(.el-form-item) {
  margin-right: 24px;
}

.query-panel {
  padding: 6px 0 2px;
}

.action-row {
  align-items: center;
}

.patient-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.patient-summary > div {
  min-height: 62px;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
}

.patient-summary span {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
}

.patient-summary strong {
  color: #1f2937;
  font-size: 14px;
  line-height: 1.4;
  word-break: break-all;
}

.result-form {
  padding: 14px 14px 0;
  margin-bottom: 14px;
  border: 1px solid #edf2f7;
  border-radius: 6px;
  background: #fff;
}

.import-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
}

.import-title strong {
  margin-right: 10px;
  color: #1f2937;
}

.import-title span {
  color: #6b7280;
  font-size: 13px;
}

.detail-table :deep(.el-table__header th) {
  color: #fff;
  background-color: #2fbfb2 !important;
}

.detail-table :deep(.el-input__wrapper),
.detail-table :deep(.el-textarea__inner) {
  box-shadow: none;
  border: 1px solid #dcdfe6;
}

.detail-table :deep(.el-table__cell) {
  padding: 7px 0;
}
</style>
