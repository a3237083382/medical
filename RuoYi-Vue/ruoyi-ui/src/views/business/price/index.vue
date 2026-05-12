<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="查询类型" prop="queryType">
        <el-input v-model="queryParams.queryType" placeholder="请输入查询类型" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="查询名称" prop="queryName">
        <el-input v-model="queryParams.queryName" placeholder="请输入查询名称" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['business:price:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :multiple="true" @click="handleDelete()" v-hasPermi="['business:price:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['business:price:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="priceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="查询类型" align="center" prop="queryType" />
      <el-table-column label="查询名称" align="center" prop="queryName" />
      <el-table-column label="单价(元)" align="center" prop="fee">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.fee) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:price:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['business:price:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="查询类型" prop="queryType">
          <el-input v-model="form.queryType" placeholder="请输入查询类型编码" />
        </el-form-item>
        <el-form-item label="查询名称" prop="queryName">
          <el-input v-model="form.queryName" placeholder="请输入查询名称" />
        </el-form-item>
        <el-form-item label="单价(元)" prop="fee">
          <el-input-number v-model="form.fee" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPrice, getPrice, addPrice, updatePrice, delPrice } from "@/api/business/price";

export default {
  name: "Price",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      priceList: [],
      title: "",
      open: false,
      queryParams: { pageNum: 1, pageSize: 10, queryType: undefined, queryName: undefined, status: undefined },
      form: {},
      rules: {
        queryType: [{ required: true, message: "查询类型不能为空", trigger: "blur" }],
        queryName: [{ required: true, message: "查询名称不能为空", trigger: "blur" }],
        fee: [{ required: true, message: "单价不能为空", trigger: "blur" }],
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listPrice(this.queryParams).then(response => {
        this.priceList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList(); },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery(); },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加查询价目";
    },
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getPrice(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改查询价目";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updatePrice(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addPrice(this.form).then(() => {
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
      this.$modal.confirm('是否确认删除查询价目编号为"' + ids + '"的数据项？').then(() => {
        return delPrice(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download("business/price/export", { ...this.queryParams }, `price_${new Date().getTime()}.xlsx`);
    },
    reset() {
      this.form = { id: undefined, queryType: undefined, queryName: undefined, fee: 0.01, status: "0" };
      this.resetForm("form");
    },
    formatMoney(val) {
      if (!val) return '0.00';
      return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    },
  },
};
</script>
