<template>
  <div class="app-container">
    <!-- 导入操作 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <template #header>
        <span>导入历史数据</span>
      </template>
      <el-alert
        title="支持 .xlsx / .xls 格式的 Excel 文件，第一行为列名，大小不限。导入的数据可到「医疗数据查询」页面进行历史数据查询。"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />
      <el-upload
        ref="uploadRef"
        drag
        :limit="1"
        accept=".xlsx,.xls"
        :auto-upload="false"
        :file-list="fileList"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或 <em>点击选择</em>
        </div>
        <template #tip>
          <div class="el-upload__tip" style="color: #909399">
            支持 .xlsx / .xls 格式的 Excel 文件
          </div>
        </template>
      </el-upload>
      <div style="margin-top: 16px; text-align: center">
        <el-button type="primary" icon="Upload" :loading="uploading" @click="handleUpload" :disabled="!selectedFile">
          开始导入
        </el-button>
        <el-button icon="Download" @click="downloadTemplate">
          下载导入模板
        </el-button>
      </div>
    </el-card>

    <!-- 导入记录列表 -->
    <el-card shadow="never">
      <template #header>
        <span>导入记录</span>
      </template>
      <el-table v-loading="loading" :data="importList" border stripe>
        <el-table-column label="批次号" prop="batchNo" min-width="180" show-overflow-tooltip />
        <el-table-column label="文件名" prop="fileName" min-width="200" show-overflow-tooltip />
        <el-table-column label="文件大小" prop="fileSize" width="120" align="right">
          <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="总行数" prop="totalRows" width="90" align="center" />
        <el-table-column label="成功" prop="successRows" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.successRows }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="失败" prop="failedRows" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.failedRows > 0" type="danger" size="small">{{ row.failedRows }}</el-tag>
            <span v-else>0</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === '0'" type="warning">导入中</el-tag>
            <el-tag v-else-if="row.status === '1'" type="success">全部成功</el-tag>
            <el-tag v-else-if="row.status === '2'" type="danger">部分失败</el-tag>
            <el-tag v-else type="info">导入失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="导入时间" prop="createTime" width="170" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)" v-hasPermi="['business:medical:import:delete']">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="导入详情" v-model="detailOpen" width="500px" append-to-body>
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="批次号">{{ detail.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="文件名">{{ detail.fileName }}</el-descriptions-item>
        <el-descriptions-item label="文件大小">{{ formatFileSize(detail.fileSize) }}</el-descriptions-item>
        <el-descriptions-item label="总行数">{{ detail.totalRows }}</el-descriptions-item>
        <el-descriptions-item label="成功行数">{{ detail.successRows }}</el-descriptions-item>
        <el-descriptions-item label="失败行数">{{ detail.failedRows }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detail.status === '0'" type="warning">导入中</el-tag>
          <el-tag v-else-if="detail.status === '1'" type="success">全部成功</el-tag>
          <el-tag v-else-if="detail.status === '2'" type="danger">部分失败</el-tag>
          <el-tag v-else type="info">导入失败</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMsg" label="错误信息" :span="1">
          <pre style="max-height: 150px; overflow: auto; white-space: pre-wrap">{{ detail.errorMsg }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="导入人">{{ detail.createBy }}</el-descriptions-item>
        <el-descriptions-item label="导入时间">{{ detail.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="AdminDataImport">
import { ref, reactive, getCurrentInstance } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const uploading = ref(false)
const total = ref(0)
const importList = ref([])
const selectedFile = ref(null)
const fileList = ref([])
const detailOpen = ref(false)
const detail = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

function getList() {
  loading.value = true
  request({
    url: '/business/data-import/list',
    method: 'get',
    params: queryParams
  }).then(res => {
    importList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleFileChange(file) {
  selectedFile.value = file.raw
}

function handleFileRemove() {
  selectedFile.value = null
}

function handleUpload() {
  if (!selectedFile.value) {
    proxy.$modal.msgWarning('请选择文件')
    return
  }

  const formData = new FormData()
  formData.append('file', selectedFile.value)

  uploading.value = true
  request({
    url: '/business/data-import',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: formData
  }).then(res => {
    if (res.code === 200) {
      proxy.$modal.msgSuccess('导入任务已提交')
      selectedFile.value = null
      fileList.value = []
      getList()
    } else {
      proxy.$modal.msgError(res.msg || '导入失败')
    }
  }).catch(err => {
    proxy.$modal.msgError('导入异常: ' + (err.message || '未知错误'))
  }).finally(() => {
    uploading.value = false
  })
}

function handleDetail(row) {
  request({
    url: '/business/data-import/detail',
    method: 'get',
    params: { id: row.id }
  }).then(res => {
    detail.value = res.data || {}
    detailOpen.value = true
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除该批次所有导入数据吗？', '警告', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request({
      url: '/business/data-import/delete',
      method: 'post',
      data: { batchNo: row.batchNo }
    }).then(res => {
      if (res.code === 200) {
        proxy.$modal.msgSuccess('删除成功')
        getList()
      } else {
        proxy.$modal.msgError(res.msg)
      }
    })
  }).catch(() => {})
}

function downloadTemplate() {
  request({
    url: '/business/data-import/template',
    method: 'get',
    responseType: 'blob'
  }).then(res => {
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '历史数据导入模板.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }).catch(() => {
    proxy.$modal.msgInfo('请参考导入说明：Excel 第一行为列名，至少包含「姓名」列')
  })
}

function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(1) + ' ' + units[i]
}

getList()
</script>
