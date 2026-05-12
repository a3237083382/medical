<template>
  <div class="app-container">
    <el-card shadow="hover">
      <div slot="header"><span>提交充值申请</span></div>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" style="max-width:500px">
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" style="width:200px" />
        </el-form-item>
        <el-form-item label="备注说明" prop="submitRemark">
          <el-input v-model="form.submitRemark" type="textarea" :rows="3" placeholder="请填写转账信息或备注" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitForm">提交申请</el-button>
          <el-button @click="$router.push('/company/dashboard')">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { submitRecharge } from "@/api/business/portal";

export default {
  name: "CompanyRecharge",
  data() {
    return {
      submitting: false,
      form: { amount: 0.01, submitRemark: "" },
      rules: {
        amount: [{ required: true, message: "请输入充值金额" }],
      },
    };
  },
  methods: {
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) return;
        this.submitting = true;
        submitRecharge(this.form).then(() => {
          this.$modal.msgSuccess("充值申请已提交，等待管理员审核");
          this.form = { amount: 0.01, submitRemark: "" };
        }).finally(() => { this.submitting = false; });
      });
    },
  },
};
</script>
