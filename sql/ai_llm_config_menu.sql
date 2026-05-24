-- ----------------------------
-- AI治理菜单配置
-- ----------------------------

-- 1. 首先创建父菜单：AI治理 (如果不存在)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'AI治理', 0, 8, 'ai', 'ai/index', 'M', '0', '0', '', '大脑', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'AI治理' AND parent_id = 0);

-- 获取 AI治理 的 menu_id
SET @parentId = (SELECT menu_id FROM sys_menu WHERE menu_name = 'AI治理' LIMIT 1);

-- 2. 创建子菜单：LLM大模型配置
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'LLM大模型配置', @parentId, 1, 'llmConfig', 'ai/llmConfig/index', 'C', '0', '0', 'ai:llm:list', '大脑', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'LLM大模型配置' AND parent_id = @parentId);

-- 3. 查看菜单权限（需要手动添加到角色）
-- 权限标识：
-- ai:llm:list   - 查看列表
-- ai:llm:add    - 新增配置
-- ai:llm:edit   - 修改配置
-- ai:llm:remove - 删除配置

-- 4. 给超级管理员添加所有权限（可选）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'ai:llm:%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'ai:llm:list' LIMIT 1));
