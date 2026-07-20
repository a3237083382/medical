-- =====================================================
-- 种子数据：测试保险公司用户（手动备用�?-- 日期�?026-05-11
--
-- 注意：后端启动时会通过 DataInitializer 自动创建此账号，
-- 正常情况下不需要手动执行此脚本�?-- 如需手动导入�?--    mysql -u root -p ry_vue < biz_seed_test.sql
--
-- 测试账号：test001 / 123456
-- =====================================================

-- 如果 app_key 冲突，先删除旧记录：
-- DELETE FROM biz_insurance_company WHERE company_code = 'TEST001';

INSERT INTO biz_insurance_company (
    company_name, company_code, username, password,
    app_key, app_secret, balance, billing_cycle_days,
    status, contact_person, contact_phone, remark,
    create_by, create_time
) VALUES (
    '测试保险公司', 'TEST001', 'test001',
    -- bcrypt($2a$10$) encode of "123456" �?�?DataInitializer 使用相同逻辑
    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
    'a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6',
    
    100000.00, 30,
    '0', '张三', '13800138000', '测试用保险公�?,
    'admin', sysdate()
);
