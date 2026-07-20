USE ry_vue;
UPDATE magic_api_file SET file_content = '{"properties": {}, "id": "external_medical_query", "script": "import biz;\nreturn biz.queryMedical(body.companyId, body.queryType, body.queryParams, request.getRemoteAddr());", "groupId": "external_group", "name": "外部医疗大数据模拟查询", "createTime": 1778663247170, "updateTime": 1778663247170, "lock": null, "createBy": "system", "updateBy": null, "path": "/medical/query", "method": "POST", "parameters": [{"name": "companyId", "required": true}, {"name": "queryType", "required": true}, {"name": "queryParams", "required": true}], "options": [], "requestBody": null, "headers": [], "paths": [], "responseBody": null, "description": null, "requestBodyDefinition": null, "responseBodyDefinition": null}
================================
import biz;
return biz.queryMedical(body.companyId, body.queryType, body.queryParams, request.getRemoteAddr());' WHERE file_path = 'magic-api/api/对外接口/外部医疗大数据模拟查询.ms';
UPDATE magic_api_file SET file_content = '{"properties": {}, "id": "external_balance_query", "script": "import biz;\nreturn biz.getBalance(companyId);", "groupId": "external_group", "name": "外部余额查询", "createTime": 1778663247148, "updateTime": 1778663247148, "lock": null, "createBy": "system", "updateBy": null, "path": "/balance/query", "method": "GET", "parameters": [{"name": "companyId", "required": true, "value": "5"}], "options": [], "requestBody": null, "headers": [], "paths": [], "responseBody": null, "description": null, "requestBodyDefinition": null, "responseBodyDefinition": null}
================================
import biz;
return biz.getBalance(companyId);' WHERE file_path = 'magic-api/api/对外接口/外部余额查询.ms';
INSERT IGNORE INTO magic_api_file (file_path, file_content) VALUES ('magic-api/api/对外接口/外部价格查询.ms', '{"properties": {}, "id": "external_price_query", "script": "import biz;\nreturn biz.getQueryPrice(queryType);", "groupId": "external_group", "name": "外部价格查询", "createTime": 1778663247170, "updateTime": 1778663247170, "lock": null, "createBy": "system", "updateBy": null, "path": "/price/query", "method": "GET", "parameters": [{"name": "queryType", "required": true}], "options": [], "requestBody": null, "headers": [], "paths": [], "responseBody": null, "description": null, "requestBodyDefinition": null, "responseBodyDefinition": null}
================================
import biz;
return biz.getQueryPrice(queryType);');
SELECT COUNT(*) as updated FROM magic_api_file WHERE file_path LIKE '%.ms';