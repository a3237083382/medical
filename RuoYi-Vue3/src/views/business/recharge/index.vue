<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
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
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="公司名称" align="center" prop="companyName" width="180" />
      <el-table-column label="金额" align="center" prop="amount" width="120">
        <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
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
        <template #default="{ row }">
          <el-button v-if="row.status==='0'" type="success" size="small" icon="Check" @click="handleApprove(row)">通过</el-button>
          <el-button v-if="row.status==='0'" type="danger" size="small" icon="Close" @click="handleReject(row)">驳回</el-button>
          <span v-else style="color:#999">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="auditTitle" v-model="dialogOpen" width="400px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.reviewRemark" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAudit">确 定</el-button>
        <el-button @click="dialogOpen=false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RechargeAudit">
import { listRecharge, approveRecharge, rejectRecharge } from "@/api/business/recharge"

const { proxy } = getCurrentInstance()

const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const list = ref([])
const dialogOpen = ref(false)
const auditTitle = ref("")

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  companyName: undefined,
  status: undefined
})

const auditForm = reactive({
  id: undefined,
  reviewRemark: "",
  action: ""
})

function getList() {
  loading.value = true
  listRecharge(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
  }).finally(() => { loading.value = false })
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }

function handleApprove(row) {
  auditForm.id = row.id
  auditForm.reviewRemark = ""
  auditForm.action = "approve"
  auditTitle.value = "审核通过 - " + row.companyName
  dialogOpen.value = true
}

function handleReject(row) {
  auditForm.id = row.id
  auditForm.reviewRemark = ""
  auditForm.action = "reject"
  auditTitle.value = "驳回 - " + row.companyName
  dialogOpen.value = true
}

function submitAudit() {
  const req = { id: auditForm.id, reviewRemark: auditForm.reviewRemark }
  const api = auditForm.action === "approve" ? approveRecharge(req) : rejectRecharge(req)
  api.then(() => {
    proxy.$modal.msgSuccess(auditForm.action === "approve" ? "已通过" : "已驳回")
    dialogOpen.value = false
    getList()
  })
}

function formatMoney(val) {
  if (!val) return "0.00"
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",")
}

getList()
</script>
