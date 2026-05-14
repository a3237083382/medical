-- Migration 05: 管理端统计仪表盘菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
SELECT 2060, '统计仪表盘', 2000, 1, 'business-dashboard', 'business/dashboard/index', 1, 0, 'C', '0', '0', 'business:dashboard:view', 'dashboard', 'admin', sysdate(), '', NULL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2060);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2060
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu
    WHERE role_id = 1 AND menu_id = 2060
);
