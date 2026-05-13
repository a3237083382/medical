# 保险公司开放接口接入 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the mistaken company-side manual medical query page with a B2B API access flow: documentation in the company portal plus a signed open API that authenticates, prices, logs, and returns sample desensitized data.

**Architecture:** Keep `/company/query` as the company portal route, but render it as “接口接入” documentation. Move actual querying to `POST /open/api/medical/query`, authenticated by `AppKey + AppSecret` request signing. Reuse `biz_query_price` for available query abilities and `biz_query_log` for immutable query records.

**Tech Stack:** RuoYi Spring Boot 3 / Java 17 / MyBatis XML / Vue 3 / Element Plus / Vite.

---

## File Structure

- Modify `RuoYi-Vue3/scripts/company-query-page.test.mjs`: change the regression test from “manual query exists” to “API access docs exist and manual query controls do not exist”.
- Modify `RuoYi-Vue3/scripts/company-login-target.test.mjs`: keep `/company/query` as an allowed iframe target.
- Modify `RuoYi-Vue3/src/views/company/query.vue`: replace form/result UI with API access documentation, AppKey display, available interface price table, examples, and quick links.
- Modify `RuoYi-Vue3/src/layout/company/index.vue`: rename the menu/tab label from “医疗查询” to “接口接入”.
- Modify `RuoYi-Vue3/src/api/business/portal.js`: remove `submitMedicalQuery`; keep `listMedicalQueryTypes`.
- Modify `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/business/CompanyMedicalQueryController.java`: keep only `GET /company/api/medical/query-types`; remove the company-login manual query endpoint.
- Create `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/business/OpenMedicalQueryController.java`: implement `POST /open/api/medical/query` with AppKey signature auth, query price lookup, log writing, and sample desensitized response.
- Modify `RuoYi-Vue/ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java`: permit `/open/api/**` so open API requests do not require admin JWT.
- Modify docs `docs/开发计划.md` and `docs/phase4-验收记录.md`: describe the “接口接入 + 开放 API” model instead of page manual query.

---

### Task 1: Frontend Contract Test For API Access Page

**Files:**
- Modify: `RuoYi-Vue3/scripts/company-query-page.test.mjs`

- [ ] **Step 1: Write the failing test**

Replace the current assertions with checks that `/company/query` is an API access page and no longer a manual query page:

```js
assert.match(router, /path:\s*['"]query['"]/)
assert.match(router, /import\(['"]@\/views\/company\/query['"]\)/)

assert.match(layout, /index="\/company\/query"/)
assert.match(layout, /name="\/company\/query"/)
assert.match(layout, />接口接入</)
assert.doesNotMatch(layout, />医疗查询</)

const queryPage = read("src/views/company/query.vue")
assert.match(queryPage, /接口接入/)
assert.match(queryPage, /X-App-Key/)
assert.match(queryPage, /X-Timestamp/)
assert.match(queryPage, /X-Nonce/)
assert.match(queryPage, /X-Sign/)
assert.match(queryPage, /POST \/open\/api\/medical\/query/)
assert.match(queryPage, /AppSecret 不在门户页面展示/)
assert.doesNotMatch(queryPage, /请输入姓名/)
assert.doesNotMatch(queryPage, /请输入身份证号/)
assert.doesNotMatch(queryPage, /submitMedicalQuery/)

assert.match(portalApi, /url:\s*['"]\/company\/api\/medical\/query-types['"]/)
assert.doesNotMatch(portalApi, /url:\s*['"]\/company\/api\/medical\/query['"]/)
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
cd RuoYi-Vue3
node scripts/company-query-page.test.mjs
```

Expected: fail because the current page still contains “医疗查询”, name/id-card inputs, `submitMedicalQuery`, and the layout label still says “医疗查询”.

---

### Task 2: Replace `/company/query` With API Access Documentation

**Files:**
- Modify: `RuoYi-Vue3/src/views/company/query.vue`
- Modify: `RuoYi-Vue3/src/layout/company/index.vue`
- Modify: `RuoYi-Vue3/src/api/business/portal.js`

- [ ] **Step 1: Update API module**

Remove this export from `portal.js`:

```js
export function submitMedicalQuery(data) {
  return request({
    url: '/company/api/medical/query',
    method: 'post',
    data
  })
}
```

Keep:

```js
export function listMedicalQueryTypes() {
  return request({
    url: '/company/api/medical/query-types',
    method: 'get'
  })
}
```

- [ ] **Step 2: Update menu labels**

In `src/layout/company/index.vue`, change both company sidebar and embedded tab labels:

```vue
<el-menu-item index="/company/query">
  <el-icon><Connection /></el-icon>
  <span>接口接入</span>
</el-menu-item>

<el-tab-pane label="接口接入" name="/company/query" />
```

- [ ] **Step 3: Replace query page**

Replace `src/views/company/query.vue` with an API access documentation page that:

```vue
<script setup name="CompanyQuery">
import { getCompanyProfile, listMedicalQueryTypes } from "@/api/business/portal"

const loading = ref(false)
const company = ref({})
const queryTypes = ref([])
const endpoint = `${window.location.origin}/dev-api/open/api/medical/query`

const headerRows = [
  { name: "X-App-Key", desc: "保险公司 AppKey，每家公司唯一" },
  { name: "X-Timestamp", desc: "当前毫秒时间戳" },
  { name: "X-Nonce", desc: "每次请求唯一随机字符串" },
  { name: "X-Sign", desc: "SHA256(appKey + timestamp + nonce + body + appSecret)" }
]

const requestExample = `{
  "queryType": "hospital_visit",
  "name": "张三",
  "idCard": "430102199001011234"
}`

const responseExample = `{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "queryType": "hospital_visit",
    "queryName": "门诊住院记录查询",
    "fee": 10.00,
    "name": "张*",
    "idCard": "4301**********1234",
    "summary": "未发现影响承保的高风险医疗记录",
    "records": []
  }
}`

function loadData() {
  loading.value = true
  Promise.all([getCompanyProfile(), listMedicalQueryTypes()]).then(([profileRes, priceRes]) => {
    company.value = profileRes.data || {}
    queryTypes.value = priceRes.data || []
  }).finally(() => { loading.value = false })
}

function formatMoney(val) {
  if (!val) return "0.00"
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",")
}

loadData()
</script>
```

The template must render:

```vue
<template>
  <div class="app-container" v-loading="loading">
    <el-card shadow="hover">
      <template #header><span>接口接入</span></template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="AppKey">{{ company.appKey || "-" }}</el-descriptions-item>
        <el-descriptions-item label="接口地址">{{ endpoint }}</el-descriptions-item>
        <el-descriptions-item label="密钥说明">AppSecret 不在门户页面展示，由管理员通过安全渠道交付或重置。</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="hover" style="margin-top:16px">
      <template #header><span>签名请求头</span></template>
      <el-table :data="headerRows" border>
        <el-table-column label="请求头" prop="name" width="180" />
        <el-table-column label="说明" prop="desc" />
      </el-table>
    </el-card>

    <el-card shadow="hover" style="margin-top:16px">
      <template #header><span>可用接口与价目</span></template>
      <el-table :data="queryTypes" border stripe>
        <el-table-column label="queryType" prop="queryType" width="220" />
        <el-table-column label="接口名称" prop="queryName" />
        <el-table-column label="单次费用" width="140">
          <template #default="{ row }">{{ formatMoney(row.fee) }} 元/次</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header><span>请求示例</span></template>
          <pre class="code-block">{{ requestExample }}</pre>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header><span>返回示例</span></template>
          <pre class="code-block">{{ responseExample }}</pre>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
```

- [ ] **Step 4: Run frontend contract test**

Run:

```powershell
cd RuoYi-Vue3
node scripts/company-query-page.test.mjs
```

Expected: pass.

---

### Task 3: Backend Contract Test For Open API Shape

**Files:**
- Create: `RuoYi-Vue3/scripts/open-api-contract.test.mjs`

- [ ] **Step 1: Write the failing test**

Create:

```js
import assert from "node:assert/strict"
import { readFileSync } from "node:fs"
import { resolve } from "node:path"

const repo = resolve(import.meta.dirname, "..", "..")

function read(path) {
  return readFileSync(resolve(repo, path), "utf8")
}

const openController = read("RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/business/OpenMedicalQueryController.java")
const companyController = read("RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/business/CompanyMedicalQueryController.java")
const securityConfig = read("RuoYi-Vue/ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java")

assert.match(openController, /@RequestMapping\("\/open\/api\/medical"\)/)
assert.match(openController, /@PostMapping\("\/query"\)/)
assert.match(openController, /X-App-Key/)
assert.match(openController, /X-Timestamp/)
assert.match(openController, /X-Nonce/)
assert.match(openController, /X-Sign/)
assert.match(openController, /selectBizInsuranceCompanyByAppKey/)
assert.match(openController, /selectBizQueryPriceByQueryType/)
assert.match(openController, /insertBizQueryLog/)
assert.match(openController, /maskIdCard/)

assert.doesNotMatch(companyController, /@PostMapping\("\/query"\)/)
assert.match(securityConfig, /\/open\/api\/\*\*/)
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
cd RuoYi-Vue3
node scripts/open-api-contract.test.mjs
```

Expected: fail because `OpenMedicalQueryController.java` does not exist and `CompanyMedicalQueryController` still has `@PostMapping("/query")`.

---

### Task 4: Implement Signed Open API

**Files:**
- Create: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/business/OpenMedicalQueryController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/business/CompanyMedicalQueryController.java`
- Modify: `RuoYi-Vue/ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java`

- [ ] **Step 1: Remove company-login manual query endpoint**

In `CompanyMedicalQueryController.java`, keep only:

```java
@RestController
@RequestMapping("/company/api/medical")
public class CompanyMedicalQueryController extends BaseController
{
    @Autowired
    private IBizQueryPriceService priceService;

    @GetMapping("/query-types")
    public AjaxResult queryTypes()
    {
        BizQueryPrice filter = new BizQueryPrice();
        filter.setStatus("0");
        return AjaxResult.success(priceService.selectBizQueryPriceList(filter));
    }
}
```

- [ ] **Step 2: Add security permit**

In `SecurityConfig.java`, add `/open/api/**` to anonymous permit rules near `/company/login` and `/company/api/**` rules:

```java
.requestMatchers("/company/login", "/open/api/**").permitAll()
```

If the existing file uses a different `requestMatchers` layout, add only `/open/api/**` to the current permit list.

- [ ] **Step 3: Create open controller**

Implement `OpenMedicalQueryController.java` with these required imports and fields:

```java
@RestController
@RequestMapping("/open/api/medical")
public class OpenMedicalQueryController extends BaseController
{
    private static final long SIGN_EXPIRE_MILLIS = 5 * 60 * 1000L;

    @Autowired
    private IBizInsuranceCompanyService companyService;

    @Autowired
    private IBizQueryPriceService priceService;

    @Autowired
    private IBizQueryLogService queryLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/query")
    public AjaxResult query(@RequestBody Map<String, String> params, HttpServletRequest request)
    {
        String body = toJson(params);
        BizInsuranceCompany company = authenticate(request, body);
        if (company == null)
        {
            return AjaxResult.error(401, "签名校验失败");
        }
        if ("1".equals(company.getStatus()))
        {
            return AjaxResult.error(403, "保险公司账号已停用");
        }

        String queryType = trim(params.get("queryType"));
        String name = trim(params.get("name"));
        String idCard = trim(params.get("idCard"));
        if (StringUtils.isEmpty(queryType) || StringUtils.isEmpty(name) || StringUtils.isEmpty(idCard))
        {
            recordLog(company.getId(), queryType, name, idCard, BigDecimal.ZERO, "1", "请求参数缺失");
            return AjaxResult.error(400, "queryType、name、idCard不能为空");
        }

        BizQueryPrice price = priceService.selectBizQueryPriceByQueryType(queryType);
        if (price == null || !"0".equals(price.getStatus()))
        {
            recordLog(company.getId(), queryType, name, idCard, BigDecimal.ZERO, "1", "查询类型不存在或已停用");
            return AjaxResult.error(400, "查询类型不存在或已停用");
        }

        recordLog(company.getId(), queryType, name, idCard, price.getFee(), "0", "开放接口查询成功，费用待结算");
        return AjaxResult.success("查询成功", buildResult(price, name, idCard));
    }
}
```

The helper methods must include:

```java
private BizInsuranceCompany authenticate(HttpServletRequest request, String body)
private boolean isExpired(String timestamp)
private String sha256(String value)
private String toJson(Map<String, String> params)
private void recordLog(Long companyId, String queryType, String name, String idCard, BigDecimal fee, String status, String remark)
private Map<String, Object> buildResult(BizQueryPrice price, String name, String idCard)
private String maskName(String value)
private String maskIdCard(String value)
private String trim(String value)
```

`authenticate` must read:

```java
String appKey = request.getHeader("X-App-Key");
String timestamp = request.getHeader("X-Timestamp");
String nonce = request.getHeader("X-Nonce");
String sign = request.getHeader("X-Sign");
```

Then it must:

```java
if any header is empty -> return null
if timestamp expired -> return null
company = companyService.selectBizInsuranceCompanyByAppKey(appKey)
if company missing -> return null
expected = sha256(appKey + timestamp + nonce + body + company.getAppSecret())
return expected.equalsIgnoreCase(sign) ? company : null
```

- [ ] **Step 4: Run backend contract test**

Run:

```powershell
cd RuoYi-Vue3
node scripts/open-api-contract.test.mjs
```

Expected: pass.

---

### Task 5: Update Docs

**Files:**
- Modify: `docs/开发计划.md`
- Modify: `docs/phase4-验收记录.md`

- [ ] **Step 1: Update roadmap wording**

In `docs/开发计划.md`, update Phase 4 company portal items so they say:

```markdown
- [X] 4.18.1 将 `/company/query` 改为接口接入页：展示 AppKey、签名规则、接口地址、价目列表和调用示例，不提供页面手工查询
- [X] 4.18.2 新增开放接口框架 `POST /open/api/medical/query`：支持 AppKey/AppSecret 签名、查价、日志留痕和示例脱敏返回
```

- [ ] **Step 2: Update acceptance record**

In `docs/phase4-验收记录.md`, replace any wording implying manual medical query with:

```markdown
- `/company/query` 为接口接入页，不承载实际医疗查询操作。
- 保险公司按接口文档调用 `POST /open/api/medical/query`。
- 开放接口本阶段返回标准示例脱敏数据，真实医院/卫健委数据源后续接入。
```

---

### Task 6: Verification

**Files:**
- No production file edits.

- [ ] **Step 1: Run frontend contract tests**

Run:

```powershell
cd RuoYi-Vue3
node scripts/company-login-target.test.mjs
node scripts/company-query-page.test.mjs
node scripts/open-api-contract.test.mjs
```

Expected: all commands exit `0`.

- [ ] **Step 2: Run frontend production build**

Run:

```powershell
cd RuoYi-Vue3
npm run build:prod
```

Expected: Vite build completes successfully.

- [ ] **Step 3: Run backend compile**

Run:

```powershell
cd RuoYi-Vue
$env:JAVA_HOME='D:\Program Files (x86)\jdk\jdk17'
$env:MAVEN_OPTS='-Xmx512m -XX:MaxMetaspaceSize=256m'
mvn -pl ruoyi-business,ruoyi-admin -am compile -DskipTests
```

Expected: reactor summary shows `ruoyi-admin SUCCESS` and `BUILD SUCCESS`.

- [ ] **Step 4: Run HTTP verification against local dev server**

After backend and frontend are running, verify:

```powershell
Invoke-WebRequest -UseBasicParsing -Uri 'http://127.0.0.1:18080/company/query'
```

Expected: HTTP `200` and page body contains “接口接入”, not “请输入姓名”.

Call without signature:

```powershell
Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:18080/dev-api/open/api/medical/query' -ContentType 'application/json' -Body '{"queryType":"hospital_visit","name":"张三","idCard":"430102199001011234"}'
```

Expected: `code=401`.

Call with correct signature using the known test company `test001/123456`, its `AppKey`, and database `AppSecret`. Expected: `code=200`, desensitized `name` and `idCard`, and latest `/company/api/query-log/list` item has `status=0`.

