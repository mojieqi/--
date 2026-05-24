-- ----------------------------
-- AI校园墙 互动功能-菜单权限SQL
-- Phase 7: 评论、点赞、举报
-- ----------------------------

-- 获取校园墙父菜单ID
SET @campusId = (SELECT menu_id FROM sys_menu WHERE menu_name = '校园墙' AND parent_id = 0 LIMIT 1);

-- ==================== 评论管理菜单 ====================

-- 评论管理(用户端)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '评论查询', @campusId, 5, '', 'F', '0', '0', 'campus:comment:query', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:comment:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '评论新增', @campusId, 6, '', 'F', '0', '0', 'campus:comment:add', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:comment:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '评论删除', @campusId, 7, '', 'F', '0', '0', 'campus:comment:remove', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:comment:remove');

-- ==================== 点赞管理菜单 ====================

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '点赞操作', @campusId, 8, '', 'F', '0', '0', 'campus:like:operate', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:like:operate');

-- ==================== 举报管理菜单 ====================

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '举报新增', @campusId, 9, '', 'F', '0', '0', 'campus:report:add', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:report:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '举报查询', @campusId, 10, '', 'F', '0', '0', 'campus:report:query', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'campus:report:query');

-- ==================== 给超级管理员(role_id=1)添加所有互动权限 ====================

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms IN (
  'campus:comment:query', 'campus:comment:add', 'campus:comment:remove',
  'campus:like:operate',
  'campus:report:add', 'campus:report:query'
)
AND NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm2 WHERE rm2.role_id = 1
  AND rm2.menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms IN (
      'campus:comment:query', 'campus:comment:add', 'campus:comment:remove',
      'campus:like:operate',
      'campus:report:add', 'campus:report:query'
    )
  ) LIMIT 1
);
