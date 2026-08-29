# Findings & Decisions

## Requirements
- 先做一个本地 mock 版本，让实时查询接口能出数据
- 返回结构尽量和外网真实接口一致
- 后续数产开通外网后，要能回到 `38090c6` 基线继续接入
- 对外调用方式尽量不变
- 管理前端需要把若依品牌改为 `医疗数据管理系统`
- 管理前端需要隐藏默认导航：`首页`、`系统管理`、`系统监控`、`系统工具`、`若依官网`
- 管理前端顶部栏需要移除源码/文档入口，右上角显示 `管理员`
- 管理前端不再保留默认首页，旧首页地址需要兼容跳到真实业务入口

## Research Findings
- 仓库里已经有 `MedicalDataSource`、`MedicalDataSourceRouter`、`MockMedicalDataSource`、`DigitalIndustryDataSource`
- `ruoyi-admin/src/main/resources/application.yml` 里已有 `medical.datasource` 配置
- 当前对外实时查询主要走 `CompanyEmbedMedicalQueryController`
- 业务层 `MedicalQueryServiceImpl` 已负责计费、日志、脱敏和结果落库
- `index.html` 里已经有完整的前端查询页面和结果详情渲染逻辑，可以直接反推 mock 接口返回结构
- 实时查询入口是 `/company/embed/medical/query`
- 历史列表入口是 `/company/embed/medical/history/requests`
- 延时查询入口是 `/company/embed/medical/delayed/requests` 和 `/company/embed/medical/batches`
- 前端明确依赖 `requestNo`、`serviceMode`、`queryType`、`name`、`idCard`、`queryTime`、`resultStatus`、`fee`
- 实时结果要有 `data.summary` 和 `data.visits[]`
- `visits[]` 里至少要有 `basicInfo`、`electronicMedicalRecord`、`medicalImaging`
- 延时结果要能展示 `insuranceCoverage` 和表格型详情
- 截图里的样例值可以直接做 seed，例如 `张三B`、`320683198312120713`、`张三A/C/D/E/F` 这些历史记录

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| 不单独建新仓库 | 这是同一套平台的不同数据源，不是独立产品 |
| 以后外网版从 `38090c6` 重新开分支 | 保留干净基线，便于回退 |
| mock 和外网统一到同一数据源接口 | 控制器和业务流程不需要重写 |
| 以 `index.html` 的结构和截图内容作为 mock 种子 | 直接贴合前端展示，不用额外猜字段 |
| 管理端导航清理在前端过滤层完成 | 当前运行菜单来自后端，前端过滤能立即隐藏入口且不破坏权限配置 |
| 首页页签在 TagsView 单独隐藏 | TagsView 从完整路由初始化，不能只依赖侧边栏过滤 |
| 管理端首页默认内容直接清空 | 用户要求去掉首页若依默认展示，空白容器是最小改动 |
| 管理端首页最终删除，`/index` 和 `/noRedirect` 仅保留隐藏重定向 | 用户明确不要首页；兼容旧地址和面包屑占位地址避免直接 404 |
| 管理端默认入口统一为 `/business/company` | 这是业务管理下第一个真实页面，删除首页后可作为落点 |

## Issues Encountered
| Issue | Resolution |
|-------|------------|
| 初始技能路径不明确 | 已改为读取现有 `planning-with-files` 技能 |
| `medical_all` 原路由为 `digital`，本地查询会访问数产地址 | 当前环境路由切换为 `mock`，外网接入时再切回 `digital` |
| 前端实时契约只提交 `sfzhm`，旧 Mock 只按姓名和 `idCard` 查询 | 增加按 `query_type + id_card` 的查询映射 |
| 深层中文字段可能泄露身份证、姓名和诊断 | 实时结果调用深层脱敏，并识别中文字段名 |
| `MedicalQueryServiceImplTest` 既有 5 项失败 | 原测试未 stub 余额扣减返回值，本次未改动该无关测试 |
| IDEA 里执行 `install/package` 报 `Unable to rename ... ruoyi-admin.jar` | 这不是编译错误，是 `ruoyi-admin.jar` 正在被运行中的后端占用；先停止该 Java 进程或改用 `spring-boot:run` 启动开发环境 |
| 浏览器停留 `/noRedirect` 后显示 404 | 将 `/noRedirect` 改为隐藏兼容重定向到 `/business/company` |
| 本机 `3000` 端口启动 Vite 返回 `EACCES` | 未影响构建；开发服务已在 `http://127.0.0.1:3002/` 启动并验证标题 |

## Resources
- `D:/work/proj2/RuoYi-Vue/ruoyi-admin/src/main/resources/application.yml`
- `D:/work/proj2/RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/service/impl/MedicalQueryServiceImpl.java`
- `D:/work/proj2/RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/service/impl/MedicalDataSourceRouter.java`
- `D:/work/proj2/RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/service/impl/MockMedicalDataSource.java`
- `D:/work/proj2/RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/service/impl/DigitalIndustryDataSource.java`
- `D:/work/proj2/index.html`

## Visual/Browser Findings
- `index.html` 里展示了查询详情、历史记录、延时查询、批量查询、通知和额度状态
- 实时详情页展示 `门诊记录`、`住院记录`、`电子病历`、`医学影像检查`
- 延时详情页展示 `查得/未查得`、查询费用、处理状态、结果上传状态

## Realtime Mock Contract
- Input: `queryType=medical_all`, `queryParams.sfzhm/startdate/enddate`。
- Seed identity: `张三B / 320683198312120713`。
- Seed visits: 1 outpatient + 3 inpatient records。
- Output: existing `MedicalQueryServiceImpl` organizes `res` into `data.visits` and `totalVisits`。
- Price: `5.00`，no-result fee remains `0.00`。
