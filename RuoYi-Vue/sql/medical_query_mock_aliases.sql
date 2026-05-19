-- 旧演示查询类型 mock 数据补丁
-- 用途：保留旧的演示接口编码，同时避免查询时报 medical data not found。

CREATE TABLE IF NOT EXISTS `mock_medical_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `query_type` varchar(50) NOT NULL COMMENT '查询类型',
  `patient_name` varchar(50) NOT NULL COMMENT '姓名',
  `id_card` varchar(32) NOT NULL COMMENT '身份证号',
  `diagnosis` varchar(200) DEFAULT NULL COMMENT '诊断',
  `data_json` json DEFAULT NULL COMMENT '扩展模拟数据',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0启用 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_query_type` (`query_type`),
  KEY `idx_id_card` (`id_card`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟医疗数据表';

DELETE FROM `mock_medical_data`
WHERE `query_type` IN (
  'PW_TYPE_1778471922',
  'PW_TYPE_1778472098',
  'ALL',
  'MEDICAL_VISIT',
  'MED_INSURANCE',
  'EMR',
  'ORDERS',
  'IMAGING',
  'SURGERY',
  'CHECKUP',
  'MEDICAL_BIG_DATA'
)
  AND `remark` = '旧演示接口 mock 数据';

INSERT INTO `mock_medical_data` (`query_type`, `patient_name`, `id_card`, `diagnosis`, `data_json`, `status`, `remark`, `create_time`) VALUES
('PW_TYPE_1778471922', '刘亮', '432503198706012770', '演示价目查询', '{"summary":"旧演示价目接口返回的通用医疗查询演示数据。","riskLevel":"低风险","records":[{"name":"演示接口","value":"PW价目_1778471922","remark":"该接口为旧演示价目，已保留 mock 返回"}]}', '0', '旧演示接口 mock 数据', sysdate()),
('PW_TYPE_1778472098', '刘亮', '432503198706012770', '演示价目查询', '{"summary":"旧演示价目接口返回的通用医疗查询演示数据。","riskLevel":"低风险","records":[{"name":"演示接口","value":"PW价目_1778472098","remark":"该接口为旧演示价目，已保留 mock 返回"}]}', '0', '旧演示接口 mock 数据', sysdate()),
('ALL', '刘亮', '432503198706012770', '高血压', '{"summary":"返回全部医疗大数据演示结果。","riskLevel":"关注","records":[{"name":"湘雅医院 神经内科","value":"住院/门诊 20251001","remark":"诊断：高血压"},{"name":"医保参保","value":"正常参保","remark":"医保区划：长沙市"},{"name":"体检记录","value":"未见明显异常","remark":"湘雅二医院"}]}', '0', '旧演示接口 mock 数据', sysdate()),
('MEDICAL_VISIT', '刘亮', '432503198706012770', '高血压', '{"summary":"返回医疗就诊机构、科室、就诊类型、日期和诊断。","riskLevel":"关注","records":[{"name":"湘雅医院 神经内科","value":"住院/门诊 20251001","remark":"诊断：高血压"},{"name":"湘雅医院 神经内科","value":"住院 20251001-20251007","remark":"诊断：高血压"}]}', '0', '旧演示接口 mock 数据', sysdate()),
('MED_INSURANCE', '刘亮', '432503198706012770', '高血压', '{"summary":"返回医保参保信息和参保至今的就诊记录。","riskLevel":"关注","records":[{"name":"参保状态","value":"正常参保","remark":"医保区划：长沙市，单位名称：中南大学"},{"name":"住院记录","value":"湘雅医院 20251001-20251023","remark":"病种：高血压，报销费用：2000"},{"name":"门诊记录","value":"湘雅医院 20251201","remark":"病种：高血压，报销费用：100"}]}', '0', '旧演示接口 mock 数据', sysdate()),
('EMR', '刘亮', '432503198706012770', '伤筋', '{"summary":"返回电子病历中的入院记录、出院记录、手术记录和影像报告。","riskLevel":"关注","records":[{"name":"入院记录","value":"2022-09-20 涟源中医院骨科","remark":"主诉：腰退疼痛，入院诊断：伤筋"},{"name":"出院记录","value":"住院 7 天","remark":"诊疗经过：中医+西医，医嘱：定期门诊复查"},{"name":"手术记录","value":"2022-10-01 半月板修复","remark":"麻醉方法：椎管内麻醉"},{"name":"影像报告","value":"左膝关节正侧位","remark":"报告医生：练小红"}]}', '0', '旧演示接口 mock 数据', sysdate()),
('ORDERS', '刘亮', '432503198706012770', '尾骨折', '{"summary":"返回医嘱相关的就诊日期、医院、药品、科室和诊断。","riskLevel":"低风险","records":[{"name":"浏阳市中医院 针灸科","value":"红花 1(g)","remark":"就诊日期：20250611，诊断：尾骨折，类型：门诊"}]}', '0', '旧演示接口 mock 数据', sysdate()),
('IMAGING', '刘亮', '432503198706012770', '影像检查', '{"summary":"返回影像检查报告单信息。","riskLevel":"低风险","records":[{"name":"左膝关节正侧位","value":"影像号：1067216","remark":"检查日期：2022-07-22，医院：涟源中医院，科室：骨科，报告医生：练小红"}]}', '0', '旧演示接口 mock 数据', sysdate()),
('SURGERY', '刘亮', '432503198706012770', '半月板撕裂', '{"summary":"返回近期手术记录。","riskLevel":"关注","records":[{"name":"半月板修复","value":"2022-10-01","remark":"术前/术中诊断：半月板撕裂，手术者：毛晓东，麻醉者：张旭佳"}]}', '0', '旧演示接口 mock 数据', sysdate()),
('CHECKUP', '刘亮', '432503198706012770', '体检未见明显异常', '{"summary":"返回体检项目、小结、报告日期、医师和体检医院。","riskLevel":"低风险","records":[{"name":"总检结果","value":"身体健康","remark":"模板样例"},{"name":"总检建议","value":"身体健康，注意清淡饮食","remark":"模板样例"},{"name":"心电图","value":"未见明显异常","remark":"2025-01-01 湘雅二医院 毛晓东"},{"name":"血常规","value":"未见明显异常","remark":"2025-01-01 湘雅二医院 毛晓东"},{"name":"B超检查","value":"未见明显异常","remark":"2025-01-01 湘雅二医院 毛晓东"},{"name":"CT","value":"未见明显异常","remark":"2025-01-01 湘雅二医院 毛晓东"}]}', '0', '旧演示接口 mock 数据', sysdate()),
('MEDICAL_BIG_DATA', '刘亮', '432503198706012770', '高血压', '{"summary":"返回医疗大数据演示结果。","riskLevel":"关注","records":[{"name":"湘雅医院 神经内科","value":"住院/门诊 20251001","remark":"诊断：高血压"},{"name":"湘雅医院 神经内科","value":"住院 20251001-20251007","remark":"诊断：高血压"}]}', '0', '旧演示接口 mock 数据', sysdate());
