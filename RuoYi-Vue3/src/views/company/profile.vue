<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>个人信息</span></template>
          <el-form ref="profileRef" :model="profileForm" label-width="100px" style="max-width: 520px">
            <el-form-item label="公司名称">
              <el-input v-model="profileForm.companyName" disabled />
            </el-form-item>
            <el-form-item label="登录账号">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="联系人">
              <el-input v-model="profileForm.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="profileForm.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="profileForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingProfile" @click="submitProfile">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>修改密码</span></template>
          <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 520px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingPassword" @click="submitPassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="CompanyProfile">
import { getCompanyProfile, updateCompanyProfile, updateCompanyPassword } from "@/api/business/portal"

const { proxy } = getCurrentInstance()
const savingProfile = ref(false)
const savingPassword = ref(false)

const profileForm = reactive({
  companyName: "",
  username: "",
  contactPerson: "",
  contactPhone: "",
  remark: ""
})

const pwdForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error("两次输入的新密码不一致"))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "新密码至少6位", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    { validator: validateConfirm, trigger: "blur" }
  ]
}

function loadProfile() {
  getCompanyProfile().then(res => {
    Object.assign(profileForm, res.data || {})
  })
}

function submitProfile() {
  savingProfile.value = true
  updateCompanyProfile({
    contactPerson: profileForm.contactPerson,
    contactPhone: profileForm.contactPhone,
    remark: profileForm.remark
  }).then(() => {
    proxy.$modal.msgSuccess("保存成功")
    loadProfile()
  }).finally(() => { savingProfile.value = false })
}

function submitPassword() {
  proxy.$refs["pwdRef"].validate(valid => {
    if (!valid) return
    savingPassword.value = true
    updateCompanyPassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    }).then(() => {
      proxy.$modal.msgSuccess("密码修改成功")
      pwdForm.oldPassword = ""
      pwdForm.newPassword = ""
      pwdForm.confirmPassword = ""
      proxy.resetForm("pwdRef")
    }).finally(() => { savingPassword.value = false })
  })
}

loadProfile()
</script>
