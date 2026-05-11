<template>
  <div class="app-container">
    <el-card shadow="hover">
      <template #header><span>充值记录</span></template>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="金额" align="center" prop="amount" width="120">
          <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status==='1'?'success':row.status==='2'?'danger':'warning'">
              {{ row.status==='0'?'待审核':row.status==='1'?'已通过':'已驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" align="center" prop="submitTime" width="170" />
        <el-table-column label="备注" align="center" prop="submitRemark" />
        <el-table-column label="审核人" align="center" prop="reviewer" width="100" />
        <el-table-column label="审核时间" align="center" prop="reviewTime" width="170" />
        <el-table-column label="审核备注" align="center" prop="reviewRemark" min-width="150" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="CompanyRechargeList">
import { listRechargeRecords } from "@/api/business/portal"

const loading = ref(true)
const list = ref([])

function getList() {
  loading.value = true
  listRechargeRecords().then(res => {
    list.value = res.data || []
  }).finally(() => { loading.value = false })
}

function formatMoney(val) {
  if (!val) return "0.00"
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",")
}

getList()
</script>
