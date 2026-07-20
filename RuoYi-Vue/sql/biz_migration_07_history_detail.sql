-- =====================================================
-- Migration 07: 非实时历史数据明细表
-- =====================================================

-- 1. 就诊记录表（对应"人员就诊记录"Excel）
CREATE TABLE IF NOT EXISTS `biz_his_clinic_visit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `import_batch_no` varchar(32) NOT NULL COMMENT '导入批次号',
  `patient_name` varchar(50) NOT NULL COMMENT '人员姓名',
  `id_card` varchar(32) NOT NULL COMMENT '证件号码',
  `visit_time` datetime DEFAULT NULL COMMENT '就诊时间',
  `patient_no` varchar(50) DEFAULT NULL COMMENT '人员编号',
  `insurance_type` varchar(10) DEFAULT NULL COMMENT '险种类型',
  `valid_flag` varchar(10) DEFAULT NULL COMMENT '有效标志',
  `org_code` varchar(50) DEFAULT NULL COMMENT '定点医药机构代码',
  `org_name` varchar(200) DEFAULT NULL COMMENT '定点医药机构名称',
  `medical_record_no` varchar(100) DEFAULT NULL COMMENT '病历号',
  `disease_name` varchar(200) DEFAULT NULL COMMENT '病种名称',
  `outpatient_diagnosis` varchar(500) DEFAULT NULL COMMENT '门诊诊断信息',
  `doctor_name` varchar(50) DEFAULT NULL COMMENT '主诊医师姓名',
  `inpatient_diagnosis` varchar(500) DEFAULT NULL COMMENT '住院主诊断名称',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `condition_desc` varchar(1000) DEFAULT NULL COMMENT '主要病情描述',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_his_cv_batch` (`import_batch_no`),
  KEY `idx_his_cv_idcard` (`id_card`),
  KEY `idx_his_cv_name` (`patient_name`),
  KEY `idx_his_cv_visit_time` (`visit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史就诊记录表';

-- 2. 住院费用表（对应"个人住院信息表"Excel）
CREATE TABLE IF NOT EXISTS `biz_his_hospitalization` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `import_batch_no` varchar(32) NOT NULL COMMENT '导入批次号',
  `patient_name` varchar(50) NOT NULL COMMENT '姓名',
  `id_card` varchar(32) NOT NULL COMMENT '身份证号码',
  `insurance_type` varchar(10) DEFAULT NULL COMMENT '险种类型',
  `insurance_name` varchar(50) DEFAULT NULL COMMENT '险种名称',
  `medical_category` varchar(10) DEFAULT NULL COMMENT '医疗类别',
  `medical_category_name` varchar(50) DEFAULT NULL COMMENT '医疗类别名称',
  `pay_location` varchar(10) DEFAULT NULL COMMENT '支付地点',
  `org_code` varchar(50) DEFAULT NULL COMMENT '定点医药机构代码',
  `org_name` varchar(200) DEFAULT NULL COMMENT '定点医药机构名称',
  `org_level` varchar(10) DEFAULT NULL COMMENT '医疗机构等级',
  `visit_start_time` datetime DEFAULT NULL COMMENT '就医开始时间',
  `visit_end_time` datetime DEFAULT NULL COMMENT '就医结束时间',
  `settlement_time` datetime DEFAULT NULL COMMENT '结算时间',
  `total_fee` decimal(12,2) DEFAULT NULL COMMENT '医疗费总额（元）',
  `self_pay_full` decimal(12,2) DEFAULT NULL COMMENT '全自费（元）',
  `excess_limit_self_pay` decimal(12,2) DEFAULT NULL COMMENT '超限价自付金额（元）',
  `advance_self_pay` decimal(12,2) DEFAULT NULL COMMENT '先行自付金额（元）',
  `within_range_fee` decimal(12,2) DEFAULT NULL COMMENT '符合范围金额（元）',
  `deductible_standard` decimal(12,2) DEFAULT NULL COMMENT '起付标准（元）',
  `current_deductible` decimal(12,2) DEFAULT NULL COMMENT '本次起付线（元）',
  `actual_deductible` decimal(12,2) DEFAULT NULL COMMENT '实际支付起付线（元）',
  `pool_fund_payment` decimal(12,2) DEFAULT NULL COMMENT '统筹基金支出（元）',
  `basic_medical_pay_ratio` decimal(6,4) DEFAULT NULL COMMENT '基本医疗统筹支付比例',
  `civil_servant_subsidy` decimal(12,2) DEFAULT NULL COMMENT '公务员医疗补助资金支出（元）',
  `supplement_insurance_payment` decimal(12,2) DEFAULT NULL COMMENT '补充医疗保险基金支出（元）',
  `serious_illness_insurance` decimal(12,2) DEFAULT NULL COMMENT '大病补充医疗保险基金支出（元）',
  `large_medical_subsidy` decimal(12,2) DEFAULT NULL COMMENT '大额医疗补助基金支出（元）',
  `disabled_personnel_fund` decimal(12,2) DEFAULT NULL COMMENT '伤残人员医疗保障基金支出（元）',
  `medical_assistance_fund` decimal(12,2) DEFAULT NULL COMMENT '医疗救助基金支出（元）',
  `other_fund_payment` decimal(12,2) DEFAULT NULL COMMENT '其它基金支付（元）',
  `fund_total_payment` decimal(12,2) DEFAULT NULL COMMENT '基金支付总额（元）',
  `personal_payment` decimal(12,2) DEFAULT NULL COMMENT '个人支付金额（元）',
  `personal_account_payment` decimal(12,2) DEFAULT NULL COMMENT '个人账户支出（元）',
  `cash_payment` decimal(12,2) DEFAULT NULL COMMENT '现金支付金额（元）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_his_hp_batch` (`import_batch_no`),
  KEY `idx_his_hp_idcard` (`id_card`),
  KEY `idx_his_hp_name` (`patient_name`),
  KEY `idx_his_hp_start_time` (`visit_start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史住院费用记录表';

-- 3. 大数据记录表
CREATE TABLE IF NOT EXISTS `biz_his_bigdata` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `import_batch_no` varchar(32) NOT NULL COMMENT '导入批次号',
  `patient_name` varchar(50) NOT NULL COMMENT '姓名',
  `id_card` varchar(32) NOT NULL COMMENT '身份证号',
  `data_category` varchar(50) DEFAULT NULL COMMENT '数据分类（如：门诊费用、住院费用、体检等）',
  `data_json` json DEFAULT NULL COMMENT '原始数据（JSON格式存储）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_his_bd_batch` (`import_batch_no`),
  KEY `idx_his_bd_idcard` (`id_card`),
  KEY `idx_his_bd_name` (`patient_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史大数据记录表';

-- 4. 附件表
CREATE TABLE IF NOT EXISTS `biz_his_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `import_batch_no` varchar(32) NOT NULL COMMENT '导入批次号',
  `patient_name` varchar(50) NOT NULL COMMENT '姓名',
  `id_card` varchar(32) NOT NULL COMMENT '身份证号',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型（影像报告/体检报告/其他）',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_his_at_batch` (`import_batch_no`),
  KEY `idx_his_at_idcard` (`id_card`),
  KEY `idx_his_at_name` (`patient_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史附件记录表';

-- 5. 在 biz_query_price 中注册 history_medical 查询类型
INSERT IGNORE INTO \iz_query_price\ (\query_type\, \query_name\, \ee\, \status\, \emark\, \create_by\, \create_time\)
VALUES ('history_medical', '历史医疗数据查询', 10.00, '0', '查询医保局/卫健委历史存档数据（非实时数据）', 'system', sysdate());

-- 6. 在 biz_his_bigdata 表添加 data_md5 生成列（用于去重）
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = 'biz_his_bigdata' AND COLUMN_NAME = 'data_md5');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE biz_his_bigdata ADD COLUMN data_md5 char(64) GENERATED ALWAYS AS (MD5(data_json)) STORED COMMENT ''数据MD5指纹'' AFTER data_json, ADD INDEX idx_his_bd_md5 (data_md5)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
