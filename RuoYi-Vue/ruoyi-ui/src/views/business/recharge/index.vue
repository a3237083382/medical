<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width:200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width:120px">
          <el-option label="待审核" value="0" />
          <el-option label="已通过" value="1" />
          <el-option label="已驳回" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="公司名称" align="center" prop="companyName" width="180" />
      <el-table-column label="金额" align="center" prop="amount" width="120">
        <template slot-scope="{row}">{{ formatMoney(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.status==='1'?'success':row.status==='2'?'danger':'warning'">
            {{ row.status==='0'?'待审核':row.status==='1'?'已通过':'已驳回' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="submitTime" width="170" />
      <el-table-column label="备注" align="center" prop="submitRemark" min-width="150" />
      <el-table-column label="审核人" align="center" prop="reviewer" width="100" />
      <el-table-column label="审核时间" align="center" prop="reviewTime" width="170" />
      <el-table-column label="审核备注" align="center" prop="reviewRemark" min-width="150" />
      <el-table-column label="操作" align="center" width="180" fixed="right">
        <template slot-scope="{row}">
          <el-button v-if="row.status==='0'" type="success" size="mini" icon="el-icon-check" @click="handleApprove(row)">通过</el-button>
          <el-button v-if="row.status==='0'" type="danger" size="mini" icon="el-icon-close" @click="handleReject(row)">驳回</el-button>
          <span v-else style="color:#999">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 审核对话框 -->
    <el-dialog :title="auditTitle" :visible.sync="dialogOpen" width="400px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.reviewRemark" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitAudit">确 定</el-button>
        <el-button @click="dialogOpen=false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRecharge, approveRecharge, rejectRecharge } from "@/api/business/recharge";

export default {
  name: "RechargeAudit",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      list: [],
      queryParams: { pageNum: 1, pageSize: 10, companyName: undefined, status: undefined },
      dialogOpen: false,
      auditTitle: "",
      auditForm: { id: undefined, reviewRemark: "", action: "" },
    };
  },
  created() { this.getList(); },
  methods: {
    getList() {
      this.loading = true;
      listRecharge(this.queryParams).then(res => {
        this.list = res.rows;
        this.total = res.total;
      }).finally(() => { this.loading = false; });
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList(); },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery(); },
    handleApprove(row) {
      this.auditForm = { id: row.id, reviewRemark: "", action: "approve" };
      this.auditTitle = "审核通过 - " + row.companyName;
      this.dialogOpen = true;
    },
    handleReject(row) {
      this.auditForm = { id: row.id, reviewRemark: "", action: "reject" };
      this.auditTitle = "驳回 - " + row.companyName;
      this.dialogOpen = true;
    },
    submitAudit() {
      const req = { id: this.auditForm.id, reviewRemark: this.auditForm.reviewRemark };
      const api = this.auditForm.action === "approve" ? approveRecharge(req) : rejectRecharge(req);
      api.then(() => {
        this.$modal.msgSuccess(this.auditForm.action === "approve" ? "已通过" : "已驳回");
        this.dialogOpen = false;
        this.getList();
      });
    },
    formatMoney(val) {
      if (!val) return "0.00";
      return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    },
  },
};
</script>
