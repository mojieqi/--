-- ----------------------------
-- AI校园墙 核心业务建表脚本
-- Phase 6: 帖子管理
-- ----------------------------

DROP TABLE IF EXISTS cw_post_image;
DROP TABLE IF EXISTS cw_post;
DROP TABLE IF EXISTS cw_category;

-- 1. 内容分类表
CREATE TABLE cw_category (
  category_id     BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
  category_name   VARCHAR(50)  NOT NULL                 COMMENT '分类名称',
  category_code   VARCHAR(50)  NOT NULL                 COMMENT '分类代码',
  category_icon   VARCHAR(200) DEFAULT NULL             COMMENT '分类图标',
  sort            INT(4)       DEFAULT 0                COMMENT '排序',
  status          CHAR(1)      DEFAULT '0'              COMMENT '状态(0启用 1停用)',
  description     VARCHAR(500) DEFAULT NULL             COMMENT '分类描述',
  create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  create_time     DATETIME                              COMMENT '创建时间',
  update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  update_time     DATETIME                              COMMENT '更新时间',
  PRIMARY KEY (category_id),
  UNIQUE KEY uk_category_code (category_code)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='校园墙内容分类表';

-- 2. 帖子表
CREATE TABLE cw_post (
  post_id         BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '帖子ID',
  user_id         BIGINT(20)   NOT NULL                 COMMENT '发布用户ID',
  category_id     BIGINT(20)   DEFAULT NULL             COMMENT '分类ID(AI分类或用户选择)',
  title           VARCHAR(200) NOT NULL                 COMMENT '帖子标题',
  content         TEXT         NOT NULL                 COMMENT '帖子内容',
  cover_image     VARCHAR(500) DEFAULT NULL             COMMENT '封面图片URL',
  is_anonymous    CHAR(1)      DEFAULT '0'              COMMENT '是否匿名(0否 1是)',
  view_count      INT(11)      DEFAULT 0                COMMENT '浏览数',
  like_count      INT(11)      DEFAULT 0                COMMENT '点赞数',
  comment_count   INT(11)      DEFAULT 0                COMMENT '评论数',
  report_count    INT(11)      DEFAULT 0                COMMENT '被举报次数',
  audit_status    CHAR(1)      DEFAULT '0'              COMMENT '审核状态(0待审核 1通过 2驳回)',
  audit_reason    VARCHAR(500) DEFAULT NULL             COMMENT '审核驳回原因',
  ai_category     VARCHAR(50)  DEFAULT NULL             COMMENT 'AI分类结果',
  ai_audit_score  DECIMAL(5,2) DEFAULT NULL             COMMENT 'AI审核分数(0-100)',
  is_top          CHAR(1)      DEFAULT '0'              COMMENT '是否置顶(0否 1是)',
  is_hot          CHAR(1)      DEFAULT '0'              COMMENT '是否热门(0否 1是)',
  status          CHAR(1)      DEFAULT '0'              COMMENT '状态(0正常 1已删除 2违规下架)',
  create_by       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  create_time     DATETIME                              COMMENT '创建时间',
  update_by       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  update_time     DATETIME                              COMMENT '更新时间',
  PRIMARY KEY (post_id),
  KEY idx_user_id (user_id),
  KEY idx_category_id (category_id),
  KEY idx_audit_status (audit_status),
  KEY idx_create_time (create_time),
  KEY idx_status (status),
  FULLTEXT KEY ft_content (title, content)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='校园墙帖子表';

-- 3. 帖子图片表
CREATE TABLE cw_post_image (
  image_id        BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '图片ID',
  post_id         BIGINT(20)   NOT NULL                 COMMENT '帖子ID',
  image_url       VARCHAR(500) NOT NULL                 COMMENT '图片URL',
  thumbnail_url   VARCHAR(500) DEFAULT NULL             COMMENT '缩略图URL',
  sort            INT(4)       DEFAULT 0                COMMENT '排序',
  create_time     DATETIME                              COMMENT '创建时间',
  PRIMARY KEY (image_id),
  KEY idx_post_id (post_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='校园墙帖子图片表';


-- ----------------------------
-- 初始化数据: 7个校园墙分类
-- ----------------------------
INSERT INTO cw_category (category_name, category_code, category_icon, sort, status, description, create_by, create_time) VALUES
('表白墙',   'confession',  'heart',    1, '0', '情感表白、暗恋、寻找某人的帖子', 'admin', sysdate()),
('寻物启事', 'lost_found',  'search',   2, '0', '寻找丢失物品的帖子',           'admin', sysdate()),
('求助问答', 'help',        'question', 3, '0', '寻求帮助、提问的帖子',           'admin', sysdate()),
('校园资讯', 'news',        'bell',     4, '0', '学校通知、活动信息、新闻',       'admin', sysdate()),
('生活吐槽', 'life',        'chat',     5, '0', '日常生活分享、吐槽、闲聊',       'admin', sysdate()),
('二手交易', 'trade',       'goods',    6, '0', '二手物品买卖信息',             'admin', sysdate()),
('学习交流', 'study',       'book',     7, '0', '学习经验、考试信息、课程讨论',     'admin', sysdate());
