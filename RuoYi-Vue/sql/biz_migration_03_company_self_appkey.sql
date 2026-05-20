-- 保险公司 AppKey 改为公司端自助生成，允许新建公司默认没有 AppKey
ALTER TABLE `biz_insurance_company`
  MODIFY COLUMN `app_key` varchar(32) DEFAULT NULL COMMENT '应用Key',
  MODIFY COLUMN `app_secret` varchar(64) DEFAULT NULL COMMENT '应用Secret';
