-- ----------------------------
-- AI Agent 控制台 菜单SQL
-- ----------------------------

-- 获取 AI治理 的 menu_id
SET @parentId = (SELECT menu_id FROM sys_menu WHERE menu_name = 'AI治理' AND parent_id = 0 LIMIT 1);

-- 1. 创建子菜单：AI Agent 控制台
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'AI Agent 控制台', @parentId, 4, 'agent', 'ai/agent/index', 'C', '0', '0', 'ai:agent:list', 'chat', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'AI Agent 控制台' AND parent_id = @parentId);

-- 获取 AI Agent 控制台 的 menu_id
SET @agentId = (SELECT menu_id FROM sys_menu WHERE menu_name = 'AI Agent 控制台' LIMIT 1);

-- 2. 子按钮: 查看
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'Agent查询', @agentId, 1, '', '', 'F', '0', '0', 'ai:agent:query', '#', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'Agent查询' AND parent_id = @agentId);

-- 3. 子按钮: 会话新增
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '会话新增', @agentId, 2, '', '', 'F', '0', '0', 'ai:agent:add', '#', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '会话新增' AND parent_id = @agentId);

-- 4. 子按钮: 会话删除
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '会话删除', @agentId, 3, '', '', 'F', '0', '0', 'ai:agent:remove', '#', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '会话删除' AND parent_id = @agentId);

-- 5. 子按钮: 发送消息
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '发送消息', @agentId, 4, '', '', 'F', '0', '0', 'ai:agent:chat', '#', 'admin', sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '发送消息' AND parent_id = @agentId);

-- 6. 为超级管理员(role_id=1)赋权
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'ai:agent:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);
