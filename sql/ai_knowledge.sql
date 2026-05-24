-- ----------------------------
-- AI知识库表
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_base`;
CREATE TABLE `ai_knowledge_base` (
  `kb_id`           BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '知识库ID',
  `kb_name`         VARCHAR(100) NOT NULL                 COMMENT '知识库名称',
  `kb_desc`         VARCHAR(500) DEFAULT NULL             COMMENT '知识库描述',
  `embedding_model` VARCHAR(100) DEFAULT NULL             COMMENT '向量化模型',
  `doc_count`       INT(11)      DEFAULT 0                COMMENT '文档数量',
  `chunk_count`     INT(11)      DEFAULT 0                COMMENT '分块总数',
  `status`          CHAR(1)      DEFAULT '0'              COMMENT '状态(0正常 1停用)',
  `create_by`       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME                              COMMENT '创建时间',
  `update_by`       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME                              COMMENT '更新时间',
  PRIMARY KEY (`kb_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='AI知识库表';

-- ----------------------------
-- AI知识库文档表
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_document`;
CREATE TABLE `ai_knowledge_document` (
  `doc_id`          BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '文档ID',
  `kb_id`           BIGINT(20)   NOT NULL                 COMMENT '知识库ID',
  `doc_name`        VARCHAR(200) NOT NULL                 COMMENT '文档名称',
  `doc_type`        VARCHAR(20)  DEFAULT NULL             COMMENT '文档类型(txt/pdf/docx/xlsx/md)',
  `file_path`       VARCHAR(500) DEFAULT NULL             COMMENT '文件存储路径',
  `file_size`       BIGINT(20)   DEFAULT 0                COMMENT '文件大小(字节)',
  `content_text`    LONGTEXT     DEFAULT NULL             COMMENT '提取的文本内容',
  `chunk_count`     INT(11)      DEFAULT 0                COMMENT '分块数量',
  `status`          CHAR(1)      DEFAULT '0'              COMMENT '状态(0处理中 1完成 2失败)',
  `error_msg`       VARCHAR(500) DEFAULT NULL             COMMENT '错误信息',
  `create_by`       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME                              COMMENT '创建时间',
  PRIMARY KEY (`doc_id`),
  KEY `idx_kb_id` (`kb_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='AI知识库文档表';

-- ----------------------------
-- 文档分块表
-- ----------------------------
DROP TABLE IF EXISTS `ai_document_chunk`;
CREATE TABLE `ai_document_chunk` (
  `chunk_id`        BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '分块ID',
  `doc_id`          BIGINT(20)   NOT NULL                 COMMENT '文档ID',
  `kb_id`           BIGINT(20)   NOT NULL                 COMMENT '知识库ID',
  `chunk_index`     INT(11)      NOT NULL                 COMMENT '分块序号',
  `chunk_content`   TEXT         NOT NULL                 COMMENT '分块文本',
  `chunk_vector`    JSON         DEFAULT NULL             COMMENT '向量数据(JSON格式)',
  `token_count`     INT(11)      DEFAULT 0                COMMENT 'Token数量',
  PRIMARY KEY (`chunk_id`),
  KEY `idx_doc_id` (`doc_id`),
  KEY `idx_kb_id` (`kb_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='文档分块表';
