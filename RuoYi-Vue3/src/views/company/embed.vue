<template>
  <div class="embed-entry">
    <div class="loading-box">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在进入保险公司登录</span>
    </div>
  </div>
</template>

<script setup name="CompanyEmbed">
import { getCompanyToken, setCompanyEmbedMode } from "@/utils/companyAuth"
import { resolveCompanyEmbedMode, resolveCompanyLoginTarget } from "@/utils/companyLoginTarget"

const route = useRoute()
const router = useRouter()
const target = resolveCompanyLoginTarget(route.query)

setCompanyEmbedMode(resolveCompanyEmbedMode(route.query))
router.replace(getCompanyToken()
  ? target
  : { path: "/company/login", query: { embedMode: route.query.embedMode || "iframe", target } })
</script>

<style scoped>
.embed-entry {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f6f8fb;
}

.loading-box {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #344054;
  font-size: 14px;
}
</style>
