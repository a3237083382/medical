<template>
  <div class="app-container monthly-bill-page">
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <span>月度对账</span>
        <el-input v-model="query.billingMonth" size="small" clearable placeholder="yyyy-MM" style="width: 140px" @keyup.enter.native="getList">
          <el-button slot="append" icon="el-icon-search" @click="getList" />
        </el-input>
      </div>
      <el-table :data="billList" v-loading="loading" border stripe empty-text="暂无月度账单">
        <el-table-column label="账单月份" prop="billingMonth" width="120" />
        <el-table-column label="查询次数" prop="queryCount" width="100" />
        <el-table-column label="查得次数" prop="hitCount" width="100" />
        <el-table-column label="未查得次数" prop="noResultCount" width="110" />
        <el-table-column label="对账金额" prop="totalAmount" width="120">
          <template slot-scope="{ row }">{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="生成时间" prop="generatedTime" width="170" />
        <el-table-column label="操作" width="90" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" size="mini" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="对账明细" :visible.sync="detailOpen" width="860px" append-to-body>
      <el-descriptions :column="3" border v-if="detail">
        <el-descriptions-item label="账单月份">{{ detail.billingMonth }}</el-descriptions-item>
        <el-descriptions-item label="总查询次数">{{ detail.queryCount }}</el-descriptions-item>
        <el-descriptions-item label="对账金额">{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="interfaceRows" border stripe style="margin-top: 16px">
        <el-table-column label="接口分类" prop="queryName" min-width="170" />
        <el-table-column label="接口编码" prop="queryType" min-width="130" />
        <el-table-column label="查得次数" prop="hitCount" width="100" />
        <el-table-column label="未查得次数" prop="noResultCount" width="110" />
        <el-table-column label="合计次数" prop="totalCount" width="100" />
        <el-table-column label="金额" prop="totalAmount" width="110">
          <template slot-scope="{ row }">{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { listMonthlyBill, getMonthlyBill } from '@/api/business/portal'

export default {
  name: 'CompanyMonthlyBill',
  data() {
    return {
      loading: true,
      query: { billingMonth: this.currentMonth() },
      billList: [],
      detailOpen: false,
      detail: null,
      detailRows: []
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listMonthlyBill(this.query).then(res => {
        this.billList = res.data || []
      }).finally(() => {
        this.loading = false
      })
    },
    showDetail(row) {
      getMonthlyBill(row.id).then(res => {
        const data = res.data || {}
        this.detail = data.bill || null
        this.detailRows = data.details || []
        this.detailOpen = true
      })
    },
    currentMonth() {
      const date = new Date()
      return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0')
    },
    formatMoney(value) {
      return Number(value || 0).toFixed(2)
    }
  },
  computed: {
    interfaceRows() {
      const map = {}
      this.detailRows.forEach(row => {
        const key = row.queryType || row.queryName
        if (!map[key]) {
          map[key] = {
            queryType: row.queryType,
            queryName: row.queryName,
            hitCount: 0,
            noResultCount: 0,
            totalCount: 0,
            totalAmount: 0
          }
        }
        const count = Number(row.queryCount || 0)
        if (row.resultStatus === 'HIT') map[key].hitCount += count
        if (row.resultStatus === 'NO_RESULT') map[key].noResultCount += count
        map[key].totalCount += count
        map[key].totalAmount += Number(row.totalAmount || 0)
      })
      return Object.values(map)
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
