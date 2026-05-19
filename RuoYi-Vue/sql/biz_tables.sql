-- 先删除旧表和触发器（按依赖顺序）
DROP TRIGGER IF EXISTS `trg_query_log_before_update`;
DROP TRIGGER IF EXISTS `trg_query_log_before_delete`;
DROP TABLE IF EXISTS `biz_settlement`;
DROP TABLE IF EXISTS `biz_fee_flow`;
DROP TABLE IF EXISTS `biz_query_log`;
DROP TABLE IF EXISTS `mock_medical_data`;
DROP TABLE IF EXISTS `biz_query_price`;
DROP TABLE IF EXISTS `biz_insurance_company`;

-- 保险公司用户表
CREATE TABLE `biz_insurance_company` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_name` varchar(100) NOT NULL COMMENT '公司名称',
  `company_code` varchar(50) DEFAULT NULL COMMENT '公司编码',
  `username` varchar(50) DEFAULT NULL COMMENT '登录用户名',
  `password` varchar(255) DEFAULT NULL COMMENT '登录密码(bcrypt加密)',
  `login_ip` varchar(128) DEFAULT NULL COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `app_key` varchar(32) NOT NULL COMMENT '应用Key',
  `app_secret` varchar(64) NOT NULL COMMENT '应用Secret',
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
  `billing_cycle_days` int NOT NULL DEFAULT 30 COMMENT '计费周期天数（如7=一周，30=一个月）',
  `balance_update_time` datetime DEFAULT NULL COMMENT '最后余额更新时间（上次结算时间）',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_key` (`app_key`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_company_code` (`company_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保险公司用户表';

-- 查询价目表
CREATE TABLE `biz_query_price` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `query_type` varchar(50) NOT NULL COMMENT '查询类型',
  `query_name` varchar(100) NOT NULL COMMENT '查询名称',
  `fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '单次查询费用',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0启用 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_query_type` (`query_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='查询价目表';

-- 查询日志表（禁止UPDATE/DELETE，记录每次成功查询和实时扣费凭证）
CREATE TABLE `biz_query_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '保险公司ID',
  `query_type` varchar(50) NOT NULL COMMENT '查询类型',
  `query_params` json DEFAULT NULL COMMENT '查询参数',
  `fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '本次查询费用（待结算）',
  `settlement_id` bigint DEFAULT NULL COMMENT '结算记录ID（结算后回填）',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0成功 1失败）',
  `request_time` datetime NOT NULL COMMENT '请求时间',
  `request_ip` varchar(50) DEFAULT NULL COMMENT '请求IP',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_query_type` (`query_type`),
  KEY `idx_request_time` (`request_time`),
  KEY `idx_settlement_id` (`settlement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='查询日志表';

-- 模拟医疗数据表（用于开发和测试，Phase 7 切换真实数据源）
CREATE TABLE `mock_medical_data` (
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

-- 结算记录表
CREATE TABLE `biz_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '保险公司ID',
  `period_start` datetime NOT NULL COMMENT '结算周期开始时间',
  `period_end` datetime NOT NULL COMMENT '结算周期结束时间',
  `query_count` int NOT NULL DEFAULT 0 COMMENT '周期内查询次数',
  `total_fee` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '周期内总费用',
  `balance_before` decimal(12,2) NOT NULL COMMENT '结算前余额',
  `balance_after` decimal(12,2) NOT NULL COMMENT '结算后余额（可为负）',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0待结算 1已结算）',
  `settlement_time` datetime DEFAULT NULL COMMENT '结算执行时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_status` (`status`),
  KEY `idx_period_end` (`period_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算记录表';

-- 费用流水表
CREATE TABLE `biz_fee_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '保险公司ID',
  `operation_type` varchar(20) NOT NULL COMMENT '操作类型（RECHARGE充值/DEDUCT扣费/SETTLEMENT结算/REFUND退款/ADJUST冲正）',
  `amount` decimal(12,2) NOT NULL COMMENT '金额',
  `balance_before` decimal(12,2) NOT NULL COMMENT '操作前余额',
  `balance_after` decimal(12,2) NOT NULL COMMENT '操作后余额',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人',
  `biz_id` bigint DEFAULT NULL COMMENT '关联业务ID（结算时关联settlement.id）',
  `operation_time` datetime NOT NULL COMMENT '操作时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用流水表';

-- Phase 6 开发测试价目和模拟数据
INSERT INTO `biz_query_price` (`query_type`, `query_name`, `fee`, `status`, `remark`, `create_by`, `create_time`) VALUES
('medical_all', '医疗大数据', 50.00, '0', '按结果模板建立的查询接口', 'system', sysdate()),
('medical_insurance', '医保信息', 30.00, '0', '按结果模板建立的查询接口', 'system', sysdate()),
('medical_record', '电子病历', 40.00, '0', '按结果模板建立的查询接口', 'system', sysdate()),
('medical_order', '医嘱信息', 20.00, '0', '按结果模板建立的查询接口', 'system', sysdate()),
('medical_image', '影像信息', 25.00, '0', '按结果模板建立的查询接口', 'system', sysdate()),
('medical_surgery', '近期手术', 35.00, '0', '按结果模板建立的查询接口', 'system', sysdate()),
('medical_exam', '体检信息', 20.00, '0', '按结果模板建立的查询接口', 'system', sysdate());

INSERT INTO `mock_medical_data` (`query_type`, `patient_name`, `id_card`, `diagnosis`, `data_json`, `status`, `remark`, `create_time`) VALUES
('medical_all', '刘亮', '432503198706012770', '高血压', '{"summary":"返回医疗机构、科室、就诊类型、日期和诊断。","riskLevel":"关注","records":[{"name":"湘雅医院 神经内科","value":"住院/门诊 20251001","remark":"诊断：高血压"},{"name":"湘雅医院 神经内科","value":"住院 20251001-20251007","remark":"诊断：高血压"}]}', '0', '按结果模板建立的 mock 数据', sysdate()),
('medical_insurance', '刘亮', '432503198706012770', '高血压', '{"summary":"返回医保参保信息和参保至今的就诊记录。","riskLevel":"关注","records":[{"name":"参保状态","value":"正常参保","remark":"医保区划：长沙市，单位名称：中南大学"},{"name":"住院记录","value":"湘雅医院 20251001-20251023","remark":"病种：高血压，报销费用：2000"},{"name":"门诊记录","value":"湘雅医院 20251201","remark":"病种：高血压，报销费用：100"}]}', '0', '按结果模板建立的 mock 数据', sysdate()),
('medical_record', '刘亮', '432503198706012770', '伤筋', '{"summary":"返回电子病历中的入院记录、出院记录、手术记录和影像报告。","riskLevel":"关注","records":[{"name":"入院记录","value":"2022-09-20 涟源中医院骨科","remark":"主诉：腰退疼痛，入院诊断：伤筋"},{"name":"出院记录","value":"住院 7 天","remark":"诊疗经过：中医+西医，医嘱：定期门诊复查"},{"name":"手术记录","value":"2022-10-01 半月板修复","remark":"麻醉方法：椎管内麻醉"},{"name":"影像报告","value":"左膝关节正侧位","remark":"报告医生：练小红"}]}', '0', '按结果模板建立的 mock 数据', sysdate()),
('medical_order', '刘亮', '432503198706012770', '尾骨折', '{"summary":"返回医嘱相关的就诊日期、医院、药品、科室和诊断。","riskLevel":"低风险","records":[{"name":"浏阳市中医院 针灸科","value":"红花 1(g)","remark":"就诊日期：20250611，诊断：尾骨折，类型：门诊"}]}', '0', '按结果模板建立的 mock 数据', sysdate()),
('medical_image', '刘亮', '432503198706012770', '影像检查', '{"summary":"返回影像检查报告单信息。","riskLevel":"低风险","records":[{"name":"左膝关节正侧位","value":"影像号：1067216","remark":"检查日期：2022-07-22，医院：涟源中医院，科室：骨科，报告医生：练小红"}]}', '0', '按结果模板建立的 mock 数据', sysdate()),
('medical_surgery', '刘亮', '432503198706012770', '半月板撕裂', '{"summary":"返回近期手术记录。","riskLevel":"关注","records":[{"name":"半月板修复","value":"2022-10-01","remark":"术前/术中诊断：半月板撕裂，手术者：毛晓东，麻醉者：张旭佳"}]}', '0', '按结果模板建立的 mock 数据', sysdate()),
('medical_exam', '刘亮', '432503198706012770', '体检未见明显异常', '{"summary":"返回体检项目、小结、报告日期、医师和体检医院。","riskLevel":"低风险","records":[{"name":"总检结果","value":"身体健康","remark":"模板样例"},{"name":"总检建议","value":"身体健康，注意清淡饮食","remark":"模板样例"},{"name":"心电图","value":"未见明显异常","remark":"2025-01-01 湘雅二医院 毛晓东"},{"name":"血常规","value":"未见明显异常","remark":"2025-01-01 湘雅二医院 毛晓东"},{"name":"B超检查","value":"未见明显异常","remark":"2025-01-01 湘雅二医院 毛晓东"},{"name":"CT","value":"未见明显异常","remark":"2025-01-01 湘雅二医院 毛晓东"}]}', '0', '按结果模板建立的 mock 数据', sysdate());

-- 查询日志表触发器：禁止UPDATE
DELIMITER //
CREATE TRIGGER `trg_query_log_before_update` BEFORE UPDATE ON `biz_query_log` FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '查询日志不允许修改';
END //

-- 查询日志表触发器：禁止DELETE
CREATE TRIGGER `trg_query_log_before_delete` BEFORE DELETE ON `biz_query_log` FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '查询日志不允许删除';
END //
DELIMITER ;

-- ========================================
-- 业务管理菜单数据（持久化，重建数据库后需重新导入）
-- 管理员角色(1)默认拥有全部权限
-- ========================================

-- 一级菜单：业务管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2000, '业务管理', 0, 5, 'business', NULL, 1, 0, 'M', '0', '0', NULL, 'briefcase', 'admin', sysdate(), '', NULL);

-- 二级菜单：保险公司管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2010, '保险公司管理', 2000, 1, 'company', 'business/company/index', 1, 0, 'C', '0', '0', 'business:company:list', 'list', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2011, '查询保险公司', 2010, 1, NULL, NULL, 0, 0, 'F', '0', '0', 'business:company:query', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2012, '新增保险公司', 2010, 2, NULL, NULL, 0, 0, 'F', '0', '0', 'business:company:add', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2013, '修改保险公司', 2010, 3, NULL, NULL, 0, 0, 'F', '0', '0', 'business:company:edit', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2014, '删除保险公司', 2010, 4, NULL, NULL, 0, 0, 'F', '0', '0', 'business:company:remove', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2015, '导出保险公司', 2010, 5, NULL, NULL, 0, 0, 'F', '0', '0', 'business:company:export', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2016, '保险公司充值', 2010, 6, NULL, NULL, 0, 0, 'F', '0', '0', 'business:company:recharge', '#', 'admin', sysdate(), '', NULL);

-- 二级菜单：查询价目
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2020, '查询价目', 2000, 2, 'price', 'business/price/index', 1, 0, 'C', '0', '0', 'business:price:list', 'table', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2021, '查询价目详情', 2020, 1, NULL, NULL, 0, 0, 'F', '0', '0', 'business:price:query', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2022, '新增价目', 2020, 2, NULL, NULL, 0, 0, 'F', '0', '0', 'business:price:add', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2023, '修改价目', 2020, 3, NULL, NULL, 0, 0, 'F', '0', '0', 'business:price:edit', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2024, '删除价目', 2020, 4, NULL, NULL, 0, 0, 'F', '0', '0', 'business:price:remove', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2025, '导出价目', 2020, 5, NULL, NULL, 0, 0, 'F', '0', '0', 'business:price:export', '#', 'admin', sysdate(), '', NULL);

-- 二级菜单：充值管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2030, '充值管理', 2000, 3, 'recharge', 'business/recharge/index', 1, 0, 'C', '0', '0', 'business:recharge:list', 'money', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2031, '查询充值', 2030, 1, NULL, NULL, 0, 0, 'F', '0', '0', 'business:recharge:query', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2032, '审核通过', 2030, 2, NULL, NULL, 0, 0, 'F', '0', '0', 'business:recharge:approve', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2033, '审核拒绝', 2030, 3, NULL, NULL, 0, 0, 'F', '0', '0', 'business:recharge:reject', '#', 'admin', sysdate(), '', NULL);

-- 二级菜单：日志管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2040, '日志管理', 2000, 4, 'query-log', 'business/log/index', 1, 0, 'C', '0', '0', 'business:log:list', 'log', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2041, '查询日志详情', 2040, 1, NULL, NULL, 0, 0, 'F', '0', '0', 'business:log:query', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2042, '导出日志', 2040, 2, NULL, NULL, 0, 0, 'F', '0', '0', 'business:log:export', '#', 'admin', sysdate(), '', NULL);

-- 二级菜单：扣费管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2050, '扣费管理', 2000, 5, 'fee-flow', 'business/fee/index', 1, 0, 'C', '0', '0', 'business:fee:list', 'money', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2051, '查询扣费详情', 2050, 1, NULL, NULL, 0, 0, 'F', '0', '0', 'business:fee:query', '#', 'admin', sysdate(), '', NULL);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
VALUES (2052, '导出扣费', 2050, 2, NULL, NULL, 0, 0, 'F', '0', '0', 'business:fee:export', '#', 'admin', sysdate(), '', NULL);

-- 将菜单授权给管理员角色(role_id=1)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id >= 2000 AND menu_id <= 2099;
