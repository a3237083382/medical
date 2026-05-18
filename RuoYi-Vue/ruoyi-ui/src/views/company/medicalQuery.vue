<template>
  <div class="medical-query-page">
    <div class="query-header">
      <div class="brand">
        <div class="brand-icon">医</div>
        <div>
          <div class="brand-title">医疗信息查询平台</div>
          <div class="brand-subtitle">保险公司嵌入式查询模块</div>
        </div>
      </div>
      <div class="company-state">
        <span v-if="companyInfo.companyName">已登录：{{ companyInfo.companyName }}</span>
        <span v-else>待登录</span>
      </div>
    </div>

    <section class="login-section">
      <div class="section-head">
        <h2>保险公司账号登录</h2>
        <span>登录后调用我方接口查询投保人医疗信息</span>
      </div>
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入保险公司账号" autocomplete="off" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="off"
            show-password
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        <el-form-item class="login-action">
          <el-button type="primary" :loading="loginLoading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </section>

    <div class="workbench" :class="{ disabled: !loggedIn }">
      <section class="query-section">
        <div class="section-head">
          <h2>投保人医疗信息查询</h2>
          <span>选择接口并输入投保人信息</span>
        </div>
        <el-form ref="queryForm" :model="queryForm" :rules="queryRules" label-position="top" class="query-form">
          <el-form-item label="查询接口" prop="queryType">
            <el-select v-model="queryForm.queryType" placeholder="请选择查询接口" filterable>
              <el-option
                v-for="item in queryTypes"
                :key="item.queryType"
                :label="item.queryName || item.queryType"
                :value="item.queryType"
              >
                <span>{{ item.queryName || item.queryType }}</span>
                <span class="option-fee">￥{{ formatMoney(item.fee) }}</span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="投保人姓名" prop="name">
            <el-input v-model="queryForm.name" placeholder="请输入姓名" autocomplete="off" />
          </el-form-item>

          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="queryForm.idCard" placeholder="请输入18位身份证号" maxlength="18" autocomplete="off" />
          </el-form-item>

          <div class="fee-line">
            <span>本次查询费用</span>
            <strong>￥{{ formatMoney(selectedFee) }}</strong>
          </div>

          <div class="actions">
            <el-button type="primary" :loading="queryLoading" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">清空</el-button>
          </div>
        </el-form>
      </section>

      <section class="result-section">
        <div class="section-head">
          <h2>查询结果</h2>
          <span>{{ resultTime || '等待查询' }}</span>
        </div>

        <div v-if="!result" class="empty-result">
          <div class="empty-icon">查</div>
          <p>登录后输入投保人信息，查询结果会显示在这里。</p>
        </div>

        <div v-else class="result-body">
          <div class="metric-grid">
            <div class="metric">
              <span>查询接口</span>
              <strong>{{ result.queryName || result.queryType || '-' }}</strong>
            </div>
            <div class="metric">
              <span>姓名</span>
              <strong>{{ result.name || result.patientName || maskedName }}</strong>
            </div>
            <div class="metric">
              <span>身份证号</span>
              <strong>{{ result.idCard || maskedIdCard }}</strong>
            </div>
            <div class="metric">
              <span>本次费用</span>
              <strong>￥{{ formatMoney(result.fee || selectedFee) }}</strong>
            </div>
          </div>

          <div class="summary">
            {{ result.summary || '查询已完成，正式环境以数据源返回结果为准。' }}
          </div>

          <el-table :data="resultRecords" border>
            <el-table-column prop="name" label="项目" width="160" />
            <el-table-column prop="value" label="结果" width="160" />
            <el-table-column prop="remark" label="说明" />
          </el-table>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { companyLogin, listMedicalQueryTypes, queryMedical } from '@/api/business/portal'

export default {
  name: 'CompanyMedicalQuery',
  data() {
    return {
      loginLoading: false,
      queryLoading: false,
      loggedIn: false,
      companyInfo: {},
      queryTypes: [],
      result: null,
      resultTime: '',
      loginForm: {
        username: '',
        password: ''
      },
      queryForm: {
        queryType: '',
        name: '',
        idCard: ''
      },
      loginRules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      queryRules: {
        queryType: [{ required: true, message: '请选择查询接口', trigger: 'change' }],
        name: [{ required: true, message: '请输入投保人姓名', trigger: 'blur' }],
        idCard: [
          { required: true, message: '请输入身份证号', trigger: 'blur' },
          { pattern: /^[0-9]{17}[0-9Xx]$/, message: '请输入正确的18位身份证号', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    selectedFee() {
      const current = this.queryTypes.find(item => item.queryType === this.queryForm.queryType)
      return current && current.fee ? current.fee : 0
    },
    maskedName() {
      const name = this.queryForm.name || ''
      return name.length <= 1 ? '*' : name.substring(0, 1) + '*'
    },
    maskedIdCard() {
      const value = this.queryForm.idCard || ''
      if (value.length < 8) {
        return '****'
      }
      return value.substring(0, 4) + '**********' + value.substring(value.length - 4)
    },
    resultRecords() {
      const records = this.result && this.result.records
      if (Array.isArray(records)) {
        return records
      }
      return [
        { name: '医疗风险汇总', value: this.result && this.result.riskLevel ? this.result.riskLevel : '低风险', remark: '未命中样例高风险医疗记录' },
        { name: '数据处理', value: '已脱敏', remark: '姓名、身份证和诊断信息按平台规则脱敏展示' }
      ]
    }
  },
  created() {
    this.restoreCompanySession()
  },
  methods: {
    restoreCompanySession() {
      const token = sessionStorage.getItem('companyToken') || localStorage.getItem('companyToken')
      const infoText = sessionStorage.getItem('companyInfo') || localStorage.getItem('companyInfo')
      if (!token || !infoText) {
        return
      }
      try {
        this.companyInfo = JSON.parse(infoText)
        this.loggedIn = true
        this.loadQueryTypes()
      } catch (e) {
        sessionStorage.removeItem('companyInfo')
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) {
          return
        }
        this.loginLoading = true
        companyLogin(this.loginForm).then(res => {
          const data = res.data || {}
          sessionStorage.setItem('companyToken', data.token)
          sessionStorage.setItem('companyInfo', JSON.stringify(data))
          this.companyInfo = data
          this.loggedIn = true
          this.$message.success('登录成功')
          this.loadQueryTypes()
        }).finally(() => {
          this.loginLoading = false
        })
      })
    },
    loadQueryTypes() {
      listMedicalQueryTypes().then(res => {
        this.queryTypes = res.data || []
        if (!this.queryForm.queryType && this.queryTypes.length > 0) {
          this.queryForm.queryType = this.queryTypes[0].queryType
        }
      })
    },
    handleQuery() {
      this.$refs.queryForm.validate(valid => {
        if (!valid) {
          return
        }
        this.queryLoading = true
        this.result = null
        queryMedical({
          queryType: this.queryForm.queryType,
          queryParams: {
            name: this.queryForm.name,
            idCard: this.queryForm.idCard
          }
        }).then(res => {
          const data = res.data || {}
          this.result = Object.assign({}, data.data || {}, {
            fee: data.fee,
            queryType: this.queryForm.queryType
          })
          this.resultTime = new Date().toLocaleString('zh-CN', { hour12: false })
        }).finally(() => {
          this.queryLoading = false
        })
      })
    },
    resetQuery() {
      this.$refs.queryForm.resetFields()
      this.result = null
      this.resultTime = ''
      if (this.queryTypes.length > 0) {
        this.queryForm.queryType = this.queryTypes[0].queryType
      }
    },
    formatMoney(value) {
      const number = Number(value || 0)
      return number.toFixed(2)
    }
  }
}
</script>

<style scoped>
.medical-query-page {
  min-height: 100vh;
  background: #fff;
  color: #172033;
}

.query-header {
  min-height: 64px;
  padding: 0 24px;
  border-bottom: 1px solid #d9e2ef;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  color: #0d4385;
  background: #e8f1fb;
  font-weight: 900;
}

.brand-title {
  font-size: 17px;
  font-weight: 700;
}

.brand-subtitle,
.company-state,
.section-head span {
  color: #667085;
  font-size: 12px;
}

.login-section,
.query-section,
.result-section {
  border: 1px solid #d9e2ef;
  border-radius: 8px;
  background: #fff;
}

.login-section {
  margin: 20px 24px 0;
}

.section-head {
  min-height: 54px;
  padding: 0 18px;
  border-bottom: 1px solid #d9e2ef;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-head h2 {
  margin: 0;
  font-size: 16px;
}

.login-form {
  padding: 16px 18px;
  display: grid;
  grid-template-columns: minmax(180px, 1fr) minmax(180px, 1fr) auto;
  gap: 12px;
  align-items: end;
}

.login-action {
  margin-bottom: 22px;
}

.workbench {
  padding: 18px 24px 24px;
  display: grid;
  grid-template-columns: minmax(320px, 420px) minmax(0, 1fr);
  gap: 16px;
}

.workbench.disabled {
  opacity: 0.5;
  pointer-events: none;
}

.query-form {
  padding: 18px;
}

.option-fee {
  float: right;
  color: #b7791f;
  margin-left: 24px;
}

.fee-line {
  min-height: 42px;
  padding: 0 12px;
  margin-bottom: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 7px;
  background: #f5f8fc;
  color: #667085;
}

.fee-line strong {
  color: #b7791f;
  font-size: 16px;
}

.actions {
  display: flex;
  gap: 10px;
}

.empty-result {
  min-height: 360px;
  display: grid;
  place-items: center;
  color: #667085;
  text-align: center;
  padding: 34px 20px;
}

.empty-icon {
  width: 62px;
  height: 62px;
  margin: 0 auto 14px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #edf4fc;
  color: #1957a6;
  font-size: 24px;
  font-weight: 900;
}

.result-body {
  padding: 18px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.metric {
  min-height: 78px;
  border: 1px solid #d9e2ef;
  border-radius: 8px;
  background: #fbfcfe;
  padding: 13px;
}

.metric span {
  display: block;
  color: #667085;
  font-size: 12px;
  margin-bottom: 8px;
}

.metric strong {
  display: block;
  font-size: 16px;
  overflow-wrap: anywhere;
}

.summary {
  border-left: 4px solid #168464;
  background: #effaf5;
  color: #165b47;
  padding: 12px 14px;
  margin-bottom: 16px;
  line-height: 1.7;
}

@media (max-width: 960px) {
  .login-form,
  .workbench {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .query-header,
  .section-head {
    align-items: flex-start;
    flex-direction: column;
    justify-content: center;
    padding: 14px 16px;
  }

  .login-section {
    margin: 12px 12px 0;
  }

  .workbench {
    padding: 12px;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
