<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:company:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:company:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:company:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="companyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="公司名称" align="center" prop="companyName" />
      <el-table-column label="AppKey" align="center" prop="appKey" width="280">
        <template #default="scope">
          <span>{{ scope.row.appKey }}</span>
          <el-button type="primary" link icon="CopyDocument" @click="copyText(scope.row.appKey)" style="margin-left: 5px;" />
        </template>
      </el-table-column>
      <el-table-column label="余额(元)" align="center" prop="balance">
        <template #default="scope">
          <span>{{ formatMoney(scope.row.balance) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="联系人" align="center" prop="contactPerson" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:company:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:company:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="公司编码" prop="companyCode">
          <el-input v-model="form.companyCode" placeholder="请输入公司编码" />
        </el-form-item>
        <el-form-item label="登录用户名" prop="username" v-if="!form.id">
          <el-input v-model="form.username" placeholder="请输入登录用户名" />
        </el-form-item>
        <el-form-item :label="form.id ? '登录密码(留空不修改)' : '登录密码'" prop="password" :rules="form.id ? [] : rules.password">
          <el-input v-model="form.password" type="password" placeholder="请输入登录密码" show-password />
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
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup name="Company">
import { listCompany, getCompany, addCompany, updateCompany, delCompany, changeStatus } from "@/api/business/company"
import { addDateRange } from "@/utils/ruoyi"

const router = useRouter()
const { proxy } = getCurrentInstance()

const companyList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const dateRange = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  companyName: undefined,
  status: undefined
})

const form = reactive({
  id: undefined,
  companyName: undefined,
  companyCode: undefined,
  username: undefined,
  password: undefined,
  contactPerson: undefined,
  contactPhone: undefined,
  remark: undefined
})

const rules = {
  companyName: [{ required: true, message: "公司名称不能为空", trigger: "blur" }],
  username: [{ required: true, message: "登录用户名不能为空", trigger: "blur" }],
  password: [{ required: true, message: "登录密码不能为空", trigger: "blur" }]
}

function getList() {
  loading.value = true
  listCompany(addDateRange(queryParams, dateRange.value)).then(response => {
    companyList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; proxy.resetForm("queryRef"); handleQuery() }

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加保险公司"
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getCompany(id).then(response => {
    Object.assign(form, response.data)
    form.password = undefined
    open.value = true
    title.value = "修改保险公司"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.id != null) {
        updateCompany(form).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCompany(form).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除保险公司编号为"' + delIds + '"的数据项？').then(() => {
    return delCompany(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + text + '"' + row.companyName + '"吗？').then(() => {
    return changeStatus(row.id, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(() => { row.status = row.status === "0" ? "1" : "0" })
}

function handleExport() {
  proxy.download("business/company/export", { ...queryParams }, `company_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.id = undefined
  form.companyName = undefined
  form.companyCode = undefined
  form.username = undefined
  form.password = undefined
  form.contactPerson = undefined
  form.contactPhone = undefined
  form.remark = undefined
  proxy.resetForm("formRef")
}

function formatMoney(val) {
  if (!val) return '0.00'
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

function copyText(text) {
  navigator.clipboard.writeText(text).then(() => {
    proxy.$modal.msgSuccess("已复制")
  })
}

getList()
</script>
