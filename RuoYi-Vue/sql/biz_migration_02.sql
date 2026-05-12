-- =====================================================
-- Migration 02: 公司账号用户名唯一约束
-- 日期：2026-05-12
-- =====================================================

ALTER TABLE `biz_insurance_company`
  ADD UNIQUE KEY `uk_username` (`username`);
