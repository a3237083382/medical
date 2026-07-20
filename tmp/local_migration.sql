-- =============================================
-- 本地数据库补全迁移（安全执行）
-- 用途：为本机测试补全缺少的表和字段
-- =============================================

-- ============ 新增列（安全添加） ============
-- biz_insurance_company 新增列
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_insurance_company' AND COLUMN_NAME = 'monthly_budget') > 0,
    'SELECT 1',
    'ALTER TABLE biz_insurance_company ADD COLUMN monthly_budget decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT ''月度服务预算'''
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_insurance_company' AND COLUMN_NAME = 'budget_enabled') > 0,
    'SELECT 1',
    'ALTER TABLE biz_insurance_company ADD COLUMN budget_enabled char(1) NOT NULL DEFAULT ''0'' COMMENT ''预算控制状态 0启用 1停用'''
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_insurance_company' AND COLUMN_NAME = 'opening_balance') > 0,
    'SELECT 1',
    'ALTER TABLE biz_insurance_company ADD COLUMN opening_balance decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT ''期初余额'''
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_query_log 新增列
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_query_log' AND COLUMN_NAME = 'billing_month') > 0,
    'SELECT 1',
    'ALTER TABLE biz_query_log ADD COLUMN billing_month char(7) DEFAULT NULL COMMENT ''账单月份 yyyy-MM'''
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_query_log' AND COLUMN_NAME = 'result_status') > 0,
    'SELECT 1',
    'ALTER TABLE biz_query_log ADD COLUMN result_status varchar(20) DEFAULT NULL COMMENT ''查询结果 HIT/NO_RESULT/FAILED'''
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_query_log' AND COLUMN_NAME = 'fee_snapshot') > 0,
    'SELECT 1',
    'ALTER TABLE biz_query_log ADD COLUMN fee_snapshot decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''入账金额快照'''
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_query_log' AND COLUMN_NAME = 'price_config_id') > 0,
    'SELECT 1',
    'ALTER TABLE biz_query_log ADD COLUMN price_config_id bigint DEFAULT NULL COMMENT ''公司级价格配置ID'''
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============ 新增表（安全创建） ============
CREATE TABLE IF NOT EXISTS biz_monthly_usage (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  company_id bigint NOT NULL COMMENT '保险公司ID',
  billing_month char(7) NOT NULL COMMENT '账单月份',
  monthly_budget decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '月度预算',
  used_amount decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际使用金额',
  reserved_amount decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '预扣金额',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_company_month (company_id, billing_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度额度使用表';

CREATE TABLE IF NOT EXISTS biz_monthly_bill (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  company_id bigint NOT NULL COMMENT '保险公司ID',
  billing_month char(7) NOT NULL COMMENT '账单月份',
  total_queries int NOT NULL DEFAULT 0 COMMENT '总查询次数',
  hit_count int NOT NULL DEFAULT 0 COMMENT '查得次数',
  no_result_count int NOT NULL DEFAULT 0 COMMENT '未查得次数',
  total_amount decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '账单总金额',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态 0待确认 1已确认',
  create_time datetime DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_company_month (company_id, billing_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度账单表';

CREATE TABLE IF NOT EXISTS biz_monthly_bill_detail (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  bill_id bigint NOT NULL COMMENT '账单ID',
  company_id bigint NOT NULL COMMENT '保险公司ID',
  billing_month char(7) NOT NULL,
  query_type varchar(50) NOT NULL COMMENT '查询类型',
  hit_count int NOT NULL DEFAULT 0 COMMENT '查得次数',
  no_result_count int NOT NULL DEFAULT 0 COMMENT '未查得次数',
  hit_rate decimal(12,2) NOT NULL DEFAULT 0.00,
  no_result_rate decimal(12,2) NOT NULL DEFAULT 0.00,
  amount decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度账单明细表';

CREATE TABLE IF NOT EXISTS biz_company_query_price (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  company_id bigint NOT NULL COMMENT '保险公司ID',
  query_type varchar(50) NOT NULL COMMENT '查询类型',
  query_name varchar(100) NOT NULL COMMENT '查询名称',
  hit_fee decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '查得标准',
  no_result_fee decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '未查得标准',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态 0启用 1停用',
  remark varchar(500) DEFAULT NULL,
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_company_type (company_id, query_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司级接口价格表';

CREATE TABLE IF NOT EXISTS mock_medical_data (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  query_type varchar(50) NOT NULL COMMENT '查询类型',
  patient_name varchar(50) NOT NULL COMMENT '姓名',
  id_card varchar(32) NOT NULL COMMENT '身份证号',
  diagnosis varchar(200) DEFAULT NULL COMMENT '诊断',
  data_json json DEFAULT NULL COMMENT '扩展模拟数据',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态 0启用 1停用',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_query_type (query_type),
  KEY idx_id_card (id_card)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟医疗数据表';

CREATE TABLE IF NOT EXISTS biz_data_import_log (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  file_name varchar(255) DEFAULT NULL,
  total_rows int DEFAULT 0,
  success_rows int DEFAULT 0,
  error_rows int DEFAULT 0,
  import_type varchar(50) DEFAULT NULL,
  status char(1) NOT NULL DEFAULT '0',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据导入记录表';

CREATE TABLE IF NOT EXISTS biz_history_medical_data (
  id bigint NOT NULL AUTO_INCREMENT,
  batch_no varchar(50) DEFAULT NULL COMMENT '批次号',
  patient_name varchar(50) DEFAULT NULL COMMENT '姓名',
  id_card varchar(32) DEFAULT NULL COMMENT '身份证号',
  query_type varchar(50) DEFAULT NULL COMMENT '查询类型',
  data_json json DEFAULT NULL COMMENT '数据',
  import_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_id_card (id_card),
  KEY idx_batch_no (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史医疗数据表';

SELECT 'Migration completed successfully' as status;
