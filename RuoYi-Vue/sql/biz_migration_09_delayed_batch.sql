-- 为精准延时查询保存批次编号，支持批次进度、取消和导出
set @batch_no_missing := (
    select count(1) = 0
    from information_schema.columns
    where table_schema = database()
      and table_name = 'biz_delayed_query_request'
      and column_name = 'batch_no'
);
set @batch_no_sql := if(
    @batch_no_missing,
    'alter table biz_delayed_query_request add column batch_no varchar(64) null comment ''批次编号'' after request_no',
    'select 1'
);
prepare batch_no_stmt from @batch_no_sql;
execute batch_no_stmt;
deallocate prepare batch_no_stmt;

set @batch_no_index_missing := (
    select count(1) = 0
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'biz_delayed_query_request'
      and index_name = 'idx_delayed_batch'
);
set @batch_no_index_sql := if(
    @batch_no_index_missing,
    'alter table biz_delayed_query_request add key idx_delayed_batch (company_id, batch_no)',
    'select 1'
);
prepare batch_no_index_stmt from @batch_no_index_sql;
execute batch_no_index_stmt;
deallocate prepare batch_no_index_stmt;
