# Progress Log

## Session: 2026-08-29

### 管理前端品牌与导航清理
- **Status:** completed
- Actions taken:
  - 将 `RuoYi-Vue3` 三个环境文件的 `VITE_APP_TITLE` 改为 `医疗数据管理系统`。
  - 从管理端顶部栏移除源码地址和文档地址图标入口。
  - 将右上角显示名固定为 `管理员`。
  - 将页脚版权品牌从默认 RuoYi 改为 `医疗数据管理系统`。
  - 在前端权限菜单生成后过滤 `首页`、`系统管理`、`系统监控`、`系统工具`、`若依官网` 导航项。
  - 在 TagsView 中隐藏 `/index`，避免首页固定页签继续显示。
  - 增加 `scripts/verify-admin-branding.mjs` 用于验证本次品牌和导航要求。
- Verification completed:
  - `node scripts/verify-admin-branding.mjs` passed.
  - `npm run build:prod` passed, Vite built 2574 modules.
  - Dev server started at `http://127.0.0.1:3001/`.
  - HTML title check returned `医疗数据管理系统`.
- Files created/modified:
  - `RuoYi-Vue3/.env.development`
  - `RuoYi-Vue3/.env.production`
  - `RuoYi-Vue3/.env.staging`
  - `RuoYi-Vue3/src/settings.js`
  - `RuoYi-Vue3/src/layout/components/Navbar.vue`
  - `RuoYi-Vue3/src/layout/components/TagsView/index.vue`
  - `RuoYi-Vue3/src/store/modules/permission.js`
  - `RuoYi-Vue3/scripts/verify-admin-branding.mjs`

### 移除首页默认若依内容
- **Status:** completed
- Actions taken:
  - 将管理端首页 `RuoYi-Vue3/src/views/index.vue` 缩减为空白容器。
  - 移除首页中的若依介绍、技术选型、联系信息、更新日志、捐赠支持和访问码云/访问主页按钮。
  - 扩展 `scripts/verify-admin-branding.mjs`，验证默认首页文案不再存在。
- Verification completed:
  - `node scripts/verify-admin-branding.mjs` passed.
  - `npm run build:prod` passed, Vite built 2572 modules.
- Files created/modified:
  - `RuoYi-Vue3/src/views/index.vue`
  - `RuoYi-Vue3/scripts/verify-admin-branding.mjs`

### 不再保留管理端首页
- **Status:** completed
- Actions taken:
  - 删除管理端首页组件 `RuoYi-Vue3/src/views/index.vue`。
  - 管理端根路径默认重定向改为 `/business/company`。
  - `/index` 改为隐藏兼容重定向到 `/business/company`，不再加载首页组件。
  - `/noRedirect` 改为隐藏兼容重定向到 `/business/company`，避免旧面包屑占位地址进入 404。
  - 移除面包屑自动添加 `首页` 的逻辑。
  - 退出登录、会话过期、锁屏兜底、401/404 返回入口统一改到保险公司管理。
  - 扩展 `scripts/verify-admin-branding.mjs` 验证首页路由和首页文案不再存在。
- Verification completed:
  - 2026-08-30 `node scripts/verify-admin-branding.mjs` passed.
  - 2026-08-30 `npm run build:prod` passed, Vite built 2571 modules in 42.88s.
  - 2026-08-30 Dev server started at `http://127.0.0.1:3002/` after port 3001 was already in use.
  - 2026-08-30 HTML title check for `http://127.0.0.1:3002/` returned `医疗数据管理系统`.
- Files created/modified:
  - `RuoYi-Vue3/src/router/index.js`
  - `RuoYi-Vue3/src/settings.js`
  - `RuoYi-Vue3/src/components/Breadcrumb/index.vue`
  - `RuoYi-Vue3/src/layout/components/Navbar.vue`
  - `RuoYi-Vue3/src/utils/request.js`
  - `RuoYi-Vue3/src/store/modules/lock.js`
  - `RuoYi-Vue3/src/views/error/401.vue`
  - `RuoYi-Vue3/src/views/error/404.vue`
  - `RuoYi-Vue3/scripts/verify-admin-branding.mjs`
  - `RuoYi-Vue3/src/views/index.vue` (deleted)

### Phase 1: Requirements & Baseline Freeze
- **Status:** in_progress
- **Started:** 2026-08-29 00:00
- Actions taken:
  - 复核了当前仓库结构和医疗查询调用链
  - 确认了 `medical.datasource` 配置入口
  - 确认了 `MockMedicalDataSource` 与 `DigitalIndustryDataSource` 已存在
  - 明确后续外网版以 `38090c6` 作为回退基线
  - 读取了 `index.html`，确认前端真实依赖的历史记录与详情结构
- Files created/modified:
  - `task_plan.md` (created)
  - `findings.md` (created)
  - `progress.md` (created)

### Phase 2: Planning & Structure
- **Status:** completed
- Actions taken:
  - 识别了 mock 接口必须对齐的前端字段
  - 识别了可直接复用的种子数据样例
- Files created/modified:
  - `findings.md`
  - `progress.md`

### Phase 3: Mock Realtime Implementation
- **Status:** completed
- Actions taken:
  - 将 `medical_all` 本地路由切换为 `mock`，避免实时查询访问内网地址。
  - 增加按身份证号命中的 Mock 查询映射，兼容前端提交的 `sfzhm/startdate/enddate` 契约。
  - 按 `张三B / 320683198312120713` 样例写入 1 条门诊和 3 条住院原始记录。
  - 增加实时结果深层脱敏，覆盖嵌套中文字段 `身份证号码`、`姓名` 和诊断字段。
- Files created/modified:
  - `RuoYi-Vue/ruoyi-admin/src/main/resources/application.yml`
  - `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/mapper/MockMedicalDataMapper.java`
  - `RuoYi-Vue/ruoyi-business/src/main/resources/mapper/business/MockMedicalDataMapper.xml`
  - `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/service/impl/MockMedicalDataSource.java`
  - `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/util/DesensitizeUtil.java`
  - `RuoYi-Vue/ruoyi-business/src/test/java/com/ruoyi/business/service/impl/MockMedicalDataSourceTest.java`
  - `RuoYi-Vue/ruoyi-business/src/test/java/com/ruoyi/business/util/DesensitizeUtilTest.java`
  - `RuoYi-Vue/sql/medical_realtime_mock.sql`

### Phase 5: Verification
- **Status:** completed
- Verification completed:
  - Mock data source unit test: 1 passed.
  - Deep desensitization unit test: 1 passed.
  - HTTP API against temporary backend before deep desensitization: `HIT`, fee `5.0`, 4 visits returned.
  - Full Maven compile/install reached compilation successfully; executable Jar repackage was blocked because the currently running backend holds `ruoyi-admin.jar`.
  - After aligning the stale `MedicalQueryServiceImplTest` budget assertions with the current company-balance implementation, `mvn -pl ruoyi-business test` passed all 84 tests.
  - After aligning `CompanyEmbedMedicalQueryControllerTest` fixtures with the current company-balance usage response, `mvn -pl ruoyi-admin test` passed all 32 tests.
  - Name backfill unit test: `Tests run: 1, Failures: 0, Errors: 0`.
  - The running backend must be restarted with the rebuilt Jar before the page can show the fix.

## Test Results
| Test | Input | Expected | Actual | Status |
|------|-------|----------|--------|--------|
| Maven install | `mvn -pl ruoyi-admin -am -DskipTests install` | 后端可编译并打包 | BUILD SUCCESS | Pass |
| Mock realtime unit test | `sfzhm=320683198312120713` | 命中并返回原始就诊记录 | 1 passed | Pass |
| Deep desensitization unit test | 中文嵌套 `身份证号码` | 返回 `320***********0713` | 断言通过 | Pass |
| Business module full tests | `mvn -pl ruoyi-business test` | 全部通过 | 原有 `MedicalQueryServiceImplTest` 5 项失败 | Existing test issue |
| Patient name backfill unit test | 仅提交身份证号的命中查询 | 写回数据源姓名 `张三` | 1 passed | Pass |

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-08-29 14:00 | `systemPath` 本地 jar 缺失导致 `ruoyi-business` 构建失败 | 1 | 移除失效的本地 jar 依赖，并将 `DigitalIndustryDataSource` 改为只依赖现有 Maven 组件 |
| 2026-08-29 18:02 | 原有 `MedicalQueryServiceImplTest` 未 stub `companyMapper.deductBalance`，默认返回 0 | 1 | 未修改无关业务；记录为既有测试缺陷 |
| 2026-08-29 18:09 | 中文嵌套身份证字段未被原有脱敏工具识别 | 1 | 增加实时结果深层脱敏和中文字段识别，并补测试 |
| 2026-08-29 21:36 | MySQL 8 导出文件无法导入服务器 MySQL 5.5 | 1 | 生成 `ry_vue_20260829_mysql55.sql`，转换排序规则和 JSON 字段，移除生成列及 MySQL 8 不兼容触发器；原始导出文件保留 |
| 2026-08-29 21:40 | MySQL 5.5 报 `1071 max key length is 767 bytes` | 1 | 将 `magic_api_file.file_path` 保持为 `varchar(512)` 但改用 `ascii` 字符集，保留完整路径并使主键索引兼容 |
| 2026-08-29 22:03 | Maven repackage 无法重命名 `ruoyi-admin.jar` | 1 | 编译和业务测试已通过；需停止当前后端后重新执行打包，避免覆盖运行中的 Jar |
| 2026-08-29 22:20 | `--offline package` 因业务测试断言过期而失败 | 1 | 更新测试 Stub/断言以匹配当前余额扣款实现，`ruoyi-business` 84 项测试全部通过 |
| 2026-08-29 22:43 | `ruoyi-admin` 测试因测试辅助数据未设置 `balance` 而失败 | 1 | 补充测试公司的余额字段，`ruoyi-admin` 32 项测试全部通过 |

## 5-Question Reboot Check
| Question | Answer |
|----------|--------|
| Where am I? | Phase 1 |
| Where am I going? | 设计 mock 方案，再进入实现 |
| What's the goal? | 先做本地 mock，同时保留回到 38090c6 的外网接入基线 |
| What have I learned? | See findings.md |
| What have I done? | See above |
