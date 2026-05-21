<template>
  <div class="app-container">
    <el-card shadow="hover">
      <div slot="header"><span>额度申请记录</span></div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="申请额度" align="center" prop="amount" width="120">
          <template slot-scope="{row}">{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template slot-scope="{row}">
            <el-tag :type="row.status === '1' ? 'success' : row.status === '2' ? 'danger' : 'warning'">
              {{ row.status === '0' ? '待审核' : row.status === '1' ? '已通过' : '已驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" align="center" prop="submitTime" width="170" />
        <el-table-column label="申请说明" align="center" prop="submitRemark" />
        <el-table-column label="审核人" align="center" prop="reviewer" width="100" />
        <el-table-column label="审核时间" align="center" prop="reviewTime" width="170" />
        <el-table-column label="审核备注" align="center" prop="reviewRemark" min-width="150" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { listRecharge } from '@/api/business/portal'

export default {
  name: 'CompanyRechargeList',
  data() {
    return { loading: true, list: [] }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listRecharge().then(res => {
        this.list = res.data || []
      }).finally(() => {
        this.loading = false
      })
    },
    formatMoney(val) {
      return Number(val || 0).toFixed(2)
    }
  }
}
</script>
