-- 精准延时查询：后台处理、结果返回、修改、计费、日志

create table if not exists biz_delayed_query_request (
    id bigint not null auto_increment comment '主键',
    request_no varchar(64) not null comment '请求编号',
    company_id bigint not null comment '保险公司ID',
    company_name_snapshot varchar(128) comment '公司名称快照',
    patient_name varchar(64) not null comment '姓名',
    id_card varchar(32) not null comment '身份证号',
    query_type varchar(64) not null default 'delayed_precise' comment '查询类型',
    query_status varchar(32) not null default 'PENDING' comment '查询状态',
    upload_status varchar(32) not null default 'NOT_UPLOADED' comment '上传状态',
    result_status varchar(32) comment '结果状态',
    result_message varchar(1000) comment '结果说明',
    submit_time datetime not null comment '提交时间',
    request_ip varchar(64) comment '请求IP',
    handler_id bigint comment '处理人ID',
    handler_name varchar(64) comment '处理人',
    handled_time datetime comment '处理时间',
    uploaded_time datetime comment '上传完毕时间',
    fee decimal(12,2) comment '本次费用',
    reserved_fee decimal(12,2) not null default 0.00 comment '预留费用快照',
    billing_month varchar(7) comment '账单月份',
    charged_flag char(1) not null default '0' comment '是否已入账',
    price_config_id bigint comment '价格配置ID',
    modify_by varchar(64) comment '修改人',
    modify_time datetime comment '修改时间',
    modify_reason varchar(500) comment '修改说明',
    remark varchar(500) comment '备注',
    create_by varchar(64) comment '创建者',
    create_time datetime comment '创建时间',
    update_by varchar(64) comment '更新者',
    update_time datetime comment '更新时间',
    primary key (id),
    unique key uk_delayed_request_no (request_no),
    key idx_delayed_company_patient_status (company_id, patient_name, id_card, query_status, upload_status),
    key idx_delayed_company_submit (company_id, submit_time),
    key idx_delayed_status (query_status, upload_status, result_status)
) engine=innodb default charset=utf8mb4 comment='精准延时查询请求表';

set @reserved_fee_missing := (
    select count(1) = 0
    from information_schema.columns
    where table_schema = database()
      and table_name = 'biz_delayed_query_request'
      and column_name = 'reserved_fee'
);
set @reserved_fee_sql := if(
    @reserved_fee_missing,
    'alter table biz_delayed_query_request add column reserved_fee decimal(12,2) not null default 0.00 comment ''预留费用快照'' after fee',
    'select 1'
);
prepare reserved_fee_stmt from @reserved_fee_sql;
execute reserved_fee_stmt;
deallocate prepare reserved_fee_stmt;

create table if not exists biz_delayed_query_result (
    id bigint not null auto_increment comment '主键',
    request_id bigint not null comment '请求ID',
    row_no int not null comment '行号',
    raw_json json not null comment '动态结果行',
    create_by varchar(64) comment '创建者',
    create_time datetime comment '创建时间',
    update_by varchar(64) comment '更新者',
    update_time datetime comment '更新时间',
    primary key (id),
    key idx_delayed_result_request (request_id, row_no)
) engine=innodb default charset=utf8mb4 comment='精准延时查询结果明细表';

create table if not exists biz_delayed_query_import (
    id bigint not null auto_increment comment '主键',
    request_id bigint not null comment '请求ID',
    file_name varchar(255) comment '文件名',
    file_size bigint comment '文件大小',
    total_rows int default 0 comment '总行数',
    success_rows int default 0 comment '成功行数',
    failed_rows int default 0 comment '失败行数',
    status char(1) default '0' comment '状态',
    error_msg text comment '错误信息',
    create_by varchar(64) comment '创建者',
    create_time datetime comment '创建时间',
    primary key (id),
    key idx_delayed_import_request (request_id)
) engine=innodb default charset=utf8mb4 comment='精准延时查询导入记录';

insert into biz_query_price (query_type, query_name, fee, status, remark, create_by, create_time)
select 'delayed_precise', '精准延时数据查询', 20.00, '0', '精准延时查询上传完毕后按查得计费，未查得不扣费', 'system', sysdate()
where not exists (select 1 from biz_query_price where query_type = 'delayed_precise');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
select '精准延时查询', menu_id, 7, 'delayed-query', 'business/delayed-query/index', 1, 0, 'C', '0', '0', 'business:delayed-query:list', 'table', 'system', sysdate()
from sys_menu
where menu_name = '业务管理'
  and not exists (select 1 from sys_menu where perms = 'business:delayed-query:list');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
select '精准延时查询查询', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'business:delayed-query:query', '#', 'system', sysdate()
from sys_menu
where perms = 'business:delayed-query:list'
  and not exists (select 1 from sys_menu where perms = 'business:delayed-query:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
select '精准延时查询处理', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'business:delayed-query:edit', '#', 'system', sysdate()
from sys_menu
where perms = 'business:delayed-query:list'
  and not exists (select 1 from sys_menu where perms = 'business:delayed-query:edit');
