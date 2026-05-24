# LLM大模型配置 - 实施计划

**版本：** 1.0.0
**日期：** 2026-05-17
**设计文档：** `../plans/2026-05-17-llm-config-design.md`

---

## 实施流程

每个任务遵循 **TDD 流程**：写测试 → 验证失败 → 实现代码 → 验证通过 → 提交

---

## Task 1: 数据库层

### 1.1 创建数据库表

**文件：** `sql/ai_llm_config.sql`

```sql
-- 创建 LLM 大模型配置表
CREATE TABLE ai_llm_config (...);
```

**验证：** 执行 SQL，无错误

---

### 1.2 创建实体类

**文件：** `ruoyi-system/src/main/java/com/ruoyi/system/domain/AiLlmConfig.java`

**验证：** 编译通过

---

### 1.3 创建 Mapper 接口

**文件：** `ruoyi-system/src/main/java/com/ruoyi/system/mapper/AiLlmConfigMapper.java`

**验证：** MyBatis 扫描到

---

### 1.4 创建 Mapper XML

**文件：** `ruoyi-system/src/main/resources/mapper/system/AiLlmConfigMapper.xml`

**验证：** SQL 语法正确

---

## Task 2: 服务层

### 2.1 创建 Service 接口

**文件：** `ruoyi-system/src/main/java/com/ruoyi/system/service/IAiLlmConfigService.java`

**验证：** 编译通过

---

### 2.2 创建 Service 实现

**文件：** `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/AiLlmConfigServiceImpl.java`

**验证：** 编译通过

---

## Task 3: 控制层

### 3.1 创建 Controller

**文件：** `ruoyi-system/src/main/java/com/ruoyi/system/controller/system/AiLlmConfigController.java`

**验证：** REST 接口可访问

---

### 3.2 添加权限注解

**验证：** Swagger 文档显示权限标识

---

## Task 4: 前端 API

### 4.1 创建 API 文件

**文件：** `ruoyi-ui/src/api/ai/llmConfig.js`

**验证：** 文件创建成功

---

## Task 5: 前端页面

### 5.1 创建主页面

**文件：** `ruoyi-ui/src/views/ai/llmConfig/index.vue`

**包含：**
- 卡片列表展示
- 添加按钮
- 卡片交互（编辑/删除/测试）

**验证：** 页面渲染正常

---

### 5.2 创建添加/编辑对话框

**组件：** 内嵌于 index.vue

**验证：** 表单验证正常

---

### 5.3 创建模型选择对话框

**组件：** 内嵌于 index.vue

**验证：** 模型列表加载正常

---

## Task 6: 集成测试

### 6.1 测试完整流程

1. 添加配置 → 保存成功
2. 测试接入 → 获取模型列表
3. 选择模型 → 保存默认模型
4. 编辑配置 → 更新成功
5. 删除配置 → 确认删除

**验证：** 所有操作正常

---

## Task 7: 菜单配置

### 7.1 插入菜单数据

**文件：** `sql/ai_llm_config_menu.sql`

**验证：** 菜单出现在侧边栏

---

## 实施顺序

```
1. Task 1 (数据库层)
2. Task 2 (服务层)
3. Task 3 (控制层)
4. Task 4 (前端API)
5. Task 5 (前端页面)
6. Task 6 (集成测试)
7. Task 7 (菜单配置)
```

---

**状态：** 待执行
