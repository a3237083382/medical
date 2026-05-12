<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="操作类型" prop="operationType">
        <el-select v-model="queryParams.operationType" placeholder="操作类型" clearable style="width: 150px">
          <el-option label="充值" value="RECHARGE" />
          <el-option label="扣费结算" value="SETTLEMENT" />
          <el-option label="退款" value="REFUND" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间">
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
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:fee:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="flowList" border stripe>
      <el-table-column label="公司名称" align="center" prop="companyName" min-width="160" />
      <el-table-column label="操作类型" align="center" width="110">
        <template #default="{ row }">
          <el-tag :type="operationTag(row.operationType)">{{ operationLabel(row.operationType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额(元)" align="center" prop="amount" width="120">
        <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="操作前余额" align="center" prop="balanceBefore" width="130">
        <template #default="{ row }">{{ formatMoney(row.balanceBefore) }}</template>
      </el-table-column>
      <el-table-column label="操作后余额" align="center" prop="balanceAfter" width="130">
        <template #default="{ row }">{{ formatMoney(row.balanceAfter) }}</template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="operator" width="110" />
      <el-table-column label="业务ID" align="center" prop="bizId" width="90" />
      <el-table-column label="操作时间" align="center" prop="operationTime" width="170" />
      <el-table-column label="备注" align="center" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)" v-hasPermi="['business:fee:query']">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="扣费流水详情" v-model="detailOpen" width="620px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="公司名称">{{ detail.companyName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ operationLabel(detail.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ formatMoney(detail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="业务ID">{{ detail.bizId }}</el-descriptions-item>
        <el-descriptions-item label="操作前余额">{{ formatMoney(detail.balanceBefore) }}</el-descriptions-item>
        <el-descriptions-item label="操作后余额">{{ formatMoney(detail.balanceAfter) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operator }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detail.operationTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessFeeFlow">
import { listFeeFlow, getFeeFlow } from "@/api/business/feeFlow"
import { addDateRange } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const flowList = ref([])
const dateRange = ref([])
const detailOpen = ref(false)
const detail = ref({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  companyName: undefined,
  operationType: undefined
})

function getList() {
  loading.value = true
  listFeeFlow(addDateRange(queryParams, dateRange.value)).then(res => {
    flowList.value = res.rows
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
  getFeeFlow(row.id).then(res => {
    detail.value = res.data || {}
    detailOpen.value = true
  })
}

function handleExport() {
  proxy.download("business/fee/export", addDateRange(queryParams, dateRange.value), `fee_flow_${new Date().getTime()}.xlsx`)
}

function operationLabel(type) {
  const labels = {
    RECHARGE: "充值",
    SETTLEMENT: "扣费结算",
    REFUND: "退款"
  }
  return labels[type] || type || ""
}

function operationTag(type) {
  if (type === "RECHARGE") return "success"
  if (type === "SETTLEMENT") return "warning"
  if (type === "REFUND") return "info"
  return ""
}

function formatMoney(val) {
  if (!val) return "0.00"
  return Number(val).toFixed(2)
}

getList()
</script>
