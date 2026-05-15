<template>
  <div class="record-page">
    <section class="table-card">
      <div class="table-title">
        <div>
          <span>FEE FLOW</span>
          <h2>费用流水</h2>
        </div>
      </div>
      <el-form class="filter-form" :model="queryParams" :inline="true" label-width="80px">
        <el-form-item label="类型">
          <el-select v-model="queryParams.operationType" placeholder="全部" clearable style="width: 140px">
            <el-option label="充值" value="RECHARGE" />
            <el-option label="扣费" value="DEDUCT" />
            <el-option label="周期扣费" value="SETTLEMENT" />
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
    </section>
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
    SETTLEMENT: "周期扣费",
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

<style scoped>
.record-page {
  display: grid;
  gap: 18px;
}

.table-card {
  padding: 20px;
  border: 1px solid rgba(16, 32, 47, 0.08);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 10px 28px rgba(12, 36, 48, 0.06);
}

.table-title {
  margin-bottom: 16px;
}

.table-title span {
  color: #0f766e;
  font-size: 12px;
  font-weight: 800;
}

.table-title h2 {
  margin: 8px 0 0;
  color: #10202f;
  font-size: 24px;
}

.filter-form {
  padding: 14px 14px 2px;
  margin-bottom: 14px;
  border-radius: 8px;
  background: #f4f8f8;
}

:deep(.el-table) {
  border-radius: 8px;
}
</style>
