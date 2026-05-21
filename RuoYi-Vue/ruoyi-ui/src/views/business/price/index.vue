<template>
  <div class="app-container price-config-page">
    <div class="company-panel">
      <div class="panel-header">
        <span>保险公司</span>
        <el-input
          v-model="companyQuery.companyName"
          size="small"
          clearable
          placeholder="搜索公司"
          @keyup.enter.native="getCompanyList"
          @clear="getCompanyList"
        >
          <el-button slot="append" icon="el-icon-search" @click="getCompanyList" />
        </el-input>
      </div>
      <el-table
        v-loading="companyLoading"
        :data="companyList"
        height="calc(100vh - 210px)"
        highlight-current-row
        border
        @current-change="selectCompany"
      >
        <el-table-column label="公司名称" prop="companyName" min-width="160" />
        <el-table-column label="状态" prop="status" width="76" align="center">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="row.status === '0' ? 'success' : 'danger'">
              {{ row.status === '0' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="price-panel">
      <div class="panel-header">
        <div>
          <span>接口费用标准</span>
          <small v-if="selectedCompany">当前公司：{{ selectedCompany.companyName }}</small>
        </div>
        <div>
          <el-button icon="el-icon-refresh" size="mini" @click="reloadCurrentCompany" :disabled="!selectedCompany">刷新</el-button>
          <el-button type="primary" icon="el-icon-check" size="mini" @click="saveAll" :disabled="!selectedCompany || !priceItems.length" v-hasPermi="['business:price:edit']">保存全部</el-button>
        </div>
      </div>

      <el-empty v-if="!selectedCompany" description="请先选择左侧保险公司" />
      <el-table v-else v-loading="priceLoading" :data="priceItems" border stripe>
        <el-table-column label="接口编码" prop="queryType" min-width="150" />
        <el-table-column label="接口名称" prop="queryName" min-width="170" />
        <el-table-column label="查得标准(元)" width="160">
          <template slot-scope="{ row }">
            <el-input-number v-model="row.hitFee" :min="0" :precision="2" :step="1" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="未查得标准(元)" width="170">
          <template slot-scope="{ row }">
            <el-input-number v-model="row.noResultFee" :min="0" :precision="2" :step="1" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template slot-scope="{ row }">
            <el-switch v-model="row.status" active-value="0" inactive-value="1" active-text="启用" inactive-text="停用" />
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="170">
          <template slot-scope="{ row }">
            <el-input v-model="row.remark" size="small" placeholder="可选" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" size="mini" icon="el-icon-check" @click="saveRow(row)" v-hasPermi="['business:price:edit']">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { listCompany } from '@/api/business/company'
import { listCompanyPriceItems, addPrice, updatePrice } from '@/api/business/price'

export default {
  name: 'CompanyQueryPrice',
  data() {
    return {
      companyLoading: false,
      priceLoading: false,
      companyQuery: { pageNum: 1, pageSize: 1000, companyName: undefined },
      companyList: [],
      selectedCompany: null,
      priceItems: []
    }
  },
  created() {
    this.getCompanyList()
  },
  methods: {
    getCompanyList() {
      this.companyLoading = true
      listCompany(this.companyQuery).then(response => {
        this.companyList = response.rows || []
        if (!this.selectedCompany && this.companyList.length) {
          this.selectCompany(this.companyList[0])
        }
      }).finally(() => {
        this.companyLoading = false
      })
    },
    selectCompany(row) {
      if (!row) return
      this.selectedCompany = row
      this.loadCompanyPrices(row.id)
    },
    loadCompanyPrices(companyId) {
      this.priceLoading = true
      listCompanyPriceItems(companyId).then(response => {
        this.priceItems = (response.data || []).map(item => ({
          id: item.id,
          companyId: companyId,
          queryType: item.queryType,
          queryName: item.queryName,
          hitFee: Number(item.hitFee || 0),
          noResultFee: Number(item.noResultFee || 0),
          status: item.status || '0',
          remark: item.remark
        }))
      }).finally(() => {
        this.priceLoading = false
      })
    },
    reloadCurrentCompany() {
      if (this.selectedCompany) {
        this.loadCompanyPrices(this.selectedCompany.id)
      }
    },
    saveRow(row) {
      const payload = Object.assign({}, row)
      const request = payload.id ? updatePrice(payload) : addPrice(payload)
      return request.then(() => {
        this.$modal.msgSuccess('保存成功')
        this.reloadCurrentCompany()
      })
    },
    saveAll() {
      this.priceLoading = true
      const tasks = this.priceItems.map(row => {
        const payload = Object.assign({}, row)
        return payload.id ? updatePrice(payload) : addPrice(payload)
      })
      Promise.all(tasks).then(() => {
        this.$modal.msgSuccess('全部保存成功')
        this.reloadCurrentCompany()
      }).finally(() => {
        this.priceLoading = false
      })
    }
  }
}
</script>

<style scoped>
.price-config-page {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 16px;
}
.company-panel,
.price-panel {
  min-width: 0;
}
.panel-header {
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  font-weight: 700;
  color: #24364a;
}
.panel-header small {
  display: block;
  margin-top: 4px;
  color: #7a8a9a;
  font-weight: 400;
}
.company-panel .panel-header {
  align-items: flex-start;
  flex-direction: column;
}
</style>
