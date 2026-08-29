# Findings & Decisions

## Requirements
- 先做一个本地 mock 版本，让实时查询接口能出数据
- 返回结构尽量和外网真实接口一致
- 后续数产开通外网后，要能回到 `38090c6` 基线继续接入
- 对外调用方式尽量不变

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

## Issues Encountered
| Issue | Resolution |
|-------|------------|
| 初始技能路径不明确 | 已改为读取现有 `planning-with-files` 技能 |

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
