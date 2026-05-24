-- ----------------------------
-- AI工具管理 菜单SQL
-- Phase 4: 工具与联网检索
-- ----------------------------

-- 获取 AI治理 的 menu_id
SET @parentId = (SELECT menu_id FROM sys_menu WHERE menu_name = 'AI治理' AND parent_id = 0 LIMIT 1);

-- 1. 创建子菜单：工具管理 (order_num=5，排在LLM配置=1、提示词=2、知识库=3、Agent=4之后)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '工具管理', @parentId, 5, 'tool', 'ai/tool/index', 'C', '0', '0', 'ai:tool:list', 'tool', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '工具管理' AND parent_id = @parentId);

-- 获取工具管理的 menu_id
SET @toolId = (SELECT menu_id FROM sys_menu WHERE menu_name = '工具管理' AND parent_id = @parentId LIMIT 1);

-- 2. 子按钮: 工具查询
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '工具查询', @toolId, 1, '', 'F', '0', '0', 'ai:tool:query', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'ai:tool:query');

-- 3. 子按钮: 工具新增
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '工具新增', @toolId, 2, '', 'F', '0', '0', 'ai:tool:add', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'ai:tool:add');

-- 4. 子按钮: 工具修改
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '工具修改', @toolId, 3, '', 'F', '0', '0', 'ai:tool:edit', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'ai:tool:edit');

-- 5. 子按钮: 工具删除
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '工具删除', @toolId, 4, '', 'F', '0', '0', 'ai:tool:remove', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'ai:tool:remove');

-- 6. 子按钮: 启用/停用
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '工具状态变更', @toolId, 5, '', 'F', '0', '0', 'ai:tool:changeStatus', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'ai:tool:changeStatus');

-- 7. 给超级管理员(role_id=1)添加所有工具管理权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'ai:tool:%'
AND NOT EXISTS (
  SELECT 1 FROM sys_role_menu WHERE role_id = 1
  AND menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'ai:tool:list' LIMIT 1)
);
