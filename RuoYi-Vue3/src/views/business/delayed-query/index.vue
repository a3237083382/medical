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
      <el-form-item label="查询类型" prop="queryType">
        <el-select v-model="queryParams.queryType" placeholder="全部" clearable style="width: 140px">
          <el-option label="医保查询" value="MEDICAL" />
          <el-option label="大数据查询" value="BIG_DATA" />
        </el-select>
      </el-form-item>
      <el-form-item label="查询状态" prop="queryStatus">
        <el-select v-model="queryParams.queryStatus" placeholder="全部" clearable style="width: 130px">
          <el-option label="未查询" value="PENDING" />
          <el-option label="已查询" value="QUERIED" />
          <el-option label="已取消" value="CANCELLED" />
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
        <el-button type="warning" plain icon="Upload" :disabled="!currentId || currentRow.queryStatus === 'CANCELLED'" @click="openProcess('upload')">上传结果</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Check" :disabled="!currentId || currentRow.queryStatus === 'CANCELLED'" @click="openProcess('complete')">上传完毕</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Edit" :disabled="!currentId || currentRow.queryStatus === 'CANCELLED' || currentUploadStatus !== 'UPLOADED'" @click="openProcess('update')">修改结果</el-button>
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
      <el-table-column label="查询类型" align="center" width="110">
        <template #default="{ row }">{{ queryTypeText(row.queryType) }}</template>
      </el-table-column>
      <el-table-column label="查询状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="row.queryStatus === 'QUERIED' ? 'success' : 'warning'">
            {{ queryStatusText(row.queryStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结果上传" align="center" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.queryStatus !== 'CANCELLED'" :type="row.uploadStatus === 'UPLOADED' ? 'success' : 'info'">
            {{ uploadStatusText(row.uploadStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结果状态" align="center" width="110">
        <template #default="{ row }">{{ row.queryStatus === 'CANCELLED' ? '' : resultStatusText(row.resultStatus) }}</template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="submitTime" width="170" />
      <el-table-column label="处理人" align="center" width="110">
        <template #default="{ row }">{{ row.queryStatus === 'CANCELLED' ? '' : row.handlerName }}</template>
      </el-table-column>
      <el-table-column label="处理时间" align="center" width="170">
        <template #default="{ row }">{{ row.queryStatus === 'CANCELLED' ? '' : row.handledTime }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button v-if="row.queryStatus !== 'CANCELLED' && row.uploadStatus !== 'UPLOADED'" link type="warning" icon="Upload" @click="openProcessForRow(row, 'upload')">上传结果</el-button>
          <el-button v-else-if="row.queryStatus !== 'CANCELLED'" link type="info" icon="Edit" @click="openProcessForRow(row, 'update')">修改结果</el-button>
          <el-button v-if="row.queryStatus !== 'CANCELLED' && row.uploadStatus !== 'UPLOADED'" link type="success" icon="Check" @click="openProcessForRow(row, 'complete')">上传完毕</el-button>
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
          <span>查询类型</span>
          <strong>{{ queryTypeText(currentRow.queryType) }}</strong>
        </div>
        <div v-if="currentRow.queryStatus !== 'CANCELLED'">
          <span>请求编号</span>
          <strong>{{ currentRow.requestNo || '-' }}</strong>
        </div>
        <div v-if="currentRow.queryStatus === 'CANCELLED'">
          <span>查询状态</span>
          <strong>已取消</strong>
        </div>
        <div v-if="currentRow.queryStatus === 'CANCELLED'">
          <span>提交时间</span>
          <strong>{{ currentRow.submitTime || '-' }}</strong>
        </div>
      </div>

      <el-form v-if="currentRow.queryStatus !== 'CANCELLED'" :model="form" ref="formRef" label-width="108px" class="result-form">
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

      <div v-if="currentRow.queryStatus !== 'CANCELLED'" class="import-strip">
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

      <el-table v-if="currentRow.queryStatus !== 'CANCELLED'" :data="form.results" border size="small" max-height="420" class="detail-table">
        <el-table-column
          v-for="field in activeDetailFields"
          :key="field.prop"
          :label="field.output"
          :min-width="field.width || 150"
        >
          <template #default="{ row }">
            <el-select
              v-if="field.options"
              v-model="row[field.prop]"
              :disabled="readonly"
              clearable
              placeholder="请选择"
              style="width: 100%"
            >
              <el-option v-for="option in field.options" :key="option" :label="option" :value="option" />
            </el-select>
            <el-input v-else v-model="row[field.prop]" :disabled="readonly" :placeholder="field.output" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row, $index }">
            <el-button link type="primary" icon="DocumentCopy" :disabled="readonly" @click="copyRow(row)">复制</el-button>
            <el-button link type="danger" icon="Delete" :disabled="readonly" @click="removeRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template v-if="currentRow.queryStatus !== 'CANCELLED' && currentRow.queryType !== 'BIG_DATA'">
        <div class="import-strip coverage-strip">
          <div class="import-title">
            <strong>参保信息</strong>
            <span>此表由管理员手动填写；未填写时不会同步到保险公司页面</span>
          </div>
          <el-button type="primary" plain icon="Plus" :disabled="readonly" @click="addCoverageRow">新增一行</el-button>
        </div>

        <el-table :data="form.insuranceCoverage" border size="small" max-height="320" class="detail-table">
          <el-table-column label="序号" type="index" width="70" align="center" />
          <el-table-column v-for="field in coverageFields" :key="field.prop" :label="field.output" :min-width="field.width || 150">
            <template #default="{ row }">
              <el-input v-model="row[field.prop]" :disabled="readonly" :placeholder="field.output" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row, $index }">
              <el-button link type="primary" icon="DocumentCopy" :disabled="readonly" @click="copyCoverageRow(row)">复制</el-button>
              <el-button link type="danger" icon="Delete" :disabled="readonly" @click="removeCoverageRow($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

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
const AUTO_REFRESH_INTERVAL = 5000

let listRequest = null
let autoRefreshTimer = null

const readonly = computed(() => mode.value === "detail")

const medicalDetailFields = [
  { prop: "medicalInstitutionName", output: "定点医药机构名称", width: 190, aliases: ["定点医药机构名称", "医院名称", "医院", "hospital", "hospitalName"] },
  { prop: "visitTime", output: "就诊时间", width: 190, aliases: ["就诊时间", "就诊日期", "visitTime", "visitDate"] },
  { prop: "visitType", output: "就诊类型", width: 120, aliases: ["就诊类型", "就诊类别", "visitType"] },
  { prop: "diagnosisResult", output: "诊断结果", width: 170, aliases: ["诊断结果", "病种名称", "诊断", "diagnosis", "diagnosisResult"] },
  { prop: "reimbursed", output: "是否报销", width: 120, options: ["是", "否"], aliases: ["是否报销", "报销", "reimbursed"] },
  { prop: "endTime", output: "结束时间", width: 190, aliases: ["结束时间", "endTime"] }
]

const bigDataDetailFields = [
  { prop: "patientName", output: "姓名", width: 120, aliases: ["姓名", "人员姓名", "patientName", "name"] },
  { prop: "gender", output: "性别", width: 100, aliases: ["性别", "gender"] },
  { prop: "idCard", output: "身份证号码", width: 200, aliases: ["身份证号码", "身份证号", "证件号码", "idCard"] },
  { prop: "hospitalName", output: "就诊医院", width: 180, aliases: ["就诊医院", "医院名称", "hospitalName"] },
  { prop: "visitDate", output: "日期", width: 150, aliases: ["日期", "就诊日期", "visitDate"] },
  { prop: "visitCategory", output: "门诊/住院/体检", width: 150, aliases: ["门诊/住院/体检", "就诊类型", "visitCategory"] },
  { prop: "medicalAdvice", output: "医嘱", width: 180, aliases: ["医嘱", "medicalAdvice"] },
  { prop: "diagnosis", output: "诊断", width: 180, aliases: ["诊断", "诊断结果", "diagnosis"] }
]

const coverageFields = [
  { prop: "medicalArea", output: "医保区划", width: 140 },
  { prop: "companyName", output: "单位名称", width: 180 },
  { prop: "personType", output: "人员类型", width: 130 },
  { prop: "coverageStatus", output: "参保状态", width: 130 },
  { prop: "insuranceType", output: "险种类型", width: 140 },
  { prop: "currentCoverageDate", output: "本次参保日期", width: 160 },
  { prop: "suspensionDate", output: "暂停参保日期", width: 160 },
  { prop: "firstCoverageMonth", output: "首次参保年月", width: 150 }
]

const activeDetailFields = computed(() => currentRow.queryType === "BIG_DATA" ? bigDataDetailFields : medicalDetailFields)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  companyNameSnapshot: undefined,
  patientName: undefined,
  idCard: undefined,
  queryType: undefined,
  queryStatus: undefined,
  uploadStatus: undefined
})

const form = reactive({
  resultStatus: "HIT",
  resultMessage: undefined,
  modifyReason: undefined,
  results: [],
  insuranceCoverage: []
})

function getList(options = {}) {
  if (listRequest) {
    return options && options.silent === true
      ? listRequest
      : listRequest.then(() => getList(options))
  }

  const silent = options && options.silent === true
  if (!silent) loading.value = true
  listRequest = listDelayedQuery(addDateRange(queryParams, dateRange.value)).then(res => {
    requestList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => {
    if (!silent) loading.value = false
    listRequest = null
  })
  return listRequest
}

function startAutoRefresh() {
  stopAutoRefresh()
  autoRefreshTimer = window.setTimeout(async () => {
    if (document.visibilityState === "visible") {
      await getList({ silent: true })
    }
    startAutoRefresh()
  }, AUTO_REFRESH_INTERVAL)
}

function stopAutoRefresh() {
  if (autoRefreshTimer !== null) {
    window.clearTimeout(autoRefreshTimer)
    autoRefreshTimer = null
  }
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
  if (currentRow.queryStatus === "CANCELLED") {
    proxy.$modal.msgWarning("已取消的申请不能处理")
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
    form.insuranceCoverage = normalizeCoverage(currentRow.results || [])
    dialogOpen.value = true
  })
}

function resetEditor() {
  form.resultStatus = "HIT"
  form.resultMessage = undefined
  form.modifyReason = undefined
  form.results = []
  form.insuranceCoverage = []
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

function addCoverageRow() {
  form.insuranceCoverage.push(emptyCoverageRow())
}

function copyCoverageRow(row) {
  form.insuranceCoverage.push({ ...row })
}

function removeCoverageRow(index) {
  form.insuranceCoverage.splice(index, 1)
}

function emptyCoverageRow() {
  return coverageFields.reduce((row, field) => {
    row[field.prop] = ""
    return row
  }, {})
}

function emptyDetailRow() {
  return activeDetailFields.value.reduce((row, field) => {
    row[field.prop] = ""
    return row
  }, {})
}

function normalizeResults(rows) {
  return (rows || [])
    .filter(item => parseJsonObject(item.rawJson || JSON.stringify(item)).__recordType !== "INSURANCE_COVERAGE")
    .map(item => rawJsonToRow(item.rawJson || JSON.stringify(item)))
}

function normalizeCoverage(rows) {
  return (rows || []).map(item => parseJsonObject(item.rawJson || JSON.stringify(item)))
    .filter(item => item.__recordType === "INSURANCE_COVERAGE")
    .map(item => coverageFields.reduce((row, field) => {
      const value = item[field.output]
      row[field.prop] = value === undefined || value === null ? "" : String(value)
      return row
    }, {}))
}

function rawJsonToRow(rawJson) {
  const row = emptyDetailRow()
  const raw = parseJsonObject(rawJson)
  activeDetailFields.value.forEach(field => {
    const key = field.aliases.find(alias => raw[alias] !== undefined && raw[alias] !== null)
    if (key) {
      row[field.prop] = String(raw[key])
    }
  })
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

function buildPayload() {
  return {
    resultStatus: form.resultStatus || currentRow.resultStatus || "HIT",
    resultMessage: form.resultMessage,
    modifyReason: form.modifyReason,
    results: form.results.map(rowToPayload).filter(Boolean),
    insuranceCoverage: currentRow.queryType === "BIG_DATA"
      ? []
      : form.insuranceCoverage.map(coverageRowToPayload).filter(Boolean)
  }
}

function coverageRowToPayload(row) {
  const data = {}
  coverageFields.forEach(field => {
    const value = row[field.prop]
    data[field.output] = value === undefined || value === null ? "" : String(value).trim()
  })
  return Object.values(data).some(Boolean) ? data : null
}

function rowToPayload(row) {
  const data = {}
  activeDetailFields.value.forEach(field => {
    const value = row[field.prop]
    data[field.output] = value === undefined || value === null ? "" : value
  })
  if (!Object.values(data).some(value => value !== "")) {
    return null
  }
  return { rawJson: JSON.stringify(data) }
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
    form.insuranceCoverage = normalizeCoverage(currentRow.results || [])
    getList()
  })
  return false
}

function queryStatusText(status) {
  if (status === "QUERIED") return "已查询"
  if (status === "PENDING") return "未查询"
  if (status === "CANCELLED") return "已取消"
  return status || "-"
}

function queryTypeText(type) {
  if (type === "BIG_DATA") return "大数据查询"
  if (type === "MEDICAL" || type === "delayed_precise" || type === "precision_delayed") return "医保查询"
  return type || "-"
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

onMounted(() => {
  getList()
  startAutoRefresh()
})

onActivated(startAutoRefresh)
onDeactivated(stopAutoRefresh)
onBeforeUnmount(stopAutoRefresh)
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

.coverage-strip {
  margin-top: 18px;
  border-top: 1px solid #edf2f7;
}
</style>
