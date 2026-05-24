-- ----------------------------
-- AI知识库管理 菜单SQL
-- ----------------------------

-- 1. 知识库管理菜单 (父菜单 AI治理 menu_id=2000)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '知识库管理', 2000, 3, 'knowledge', 'ai/knowledge/index', 'C', '0', '0', 'ai:knowledge:list', '#', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_name = '知识库管理' AND parent_id = 2000
);

-- 2. 子按钮: 知识库查询
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '知识库查询', @kid := (SELECT menu_id FROM sys_menu WHERE menu_name = '知识库管理' AND parent_id = 2000), 1, '', 'F', '0', '0', 'ai:knowledge:query', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'ai:knowledge:query'
);

-- 3. 子按钮: 知识库新增
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '知识库新增', @kid, 2, '', 'F', '0', '0', 'ai:knowledge:add', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'ai:knowledge:add'
);

-- 4. 子按钮: 知识库修改
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '知识库修改', @kid, 3, '', 'F', '0', '0', 'ai:knowledge:edit', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'ai:knowledge:edit'
);

-- 5. 子按钮: 知识库删除
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '知识库删除', @kid, 4, '', 'F', '0', '0', 'ai:knowledge:remove', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'ai:knowledge:remove'
);

-- 6. 子按钮: 文档查询
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '文档查询', @kid, 5, '', 'F', '0', '0', 'ai:knowledge:doc:query', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'ai:knowledge:doc:query'
);

-- 7. 子按钮: 文档列表
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '文档列表', @kid, 6, '', 'F', '0', '0', 'ai:knowledge:doc:list', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'ai:knowledge:doc:list'
);

-- 8. 子按钮: 文档上传
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '文档上传', @kid, 7, '', 'F', '0', '0', 'ai:knowledge:doc:upload', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'ai:knowledge:doc:upload'
);

-- 9. 子按钮: 文档删除
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '文档删除', @kid, 8, '', 'F', '0', '0', 'ai:knowledge:doc:remove', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'ai:knowledge:doc:remove'
);

-- 10. 子按钮: 文档重新解析
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '文档重新解析', @kid, 9, '', 'F', '0', '0', 'ai:knowledge:doc:reparse', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'ai:knowledge:doc:reparse'
);

-- 11. 子按钮: 知识库检索
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '知识库检索', @kid, 10, '', 'F', '0', '0', 'ai:knowledge:search', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'ai:knowledge:search'
);

-- 12. 为超级管理员(role_id=1)赋权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'ai:knowledge%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);
