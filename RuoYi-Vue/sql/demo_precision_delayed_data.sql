-- Precision-delayed workflow demo data for the existing test001 company.
-- Idempotent: request_no and batch_no guards make repeated execution safe.
-- Demo rows use zero fees and do not create billing logs or monthly usage records.

SET @demo_company_id = (
    SELECT id FROM biz_insurance_company WHERE username = 'test001' LIMIT 1
);

INSERT INTO biz_medical_query_request (
    request_no, company_id, entry_type, service_mode, query_type,
    patient_name, id_card, process_status, upload_status, result_status, view_status,
    price_config_id, reserved_fee, fee_snapshot, billing_month, query_log_id,
    request_ip, process_start_time, complete_time, version, create_time, update_time
)
SELECT seed.request_no, @demo_company_id, seed.entry_type, 'DELAYED', NULL,
       seed.patient_name, seed.id_card, seed.process_status, seed.upload_status,
       seed.result_status, seed.view_status, NULL, 0.00, 0.00,
       DATE_FORMAT(CURDATE(), '%Y-%m'), NULL, '127.0.0.1',
       seed.process_start_time, seed.complete_time, 0, seed.create_time, NOW()
FROM (
    SELECT 'DEMO-S-20260817-001' request_no, 'SINGLE' entry_type, '演示待处理' patient_name,
           '430102199001010011' id_card, 'PENDING' process_status, 'NOT_UPLOADED' upload_status,
           NULL result_status, 'READ' view_status, NULL process_start_time, NULL complete_time,
           NOW() - INTERVAL 70 MINUTE create_time
    UNION ALL
    SELECT 'DEMO-S-20260817-002', 'SINGLE', '演示处理中', '430102199001010022',
           'PROCESSING', 'NOT_UPLOADED', NULL, 'READ', NOW() - INTERVAL 45 MINUTE, NULL,
           NOW() - INTERVAL 60 MINUTE
    UNION ALL
    SELECT 'DEMO-S-20260817-003', 'SINGLE', '演示新结果', '430102199001010033',
           'COMPLETED', 'UPLOADED', 'HIT', 'UNREAD', NOW() - INTERVAL 35 MINUTE,
           NOW() - INTERVAL 20 MINUTE, NOW() - INTERVAL 50 MINUTE
    UNION ALL
    SELECT 'DEMO-S-20260817-004', 'SINGLE', '演示未查得', '430102199001010044',
           'COMPLETED', 'UPLOADED', 'NO_RESULT', 'READ', NOW() - INTERVAL 30 MINUTE,
           NOW() - INTERVAL 15 MINUTE, NOW() - INTERVAL 45 MINUTE
    UNION ALL
    SELECT 'DEMO-S-20260817-005', 'SINGLE', '演示已取消', '430102199001010055',
           'CANCELLED', 'NOT_UPLOADED', 'CANCELLED', 'READ', NULL, NULL,
           NOW() - INTERVAL 40 MINUTE
    UNION ALL
    SELECT 'DEMO-B-REQ-20260817-001', 'BATCH', '批次待处理', '430102199001010066',
           'PENDING', 'NOT_UPLOADED', NULL, 'READ', NULL, NULL,
           NOW() - INTERVAL 35 MINUTE
    UNION ALL
    SELECT 'DEMO-B-REQ-20260817-002', 'BATCH', '批次处理中', '430102199001010077',
           'PROCESSING', 'NOT_UPLOADED', NULL, 'READ', NOW() - INTERVAL 25 MINUTE, NULL,
           NOW() - INTERVAL 34 MINUTE
    UNION ALL
    SELECT 'DEMO-B-REQ-20260817-003', 'BATCH', '批次已查得', '430102199001010088',
           'COMPLETED', 'UPLOADED', 'HIT', 'UNREAD', NOW() - INTERVAL 24 MINUTE,
           NOW() - INTERVAL 10 MINUTE, NOW() - INTERVAL 33 MINUTE
    UNION ALL
    SELECT 'DEMO-B-REQ-20260817-004', 'BATCH', '批次已取消', '430102199001010099',
           'CANCELLED', 'NOT_UPLOADED', 'CANCELLED', 'READ', NULL, NULL,
           NOW() - INTERVAL 32 MINUTE
) seed
WHERE @demo_company_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM biz_medical_query_request existing
      WHERE existing.request_no = seed.request_no
  );

INSERT INTO biz_medical_query_result (
    request_id, result_source, column_schema, result_data, result_summary, version,
    uploaded_by, uploaded_time, update_by, update_time, update_reason, create_time
)
SELECT request.id, 'MANUAL',
       JSON_ARRAY(
           JSON_OBJECT('field', 'hospital', 'label', '医疗机构', 'order', 0),
           JSON_OBJECT('field', 'visitDate', 'label', '就诊日期', 'order', 1),
           JSON_OBJECT('field', 'diagnosis', 'label', '诊断结果', 'order', 2)
       ),
       JSON_OBJECT('records', JSON_ARRAY(
           JSON_OBJECT('hospital', '演示市第一医院', 'visitDate', '2026-06-18', 'diagnosis', '高血压随访'),
           JSON_OBJECT('hospital', '演示区人民医院', 'visitDate', '2026-07-03', 'diagnosis', '常规复查')
       )),
       '演示数据：共查得 2 条医疗记录', 1,
       'demo', NOW() - INTERVAL 20 MINUTE, 'demo', NOW() - INTERVAL 20 MINUTE, NULL,
       NOW() - INTERVAL 20 MINUTE
FROM biz_medical_query_request request
WHERE request.request_no = 'DEMO-S-20260817-003'
  AND NOT EXISTS (SELECT 1 FROM biz_medical_query_result result WHERE result.request_id = request.id);

INSERT INTO biz_medical_query_result (
    request_id, result_source, column_schema, result_data, result_summary, version,
    uploaded_by, uploaded_time, update_by, update_time, update_reason, create_time
)
SELECT request.id, 'MANUAL', JSON_ARRAY(JSON_OBJECT('field', 'result', 'label', '查询结果', 'order', 0)),
       JSON_OBJECT('records', JSON_ARRAY()), '演示数据：未查得相关医疗记录', 1,
       'demo', NOW() - INTERVAL 15 MINUTE, 'demo', NOW() - INTERVAL 15 MINUTE, NULL,
       NOW() - INTERVAL 15 MINUTE
FROM biz_medical_query_request request
WHERE request.request_no = 'DEMO-S-20260817-004'
  AND NOT EXISTS (SELECT 1 FROM biz_medical_query_result result WHERE result.request_id = request.id);

-- Processing draft. This row must remain invisible to insurance-company APIs.
INSERT INTO biz_medical_query_result (
    request_id, result_source, column_schema, result_data, result_summary, version,
    uploaded_by, uploaded_time, update_by, update_time, update_reason, create_time
)
SELECT request.id, 'MANUAL', JSON_ARRAY(JSON_OBJECT('field', 'draft', 'label', '草稿字段', 'order', 0)),
       JSON_OBJECT('records', JSON_ARRAY(JSON_OBJECT('draft', '后台演示草稿，不应对保险公司可见'))),
       '演示草稿', 1, NULL, NULL, 'demo', NOW() - INTERVAL 10 MINUTE, NULL,
       NOW() - INTERVAL 10 MINUTE
FROM biz_medical_query_request request
WHERE request.request_no = 'DEMO-S-20260817-002'
  AND NOT EXISTS (SELECT 1 FROM biz_medical_query_result result WHERE result.request_id = request.id);

INSERT INTO biz_medical_query_result (
    request_id, result_source, column_schema, result_data, result_summary, version,
    uploaded_by, uploaded_time, update_by, update_time, update_reason, create_time
)
SELECT request.id, 'MANUAL',
       JSON_ARRAY(
           JSON_OBJECT('field', 'hospital', 'label', '医疗机构', 'order', 0),
           JSON_OBJECT('field', 'diagnosis', 'label', '诊断结果', 'order', 1)
       ),
       JSON_OBJECT('records', JSON_ARRAY(
           JSON_OBJECT('hospital', '演示市中心医院', 'diagnosis', '门诊复查')
       )),
       '演示批次成员：查得 1 条记录', 1,
       'demo', NOW() - INTERVAL 10 MINUTE, 'demo', NOW() - INTERVAL 10 MINUTE, NULL,
       NOW() - INTERVAL 10 MINUTE
FROM biz_medical_query_request request
WHERE request.request_no = 'DEMO-B-REQ-20260817-003'
  AND NOT EXISTS (SELECT 1 FROM biz_medical_query_result result WHERE result.request_id = request.id);

INSERT INTO biz_medical_query_result (
    request_id, result_source, column_schema, result_data, result_summary, version,
    uploaded_by, uploaded_time, update_by, update_time, update_reason, create_time
)
SELECT request.id, 'MANUAL', JSON_ARRAY(JSON_OBJECT('field', 'draft', 'label', '草稿字段', 'order', 0)),
       JSON_OBJECT('records', JSON_ARRAY(JSON_OBJECT('draft', '批次成员后台草稿'))),
       '演示批次草稿', 1, NULL, NULL, 'demo', NOW() - INTERVAL 8 MINUTE, NULL,
       NOW() - INTERVAL 8 MINUTE
FROM biz_medical_query_request request
WHERE request.request_no = 'DEMO-B-REQ-20260817-002'
  AND NOT EXISTS (SELECT 1 FROM biz_medical_query_result result WHERE result.request_id = request.id);

INSERT INTO biz_medical_query_batch (
    batch_no, company_id, service_mode, query_type, batch_status,
    total_count, pending_count, processing_count, completed_count, hit_count,
    no_result_count, failed_count, cancelled_count, total_fee, request_ip,
    create_time, complete_time, update_time
)
SELECT 'DEMO-B-20260817-001', @demo_company_id, 'DELAYED', NULL, 'PROCESSING',
       4, 1, 1, 1, 1, 0, 0, 1, 0.00, '127.0.0.1',
       NOW() - INTERVAL 35 MINUTE, NULL, NOW()
WHERE @demo_company_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM biz_medical_query_batch WHERE batch_no = 'DEMO-B-20260817-001'
  );

INSERT INTO biz_medical_query_batch_item (
    batch_id, request_id, row_no, reused_flag, item_status, create_time, update_time
)
SELECT batch.id, request.id, seed.row_no, '0', seed.item_status,
       NOW() - INTERVAL 35 MINUTE, NOW()
FROM (
    SELECT 1 row_no, 'DEMO-B-REQ-20260817-001' request_no, 'ACTIVE' item_status
    UNION ALL SELECT 2, 'DEMO-B-REQ-20260817-002', 'ACTIVE'
    UNION ALL SELECT 3, 'DEMO-B-REQ-20260817-003', 'ACTIVE'
    UNION ALL SELECT 4, 'DEMO-B-REQ-20260817-004', 'CANCELLED'
) seed
INNER JOIN biz_medical_query_batch batch ON batch.batch_no = 'DEMO-B-20260817-001'
INNER JOIN biz_medical_query_request request ON request.request_no = seed.request_no
WHERE NOT EXISTS (
    SELECT 1 FROM biz_medical_query_batch_item item
    WHERE item.batch_id = batch.id AND item.row_no = seed.row_no
);

SELECT 'demo_request_count' AS item, COUNT(*) AS total
FROM biz_medical_query_request WHERE request_no LIKE 'DEMO-%'
UNION ALL
SELECT 'demo_batch_count', COUNT(*)
FROM biz_medical_query_batch WHERE batch_no LIKE 'DEMO-%'
UNION ALL
SELECT 'demo_unread_count', COUNT(*)
FROM biz_medical_query_request
WHERE company_id = @demo_company_id AND request_no LIKE 'DEMO-%' AND view_status = 'UNREAD';

-- Cleanup reference (run only when demo data is no longer needed):
-- DELETE result FROM biz_medical_query_result result
-- JOIN biz_medical_query_request request ON request.id = result.request_id
-- WHERE request.request_no LIKE 'DEMO-%';
-- DELETE item FROM biz_medical_query_batch_item item
-- JOIN biz_medical_query_batch batch ON batch.id = item.batch_id
-- WHERE batch.batch_no LIKE 'DEMO-%';
-- DELETE FROM biz_medical_query_batch WHERE batch_no LIKE 'DEMO-%';
-- DELETE FROM biz_medical_query_request WHERE request_no LIKE 'DEMO-%';
