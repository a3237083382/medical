-- =====================================================
-- Migration 01: 保险公司登录 + 充值申请审核
-- 日期：2026-05-10
-- =====================================================

-- 1. biz_insurance_company 表新增登录相关字段
ALTER TABLE `biz_insurance_company`
  ADD COLUMN `username` varchar(50) DEFAULT NULL COMMENT '登录用户名' AFTER `company_code`,
  ADD COLUMN `password` varchar(256) DEFAULT NULL COMMENT '登录密码(bcrypt)' AFTER `username`,
  ADD COLUMN `login_ip` varchar(128) DEFAULT NULL COMMENT '最后登录IP' AFTER `balance_update_time`,
  ADD COLUMN `login_date` datetime DEFAULT NULL COMMENT '最后登录时间' AFTER `login_ip`;

ALTER TABLE `biz_insurance_company`
  ADD UNIQUE KEY `uk_username` (`username`);

-- 2. 创建充值申请表
DROP TABLE IF EXISTS `biz_recharge_request`;
CREATE TABLE `biz_recharge_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '保险公司ID',
  `amount` decimal(12,2) NOT NULL COMMENT '充值金额',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0待审核 1已通过 2已驳回）',
  `submit_time` datetime NOT NULL COMMENT '提交时间',
  `submit_remark` varchar(500) DEFAULT NULL COMMENT '提交备注',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reviewer` varchar(64) DEFAULT NULL COMMENT '审核人',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `fee_flow_id` bigint DEFAULT NULL COMMENT '关联费用流水ID（审核通过后回填）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_status` (`status`),
  KEY `idx_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值申请表';
