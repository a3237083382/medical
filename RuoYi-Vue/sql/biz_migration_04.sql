-- Migration 04: 管理端日志管理、充值管理、扣费管理菜单

UPDATE sys_menu
SET menu_name = '充值管理'
WHERE menu_id = 2030;

UPDATE sys_menu
SET menu_name = '导出价目'
WHERE menu_id = 2025;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
SELECT 2040, '日志管理', 2000, 4, 'query-log', 'business/log/index', 1, 0, 'C', '0', '0', 'business:log:list', 'log', 'admin', sysdate(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2040);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
SELECT 2041, '查询日志详情', 2040, 1, NULL, NULL, 0, 0, 'F', '0', '0', 'business:log:query', '#', 'admin', sysdate(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2041);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
SELECT 2042, '导出日志', 2040, 2, NULL, NULL, 0, 0, 'F', '0', '0', 'business:log:export', '#', 'admin', sysdate(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2042);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
SELECT 2050, '扣费管理', 2000, 5, 'fee-flow', 'business/fee/index', 1, 0, 'C', '0', '0', 'business:fee:list', 'money', 'admin', sysdate(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2050);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
SELECT 2051, '查询扣费详情', 2050, 1, NULL, NULL, 0, 0, 'F', '0', '0', 'business:fee:query', '#', 'admin', sysdate(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2051);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
SELECT 2052, '导出扣费', 2050, 2, NULL, NULL, 0, 0, 'F', '0', '0', 'business:fee:export', '#', 'admin', sysdate(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2052);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (2040, 2041, 2042, 2050, 2051, 2052)
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );
