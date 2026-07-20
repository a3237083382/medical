USE ry_vue;
INSERT IGNORE INTO mock_medical_data (query_type, patient_name, id_card, diagnosis, data_json, status, create_time) VALUES
('medical_all', '刘亮', '432503198706012770', '高血压',
 '{"summary":"医疗机构、科室、就诊类型、日期和诊断","riskLevel":"关注","records":[{"name":"湘雅医院 神经内科","value":"住院/门诊 20251001","remark":"诊断：高血压"},{"name":"湘雅医院 神经内科","value":"住院 20251001-20251007","remark":"诊断：高血压"},{"name":"数据处理","value":"已脱敏","remark":"脱敏展示"}]}',
 '0', NOW()),
('medical_insurance', '刘亮', '432503198706012770', '高血压',
 '{"summary":"医保参保信息","riskLevel":"关注","records":[{"name":"参保状态","value":"正常参保","remark":"医保区划：长沙市"},{"name":"住院记录","value":"湘雅医院","remark":"病种：高血压"},{"name":"门诊记录","value":"湘雅医院","remark":"病种：高血压"}]}',
 '0', NOW()),
('medical_record', '刘亮', '432503198706012770', '伤寒',
 '{"summary":"电子病历","riskLevel":"关注","records":[{"name":"入院记录","value":"2022-09-20","remark":"腰腿疼痛"},{"name":"出院记录","value":"住院7天","remark":"中医+西医"},{"name":"手术记录","value":"2022-10-01 半月板修复","remark":"椎管内麻醉"},{"name":"影像报告","value":"左膝关节","remark":"报告医生：练小红"}]}',
 '0', NOW()),
('medical_order', '刘亮', '432503198706012770', '尾骨折',
 '{"summary":"医嘱信息","riskLevel":"低风险","records":[{"name":"浏阳市中医院 针灸科","value":"红花1g","remark":"20250611 尾骨折"}]}',
 '0', NOW()),
('medical_image', '刘亮', '432503198706012770', '影像检查',
 '{"summary":"影像检查","riskLevel":"低风险","records":[{"name":"左膝关节正侧位","value":"影像号：1067216","remark":"2022-07-22 浏源中医馆"}]}',
 '0', NOW()),
('medical_surgery', '刘亮', '432503198706012770', '半月板撕裂',
 '{"summary":"近期手术","riskLevel":"关注","records":[{"name":"半月板修复","value":"2022-10-01","remark":"手术者：毛晓东"}]}',
 '0', NOW()),
('medical_exam', '刘亮', '432503198706012770', '体检未见明显异常',
 '{"summary":"体检项目","riskLevel":"低风险","records":[{"name":"总检结果","value":"身体健康","remark":""},{"name":"心电图","value":"未见明显异常","remark":"2025-01-01 湘雅二医院"},{"name":"血常规","value":"未见明显异常","remark":"2025-01-01 湘雅二医院"},{"name":"B超","value":"未见明显异常","remark":"2025-01-01 湘雅二医院"},{"name":"CT","value":"未见明显异常","remark":"2025-01-01 湘雅二医院"}]}',
 '0', NOW());
SELECT COUNT(*) as cnt FROM mock_medical_data;
