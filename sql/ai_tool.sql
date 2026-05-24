-- ----------------------------
-- AI工具管理 建表脚本
-- Phase 4: 工具与联网检索
-- ----------------------------

DROP TABLE IF EXISTS ai_tool;
CREATE TABLE ai_tool (
  tool_id         BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '工具ID',
  tool_name       VARCHAR(100) NOT NULL                 COMMENT '工具名称',
  tool_code       VARCHAR(50)  NOT NULL                 COMMENT '工具代码(web_search/kb_query/date_calc/content_audit)',
  tool_desc       VARCHAR(500) DEFAULT NULL             COMMENT '工具描述',
  function_schema JSON         NOT NULL                 COMMENT 'Function Calling Schema定义(JSON格式)',
  handler_class   VARCHAR(200) NOT NULL                 COMMENT '处理器类全限定名',
  is_builtin      CHAR(1)      DEFAULT '0'              COMMENT '是否内置(0否 1是)',
  status          CHAR(1)      DEFAULT '0'              COMMENT '状态(0启用 1停用)',
  sort            INT(4)       DEFAULT 0                COMMENT '显示排序',
  remark          VARCHAR(500) DEFAULT NULL             COMMENT '备注',
  create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  create_time     DATETIME                              COMMENT '创建时间',
  update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  update_time     DATETIME                              COMMENT '更新时间',
  PRIMARY KEY (tool_id),
  UNIQUE KEY uk_tool_code (tool_code)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI工具注册表';

-- ----------------------------
-- 内置工具数据
-- ----------------------------

-- 1. 联网搜索 (面向 Phase 4.2 WebSearchTool)
INSERT INTO ai_tool (tool_name, tool_code, tool_desc, function_schema, handler_class, is_builtin, status, sort, create_by, create_time)
VALUES (
  '联网搜索',
  'web_search',
  '通过DuckDuckGo搜索引擎检索互联网最新信息，支持获取实时新闻、百科知识等',
  '{"name":"web_search","description":"在互联网上搜索实时信息，获取最新的新闻、百科、天气等内容","parameters":{"type":"object","properties":{"query":{"type":"string","description":"搜索关键词或问题，请使用简洁准确的查询语句"}},"required":["query"]}}',
  'com.ruoyi.system.agent.tool.WebSearchTool',
  '1', '0', 1, 'admin', sysdate()
);

-- 2. 知识库查询 (面向 Phase 4.2 KbQueryTool，对接现有 EmbeddingService)
INSERT INTO ai_tool (tool_name, tool_code, tool_desc, function_schema, handler_class, is_builtin, status, sort, create_by, create_time)
VALUES (
  '知识库查询',
  'kb_query',
  '在项目知识库中搜索相关文档内容，基于向量相似度匹配，支持语义搜索',
  '{"name":"kb_query","description":"在知识库中搜索相关文档内容，返回与问题语义最匹配的文档片段","parameters":{"type":"object","properties":{"keyword":{"type":"string","description":"搜索关键词或自然语言问题"},"topK":{"type":"integer","description":"返回结果数量，默认3条"}},"required":["keyword"]}}',
  'com.ruoyi.system.agent.tool.KbQueryTool',
  '1', '0', 2, 'admin', sysdate()
);

-- 3. 日期计算
INSERT INTO ai_tool (tool_name, tool_code, tool_desc, function_schema, handler_class, is_builtin, status, sort, create_by, create_time)
VALUES (
  '日期计算',
  'date_calc',
  '计算日期差值、星期几、日期加减等常用日期操作，纯本地计算无网络依赖',
  '{"name":"date_calc","description":"执行日期相关计算，包括计算日期差值、获取某天是星期几、日期加减等操作","parameters":{"type":"object","properties":{"date1":{"type":"string","description":"第一个日期，格式yyyy-MM-dd"},"date2":{"type":"string","description":"第二个日期，格式yyyy-MM-dd，差值计算时使用"},"operation":{"type":"string","description":"操作类型","enum":["diff","weekday","add_days"]},"days":{"type":"integer","description":"加减天数，add_days操作时使用"}},"required":["date1","operation"]}}',
  'com.ruoyi.system.agent.tool.DateCalcTool',
  '1', '0', 3, 'admin', sysdate()
);

-- 4. 内容审核 (封装 Phase 3 已有审核逻辑为 Tool)
INSERT INTO ai_tool (tool_name, tool_code, tool_desc, function_schema, handler_class, is_builtin, status, sort, create_by, create_time)
VALUES (
  '内容审核',
  'content_audit',
  '审核校园墙帖子/评论内容是否合规，检测违规敏感词和不当内容',
  '{"name":"content_audit","description":"审核文本内容是否合规，检测违规词、敏感信息和不当表达","parameters":{"type":"object","properties":{"content":{"type":"string","description":"待审核的文本内容"},"scene":{"type":"string","description":"审核场景","enum":["post","comment","profile"]}},"required":["content"]}}',
  'com.ruoyi.system.agent.tool.ContentAuditTool',
  '1', '0', 4, 'admin', sysdate()
);
