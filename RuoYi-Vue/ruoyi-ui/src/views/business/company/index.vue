<template>
  <div class="app-container">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch" label-width="76px">
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width: 220px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['business:company:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:company:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['business:company:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="companyList" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="公司名称" align="center" prop="companyName" min-width="180" />
      <el-table-column label="AppKey" align="center" prop="appKey" min-width="280">
        <template slot-scope="{ row }">
          <span>{{ maskAppKey(row.appKey) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="余额(元)" align="center" prop="balance" width="120">
        <template slot-scope="{ row }">{{ formatMoney(row.balance) }}</template>
      </el-table-column>
      <el-table-column label="联系人" align="center" prop="contactPerson" width="120" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" width="140" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="{ row }">
          <el-switch v-model="row.status" active-value="0" inactive-value="1" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250" fixed="right">
        <template slot-scope="{ row }">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(row)" v-hasPermi="['business:company:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-tickets" @click="handleRecords(row)">记录</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(row)" v-hasPermi="['business:company:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="公司编码" prop="companyCode">
          <el-input v-model="form.companyCode" placeholder="请输入公司编码" />
        </el-form-item>
        <el-form-item label="登录用户名" prop="username" v-if="!form.id">
          <el-input v-model="form.username" placeholder="请输入登录用户名" />
        </el-form-item>
        <el-form-item :label="form.id ? '登录密码' : '登录密码'" :prop="form.id ? '' : 'password'">
          <el-input v-model="form.password" type="password" placeholder="留空则不修改" show-password />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="recordTitle" :visible.sync="recordOpen" width="900px" append-to-body>
      <el-tabs v-model="recordTab" @tab-click="loadCompanyRecords">
        <el-tab-pane label="查询日志" name="log">
          <el-table v-loading="recordLoading" :data="companyLogs" height="320" border>
            <el-table-column label="查询类型" prop="queryType" min-width="120" />
            <el-table-column label="费用" prop="fee" width="100">
              <template slot-scope="{ row }">{{ formatMoney(row.fee) }}</template>
            </el-table-column>
            <el-table-column label="状态" prop="status" width="90">
              <template slot-scope="{ row }">{{ row.status === '0' ? '成功' : '失败' }}</template>
            </el-table-column>
            <el-table-column label="请求时间" prop="requestTime" width="170" />
            <el-table-column label="请求 IP" prop="requestIp" width="140" />
            <el-table-column label="备注" prop="remark" min-width="140" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="费用流水" name="fee">
          <el-table v-loading="recordLoading" :data="companyFees" height="320" border>
            <el-table-column label="类型" prop="operationType" width="110" />
            <el-table-column label="金额" prop="amount" width="100">
              <template slot-scope="{ row }">{{ formatMoney(row.amount) }}</template>
            </el-table-column>
            <el-table-column label="操作前余额" prop="balanceBefore" width="120">
              <template slot-scope="{ row }">{{ formatMoney(row.balanceBefore) }}</template>
            </el-table-column>
            <el-table-column label="操作后余额" prop="balanceAfter" width="120">
              <template slot-scope="{ row }">{{ formatMoney(row.balanceAfter) }}</template>
            </el-table-column>
            <el-table-column label="操作时间" prop="operationTime" width="170" />
            <el-table-column label="备注" prop="remark" min-width="140" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import { listCompany, getCompany, addCompany, updateCompany, delCompany, changeStatus } from '@/api/business/company'
import { listLog } from '@/api/business/log'
import { listFee } from '@/api/business/fee'

export default {
  name: 'Company',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      companyList: [],
      title: '',
      open: false,
      recordOpen: false,
      recordLoading: false,
      recordTab: 'log',
      recordTitle: '',
      recordCompanyId: undefined,
      companyLogs: [],
      companyFees: [],
      dateRange: [],
      queryParams: { pageNum: 1, pageSize: 10, companyName: undefined, status: undefined },
      form: {},
      rules: {
        companyName: [{ required: true, message: '公司名称不能为空', trigger: 'blur' }],
        username: [{ required: true, message: '登录用户名不能为空', trigger: 'blur' }],
        password: [{ required: true, message: '登录密码不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listCompany(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.companyList = response.rows
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加保险公司'
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getCompany(id).then(response => {
        this.form = response.data
        this.form.password = undefined
        this.open = true
        this.title = '修改保险公司'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.id != null ? updateCompany(this.form) : addCompany(this.form)
        request.then(() => {
          this.$modal.msgSuccess(this.form.id != null ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除保险公司编号为"' + ids + '"的数据项？').then(() => {
        return delCompany(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleRecords(row) {
      this.recordCompanyId = row.id
      this.recordTitle = '关联记录 - ' + row.companyName
      this.recordTab = 'log'
      this.recordOpen = true
      this.loadCompanyRecords()
    },
    loadCompanyRecords() {
      if (!this.recordCompanyId) return
      this.recordLoading = true
      const query = { pageNum: 1, pageSize: 20, companyId: this.recordCompanyId }
      const request = this.recordTab === 'log' ? listLog(query) : listFee(query)
      request.then(response => {
        if (this.recordTab === 'log') {
          this.companyLogs = response.rows || []
        } else {
          this.companyFees = response.rows || []
        }
      }).finally(() => {
        this.recordLoading = false
      })
    },
    handleStatusChange(row) {
      const text = row.status === '0' ? '启用' : '停用'
      this.$modal.confirm('确认要' + text + '"' + row.companyName + '"吗？').then(() => {
        return changeStatus(row.id, row.status)
      }).then(() => {
        this.$modal.msgSuccess(text + '成功')
      }).catch(() => {
        row.status = row.status === '0' ? '1' : '0'
      })
    },
    handleExport() {
      this.download('business/company/export', { ...this.queryParams }, `company_${new Date().getTime()}.xlsx`)
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        id: undefined,
        companyName: undefined,
        companyCode: undefined,
        username: undefined,
        password: undefined,
        contactPerson: undefined,
        contactPhone: undefined,
        remark: undefined
      }
      this.resetForm('form')
    },
    formatMoney(value) {
      return Number(value || 0).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    maskAppKey(value) {
      if (!value) return '-'
      if (value.length <= 8) return value.slice(0, 2) + '****' + value.slice(-2)
      return value.slice(0, 4) + '****' + value.slice(-4)
    }
  }
}
</script>
