<template>
  <div class="recharge-page">
    <section class="recharge-hero">
      <div>
        <span>RECHARGE REQUEST</span>
        <h2>提交充值申请</h2>
        <p>公对公转账后提交申请，运营方审核通过后余额入账并生成费用流水。</p>
      </div>
    </section>

    <section class="recharge-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="备注说明" prop="submitRemark">
          <el-input v-model="form.submitRemark" type="textarea" :rows="4" placeholder="请填写转账流水号、付款户名或备注" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitForm">提交申请</el-button>
          <el-button @click="router.push('/company')">返回</el-button>
        </el-form-item>
      </el-form>
    </section>
  </div>
</template>

<script setup name="CompanyRecharge">
import { submitRecharge } from "@/api/business/portal"

const router = useRouter()
const { proxy } = getCurrentInstance()

const submitting = ref(false)
const form = reactive({ amount: 0.01, submitRemark: "" })
const rules = {
  amount: [{ required: true, message: "请输入充值金额" }]
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    submitting.value = true
    submitRecharge(form).then(() => {
      proxy.$modal.msgSuccess("充值申请已提交，等待平台审核")
      form.amount = 0.01
      form.submitRemark = ""
    }).finally(() => { submitting.value = false })
  })
}
</script>

<style scoped>
.recharge-page {
  display: grid;
  gap: 18px;
}

.recharge-hero,
.recharge-card {
  border: 1px solid rgba(16, 32, 47, 0.08);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 10px 28px rgba(12, 36, 48, 0.06);
}

.recharge-hero {
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(15, 118, 110, 0.11), transparent 48%),
    #ffffff;
}

.recharge-hero span {
  color: #0f766e;
  font-size: 12px;
  font-weight: 800;
}

.recharge-hero h2 {
  margin: 8px 0;
  color: #10202f;
  font-size: 26px;
}

.recharge-hero p {
  margin: 0;
  color: #667781;
}

.recharge-card {
  max-width: 760px;
  padding: 26px 24px;
}

.recharge-card :deep(.el-input-number) {
  width: 220px;
}
</style>
