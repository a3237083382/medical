# Task Plan: 保险公司医疗查询 Mock 与外网切换

## Goal
在不改变对外接口契约的前提下，先把当前实时查询接入改成可用的本地模拟数据源，并保留以后回到 `38090c6` 基线后接入数产外网接口的路径。

## Current Phase
Phase 5, completed for the current mock and name display fix

## Phases

### Phase 1: 需求确认与基线冻结
- [x] 固化当前基线为 `38090c6`
- [x] 明确 mock 版与外网版的切换方式
- [x] 记录当前接口契约与返回结构
- **Status:** completed

### Phase 2: 方案设计
- [x] 确定数据源抽象边界
- [x] 确定配置项与切换开关
- [x] 确定 mock 数据生成策略
- **Status:** completed

### Phase 3: Mock 接口实现
- [x] 实现本地 mock 数据源
- [x] 对齐返回字段与脱敏规则
- [x] 保持现有控制器和计费流程不变
- **Status:** completed

### Phase 4: 外网切换预案
- [ ] 明确以后如何回到 `38090c6`
- [ ] 明确外网接口接入时只改数据源适配层
- [ ] 写出切换步骤
- **Status:** pending

### Phase 5: 验证与交付
- [x] 验证 mock 返回可跑通前端
- [ ] 验证切换回基线的操作路径
- [x] 整理交付说明
- **Status:** completed

## Key Questions
1. mock 返回是否必须 100% 贴合数产外网最终格式
2. 后续外网接入时，是否只允许改配置，不动控制器与计费逻辑

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| 保留 `38090c6` 作为外网接入基线 | 方便以后回退和重新开分支 |
| mock 与外网共用同一对外接口 | 客户侧调用方式不变 |
| 外部接口变化只收敛到数据源适配层 | 降低后续切换成本 |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
|       | 1       |            |

## Notes
- `medical_all` 当前默认路由为 `mock`，以后外网接入只需将该路由改回 `digital` 并配置数产正式地址。
- 实时 Mock 数据脚本为 `RuoYi-Vue/sql/medical_realtime_mock.sql`，示例身份证号为 `320683198312120713`。
- 以后接外网时，优先从 `38090c6` 重新开分支。
- 姓名显示修复：仅提交身份证号时，实时查询成功后从数据源结果回写姓名；已有请求姓名不会被覆盖。
- 部署验证前需停止当前运行中的后端进程，再重新生成并替换 `ruoyi-admin.jar`。
- 2026-08-29 验证：`ruoyi-business` 84 项测试全部通过；若 Maven 仍报 `Unable to rename ... ruoyi-admin.jar`，先停止 IntelliJ 中运行的后端再执行 `package`。
- 2026-08-29 验证：`ruoyi-admin` 32 项测试全部通过；当前剩余打包注意事项仍是停止占用 `ruoyi-admin.jar` 的后端进程。

## 2026-08-29 管理前端品牌与导航清理

### Goal
将管理前端可见的若依品牌内容改为医疗数据管理系统，并隐藏默认若依导航入口。

### Design
- 使用 `VITE_APP_TITLE` 统一控制侧边栏 Logo 和浏览器标题。
- 使用 `settings.js` 页脚配置清理默认 RuoYi 版权标识。
- 在 `Navbar.vue` 移除源码/文档入口，右上角固定展示 `管理员`。
- 在权限菜单生成后做前端显示过滤，仅隐藏 `首页`、`系统管理`、`系统监控`、`系统工具`、`若依官网`，不改后端权限和实际路由。
- `TagsView` 单独隐藏 `/index`，避免首页作为固定页签继续显示。

### Implementation Plan
- [x] 增加 `RuoYi-Vue3/scripts/verify-admin-branding.mjs`，先验证旧状态失败。
- [x] 修改三个环境文件标题为 `医疗数据管理系统`。
- [x] 修改页脚版权品牌为 `医疗数据管理系统`。
- [x] 修改 `Navbar.vue` 的顶部入口和用户名展示。
- [x] 修改 `permission.js` 的导航过滤。
- [x] 修改 `TagsView/index.vue` 隐藏首页页签。
- [x] 运行品牌验证脚本和生产构建。

### Verification
- `node scripts/verify-admin-branding.mjs`：通过。
- `npm run build:prod`：通过，Vite 成功构建 2574 个模块。
- `npm run dev -- --host 127.0.0.1 --port 3001`：开发服务已启动，`http://127.0.0.1:3001/` 可访问。
- `Invoke-WebRequest http://127.0.0.1:3001/`：HTML 标题为 `医疗数据管理系统`。

### Follow-up: 移除首页默认内容
- [x] 将 `scripts/verify-admin-branding.mjs` 增加首页默认若依内容清理断言。
- [x] 将 `src/views/index.vue` 改为空白首页容器，移除若依介绍、技术选型、联系信息、更新日志、捐赠支持和外链按钮。
- [x] 运行品牌验证脚本和生产构建。

### Follow-up Verification
- `node scripts/verify-admin-branding.mjs`：通过。
- `npm run build:prod`：通过，Vite 成功构建 2572 个模块。

### Follow-up: 不再保留管理端首页
- [x] 删除管理端首页组件 `RuoYi-Vue3/src/views/index.vue`。
- [x] 将管理端根路径默认入口从 `/index` 改为 `/business/company`。
- [x] 保留隐藏兼容重定向：访问 `/index` 自动跳转 `/business/company`，避免旧地址直接 404。
- [x] 将 `/noRedirect` 兼容重定向到 `/business/company`，避免浏览器停留旧面包屑占位地址时进入 404。
- [x] 移除面包屑自动前置 `首页` 的逻辑。
- [x] 将退出登录、会话过期、锁屏兜底、401/404 返回入口改为保险公司管理。
- [x] 更新品牌验证脚本覆盖首页路由移除。

### Follow-up Verification
- 2026-08-30 `node scripts/verify-admin-branding.mjs`：通过。
- 2026-08-30 `npm run build:prod`：通过，Vite 成功构建 2571 个模块，耗时 42.88s。
- 2026-08-30 `npm run dev -- --host 127.0.0.1 --port 3001`：3001 被占用后自动启动到 `http://127.0.0.1:3002/`。
- 2026-08-30 `Invoke-WebRequest http://127.0.0.1:3002/`：HTML 标题为 `医疗数据管理系统`。
