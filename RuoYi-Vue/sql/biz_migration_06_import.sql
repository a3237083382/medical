-- =====================================================
-- Migration 06: 历史医疗数据表 & 数据导入记录表
-- =====================================================

-- 历史医疗数据表（管理员导入的历史数据）
CREATE TABLE IF NOT EXISTS `biz_history_medical_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `import_batch_no` varchar(32) NOT NULL COMMENT '导入批次号',
  `query_type` varchar(50) DEFAULT NULL COMMENT '查询类型',
  `patient_name` varchar(50) NOT NULL COMMENT '姓名',
  `id_card` varchar(32) NOT NULL COMMENT '身份证号',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `birth_date` varchar(20) DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `diagnosis` varchar(500) DEFAULT NULL COMMENT '诊断',
  `hospital` varchar(200) DEFAULT NULL COMMENT '就诊医院',
  `department` varchar(100) DEFAULT NULL COMMENT '就诊科室',
  `visit_date` varchar(20) DEFAULT NULL COMMENT '就诊日期',
  `visit_type` varchar(50) DEFAULT NULL COMMENT '就诊类型（门诊/住院/体检）',
  `disease_code` varchar(50) DEFAULT NULL COMMENT '疾病编码',
  `medical_record_no` varchar(100) DEFAULT NULL COMMENT '病历号',
  `doctor` varchar(50) DEFAULT NULL COMMENT '医生',
  `data_json` json DEFAULT NULL COMMENT '扩展字段（JSON，存储额外列）',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_import_batch_no` (`import_batch_no`),
  KEY `idx_id_card` (`id_card`),
  KEY `idx_patient_name` (`patient_name`),
  KEY `idx_query_type` (`query_type`),
  KEY `idx_visit_date` (`visit_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史医疗数据表';

-- 数据导入记录表
CREATE TABLE IF NOT EXISTS `biz_data_import_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(32) NOT NULL COMMENT '批次号',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `total_rows` int NOT NULL DEFAULT 0 COMMENT '总行数',
  `success_rows` int NOT NULL DEFAULT 0 COMMENT '成功行数',
  `failed_rows` int NOT NULL DEFAULT 0 COMMENT '失败行数',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0导入中 1成功 2部分成功 3失败）',
  `error_msg` text DEFAULT NULL COMMENT '错误信息',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据导入记录表';
