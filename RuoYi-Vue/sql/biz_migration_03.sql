-- Migration 03: 修正业务管理菜单路由路径
-- RuoYi Vue3 顶级动态路由必须由后端生成 /business，顶级目录 is_frame 需要为 1。
-- 子菜单 path 只保留父级下的相对路径，组件路径仍指向实际 views 目录。

UPDATE `sys_menu`
SET `is_frame` = 1, `path` = 'business'
WHERE `menu_id` = 2000;

UPDATE `sys_menu`
SET `is_frame` = 1, `path` = 'company'
WHERE `menu_id` = 2010;

UPDATE `sys_menu`
SET `is_frame` = 1, `path` = 'price'
WHERE `menu_id` = 2020;

UPDATE `sys_menu`
SET `is_frame` = 1, `path` = 'recharge'
WHERE `menu_id` = 2030;
