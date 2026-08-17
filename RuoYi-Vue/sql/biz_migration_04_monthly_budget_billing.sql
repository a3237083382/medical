ALTER TABLE biz_insurance_company
  ADD COLUMN monthly_budget decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '月度服务预算',
  ADD COLUMN budget_enabled char(1) NOT NULL DEFAULT '0' COMMENT '预算控制状态 0启用 1停用';

ALTER TABLE biz_query_log
  ADD COLUMN billing_month char(7) DEFAULT NULL COMMENT '账单月份 yyyy-MM',
  ADD COLUMN result_status varchar(20) DEFAULT NULL COMMENT '查询结果 HIT/NO_RESULT/FAILED',
  ADD COLUMN fee_snapshot decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '入账金额快照',
  ADD COLUMN price_config_id bigint DEFAULT NULL COMMENT '公司级价格配置ID';

CREATE TABLE biz_company_query_price (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  company_id bigint NOT NULL COMMENT '保险公司ID',
  query_type varchar(50) NOT NULL COMMENT '查询类型',
  query_name varchar(100) NOT NULL COMMENT '查询名称',
  hit_fee decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '查得数据标准',
  no_result_fee decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '未查得标准',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态 0启用 1停用',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_company_query_type (company_id, query_type),
  KEY idx_query_type (query_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司级接口价格表';

CREATE TABLE biz_monthly_usage (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  company_id bigint NOT NULL COMMENT '保险公司ID',
  billing_month char(7) NOT NULL COMMENT '账单月份 yyyy-MM',
  budget_amount decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '月度预算',
  used_amount decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '已确认使用金额',
  reserved_amount decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '预留金额',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态 0正常 1冻结',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_company_month (company_id, billing_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度额度使用表';

SET @usage_status_missing = (
  SELECT COUNT(1) = 0
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_monthly_usage'
    AND COLUMN_NAME = 'status'
);
SET @usage_status_sql = IF(
  @usage_status_missing,
  'ALTER TABLE biz_monthly_usage ADD COLUMN status char(1) NOT NULL DEFAULT ''0'' COMMENT ''状态 0正常 1冻结'' AFTER reserved_amount',
  'SELECT 1'
);
PREPARE usage_status_stmt FROM @usage_status_sql;
EXECUTE usage_status_stmt;
DEALLOCATE PREPARE usage_status_stmt;

CREATE TABLE biz_monthly_bill (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  company_id bigint NOT NULL COMMENT '保险公司ID',
  billing_month char(7) NOT NULL COMMENT '账单月份 yyyy-MM',
  query_count int NOT NULL DEFAULT 0 COMMENT '查询次数',
  hit_count int NOT NULL DEFAULT 0 COMMENT '查得次数',
  no_result_count int NOT NULL DEFAULT 0 COMMENT '未查得次数',
  total_amount decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '账单金额',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态 0已生成',
  generated_time datetime DEFAULT NULL COMMENT '生成时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_company_month (company_id, billing_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度账单表';

CREATE TABLE biz_monthly_bill_detail (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  bill_id bigint NOT NULL COMMENT '月账单ID',
  company_id bigint NOT NULL COMMENT '保险公司ID',
  billing_month char(7) NOT NULL COMMENT '账单月份 yyyy-MM',
  query_type varchar(50) NOT NULL COMMENT '查询类型',
  query_name varchar(100) NOT NULL COMMENT '查询名称',
  result_status varchar(20) NOT NULL COMMENT '查询结果 HIT/NO_RESULT',
  query_count int NOT NULL DEFAULT 0 COMMENT '查询次数',
  total_amount decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额合计',
  PRIMARY KEY (id),
  KEY idx_bill_id (bill_id),
  KEY idx_company_month (company_id, billing_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度账单明细表';

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (2060, '月度账单', 2000, 6, 'monthly-bill', 'business/monthlyBill/index', 1, 0, 'C', '0', '0', 'business:fee:list', 'money', 'admin', sysdate());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (2061, '查询月度账单', 2060, 1, NULL, NULL, 0, 0, 'F', '0', '0', 'business:fee:query', '#', 'admin', sysdate());
