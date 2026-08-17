-- =====================================================
-- Migration 08: 保险公司嵌入式查询工作流
-- 适用数据库：MySQL 8.0
--
-- 执行前必须人工确认当前数据库。此脚本不会创建、选择或切换数据库。
-- 本迁移仅新增表、字段、索引和停用的价格类型，不删除或重置现有数据。
-- =====================================================

-- 1. 查询批次表
CREATE TABLE IF NOT EXISTS `biz_medical_query_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(32) NOT NULL COMMENT '对外批次编号',
  `company_id` bigint NOT NULL COMMENT '保险公司ID',
  `service_mode` varchar(16) NOT NULL COMMENT '服务模式（REALTIME/DELAYED）',
  `query_type` varchar(50) DEFAULT NULL COMMENT '实时查询类型，延时查询为空',
  `batch_status` varchar(24) NOT NULL COMMENT '批次状态',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '总人数，业务限制最大500',
  `pending_count` int NOT NULL DEFAULT 0 COMMENT '待处理数',
  `processing_count` int NOT NULL DEFAULT 0 COMMENT '处理中数',
  `completed_count` int NOT NULL DEFAULT 0 COMMENT '完成数',
  `hit_count` int NOT NULL DEFAULT 0 COMMENT '查得数',
  `no_result_count` int NOT NULL DEFAULT 0 COMMENT '未查得数',
  `failed_count` int NOT NULL DEFAULT 0 COMMENT '失败数',
  `cancelled_count` int NOT NULL DEFAULT 0 COMMENT '已取消数',
  `total_fee` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '批次实际费用',
  `request_ip` varchar(50) DEFAULT NULL COMMENT '批次提交IP',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `complete_time` datetime DEFAULT NULL COMMENT '进入终态时间',
  `update_time` datetime DEFAULT NULL COMMENT '最近汇总时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_medical_batch_no` (`batch_no`),
  KEY `idx_medical_batch_company_time` (`company_id`, `create_time`),
  KEY `idx_medical_batch_status` (`batch_status`, `create_time`),
  KEY `idx_medical_batch_company_status` (`company_id`, `batch_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗查询批次表';

-- 2. 统一查询请求表
CREATE TABLE IF NOT EXISTS `biz_medical_query_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `request_no` varchar(32) NOT NULL COMMENT '对外请求编号',
  `company_id` bigint NOT NULL COMMENT '保险公司ID',
  `entry_type` varchar(16) NOT NULL COMMENT '原始入口（SINGLE/BATCH）',
  `service_mode` varchar(16) NOT NULL COMMENT '服务模式（REALTIME/DELAYED）',
  `query_type` varchar(50) DEFAULT NULL COMMENT '实时查询类型，延时查询为空',
  `patient_name` varchar(50) NOT NULL COMMENT '查询姓名',
  `id_card` varchar(32) NOT NULL COMMENT '身份证号',
  `process_status` varchar(20) NOT NULL COMMENT '处理状态',
  `upload_status` varchar(20) NOT NULL COMMENT '上传状态（NOT_UPLOADED/UPLOADED）',
  `result_status` varchar(20) DEFAULT NULL COMMENT '最终业务结果',
  `view_status` varchar(10) NOT NULL COMMENT '查看状态（UNREAD/READ）',
  `price_config_id` bigint DEFAULT NULL COMMENT '公司级价格配置来源ID',
  `reserved_fee` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '已预留额度',
  `fee_snapshot` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '最终计费金额',
  `billing_month` char(7) DEFAULT NULL COMMENT '账单月份（yyyy-MM）',
  `query_log_id` bigint DEFAULT NULL COMMENT '终态查询日志ID',
  `request_ip` varchar(50) DEFAULT NULL COMMENT '请求IP',
  `process_start_time` datetime DEFAULT NULL COMMENT '实际开始处理时间',
  `complete_time` datetime DEFAULT NULL COMMENT '进入终态时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_medical_request_no` (`request_no`),
  KEY `idx_medical_request_company_time` (`company_id`, `create_time`),
  KEY `idx_medical_request_dispatch` (`service_mode`, `process_status`, `create_time`),
  KEY `idx_medical_request_unread` (`company_id`, `service_mode`, `upload_status`, `view_status`),
  KEY `idx_medical_request_person` (`company_id`, `service_mode`, `id_card`, `patient_name`, `process_status`, `upload_status`),
  KEY `idx_medical_request_query_log` (`query_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗查询请求表';

-- 3. 批次成员表
CREATE TABLE IF NOT EXISTS `biz_medical_query_batch_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` bigint NOT NULL COMMENT '查询批次ID',
  `request_id` bigint NOT NULL COMMENT '查询请求ID',
  `row_no` int NOT NULL COMMENT '原名单行顺序，从1开始',
  `reused_flag` char(1) NOT NULL DEFAULT '0' COMMENT '是否复用（0否 1是）',
  `item_status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '成员状态（ACTIVE/CANCELLED）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_medical_batch_row` (`batch_id`, `row_no`),
  UNIQUE KEY `uk_medical_batch_request` (`batch_id`, `request_id`),
  KEY `idx_medical_batch_item_batch` (`batch_id`, `item_status`, `row_no`),
  KEY `idx_medical_batch_item_request` (`request_id`, `item_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗查询批次成员表';

-- 4. 动态查询结果表
CREATE TABLE IF NOT EXISTS `biz_medical_query_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `request_id` bigint NOT NULL COMMENT '查询请求ID',
  `result_source` varchar(24) NOT NULL COMMENT '结果来源（MOCK/DIGITAL_INDUSTRY/MANUAL/HISTORY）',
  `column_schema` json DEFAULT NULL COMMENT '动态列定义',
  `result_data` json DEFAULT NULL COMMENT '动态结果数据',
  `result_summary` text DEFAULT NULL COMMENT '结果说明',
  `version` int NOT NULL DEFAULT 1 COMMENT '结果版本',
  `uploaded_by` varchar(64) DEFAULT NULL COMMENT '首次上传人员',
  `uploaded_time` datetime DEFAULT NULL COMMENT '首次上传完毕时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '最近修改人员',
  `update_time` datetime DEFAULT NULL COMMENT '最近修改时间',
  `update_reason` varchar(500) DEFAULT NULL COMMENT '修改说明',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_medical_result_request` (`request_id`),
  KEY `idx_medical_result_uploaded` (`uploaded_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗查询动态结果表';

-- 5. 扩展现有查询日志表。MySQL 8.0 各小版本对 ADD COLUMN IF NOT EXISTS
-- 的支持不一致，因此统一使用 information_schema + 动态SQL。
SET @column_exists = (
  SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_query_log' AND `COLUMN_NAME` = 'request_no'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE `biz_query_log` ADD COLUMN `request_no` varchar(32) DEFAULT NULL COMMENT ''查询请求编号'' AFTER `id`',
  'SELECT 1'
);
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @column_exists = (
  SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_query_log' AND `COLUMN_NAME` = 'batch_no'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE `biz_query_log` ADD COLUMN `batch_no` varchar(32) DEFAULT NULL COMMENT ''查询批次编号'' AFTER `request_no`',
  'SELECT 1'
);
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @column_exists = (
  SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_query_log' AND `COLUMN_NAME` = 'service_mode'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE `biz_query_log` ADD COLUMN `service_mode` varchar(16) DEFAULT NULL COMMENT ''服务模式（REALTIME/DELAYED）'' AFTER `batch_no`',
  'SELECT 1'
);
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @column_exists = (
  SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_query_log' AND `COLUMN_NAME` = 'entry_type'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE `biz_query_log` ADD COLUMN `entry_type` varchar(16) DEFAULT NULL COMMENT ''入口类型（SINGLE/BATCH）'' AFTER `service_mode`',
  'SELECT 1'
);
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

-- 6. 扩展现有数据导入记录表
SET @column_exists = (
  SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_data_import_log' AND `COLUMN_NAME` = 'business_type'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE `biz_data_import_log` ADD COLUMN `business_type` varchar(32) DEFAULT NULL COMMENT ''关联业务类型'' AFTER `batch_no`',
  'SELECT 1'
);
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @column_exists = (
  SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_data_import_log' AND `COLUMN_NAME` = 'business_id'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE `biz_data_import_log` ADD COLUMN `business_id` bigint DEFAULT NULL COMMENT ''关联业务ID'' AFTER `business_type`',
  'SELECT 1'
);
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

-- 7. 补齐索引。即使表或字段由之前的不完整执行创建，重复运行也不会重复建索引。
SET @index_exists = (
  SELECT COUNT(*) FROM `information_schema`.`STATISTICS`
  WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_batch' AND `INDEX_NAME` = 'uk_medical_batch_no'
);
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_batch` ADD UNIQUE INDEX `uk_medical_batch_no` (`batch_no`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_batch' AND `INDEX_NAME` = 'idx_medical_batch_company_time');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_batch` ADD INDEX `idx_medical_batch_company_time` (`company_id`, `create_time`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_batch' AND `INDEX_NAME` = 'idx_medical_batch_status');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_batch` ADD INDEX `idx_medical_batch_status` (`batch_status`, `create_time`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_batch' AND `INDEX_NAME` = 'idx_medical_batch_company_status');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_batch` ADD INDEX `idx_medical_batch_company_status` (`company_id`, `batch_status`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_request' AND `INDEX_NAME` = 'uk_medical_request_no');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_request` ADD UNIQUE INDEX `uk_medical_request_no` (`request_no`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_request' AND `INDEX_NAME` = 'idx_medical_request_company_time');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_request` ADD INDEX `idx_medical_request_company_time` (`company_id`, `create_time`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_request' AND `INDEX_NAME` = 'idx_medical_request_dispatch');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_request` ADD INDEX `idx_medical_request_dispatch` (`service_mode`, `process_status`, `create_time`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_request' AND `INDEX_NAME` = 'idx_medical_request_unread');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_request` ADD INDEX `idx_medical_request_unread` (`company_id`, `service_mode`, `upload_status`, `view_status`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_request' AND `INDEX_NAME` = 'idx_medical_request_person');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_request` ADD INDEX `idx_medical_request_person` (`company_id`, `service_mode`, `id_card`, `patient_name`, `process_status`, `upload_status`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_request' AND `INDEX_NAME` = 'idx_medical_request_query_log');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_request` ADD INDEX `idx_medical_request_query_log` (`query_log_id`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_batch_item' AND `INDEX_NAME` = 'uk_medical_batch_row');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_batch_item` ADD UNIQUE INDEX `uk_medical_batch_row` (`batch_id`, `row_no`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_batch_item' AND `INDEX_NAME` = 'uk_medical_batch_request');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_batch_item` ADD UNIQUE INDEX `uk_medical_batch_request` (`batch_id`, `request_id`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_batch_item' AND `INDEX_NAME` = 'idx_medical_batch_item_batch');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_batch_item` ADD INDEX `idx_medical_batch_item_batch` (`batch_id`, `item_status`, `row_no`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_batch_item' AND `INDEX_NAME` = 'idx_medical_batch_item_request');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_batch_item` ADD INDEX `idx_medical_batch_item_request` (`request_id`, `item_status`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_result' AND `INDEX_NAME` = 'uk_medical_result_request');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_result` ADD UNIQUE INDEX `uk_medical_result_request` (`request_id`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_medical_query_result' AND `INDEX_NAME` = 'idx_medical_result_uploaded');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_medical_query_result` ADD INDEX `idx_medical_result_uploaded` (`uploaded_time`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_query_log' AND `INDEX_NAME` = 'idx_query_log_request_no');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_query_log` ADD INDEX `idx_query_log_request_no` (`request_no`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_query_log' AND `INDEX_NAME` = 'idx_query_log_batch_no');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_query_log` ADD INDEX `idx_query_log_batch_no` (`batch_no`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_query_log' AND `INDEX_NAME` = 'idx_query_log_company_mode');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_query_log` ADD INDEX `idx_query_log_company_mode` (`company_id`, `service_mode`, `request_time`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @index_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'biz_data_import_log' AND `INDEX_NAME` = 'idx_data_import_business');
SET @ddl = IF(@index_exists = 0, 'ALTER TABLE `biz_data_import_log` ADD INDEX `idx_data_import_business` (`business_type`, `business_id`)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

-- 8. 注册精准延时查询价格类型。实际价格尚未确定，因此默认0元且停用；
-- 后续必须由管理员配置价格并显式启用。本语句不会覆盖已有配置。
INSERT INTO `biz_query_price`
  (`query_type`, `query_name`, `fee`, `status`, `remark`, `create_by`, `create_time`)
SELECT
  'precision_delayed', '精准延时医疗查询', 0.00, '1',
  '占位价格，启用前必须配置实际价格', 'system', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `biz_query_price` WHERE `query_type` = 'precision_delayed'
);

-- 9. 注册精准延时管理后台菜单和权限。不会覆盖已有菜单。
INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`,
   `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 2070, '精准延时处理', 2000, 7, 'delayed-query', 'business/delayed-query/index', 1, 0,
       'C', '0', '0', 'business:delayed-query:list', 'time-range', 'admin', NOW(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2070);

INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`,
   `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 2071, '查询精准延时', 2070, 1, NULL, NULL, 0, 0,
       'F', '0', '0', 'business:delayed-query:query', '#', 'admin', NOW(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2071);

INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`,
   `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 2072, '开始处理精准延时', 2070, 2, NULL, NULL, 0, 0,
       'F', '0', '0', 'business:delayed-query:start', '#', 'admin', NOW(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2072);

INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`,
   `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 2073, '编辑精准延时草稿', 2070, 3, NULL, NULL, 0, 0,
       'F', '0', '0', 'business:delayed-query:edit', '#', 'admin', NOW(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2073);

INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`,
   `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 2074, '上传精准延时结果', 2070, 4, NULL, NULL, 0, 0,
       'F', '0', '0', 'business:delayed-query:complete', '#', 'admin', NOW(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2074);

INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`,
   `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 2075, '修改精准延时结果', 2070, 5, NULL, NULL, 0, 0,
       'F', '0', '0', 'business:delayed-query:update', '#', 'admin', NOW(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2075);

-- 10. 只读结构核验。预期：4张新表、6个扩展字段、20个指定索引、1条价格类型、6个菜单权限。
SELECT DATABASE() AS `current_database`;

SELECT `TABLE_NAME`, `ENGINE`, `TABLE_COLLATION`
FROM `information_schema`.`TABLES`
WHERE `TABLE_SCHEMA` = DATABASE()
  AND `TABLE_NAME` IN (
    'biz_medical_query_batch',
    'biz_medical_query_request',
    'biz_medical_query_batch_item',
    'biz_medical_query_result'
  )
ORDER BY `TABLE_NAME`;

SELECT `TABLE_NAME`, `COLUMN_NAME`, `COLUMN_TYPE`, `IS_NULLABLE`
FROM `information_schema`.`COLUMNS`
WHERE `TABLE_SCHEMA` = DATABASE()
  AND (
    (`TABLE_NAME` = 'biz_query_log' AND `COLUMN_NAME` IN ('request_no', 'batch_no', 'service_mode', 'entry_type'))
    OR
    (`TABLE_NAME` = 'biz_data_import_log' AND `COLUMN_NAME` IN ('business_type', 'business_id'))
  )
ORDER BY `TABLE_NAME`, `ORDINAL_POSITION`;

SELECT `TABLE_NAME`, `INDEX_NAME`,
       GROUP_CONCAT(`COLUMN_NAME` ORDER BY `SEQ_IN_INDEX`) AS `index_columns`
FROM `information_schema`.`STATISTICS`
WHERE `TABLE_SCHEMA` = DATABASE()
  AND `INDEX_NAME` IN (
    'uk_medical_batch_no',
    'idx_medical_batch_company_time',
    'idx_medical_batch_status',
    'idx_medical_batch_company_status',
    'uk_medical_request_no',
    'idx_medical_request_company_time',
    'idx_medical_request_dispatch',
    'idx_medical_request_unread',
    'idx_medical_request_person',
    'idx_medical_request_query_log',
    'uk_medical_batch_row',
    'uk_medical_batch_request',
    'idx_medical_batch_item_batch',
    'idx_medical_batch_item_request',
    'uk_medical_result_request',
    'idx_medical_result_uploaded',
    'idx_query_log_request_no',
    'idx_query_log_batch_no',
    'idx_query_log_company_mode',
    'idx_data_import_business'
  )
GROUP BY `TABLE_NAME`, `INDEX_NAME`
ORDER BY `TABLE_NAME`, `INDEX_NAME`;

SELECT `query_type`, `query_name`, `fee`, `status`, `remark`
FROM `biz_query_price`
WHERE `query_type` = 'precision_delayed';

SELECT `menu_id`, `menu_name`, `parent_id`, `path`, `component`, `perms`, `status`
FROM `sys_menu`
WHERE `menu_id` BETWEEN 2070 AND 2075
ORDER BY `menu_id`;
