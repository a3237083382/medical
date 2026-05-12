<template>
  <div class="login-body">
    <div class="login-panel">
      <h2 class="login-title">保险公司门户</h2>
      <el-form ref="form" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="el-icon-user" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="el-icon-lock" size="large" show-password @keyup.enter="handleLogin" />
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

<script>
import { companyLogin } from "@/api/business/portal";

export default {
  name: "CompanyLogin",
  data() {
    return {
      form: { username: "", password: "" },
      rules: {
        username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
      },
      loading: false,
    };
  },
  methods: {
    handleLogin() {
      this.$refs["form"].validate(valid => {
        if (!valid) return;
        this.loading = true;
        companyLogin(this.form).then(res => {
          localStorage.setItem("companyToken", res.data.token);
          localStorage.setItem("companyInfo", JSON.stringify(res.data));
          this.$router.push("/company/dashboard");
        }).catch(() => {
          this.loading = false;
        });
      });
    },
  },
};
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
