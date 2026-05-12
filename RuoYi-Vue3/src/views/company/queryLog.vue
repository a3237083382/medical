<template>
  <div class="app-container">
    <el-card shadow="hover">
      <template #header><span>查询记录</span></template>
      <el-form :model="queryParams" :inline="true" label-width="80px">
        <el-form-item label="查询类型">
          <el-input v-model="queryParams.queryType" placeholder="请输入查询类型" clearable style="width: 200px" @keyup.enter="getList" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" value="0" />
            <el-option label="失败" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="查询类型" align="center" prop="queryType" width="140" />
        <el-table-column label="查询参数" align="center" prop="queryParams" min-width="220" show-overflow-tooltip />
        <el-table-column label="费用" align="center" prop="fee" width="120">
          <template #default="{ row }">{{ formatMoney(row.fee) }}</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">
              {{ row.status === '0' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求时间" align="center" prop="requestTime" width="170" />
        <el-table-column label="请求IP" align="center" prop="requestIp" width="140" />
        <el-table-column label="备注" align="center" prop="remark" min-width="150" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="CompanyQueryLog">
import { listQueryLogs } from "@/api/business/portal"

const loading = ref(false)
const list = ref([])
const queryParams = reactive({
  queryType: undefined,
  status: undefined
})

function getList() {
  loading.value = true
  listQueryLogs(queryParams).then(res => {
    list.value = res.data || []
  }).finally(() => { loading.value = false })
}

function resetQuery() {
  queryParams.queryType = undefined
  queryParams.status = undefined
  getList()
}

function formatMoney(val) {
  if (!val) return "0.00"
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",")
}

getList()
</script>
