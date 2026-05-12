<template>
  <div class="login-body">
    <div class="login-panel">
      <h2 class="login-title">保险公司门户</h2>
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
          <el-button type="primary" :loading="loading" style="width:100%" size="large" @click="handleLogin">登 录</el-button>
        </el-form-item>
        <div style="text-align:center;font-size:12px;color:#999">
          <router-link to="/login" style="color:#409EFF">管理员登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup name="CompanyLogin">
import { companyLogin } from "@/api/business/portal"
import { setCompanyToken } from "@/utils/companyAuth"

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
      localStorage.setItem("companyToken", res.data.token)
      localStorage.setItem("companyInfo", JSON.stringify(res.data))
      setCompanyToken(res.data.token)
      router.push(proxy.$route.query.redirect || "/company/dashboard")
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-panel {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0,0,0,.15);
}
.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 22px;
}
</style>
