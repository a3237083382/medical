<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width: 220px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="账单月份" prop="billingMonth">
        <el-input v-model="queryParams.billingMonth" placeholder="yyyy-MM" clearable style="width: 140px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button type="success" icon="el-icon-document-checked" size="mini" @click="handleGenerate">生成账单</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="billList" border stripe empty-text="暂无月度账单">
      <el-table-column label="公司" prop="companyName" min-width="180" />
      <el-table-column label="账单月份" prop="billingMonth" width="110" />
      <el-table-column label="查询次数" prop="queryCount" width="100" />
      <el-table-column label="查得次数" prop="hitCount" width="100" />
      <el-table-column label="未查得次数" prop="noResultCount" width="110" />
      <el-table-column label="对账金额" prop="totalAmount" width="120">
        <template slot-scope="{ row }">{{ formatMoney(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="生成时间" prop="generatedTime" width="170" />
      <el-table-column label="操作" width="90" align="center">
        <template slot-scope="{ row }">
          <el-button type="text" size="mini" icon="el-icon-view" @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="月度对账详情" :visible.sync="detailOpen" width="920px" append-to-body>
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="公司">{{ detail.companyName }}</el-descriptions-item>
        <el-descriptions-item label="账单月份">{{ detail.billingMonth }}</el-descriptions-item>
        <el-descriptions-item label="查询次数">{{ detail.queryCount }}</el-descriptions-item>
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
import { listMonthlyBill, getMonthlyBill, generateMonthlyBill } from '@/api/business/monthlyBill'

export default {
  name: 'MonthlyBill',
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      billList: [],
      detailOpen: false,
      detail: null,
      detailRows: [],
      queryParams: { pageNum: 1, pageSize: 10, companyName: undefined, billingMonth: undefined }
    }
  },
  created() {
    this.queryParams.billingMonth = this.currentMonth()
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listMonthlyBill(this.queryParams).then(response => {
        this.billList = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleGenerate() {
      const month = this.queryParams.billingMonth || this.currentMonth()
      generateMonthlyBill(month).then(response => {
        this.$modal.msgSuccess('已生成 ' + (response.data || 0) + ' 家公司的月度账单')
        this.queryParams.billingMonth = month
        this.getList()
      })
    },
    handleDetail(row) {
      getMonthlyBill(row.id).then(response => {
        this.detail = response.data
        this.detailRows = response.details || []
        this.detailOpen = true
      })
    },
    currentMonth() {
      const date = new Date()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      return date.getFullYear() + '-' + month
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
