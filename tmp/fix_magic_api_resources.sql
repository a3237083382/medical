UPDATE magic_api_file
SET file_content = JSON_OBJECT(
  'properties', JSON_OBJECT(),
  'id', 'external_price_query',
  'script', CONCAT('import biz;', CONVERT(CHAR(10) USING utf8mb4), 'return biz.getQueryPrice(queryType);'),
  'groupId', 'external_group',
  'name', '外部价格查询',
  'createTime', 1778663247170,
  'updateTime', UNIX_TIMESTAMP(NOW(3)) * 1000,
  'lock', CAST(NULL AS JSON),
  'createBy', 'system',
  'updateBy', 'system',
  'path', '/price/query',
  'method', 'GET',
  'parameters', JSON_ARRAY(JSON_OBJECT('name', 'queryType', 'required', TRUE)),
  'options', JSON_ARRAY(),
  'requestBody', CAST(NULL AS JSON),
  'headers', JSON_ARRAY(),
  'paths', JSON_ARRAY(),
  'responseBody', CAST(NULL AS JSON),
  'description', CAST(NULL AS JSON),
  'requestBodyDefinition', CAST(NULL AS JSON),
  'responseBodyDefinition', CAST(NULL AS JSON)
)
WHERE file_path = 'magic-api/api/对外接口/外部价格查询.ms';

UPDATE magic_api_file
SET file_content = JSON_OBJECT(
  'properties', JSON_OBJECT(),
  'id', 'external_balance_query',
  'script', CONCAT('import biz;', CONVERT(CHAR(10) USING utf8mb4), 'return biz.getBalance(companyId);'),
  'groupId', 'external_group',
  'name', '外部余额查询',
  'createTime', 1778663247148,
  'updateTime', UNIX_TIMESTAMP(NOW(3)) * 1000,
  'lock', CAST(NULL AS JSON),
  'createBy', 'system',
  'updateBy', 'system',
  'path', '/balance/query',
  'method', 'GET',
  'parameters', JSON_ARRAY(JSON_OBJECT('name', 'companyId', 'required', TRUE, 'value', '5')),
  'options', JSON_ARRAY(),
  'requestBody', CAST(NULL AS JSON),
  'headers', JSON_ARRAY(),
  'paths', JSON_ARRAY(),
  'responseBody', CAST(NULL AS JSON),
  'description', CAST(NULL AS JSON),
  'requestBodyDefinition', CAST(NULL AS JSON),
  'responseBodyDefinition', CAST(NULL AS JSON)
)
WHERE file_path = 'magic-api/api/对外接口/外部余额查询.ms';

UPDATE magic_api_file
SET file_content = JSON_OBJECT(
  'properties', JSON_OBJECT(),
  'id', 'external_medical_query',
  'script', CONCAT('import biz;', CONVERT(CHAR(10) USING utf8mb4), 'return biz.queryMedical(body.companyId, body.queryType, body.queryParams, request.getRemoteAddr());'),
  'groupId', 'external_group',
  'name', '外部医疗大数据模拟查询',
  'createTime', 1778663247170,
  'updateTime', UNIX_TIMESTAMP(NOW(3)) * 1000,
  'lock', CAST(NULL AS JSON),
  'createBy', 'system',
  'updateBy', 'system',
  'path', '/medical/query',
  'method', 'POST',
  'parameters', JSON_ARRAY(
    JSON_OBJECT('name', 'companyId', 'required', TRUE),
    JSON_OBJECT('name', 'queryType', 'required', TRUE),
    JSON_OBJECT('name', 'queryParams', 'required', TRUE)
  ),
  'options', JSON_ARRAY(),
  'requestBody', CAST(NULL AS JSON),
  'headers', JSON_ARRAY(),
  'paths', JSON_ARRAY(),
  'responseBody', CAST(NULL AS JSON),
  'description', CAST(NULL AS JSON),
  'requestBodyDefinition', CAST(NULL AS JSON),
  'responseBodyDefinition', CAST(NULL AS JSON)
)
WHERE file_path = 'magic-api/api/对外接口/外部医疗大数据模拟查询.ms';

UPDATE magic_api_file
SET file_content = JSON_OBJECT(
  'properties', JSON_OBJECT(),
  'id', 'external_types',
  'script', CONCAT('import medicalApi;', CONVERT(CHAR(10) USING utf8mb4), 'return medicalApi.types();'),
  'groupId', 'external_group',
  'name', '外部查询类型',
  'createTime', 1778663247127,
  'updateTime', UNIX_TIMESTAMP(NOW(3)) * 1000,
  'lock', CAST(NULL AS JSON),
  'createBy', 'system',
  'updateBy', 'system',
  'path', '/medical/types',
  'method', 'GET',
  'parameters', JSON_ARRAY(),
  'options', JSON_ARRAY(),
  'requestBody', CAST(NULL AS JSON),
  'headers', JSON_ARRAY(),
  'paths', JSON_ARRAY(),
  'responseBody', CAST(NULL AS JSON),
  'description', CAST(NULL AS JSON),
  'requestBodyDefinition', CAST(NULL AS JSON),
  'responseBodyDefinition', CAST(NULL AS JSON)
)
WHERE file_path = 'magic-api/api/对外接口/外部查询类型.ms';

UPDATE magic_api_file
SET file_content = JSON_OBJECT(
  'properties', JSON_OBJECT(),
  'id', 'external_scenario_list',
  'script', CONCAT('import medicalApi;', CONVERT(CHAR(10) USING utf8mb4), 'return medicalApi.scenarios();'),
  'groupId', 'external_group',
  'name', '外部医疗场景清单',
  'createTime', 1778663247141,
  'updateTime', UNIX_TIMESTAMP(NOW(3)) * 1000,
  'lock', CAST(NULL AS JSON),
  'createBy', 'system',
  'updateBy', 'system',
  'path', '/scenario/list',
  'method', 'GET',
  'parameters', JSON_ARRAY(),
  'options', JSON_ARRAY(),
  'requestBody', CAST(NULL AS JSON),
  'headers', JSON_ARRAY(),
  'paths', JSON_ARRAY(),
  'responseBody', CAST(NULL AS JSON),
  'description', CAST(NULL AS JSON),
  'requestBodyDefinition', CAST(NULL AS JSON),
  'responseBodyDefinition', CAST(NULL AS JSON)
)
WHERE file_path = 'magic-api/api/对外接口/外部医疗场景清单.ms';

UPDATE magic_api_file
SET file_content = JSON_OBJECT(
  'properties', JSON_OBJECT(),
  'id', 'external_bill_list',
  'script', CONCAT('import medicalApi;', CONVERT(CHAR(10) USING utf8mb4), 'return medicalApi.billList(header);'),
  'groupId', 'external_group',
  'name', '外部账单查询',
  'createTime', 1778663247156,
  'updateTime', UNIX_TIMESTAMP(NOW(3)) * 1000,
  'lock', CAST(NULL AS JSON),
  'createBy', 'system',
  'updateBy', 'system',
  'path', '/bill/list',
  'method', 'GET',
  'parameters', JSON_ARRAY(),
  'options', JSON_ARRAY(),
  'requestBody', CAST(NULL AS JSON),
  'headers', JSON_ARRAY(),
  'paths', JSON_ARRAY(),
  'responseBody', CAST(NULL AS JSON),
  'description', CAST(NULL AS JSON),
  'requestBodyDefinition', CAST(NULL AS JSON),
  'responseBodyDefinition', CAST(NULL AS JSON)
)
WHERE file_path = 'magic-api/api/对外接口/外部账单查询.ms';

UPDATE magic_api_file
SET file_content = JSON_OBJECT(
  'properties', JSON_OBJECT(),
  'id', 'external_query_log_list',
  'script', CONCAT('import medicalApi;', CONVERT(CHAR(10) USING utf8mb4), 'return medicalApi.queryLogList(header);'),
  'groupId', 'external_group',
  'name', '外部查询日志',
  'createTime', 1778663247164,
  'updateTime', UNIX_TIMESTAMP(NOW(3)) * 1000,
  'lock', CAST(NULL AS JSON),
  'createBy', 'system',
  'updateBy', 'system',
  'path', '/query-log/list',
  'method', 'GET',
  'parameters', JSON_ARRAY(),
  'options', JSON_ARRAY(),
  'requestBody', CAST(NULL AS JSON),
  'headers', JSON_ARRAY(),
  'paths', JSON_ARRAY(),
  'responseBody', CAST(NULL AS JSON),
  'description', CAST(NULL AS JSON),
  'requestBodyDefinition', CAST(NULL AS JSON),
  'responseBodyDefinition', CAST(NULL AS JSON)
)
WHERE file_path = 'magic-api/api/对外接口/外部查询日志.ms';

UPDATE magic_api_file
SET file_content = JSON_SET(file_content, '$.path', '/legacy/price/query')
WHERE file_path = 'magic-api/api/对外接口/外部价格查询.ms';

UPDATE magic_api_file
SET file_content = JSON_SET(file_content, '$.path', '/legacy/balance/query')
WHERE file_path = 'magic-api/api/对外接口/外部余额查询.ms';

UPDATE magic_api_file
SET file_content = JSON_SET(file_content, '$.path', '/legacy/medical/query')
WHERE file_path = 'magic-api/api/对外接口/外部医疗大数据模拟查询.ms';
