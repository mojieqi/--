-- ----------------------------
-- AI校园墙 互动功能建表脚本
-- Phase 7: 评论、点赞、举报
-- ----------------------------

DROP TABLE IF EXISTS cw_report;
DROP TABLE IF EXISTS cw_like;
DROP TABLE IF EXISTS cw_comment;

-- 1. 评论表
CREATE TABLE cw_comment (
  comment_id      BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '评论ID',
  post_id         BIGINT(20)   NOT NULL                 COMMENT '帖子ID',
  user_id         BIGINT(20)   NOT NULL                 COMMENT '评论用户ID',
  parent_id       BIGINT(20)   DEFAULT 0                COMMENT '父评论ID(0表示一级评论)',
  reply_to_id     BIGINT(20)   DEFAULT NULL             COMMENT '回复目标评论ID',
  reply_to_uid    BIGINT(20)   DEFAULT NULL             COMMENT '回复目标用户ID',
  reply_to_name   VARCHAR(64)  DEFAULT NULL             COMMENT '回复目标用户昵称',
  content         VARCHAR(1000) NOT NULL                COMMENT '评论内容',
  like_count      INT(11)      DEFAULT 0                COMMENT '点赞数',
  audit_status    CHAR(1)      DEFAULT '0'              COMMENT '审核状态(0待审核 1通过 2驳回)',
  audit_reason    VARCHAR(500) DEFAULT NULL             COMMENT '审核驳回原因',
  ai_audit_score  DECIMAL(5,2) DEFAULT NULL             COMMENT 'AI审核分数(0-100)',
  status          CHAR(1)      DEFAULT '0'              COMMENT '状态(0正常 1已删除)',
  create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  create_time     DATETIME                              COMMENT '创建时间',
  update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  update_time     DATETIME                              COMMENT '更新时间',
  PRIMARY KEY (comment_id),
  KEY idx_post_id (post_id),
  KEY idx_user_id (user_id),
  KEY idx_parent_id (parent_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='校园墙评论表';

-- 2. 点赞表(帖子+评论统一点赞)
CREATE TABLE cw_like (
  like_id         BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '点赞ID',
  user_id         BIGINT(20)   NOT NULL                 COMMENT '用户ID',
  target_type     CHAR(1)      NOT NULL                 COMMENT '目标类型(0帖子 1评论)',
  target_id       BIGINT(20)   NOT NULL                 COMMENT '目标ID',
  status          CHAR(1)      DEFAULT '0'              COMMENT '状态(0已赞 1已取消)',
  create_time     DATETIME                              COMMENT '创建时间',
  update_time     DATETIME                              COMMENT '更新时间',
  PRIMARY KEY (like_id),
  UNIQUE KEY uk_user_target (user_id, target_type, target_id),
  KEY idx_target (target_type, target_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='校园墙点赞表';

-- 3. 举报表(帖子+评论统一举报)
CREATE TABLE cw_report (
  report_id       BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '举报ID',
  user_id         BIGINT(20)   NOT NULL                 COMMENT '举报人用户ID',
  target_type     CHAR(1)      NOT NULL                 COMMENT '目标类型(0帖子 1评论)',
  target_id       BIGINT(20)   NOT NULL                 COMMENT '目标ID',
  report_reason   VARCHAR(50)  NOT NULL                 COMMENT '举报原因代码',
  report_desc     VARCHAR(500) DEFAULT NULL             COMMENT '举报详细描述',
  handle_status   CHAR(1)      DEFAULT '0'              COMMENT '处理状态(0待处理 1已忽略 2已警告 3已删除 4已封禁)',
  handle_result   VARCHAR(500) DEFAULT NULL             COMMENT '处理结果描述',
  handle_by       VARCHAR(64)  DEFAULT NULL             COMMENT '处理人',
  handle_time     DATETIME     DEFAULT NULL             COMMENT '处理时间',
  create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  create_time     DATETIME                              COMMENT '创建时间',
  update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  update_time     DATETIME                              COMMENT '更新时间',
  PRIMARY KEY (report_id),
  KEY idx_target (target_type, target_id),
  KEY idx_handle_status (handle_status),
  UNIQUE KEY uk_user_target (user_id, target_type, target_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='校园墙举报表';
