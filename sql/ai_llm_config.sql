-- ----------------------------
-- LLM大模型配置表
-- ----------------------------
-- 如果表已存在则删除
DROP TABLE IF EXISTS ai_llm_config;

-- 创建表
CREATE TABLE ai_llm_config (
  config_id        BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '配置ID',
  config_name      VARCHAR(100)    NOT NULL                   COMMENT '配置名称',
  provider         VARCHAR(50)     NOT NULL                   COMMENT '服务商代码',
  provider_name    VARCHAR(100)    NOT NULL                   COMMENT '服务商显示名称',
  base_url         VARCHAR(500)    DEFAULT NULL                COMMENT 'API BaseURL',
  api_key          VARCHAR(500)    DEFAULT NULL                COMMENT 'API密钥(加密存储)',
  default_model    VARCHAR(100)   DEFAULT NULL                COMMENT '默认模型',
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

-- ----------------------------
-- 初始化数据
-- ----------------------------
INSERT INTO ai_llm_config VALUES
  (1, '通义千问', 'qwen', '通义千问', 'https://dashscope.aliyuncs.com/compatible-mode/v1', NULL, NULL, '0', '0', 1, '阿里云通义千问大模型', 'admin', sysdate(), '', null),
  (2, 'Deepseek', 'deepseek', 'Deepseek', 'https://api.deepseek.com', NULL, NULL, '0', '0', 2, 'Deepseek大模型', 'admin', sysdate(), '', null),
  (3, 'OpenAI', 'openai', 'OpenAI', 'https://api.openai.com/v1', NULL, NULL, '0', '0', 3, 'OpenAI GPT系列模型', 'admin', sysdate(), '', null);
