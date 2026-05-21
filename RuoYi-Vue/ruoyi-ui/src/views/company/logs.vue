<template>
  <div class="app-container">
    <el-card shadow="hover">
      <div slot="header"><span>查询记录</span></div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="查询接口" prop="queryType" min-width="130" />
        <el-table-column label="结果类型" prop="resultStatus" width="110">
          <template slot-scope="{ row }">{{ row.resultStatus === 'NO_RESULT' ? '未查得' : '查得数据' }}</template>
        </el-table-column>
        <el-table-column label="账单月份" prop="billingMonth" width="110" />
        <el-table-column label="请求时间" prop="requestTime" width="170" />
        <el-table-column label="请求IP" prop="requestIp" width="140" />
        <el-table-column label="说明" prop="remark" min-width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { listQueryLogs } from '@/api/business/portal'

export default {
  name: 'CompanyLogs',
  data() {
    return { loading: true, list: [] }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listQueryLogs({}).then(res => {
        this.list = res.data || []
      }).finally(() => {
        this.loading = false
      })
    }
  }
}
</script>
