<template>
  <div class="profile-page">
    <section class="profile-hero">
      <span>ACCOUNT PROFILE</span>
      <h2>资料信息</h2>
      <p>维护保险公司联系人和账号安全信息，基础公司名称与登录账号由平台统一管理。</p>
    </section>

    <section class="profile-grid">
      <article class="form-panel">
        <div class="panel-title">
          <h3>公司资料</h3>
          <p>联系人信息会用于运营对接和异常通知。</p>
        </div>
        <el-form ref="profileRef" :model="profileForm" label-width="100px">
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
      </article>

      <article class="form-panel">
        <div class="panel-title">
          <h3>修改密码</h3>
          <p>建议定期更换密码，并将 AppSecret 保存在保险公司服务端。</p>
        </div>
        <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
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
      </article>
    </section>
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

<style scoped>
.profile-page {
  display: grid;
  gap: 18px;
}

.profile-hero,
.form-panel {
  border: 1px solid rgba(16, 32, 47, 0.08);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 10px 28px rgba(12, 36, 48, 0.06);
}

.profile-hero {
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(15, 118, 110, 0.11), transparent 48%),
    #ffffff;
}

.profile-hero span {
  color: #0f766e;
  font-size: 12px;
  font-weight: 800;
}

.profile-hero h2 {
  margin: 8px 0;
  color: #10202f;
  font-size: 26px;
}

.profile-hero p,
.panel-title p {
  margin: 0;
  color: #667781;
  line-height: 1.7;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.form-panel {
  padding: 22px;
}

.panel-title {
  margin-bottom: 20px;
}

.panel-title h3 {
  margin: 0 0 8px;
  color: #10202f;
  font-size: 18px;
}

@media (max-width: 980px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
