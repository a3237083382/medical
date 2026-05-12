<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable>
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
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :multiple="true" @click="handleDelete()" v-hasPermi="['business:company:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['business:company:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="companyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="公司名称" align="center" prop="companyName" />
      <el-table-column label="AppKey" align="center" prop="appKey" width="280">
        <template slot-scope="scope">
          <span>{{ scope.row.appKey }}</span>
          <el-button type="text" icon="el-icon-copy-document" @click="copyText(scope.row.appKey)" style="margin-left: 5px;" />
        </template>
      </el-table-column>
      <el-table-column label="余额(元)" align="center" prop="balance">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.balance) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结算周期" align="center" prop="billingCycleDays">
        <template slot-scope="scope">
          <span>{{ scope.row.billingCycleDays }}天</span>
        </template>
      </el-table-column>
      <el-table-column label="联系人" align="center" prop="contactPerson" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:company:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-money" @click="handleRecharge(scope.row)" v-hasPermi="['business:company:recharge']">充值</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['business:company:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="公司编码" prop="companyCode">
          <el-input v-model="form.companyCode" placeholder="请输入公司编码" />
        </el-form-item>
        <el-form-item label="登录用户名" prop="username" v-if="!form.id">
          <el-input v-model="form.username" placeholder="请输入登录用户名" />
        </el-form-item>
        <el-form-item :label="form.id ? '登录密码(留空不修改)' : '登录密码'" :prop="form.id ? '' : 'password'">
          <el-input v-model="form.password" type="password" placeholder="请输入登录密码" show-password />
        </el-form-item>
        <el-form-item label="结算周期(天)" prop="billingCycleDays">
          <el-input-number v-model="form.billingCycleDays" :min="1" :max="365" />
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

    <!-- 充值对话框 -->
    <el-dialog title="充值" :visible.sync="rechargeOpen" width="400px" append-to-body>
      <el-form ref="rechargeForm" :model="rechargeForm" :rules="rechargeRules" label-width="100px">
        <el-form-item label="公司名称">
          <el-input :value="rechargeForm.companyName" disabled />
        </el-form-item>
        <el-form-item label="当前余额">
          <el-input :value="formatMoney(rechargeForm.currentBalance) + ' 元'" disabled />
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="rechargeForm.amount" :min="0.01" :precision="2" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRecharge">确认充值</el-button>
        <el-button @click="rechargeOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCompany, getCompany, addCompany, updateCompany, delCompany, recharge, changeStatus } from "@/api/business/company";

export default {
  name: "Company",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      companyList: [],
      title: "",
      open: false,
      rechargeOpen: false,
      dateRange: [],
      queryParams: { pageNum: 1, pageSize: 10, companyName: undefined, status: undefined },
      form: {},
      rechargeForm: { id: undefined, companyName: '', currentBalance: 0, amount: 0 },
      rules: {
        companyName: [{ required: true, message: "公司名称不能为空", trigger: "blur" }],
        username: [{ required: true, message: "登录用户名不能为空", trigger: "blur" }],
        password: [{ required: true, message: "登录密码不能为空", trigger: "blur" }],
      },
      rechargeRules: {
        amount: [{ required: true, message: "充值金额不能为空", trigger: "blur" }],
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listCompany(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.companyList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList(); },
    resetQuery() { this.dateRange = []; this.resetForm("queryForm"); this.handleQuery(); },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加保险公司";
    },
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getCompany(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改保险公司";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateCompany(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addCompany(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除保险公司编号为"' + ids + '"的数据项？').then(() => {
        return delCompany(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleRecharge(row) {
      this.rechargeForm = { id: row.id, companyName: row.companyName, currentBalance: row.balance, amount: 0 };
      this.rechargeOpen = true;
    },
    submitRecharge() {
      this.$refs["rechargeForm"].validate(valid => {
        if (valid) {
          recharge(this.rechargeForm.id, this.rechargeForm.amount).then(() => {
            this.$modal.msgSuccess("充值成功");
            this.rechargeOpen = false;
            this.getList();
          });
        }
      });
    },
    handleStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用";
      this.$modal.confirm('确认要"' + text + '"' + row.companyName + '"吗？').then(() => {
        return changeStatus(row.id, row.status);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(() => { row.status = row.status === "0" ? "1" : "0"; });
    },
    handleExport() {
      this.download("business/company/export", { ...this.queryParams }, `company_${new Date().getTime()}.xlsx`);
    },
    reset() {
      this.form = { id: undefined, companyName: undefined, companyCode: undefined, username: undefined, password: undefined, billingCycleDays: 30, contactPerson: undefined, contactPhone: undefined, remark: undefined };
      this.resetForm("form");
    },
    formatMoney(val) {
      if (!val) return '0.00';
      return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    },
    copyText(text) {
      const input = document.createElement('input');
      input.value = text;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      this.$modal.msgSuccess("已复制");
    },
  },
};
</script>
