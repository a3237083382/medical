<template>
  <div class="app-container">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch" label-width="76px">
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width: 220px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="查询类型" prop="queryType">
        <el-input v-model="queryParams.queryType" placeholder="请输入查询类型" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="成功" value="0" />
          <el-option label="失败" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="请求时间">
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
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['business:log:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="logList" border stripe>
      <el-table-column label="公司名称" align="center" prop="companyName" min-width="160" />
      <el-table-column label="查询类型" align="center" prop="queryType" min-width="120" />
      <el-table-column label="费用" align="center" prop="fee" width="100">
        <template slot-scope="{ row }">{{ formatMoney(row.fee) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="请求 IP" align="center" prop="requestIp" width="140" />
      <el-table-column label="请求时间" align="center" prop="requestTime" width="170" />
      <el-table-column label="备注" align="center" prop="remark" min-width="120" />
      <el-table-column label="操作" align="center" width="90" fixed="right">
        <template slot-scope="{ row }">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(row)" v-hasPermi="['business:log:query']">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="查询日志详情" :visible.sync="detailOpen" width="680px" append-to-body>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="公司名称">{{ detail.companyName }}</el-descriptions-item>
        <el-descriptions-item label="查询类型">{{ detail.queryType }}</el-descriptions-item>
        <el-descriptions-item label="费用">{{ formatMoney(detail.fee) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status === '0' ? '成功' : '失败' }}</el-descriptions-item>
        <el-descriptions-item label="请求 IP">{{ detail.requestIp }}</el-descriptions-item>
        <el-descriptions-item label="请求时间">{{ detail.requestTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
        <el-descriptions-item label="查询参数" :span="2">
          <pre class="json-block">{{ formatJson(detail.queryParams) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { listLog, getLog } from '@/api/business/log'

export default {
  name: 'BusinessLog',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      dateRange: [],
      logList: [],
      detailOpen: false,
      detail: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        companyName: undefined,
        queryType: undefined,
        status: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listLog(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.logList = response.rows
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
      getLog(row.id).then(response => {
        this.detail = response.data || {}
        this.detailOpen = true
      })
    },
    handleExport() {
      this.download('business/log/export', {
        ...this.addDateRange(this.queryParams, this.dateRange)
      }, `query_log_${new Date().getTime()}.xlsx`)
    },
    formatMoney(value) {
      return Number(value || 0).toFixed(2)
    },
    formatJson(value) {
      if (!value) {
        return ''
      }
      try {
        return JSON.stringify(JSON.parse(value), null, 2)
      } catch (e) {
        return value
      }
    }
  }
}
</script>

<style scoped>
.json-block {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>

