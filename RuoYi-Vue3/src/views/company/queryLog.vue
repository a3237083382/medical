<template>
  <div class="record-page">
    <section class="table-card">
      <div class="table-title">
        <div>
          <span>QUERY AUDIT</span>
          <h2>查询记录</h2>
        </div>
      </div>
      <el-form class="filter-form" :model="queryParams" :inline="true" label-width="80px">
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
    </section>
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
