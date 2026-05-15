# Phase 4 验收记录

更新时间：2026-05-13

## 本次完成范围

- 管理后台移除“嵌入链接”操作，管理员不再为保险公司生成专属嵌入地址。
- 嵌入入口改为保险公司自助登录：`/company/login?embedMode=iframe&target=/company/query`。
- 保险公司在 iframe/WebView 内使用自己的账号密码登录，登录后进入公司端门户。
- `/company/query` 保留路径，但页面改为“接口接入”，用于展示 AppKey、开放接口地址、签名请求头、请求/响应示例和 queryType 价目列表。
- 保险公司不在门户页面手工输入姓名/身份证查询；实际医疗查询由保险公司系统按接口文档调用开放 API。
- 新增开放接口 `POST /open/api/medical/query`，支持 `X-App-Key`、`X-Timestamp`、`X-Nonce`、`X-Sign` 签名校验。
- 开放接口根据 `queryType` 读取后台价目配置，成功查询后写入 `biz_query_log`；余额扣减和 `biz_fee_flow` 由若依定时任务周期结算统一生成。
- 当前阶段暂不接真实医院/卫健委数据源，接口返回标准示例脱敏数据，后续只替换真实数据查询实现。
- `/company/embed` 保留为兼容入口，访问时跳转到公司登录页，不再兑换 ticket。
- 嵌入模式下使用 `sessionStorage` 保存公司端 token 和必要公司信息，不把 AppKey/AppSecret 或管理员 token 放入 URL。
- 企业端补齐费用流水只读页面，只查询当前 token 绑定公司的流水。
- `public/company-iframe-test.html` 默认加载固定公司登录嵌入地址，便于本地 B/S iframe 验证。

## 接口调用说明

保险公司对接时由管理员开户并交付账号密码、AppKey 和 AppSecret。

- AppKey：每家公司唯一，可在公司门户“接口接入”页查看。
- AppSecret：每家公司唯一，只作为服务端签名密钥，不在公司门户长期展示。
- 请求地址：`POST /open/api/medical/query`
- 签名规则：`SHA256(appKey + timestamp + nonce + body + appSecret)`
- 时间戳：`X-Timestamp` 使用当前毫秒时间戳，服务端允许 5 分钟窗口。

请求体示例：

```json
{
  "queryType": "OUTPATIENT",
  "name": "张三",
  "idCard": "430102199001011234"
}
```

## 测试记录

- 前端登录目标测试：`node scripts/company-login-target.test.mjs`。
- 前端接口接入页契约测试：`node scripts/company-query-page.test.mjs`。
- 后端开放接口契约测试：`node scripts/open-api-contract.test.mjs`。
- 前端生产构建：`npm run build:prod`。
- 后端编译：`mvn -pl ruoyi-business,ruoyi-admin -am compile -DskipTests`。
- 接口验证：无签名请求应返回 `401 INVALID_SIGNATURE`；正确签名且余额大于等于 `0` 时返回成功示例数据，并写入查询日志；费用流水由周期结算任务统一生成。

## C/S 兼容验证方案

当前嵌入地址固定为公司登录入口，WebView、CEF、Edge WebView2 和外部浏览器使用同一套地址：

```text
/company/login?embedMode=iframe&target=/company/query
```

现场验收时按以下清单执行：

1. 在 WebView/CEF/Edge WebView2 或本地测试壳中打开固定嵌入地址。
2. 使用保险公司账号密码登录。
3. 验证登录后进入“接口接入”页，并保持顶部标签导航的嵌入模式。
4. 验证账户概览、充值申请、充值记录、查询记录、费用流水、资料信息可切换。
5. 验证门户只展示 AppKey，不展示 AppSecret。
6. 验证只能看到当前登录保险公司的数据。

## 未做现场验证

- 本机未接入真实 C/S WebView/CEF/Edge WebView2 测试壳，仍需在客户侧或测试壳中执行现场验收。
- 当前开放接口返回标准示例脱敏数据，真实医院/卫健委数据源接入属于后续阶段。
