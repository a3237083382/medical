<template>
  <div class="login-body">
    <div class="login-panel">
      <div class="panel-heading">
        <div class="system-name">医疗信息查询平台</div>
        <span>保险公司登录</span>
        <small>请使用保险公司账号登录</small>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="handleLogin">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button class="login-submit" type="primary" :loading="loading" size="large" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup name="CompanyLogin">
import { companyLogin } from "@/api/business/portal"
import { setCompanyEmbedMode, setCompanyInfo, setCompanyToken } from "@/utils/companyAuth"
import { resolveCompanyEmbedMode, resolveCompanyLoginTarget } from "@/utils/companyLoginTarget"

const router = useRouter()
const { proxy } = getCurrentInstance()

const form = reactive({ username: "", password: "" })
const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
}
const loading = ref(false)

function handleLogin() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    loading.value = true
    companyLogin(form).then(res => {
      localStorage.setItem("companyInfo", JSON.stringify(res.data))
      setCompanyToken(res.data.token)
      setCompanyInfo(res.data)
      setCompanyEmbedMode(resolveCompanyEmbedMode(proxy.$route.query))
      router.push(resolveCompanyLoginTarget(proxy.$route.query))
    }).catch(() => {
      loading.value = false
    })
  })
}
</script>

<style scoped>
.login-body {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #f4f7f9;
}

.login-panel {
  width: 400px;
  padding: 32px;
  background: #fff;
  border: 1px solid #e3e8ee;
  border-radius: 8px;
  box-shadow: 0 12px 32px rgba(16, 32, 47, 0.08);
}

.panel-heading {
  margin-bottom: 24px;
  text-align: center;
}

.system-name {
  margin-bottom: 14px;
  color: #0f766e;
  font-size: 15px;
  font-weight: 700;
}

.panel-heading span {
  display: block;
  color: #10202f;
  font-size: 22px;
  font-weight: 800;
}

.panel-heading small {
  display: block;
  margin-top: 8px;
  color: #6b7b84;
}

.login-submit {
  width: 100%;
  background: #0f766e;
  border-color: #0f766e;
  font-weight: 700;
}

@media (max-width: 900px) {
  .login-body {
    min-height: 100vh;
    height: auto;
    align-items: center;
  }

  .login-panel {
    width: 100%;
    max-width: 400px;
  }
}
</style>
