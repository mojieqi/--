# LLM大模型配置 - 设计文档

**版本：** 1.0.0  
**日期：** 2026-05-17  
**状态：** 已批准  

---

## 1. 功能概述

### 1.1 需求描述

为 RuoYi 后台管理系统开发「AI治理」→「LLM大模型配置」功能模块，支持多配置管理，用户可配置不同的大模型服务商、API密钥、BaseURL，并通过"测试接入"功能自动获取可用模型列表。

### 1.2 用户决策

| 决策项 | 选择 | 说明 |
|--------|------|------|
| 数据存储 | **B - 独立表** | 新建 `ai_llm_config` 表 |
| 测试接入 | **A - 后端代理** | 前端 → 后端 → LLM API |
| 配置数量 | **B - 多配置** | 支持多模型配置，设默认项 |
| 界面风格 | **A - 卡片式** | 卡片展示，直观美观 |

---

## 2. 技术架构

### 2.1 技术栈

| 层级 | 技术选型 |
|------|----------|
| 前端框架 | Vue 2 + Element UI |
| 后端框架 | Spring Boot (RuoYi) |
| 数据库 | MySQL |
| API风格 | RESTful |

### 2.2 项目结构

```
ruoyi-ui/src/
├── api/
│   └── ai/
│       └── llmConfig.js          # LLM配置API
├── views/
│   └── ai/
│       └── llmConfig/
│           └── index.vue          # LLM配置主页面
│
ruoyi-system/src/main/java/com/ruoyi/
├── system/
│   ├── controller/system/
│   │   └── AiLlmConfigController.java
│   ├── service/
│   │   ├── IAiLlmConfigService.java
│   │   └── impl/AiLlmConfigServiceImpl.java
│   ├── mapper/
│   │   └── AiLlmConfigMapper.java
│   └── domain/
│       └── AiLlmConfig.java
```

---

## 3. 数据库设计

### 3.1 表结构：ai_llm_config

```sql
CREATE TABLE ai_llm_config (
  config_id        BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '配置ID',
  config_name      VARCHAR(100)    NOT NULL                   COMMENT '配置名称',
  provider         VARCHAR(50)      NOT NULL                   COMMENT '服务商代码(qwen/deepseek/openai/anthropic/google/doubao/zhipu/openrouter)',
  provider_name    VARCHAR(100)     NOT NULL                   COMMENT '服务商显示名称',
  base_url         VARCHAR(500)    DEFAULT NULL                COMMENT 'API BaseURL',
  api_key          VARCHAR(500)    DEFAULT NULL                COMMENT 'API密钥(加密存储)',
  default_model    VARCHAR(100)    DEFAULT NULL                COMMENT '默认模型',
  status           CHAR(1)         DEFAULT '0'                 COMMENT '状态(0正常 1停用)',
  is_default       CHAR(1)         DEFAULT '0'                 COMMENT '是否默认(0否 1是)',
  sort             INT(4)          DEFAULT 0                   COMMENT '显示顺序',
  remark           VARCHAR(500)    DEFAULT NULL                COMMENT '备注',
  create_by        VARCHAR(64)     DEFAULT ''                  COMMENT '创建者',
  create_time      DATETIME                                    COMMENT '创建时间',
  update_by        VARCHAR(64)     DEFAULT ''                  COMMENT '更新者',
  update_time      DATETIME                                    COMMENT '更新时间',
  PRIMARY KEY (config_id)
) ENGINE=InnoDB COMMENT='LLM大模型配置表';
```

### 3.2 服务商配置映射

| provider | provider_name | 默认BaseURL |
|----------|---------------|-------------|
| qwen | 通义千问 | https://dashscope.aliyuncs.com/compatible-mode/v1 |
| deepseek | Deepseek | https://api.deepseek.com |
| openai | OpenAI | https://api.openai.com/v1 |
| anthropic | Anthropic | https://api.anthropic.com |
| google | Google | https://generativelanguage.googleapis.com/v1beta |
| doubao | 豆包 | https://ark.cn-beijing.volces.com/api/v3 |
| zhipu | 智谱AI | https://open.bigmodel.cn/api/paas/v4 |
| openrouter | OpenRouter | https://openrouter.ai/api/v1 |

---

## 4. 页面设计

### 4.1 页面布局

```
┌────────────────────────────────────────────────────────────────────┐
│ AI治理 / LLM大模型配置                                               │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌─────────────────────────────┐  ┌─────────────────────────────┐ │
│  │ 通义千问 (生产环境)    [默认]│  │ Deepseek (测试环境)          │ │
│  │ ● 启用                     │  │ ○ 禁用                       │ │
│  │                            │  │                              │ │
│  │ BaseURL: dashscope...      │  │ BaseURL: api.deepseek.com    │ │
│  │ API Key: •••••••••••      │  │ API Key: •••••••••••         │ │
│  │ 默认模型: qwen-turbo        │  │ 默认模型: -                   │ │
│  │                            │  │                              │ │
│  │ [测试接入] [编辑] [删除]   │  │ [测试接入] [编辑] [删除]      │ │
│  └─────────────────────────────┘  └─────────────────────────────┘ │
│                                                                    │
│  ┌─────────────────────────────┐  ┌─────────────────────────────┐ │
│  │ OpenAI                     │  │   + 添加新配置                 │ │
│  │ ○ 禁用                     │  │                              │ │
│  │                            │  │   点击添加新的LLM服务商配置     │ │
│  │ [测试接入] [编辑] [删除]   │  │                              │ │
│  └─────────────────────────────┘  └─────────────────────────────┘ │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

### 4.2 组件说明

#### 4.2.1 卡片组件 (LlmConfigCard)

| 字段 | 说明 |
|------|------|
| configName | 配置名称（用户自定义） |
| providerName | 服务商显示名 |
| status | 启用/禁用状态指示 |
| baseUrl | BaseURL（截断显示） |
| apiKey | API Key（脱敏显示） |
| defaultModel | 默认模型 |
| isDefault | 默认标识 |

**卡片交互：**
- 点击"测试接入"：调用后端接口获取模型列表，弹出选择框
- 点击"编辑"：打开编辑对话框
- 点击"删除"：二次确认后删除
- 点击"设为默认"：切换默认配置

#### 4.2.2 添加/编辑对话框 (LlmConfigDialog)

```
┌─────────────────────────────────────────────────────────┐
│  添加LLM配置                                     [×]   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  配置名称 *  │  [通义千问生产环境                    ]  │
│                                                         │
│  服务商 *   │  [通义千问                          ▼]   │
│                                                         │
│  BaseURL   │  [https://dashscope.aliyuncs.com/...   ]  │
│                                                         │
│  API Key * │  [•••••••••••••••••••••              ]   │
│                                                         │
│  备注      │  [生产环境使用的通义千问配置            ]   │
│                                                         │
│                          [取消]  [确定]                  │
└─────────────────────────────────────────────────────────┘
```

#### 4.2.3 模型选择对话框 (ModelSelectDialog)

```
┌─────────────────────────────────────────────────────────┐
│  测试接入 - 选择模型                               [×]  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  点击"测试接入"后自动获取可用模型：                     │
│                                                         │
│  ○ qwen-turbo       通义千问Turbo (默认)               │
│  ○ qwen-plus       通义千问Plus                       │
│  ○ qwen-max        通义千问Max                        │
│  ○ qwen-max-long   通义千问Long                       │
│                                                         │
│  ⚠ 如果列表为空，请检查 BaseURL 和 API Key 是否正确     │
│                                                         │
│         [取消]  [确认选择]                              │
└─────────────────────────────────────────────────────────┘
```

---

## 5. API设计

### 5.1 前端 → 后端

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 列表 | GET | /ai/llm/config/list | 获取配置列表 |
| 详情 | GET | /ai/llm/config/{id} | 获取配置详情 |
| 新增 | POST | /ai/llm/config | 新增配置 |
| 修改 | PUT | /ai/llm/config | 修改配置 |
| 删除 | DELETE | /ai/llm/config/{id} | 删除配置 |
| 测试 | POST | /ai/llm/config/test | 测试接入获取模型列表 |
| 设置默认 | PUT | /ai/llm/config/default/{id} | 设为默认配置 |

### 5.2 后端 → LLM API

```
POST {baseUrl}/models
Headers:
  Authorization: Bearer {apiKey}
```

---

## 6. 安全设计

### 6.1 API Key 加密

- 存储时使用 AES 加密
- 返回前端时脱敏显示
- 仅管理员可见完整 Key（可选）

### 6.2 权限控制

| 权限标识 | 说明 |
|----------|------|
| ai:llm:list | 查看配置列表 |
| ai:llm:add | 新增配置 |
| ai:llm:edit | 编辑配置 |
| ai:llm:remove | 删除配置 |

---

## 7. 商务风格设计规范

### 7.1 色彩方案

| 用途 | 色值 |
|------|------|
| 主色 | #409EFF (Element UI Primary) |
| 成功 | #67C23A |
| 警告 | #E6A23C |
| 危险 | #F56C6C |
| 信息 | #909399 |
| 背景 | #F5F7FA |
| 文字主色 | #303133 |
| 文字次色 | #606266 |

### 7.2 卡片样式

- 圆角：8px
- 阴影：0 2px 12px rgba(0, 0, 0, 0.1)
- 内边距：20px
- 卡片宽度：280px ~ 350px
- 卡片间距：20px

### 7.3 服务商图标

使用简洁的文字标签或 SVG 图标，配合品牌色。

---

## 8. 菜单配置

```sql
-- 菜单 SQL（需插入 sys_menu 表）
-- 父菜单：AI治理 (假设 menu_id = 2000)
INSERT INTO sys_menu VALUES (
  2001, 'LLM大模型配置', 2000, 1, 'ai/llmConfig',
  'ai/llmConfig/index', NULL, 'ai-llm-config', 1, 0, 'C', '0', '0',
  'ai:llm:list', '的大脑',
  'admin', sysdate(), '', null
);
```

---

## 9. 验收标准

- [ ] 支持添加/编辑/删除 LLM 配置
- [ ] 支持选择 8 种主流服务商
- [ ] BaseURL 根据服务商自动适配
- [ ] API Key 安全存储（加密）
- [ ] "测试接入"功能正常工作
- [ ] 支持多配置管理和默认设置
- [ ] 界面风格简洁商务
- [ ] 权限控制正常
- [ ] 响应式布局适配

---

**文档状态：** 已批准 → 进入 Phase 2
