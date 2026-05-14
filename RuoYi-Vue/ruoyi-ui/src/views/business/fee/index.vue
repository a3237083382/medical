<template>
  <div class="app-container">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch" label-width="76px">
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width: 220px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="operationType">
        <el-select v-model="queryParams.operationType" placeholder="流水类型" clearable style="width: 150px">
          <el-option label="充值" value="RECHARGE" />
          <el-option label="扣费" value="DEDUCT" />
          <el-option label="结算扣费" value="SETTLEMENT" />
          <el-option label="退款" value="REFUND" />
          <el-option label="冲正" value="ADJUST" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['business:fee:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="feeList" border stripe>
      <el-table-column label="公司名称" align="center" prop="companyName" min-width="160" />
      <el-table-column label="流水类型" align="center" prop="operationType" width="110">
        <template slot-scope="{ row }">{{ operationText(row.operationType) }}</template>
      </el-table-column>
      <el-table-column label="金额" align="center" prop="amount" width="110">
        <template slot-scope="{ row }">{{ formatMoney(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="操作前余额" align="center" prop="balanceBefore" width="120">
        <template slot-scope="{ row }">{{ formatMoney(row.balanceBefore) }}</template>
      </el-table-column>
      <el-table-column label="操作后余额" align="center" prop="balanceAfter" width="120">
        <template slot-scope="{ row }">{{ formatMoney(row.balanceAfter) }}</template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="operator" width="120" />
      <el-table-column label="业务 ID" align="center" prop="bizId" width="100" />
      <el-table-column label="操作时间" align="center" prop="operationTime" width="170" />
      <el-table-column label="备注" align="center" prop="remark" min-width="120" />
      <el-table-column label="操作" align="center" width="90" fixed="right">
        <template slot-scope="{ row }">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(row)" v-hasPermi="['business:fee:query']">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="费用流水详情" :visible.sync="detailOpen" width="620px" append-to-body>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="公司名称">{{ detail.companyName }}</el-descriptions-item>
        <el-descriptions-item label="流水类型">{{ operationText(detail.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ formatMoney(detail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="业务 ID">{{ detail.bizId }}</el-descriptions-item>
        <el-descriptions-item label="操作前余额">{{ formatMoney(detail.balanceBefore) }}</el-descriptions-item>
        <el-descriptions-item label="操作后余额">{{ formatMoney(detail.balanceAfter) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operator }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detail.operationTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { listFee, getFee } from '@/api/business/fee'

export default {
  name: 'BusinessFee',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      dateRange: [],
      feeList: [],
      detailOpen: false,
      detail: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        companyName: undefined,
        operationType: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listFee(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.feeList = response.rows
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
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleDetail(row) {
      getFee(row.id).then(response => {
        this.detail = response.data || {}
        this.detailOpen = true
      })
    },
    handleExport() {
      this.download('business/fee/export', {
        ...this.addDateRange(this.queryParams, this.dateRange)
      }, `fee_flow_${new Date().getTime()}.xlsx`)
    },
    operationText(type) {
      return {
        RECHARGE: '充值',
        DEDUCT: '扣费',
        SETTLEMENT: '结算扣费',
        REFUND: '退款',
        ADJUST: '冲正'
      }[type] || type || ''
    },
    formatMoney(value) {
      return Number(value || 0).toFixed(2)
    }
  }
}
</script>

