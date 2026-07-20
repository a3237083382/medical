USE ry_vue;
INSERT IGNORE INTO magic_api_file (file_path, file_content) VALUES ('magic-api/test/test_company.ms', '{"properties":{},"id":"test_company_query","script":"select * from biz_insurance_company where id = #{id}","groupId":"0","name":"测试公司查询","createTime":0,"updateTime":0,"path":"/test/company","method":"GET","parameters":[{"name":"id","required":true}],"options":[],"requestBody":null,"headers":[],"paths":[],"responseBody":null,"description":null,"requestBodyDefinition":null,"responseBodyDefinition":null}
================================
select * from biz_insurance_company where id = #{id}');
