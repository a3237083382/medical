<template>
  <div class="app-container dashboard-page">
    <el-row :gutter="12" class="metric-row">
      <el-col :xs="12" :sm="8" :md="4" v-for="item in metrics" :key="item.label">
        <div class="metric">
          <div class="metric-label">{{ item.label }}</div>
          <div class="metric-value">{{ item.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :xs="24" :lg="14">
        <div ref="trendChart" class="chart"></div>
      </el-col>
      <el-col :xs="24" :lg="10">
        <div ref="typeChart" class="chart"></div>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :span="24">
        <el-table :data="companyRank" border stripe>
          <el-table-column label="排名" type="index" width="80" align="center" />
          <el-table-column label="保险公司" prop="name" min-width="180" />
          <el-table-column label="查询次数" prop="value" width="140" align="center" />
          <el-table-column label="费用合计" prop="amount" width="160" align="center">
            <template slot-scope="{ row }">{{ formatMoney(row.amount) }}</template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getDashboardSummary, getMonthlyTrend, getQueryTypeStats, getCompanyRank } from '@/api/business/dashboard'

export default {
  name: 'BusinessDashboard',
  data() {
    return {
      summary: {},
      trend: [],
      queryTypes: [],
      companyRank: [],
      trendChart: null,
      typeChart: null
    }
  },
  computed: {
    metrics() {
      return [
        { label: '总查询次数', value: this.summary.totalQueryCount || 0 },
        { label: '成功查询', value: this.summary.successQueryCount || 0 },
        { label: '失败查询', value: this.summary.failedQueryCount || 0 },
        { label: '今日查询', value: this.summary.todayQueryCount || 0 },
        { label: '保险公司', value: this.summary.companyCount || 0 },
        { label: '费用合计', value: this.formatMoney(this.summary.totalFee) }
      ]
    }
  },
  mounted() {
    this.loadData()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    if (this.trendChart) this.trendChart.dispose()
    if (this.typeChart) this.typeChart.dispose()
  },
  methods: {
    loadData() {
      Promise.all([
        getDashboardSummary(),
        getMonthlyTrend(),
        getQueryTypeStats(),
        getCompanyRank()
      ]).then(([summary, trend, queryTypes, companyRank]) => {
        this.summary = summary.data || {}
        this.trend = trend.data || []
        this.queryTypes = queryTypes.data || []
        this.companyRank = companyRank.data || []
        this.$nextTick(this.renderCharts)
      })
    },
    renderCharts() {
      this.trendChart = this.trendChart || echarts.init(this.$refs.trendChart)
      this.typeChart = this.typeChart || echarts.init(this.$refs.typeChart)
      this.trendChart.setOption({
        title: { text: '近 12 个月查询趋势', left: 12, top: 8, textStyle: { fontSize: 14 } },
        tooltip: { trigger: 'axis' },
        grid: { left: 44, right: 20, top: 56, bottom: 36 },
        xAxis: { type: 'category', data: this.trend.map(item => item.name) },
        yAxis: { type: 'value' },
        series: [
          { name: '查询次数', type: 'line', smooth: true, data: this.trend.map(item => item.value || 0) }
        ]
      })
      this.typeChart.setOption({
        title: { text: '查询类型占比', left: 12, top: 8, textStyle: { fontSize: 14 } },
        tooltip: { trigger: 'item' },
        series: [
          {
            type: 'pie',
            radius: ['45%', '70%'],
            center: ['50%', '56%'],
            data: this.queryTypes.map(item => ({ name: item.name, value: item.value || 0 }))
          }
        ]
      })
    },
    resizeCharts() {
      if (this.trendChart) this.trendChart.resize()
      if (this.typeChart) this.typeChart.resize()
    },
    formatMoney(value) {
      return Number(value || 0).toFixed(2)
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  background: #f3f5f8;
}

.metric-row {
  margin-bottom: 12px;
}

.metric {
  min-height: 82px;
  padding: 14px 16px;
  margin-bottom: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.metric-label {
  color: #606266;
  font-size: 13px;
}

.metric-value {
  margin-top: 10px;
  color: #1f2937;
  font-size: 24px;
  font-weight: 600;
}

.chart {
  height: 360px;
  margin-bottom: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}
</style>

