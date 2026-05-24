-- ----------------------------
-- AI校园墙 Phase 8 推荐-菜单+权限SQL
-- ----------------------------

-- 获取校园墙顶级菜单ID
SET @campusId = (SELECT menu_id FROM sys_menu WHERE menu_name = '校园墙' AND parent_id = 0 LIMIT 1);

-- =============================================
-- 3. 发现精彩 (推荐+热榜，放在帖子广场前面)
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '发现精彩', @campusId, 0, 'discover', 'campus/discover/index', 'C', '0', '0', 'campus:discover:view', 'discover', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '发现精彩' AND parent_id = @campusId);

SET @discoverId = (SELECT menu_id FROM sys_menu WHERE menu_name = '发现精彩' AND parent_id = @campusId LIMIT 1);

-- 发现-查询权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '发现查询', @discoverId, 1, '', 'F', '0', '0', 'campus:recommend:query', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:recommend:query');

-- 推荐榜单权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '榜单查看', @discoverId, 2, '', 'F', '0', '0', 'campus:recommend:hot', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:recommend:hot');

-- =============================================
-- 给超级管理员(role_id=1)添加推荐权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms IN ('campus:discover:view', 'campus:recommend:query', 'campus:recommend:hot')
AND NOT EXISTS (
  SELECT 1 FROM sys_role_menu WHERE role_id = 1
  AND menu_id IN (SELECT menu_id FROM sys_menu WHERE perms = 'campus:discover:view')
);
