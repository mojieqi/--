# Phase 3: AI Agent 控制台 — 设计方案

**版本：** 1.0.0  
**日期：** 2026-05-23  
**状态：** 开发中  
**依赖：** Phase 0 (LLM配置) + Phase 1 (提示词管理) + Phase 2 (知识库管理)

---

## 一、方案决策

| 维度 | 决策 | 理由 |
|------|------|------|
| SSE 流式响应 | SseEmitter (方案A) | Spring MVC 原生，零依赖，与现有架构无缝集成 |
| 知识库检索 | 完整向量化 (方案A) | LLM Embedding API + 余弦相似度，保证 RAG 质量 |
| 前端组件 | 6 组件完整拆分 (方案A) | Agent 页面复杂度高，必须组件化 |
| 记忆管理 | 仅短期记忆 (方案B) | 窗口管理+摘要压缩，长期记忆 Phase 4 |

---

## 二、架构设计

```
前端 (Vue 6组件) → EventSource SSE → AiAgentController
                                          ↓
                                   AiAgentService (编排)
                                   ├── MemoryManager (窗口管理+摘要压缩)
                                   ├── PromptBuilder (系统提示词+上下文拼装)
                                   ├── EmbeddingService (向量化+相似度搜索)
                                   └── LlmCaller (RestTemplate → LLM API → SseEmitter转发)
```

---

## 三、数据库设计

```sql
-- 会话表
CREATE TABLE ai_conversation (
  conversation_id   BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '会话ID',
  title             VARCHAR(200) DEFAULT '新对话'          COMMENT '会话标题',
  llm_config_id     BIGINT(20)   NOT NULL                 COMMENT '使用的LLM配置ID',
  prompt_template_id BIGINT(20)  DEFAULT NULL             COMMENT '使用的提示词模板ID',
  kb_id             BIGINT(20)   DEFAULT NULL             COMMENT '关联知识库ID',
  memory_mode       CHAR(1)      DEFAULT '1'              COMMENT '记忆模式(0无记忆 1短期记忆 2长期记忆)',
  max_tokens        INT(11)      DEFAULT 4096             COMMENT '上下文最大Token数',
  message_count     INT(11)      DEFAULT 0                COMMENT '消息数量',
  status            CHAR(1)      DEFAULT '0'              COMMENT '状态(0进行中 1已完成)',
  create_by         VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  create_time       DATETIME                              COMMENT '创建时间',
  update_by         VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  update_time       DATETIME                              COMMENT '更新时间',
  PRIMARY KEY (conversation_id),
  KEY idx_create_by (create_by)
) ENGINE=InnoDB COMMENT='AI对话会话表';

-- 会话消息表
CREATE TABLE ai_conversation_message (
  message_id      BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '消息ID',
  conversation_id BIGINT(20)   NOT NULL                 COMMENT '会话ID',
  role            VARCHAR(20)  NOT NULL                 COMMENT '角色(system/user/assistant/tool)',
  content         LONGTEXT     NOT NULL                 COMMENT '消息内容',
  token_count     INT(11)      DEFAULT 0                COMMENT 'Token数量',
  tool_calls      JSON         DEFAULT NULL             COMMENT '工具调用(JSON)',
  metadata        JSON         DEFAULT NULL             COMMENT '元数据(含知识库引用等)',
  create_time     DATETIME                              COMMENT '创建时间',
  PRIMARY KEY (message_id),
  KEY idx_conversation_id (conversation_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='AI对话消息表';

-- 知识库引用表
CREATE TABLE ai_message_kb_ref (
  ref_id          BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '引用ID',
  message_id      BIGINT(20)   NOT NULL                 COMMENT '消息ID',
  chunk_id        BIGINT(20)   NOT NULL                 COMMENT '分块ID',
  relevance_score DECIMAL(5,4) DEFAULT 0                COMMENT '相关度评分',
  PRIMARY KEY (ref_id),
  KEY idx_message_id (message_id)
) ENGINE=InnoDB COMMENT='消息知识库引用表';

-- 用户长期记忆表（Phase 3 仅建表，Phase 4 实现逻辑）
CREATE TABLE ai_user_memory (
  memory_id       BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '记忆ID',
  user_id         BIGINT(20)   NOT NULL                 COMMENT '用户ID',
  memory_key      VARCHAR(100) NOT NULL                 COMMENT '记忆键',
  memory_value    TEXT         NOT NULL                 COMMENT '记忆内容',
  importance      INT(4)       DEFAULT 1                COMMENT '重要性(1-5)',
  last_access     DATETIME                              COMMENT '最后访问时间',
  access_count    INT(11)      DEFAULT 0                COMMENT '访问次数',
  create_time     DATETIME                              COMMENT '创建时间',
  PRIMARY KEY (memory_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='AI用户长期记忆表';
```

---

## 四、后端文件清单

```
ruoyi-system/src/main/java/com/ruoyi/system/
├── domain/
│   ├── AiConversation.java
│   ├── AiConversationMessage.java
│   ├── AiUserMemory.java
│   └── AiMessageKbRef.java
├── mapper/
│   ├── AiConversationMapper.java + .xml
│   ├── AiConversationMessageMapper.java + .xml
│   ├── AiUserMemoryMapper.java + .xml
│   └── AiMessageKbRefMapper.java + .xml
├── service/
│   ├── IAiConversationService.java
│   ├── IAiAgentService.java
│   ├── IAiMemoryService.java
│   ├── IEmbeddingService.java
│   └── impl/
│       ├── AiConversationServiceImpl.java
│       ├── AiAgentServiceImpl.java
│       ├── AiMemoryServiceImpl.java
│       └── EmbeddingServiceImpl.java
├── controller/system/
│   └── AiAgentController.java
└── agent/
    ├── AgentEngine.java
    ├── AgentContext.java
    ├── MemoryManager.java
    ├── PromptBuilder.java
    └── LlmCaller.java
```

---

## 五、API 设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 会话列表 | GET | /ai/agent/conversation/list | 当前用户会话 |
| 新建会话 | POST | /ai/agent/conversation | 创建会话 |
| 删除会话 | DELETE | /ai/agent/conversation/{id} | 删除+级联消息 |
| 消息列表 | GET | /ai/agent/message/list/{conversationId} | 历史消息 |
| 发送消息(SSE) | POST | /ai/agent/chat | 流式对话 |
| 停止生成 | POST | /ai/agent/chat/stop | 中断SSE流 |
| 清空上下文 | POST | /ai/agent/conversation/{id}/clear | 清空消息重置上下文 |
| 知识库检索 | POST | /ai/agent/search | 向量化语义搜索 |

---

## 六、前端文件清单

```
views/ai/agent/
├── index.vue                     # 主页面（三栏布局编排）
└── components/
    ├── ConversationSidebar.vue   # 左侧会话列表
    ├── ChatWindow.vue            # 中间对话窗口
    ├── AgentSettings.vue         # 右侧设置面板
    ├── MessageBubble.vue         # 消息气泡（含工具调用展示）
    ├── ModelSelector.vue         # 模型/知识库选择器
    └── ToolResultCard.vue        # 工具调用结果卡片

api/ai/
└── agent.js                      # Agent API 封装
```

---

## 七、开发任务顺序

| 序号 | 任务 | 预估 |
|:--:|------|:--:|
| 1 | SQL 建表脚本 + 菜单权限 | 1 |
| 2 | Domain 实体类 (4个) | 1 |
| 3 | Mapper 接口 + XML (4组) | 1 |
| 4 | Agent 核心引擎 (Engine/Context/MemoryManager/PromptBuilder/LlmCaller) | 3 |
| 5 | EmbeddingService (向量化+相似度) | 2 |
| 6 | Service 层 (3个服务+实现) | 2 |
| 7 | Controller (SSE流式端点) | 2 |
| 8 | 前端 API 层 + 6组件 + 主页面 | 4 |
| 9 | 集成测试 + 编译验证 | 1 |
| **合计** | | **17** |

---

**文档状态：** 设计已确认 → 开发中
