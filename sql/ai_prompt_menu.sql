-- ----------------------------
-- 提示词管理菜单配置
-- ----------------------------

-- 获取 AI治理 的 menu_id
SET @parentId = (SELECT menu_id FROM sys_menu WHERE menu_name = 'AI治理' AND parent_id = 0 LIMIT 1);

-- 创建子菜单：提示词管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '提示词管理', @parentId, 2, 'prompt', 'ai/prompt/index', 'C', '0', '0', 'ai:prompt:list', 'education', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '提示词管理' AND parent_id = @parentId);

-- 获取 提示词管理 的 menu_id
SET @promptId = (SELECT menu_id FROM sys_menu WHERE menu_name = '提示词管理' LIMIT 1);

-- 新增子按钮权限：AI生成
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'AI生成提示词', @promptId, 1, '', '', 'F', '0', '0', 'ai:prompt:generate', '#', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'AI生成提示词' AND parent_id = @promptId);

-- 给超级管理员添加所有提示词权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'ai:prompt:%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'ai:prompt:list' LIMIT 1));
