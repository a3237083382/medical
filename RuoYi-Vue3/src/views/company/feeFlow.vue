<template>
  <div class="app-container">
    <el-card shadow="hover">
      <template #header><span>费用流水</span></template>
      <el-form :model="queryParams" :inline="true" label-width="80px">
        <el-form-item label="类型">
          <el-select v-model="queryParams.operationType" placeholder="全部" clearable style="width: 140px">
            <el-option label="充值" value="RECHARGE" />
            <el-option label="扣费" value="DEDUCT" />
            <el-option label="退款" value="REFUND" />
            <el-option label="冲正" value="ADJUST" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="类型" align="center" width="110">
          <template #default="{ row }">
            <el-tag :type="operationTag(row.operationType)">{{ operationLabel(row.operationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" align="center" prop="amount" width="120">
          <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="操作前余额" align="center" prop="balanceBefore" width="140">
          <template #default="{ row }">{{ formatMoney(row.balanceBefore) }}</template>
        </el-table-column>
        <el-table-column label="操作后余额" align="center" prop="balanceAfter" width="140">
          <template #default="{ row }">{{ formatMoney(row.balanceAfter) }}</template>
        </el-table-column>
        <el-table-column label="操作人" align="center" prop="operator" width="120" />
        <el-table-column label="操作时间" align="center" prop="operationTime" width="170" />
        <el-table-column label="备注" align="center" prop="remark" min-width="180" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="CompanyFeeFlow">
import { listFeeFlows } from "@/api/business/portal"

const loading = ref(false)
const list = ref([])
const queryParams = reactive({
  operationType: undefined
})

function getList() {
  loading.value = true
  listFeeFlows(queryParams).then(res => {
    list.value = res.data || []
  }).finally(() => { loading.value = false })
}

function resetQuery() {
  queryParams.operationType = undefined
  getList()
}

function operationLabel(type) {
  const labels = {
    RECHARGE: "充值",
    DEDUCT: "扣费",
    SETTLEMENT: "扣费",
    REFUND: "退款",
    ADJUST: "冲正"
  }
  return labels[type] || type || ""
}

function operationTag(type) {
  if (type === "RECHARGE") return "success"
  if (type === "DEDUCT" || type === "SETTLEMENT") return "warning"
  if (type === "REFUND" || type === "ADJUST") return "info"
  return ""
}

function formatMoney(val) {
  if (!val) return "0.00"
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",")
}

getList()
</script>
