-- ----------------------------
-- AI Agent 控制台 - 建表脚本
-- ----------------------------

-- 会话表
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation` (
  `conversation_id`    BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '会话ID',
  `title`              VARCHAR(200) DEFAULT '新对话'          COMMENT '会话标题',
  `llm_config_id`      BIGINT(20)   NOT NULL                 COMMENT '使用的LLM配置ID',
  `prompt_template_id` BIGINT(20)   DEFAULT NULL             COMMENT '使用的提示词模板ID',
  `kb_id`              BIGINT(20)   DEFAULT NULL             COMMENT '关联知识库ID',
  `memory_mode`        CHAR(1)      DEFAULT '1'              COMMENT '记忆模式(0无记忆 1短期记忆 2长期记忆)',
  `max_tokens`         INT(11)      DEFAULT 4096             COMMENT '上下文最大Token数',
  `message_count`      INT(11)      DEFAULT 0                COMMENT '消息数量',
  `status`             CHAR(1)      DEFAULT '0'              COMMENT '状态(0进行中 1已完成)',
  `create_by`          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  `create_time`        DATETIME                              COMMENT '创建时间',
  `update_by`          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  `update_time`        DATETIME                              COMMENT '更新时间',
  PRIMARY KEY (`conversation_id`),
  KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='AI对话会话表';

-- 会话消息表
DROP TABLE IF EXISTS `ai_conversation_message`;
CREATE TABLE `ai_conversation_message` (
  `message_id`       BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '消息ID',
  `conversation_id`  BIGINT(20)   NOT NULL                 COMMENT '会话ID',
  `role`             VARCHAR(20)  NOT NULL                 COMMENT '角色(system/user/assistant/tool)',
  `content`          LONGTEXT     NOT NULL                 COMMENT '消息内容',
  `token_count`      INT(11)      DEFAULT 0                COMMENT 'Token数量',
  `tool_calls`       JSON         DEFAULT NULL             COMMENT '工具调用(JSON)',
  `metadata`         JSON         DEFAULT NULL             COMMENT '元数据(含知识库引用等)',
  `create_time`      DATETIME                              COMMENT '创建时间',
  PRIMARY KEY (`message_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='AI对话消息表';

-- 知识库引用表
DROP TABLE IF EXISTS `ai_message_kb_ref`;
CREATE TABLE `ai_message_kb_ref` (
  `ref_id`           BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '引用ID',
  `message_id`       BIGINT(20)   NOT NULL                 COMMENT '消息ID',
  `chunk_id`         BIGINT(20)   NOT NULL                 COMMENT '分块ID',
  `relevance_score`  DECIMAL(5,4) DEFAULT 0                COMMENT '相关度评分',
  PRIMARY KEY (`ref_id`),
  KEY `idx_message_id` (`message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='消息知识库引用表';

-- 用户长期记忆表 (Phase 3 建表, Phase 4 实现逻辑)
DROP TABLE IF EXISTS `ai_user_memory`;
CREATE TABLE `ai_user_memory` (
  `memory_id`        BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '记忆ID',
  `user_id`          BIGINT(20)   NOT NULL                 COMMENT '用户ID',
  `memory_key`       VARCHAR(100) NOT NULL                 COMMENT '记忆键(话题/偏好/关键信息)',
  `memory_value`     TEXT         NOT NULL                 COMMENT '记忆内容',
  `importance`       INT(4)       DEFAULT 1                COMMENT '重要性(1-5)',
  `last_access`      DATETIME                              COMMENT '最后访问时间',
  `access_count`     INT(11)      DEFAULT 0                COMMENT '访问次数',
  `create_time`      DATETIME                              COMMENT '创建时间',
  PRIMARY KEY (`memory_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='AI用户长期记忆表';
