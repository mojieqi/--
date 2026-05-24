-- ----------------------------
-- AI校园墙 菜单SQL
-- Phase 6: 帖子管理-前端路由菜单
-- ----------------------------

-- 创建顶级菜单: 校园墙
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '校园墙', 0, 3, 'campus', NULL, 'M', '0', '0', '', 'guide', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '校园墙' AND parent_id = 0);

-- 获取校园墙父菜单ID
SET @campusId = (SELECT menu_id FROM sys_menu WHERE menu_name = '校园墙' AND parent_id = 0 LIMIT 1);

-- 1. 帖子广场 (用户端浏览)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '帖子广场', @campusId, 1, 'square', 'campus/post/index', 'C', '0', '0', 'campus:post:list', 'list', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '帖子广场' AND parent_id = @campusId);

SET @squareId = (SELECT menu_id FROM sys_menu WHERE menu_name = '帖子广场' AND parent_id = @campusId LIMIT 1);

-- 帖子查询
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '帖子查询', @squareId, 1, '', 'F', '0', '0', 'campus:post:query', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:post:query');

-- 发布帖子
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '帖子新增', @squareId, 2, '', 'F', '0', '0', 'campus:post:add', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:post:add');

-- 编辑帖子
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '帖子编辑', @squareId, 3, '', 'F', '0', '0', 'campus:post:edit', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:post:edit');

-- 删除帖子
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '帖子删除', @squareId, 4, '', 'F', '0', '0', 'campus:post:remove', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:post:remove');

-- 2. 我的帖子 (用户个人)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '我的帖子', @campusId, 2, 'my-posts', 'campus/post/my', 'C', '0', '0', 'campus:post:my', 'user', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '我的帖子' AND parent_id = @campusId);


-- ----------------------------
-- 给超级管理员(role_id=1)添加所有校园墙权限
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'campus:post:%'
AND NOT EXISTS (
  SELECT 1 FROM sys_role_menu WHERE role_id = 1
  AND menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'campus:post:list' LIMIT 1)
);
