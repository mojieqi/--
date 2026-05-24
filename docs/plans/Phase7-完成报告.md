# Phase 7 完成报告 — 互动功能

**日期：** 2026-05-24  
**状态：** ✅ 已完成  
**耗时：** 1个周期（单日完成）

---

## 一、产出文件清单

| # | 文件路径 | 说明 |
|:--:|------|------|
| **SQL** (2个) |||
| 1 | `sql/cw_interaction.sql` | 建表脚本：cw_comment、cw_like、cw_report |
| 2 | `sql/cw_interaction_menu.sql` | 权限菜单脚本 |
| **实体类** (3个) |||
| 3 | `ruoyi-system/.../domain/CwComment.java` | 评论实体（含children嵌套回复列表） |
| 4 | `ruoyi-system/.../domain/CwLike.java` | 点赞实体（帖子+评论统一点赞） |
| 5 | `ruoyi-system/.../domain/CwReport.java` | 举报实体（6种举报类型） |
| **Mapper** (3接口+3XML) |||
| 6 | `.../mapper/CwCommentMapper.java` | 评论Mapper |
| 7 | `.../mapper/CwLikeMapper.java` | 点赞Mapper |
| 8 | `.../mapper/CwReportMapper.java` | 举报Mapper |
| 9 | `.../mapper/system/CwCommentMapper.xml` | 评论XML映射 |
| 10 | `.../mapper/system/CwLikeMapper.xml` | 点赞XML映射 |
| 11 | `.../mapper/system/CwReportMapper.xml` | 举报XML映射 |
| **Service** (3接口+3实现) |||
| 12 | `.../service/ICwCommentService.java` | 评论服务接口 |
| 13 | `.../service/ICwLikeService.java` | 点赞服务接口 |
| 14 | `.../service/ICwReportService.java` | 举报服务接口 |
| 15 | `.../service/impl/CwCommentServiceImpl.java` | 评论服务实现 |
| 16 | `.../service/impl/CwLikeServiceImpl.java` | 点赞服务实现 |
| 17 | `.../service/impl/CwReportServiceImpl.java` | 举报服务实现 |
| **Controller** (3个) |||
| 18 | `.../controller/system/CwCommentController.java` | 评论API |
| 19 | `.../controller/system/CwLikeController.java` | 点赞API |
| 20 | `.../controller/system/CwReportController.java` | 举报API |
| **前端API** (3个) |||
| 21 | `ruoyi-ui/src/api/campus/comment.js` | 评论API请求 |
| 22 | `ruoyi-ui/src/api/campus/like.js` | 点赞API请求 |
| 23 | `ruoyi-ui/src/api/campus/report.js` | 举报API请求 |
| **前端组件** (2个) |||
| 24 | `ruoyi-ui/src/views/campus/comment/CommentSection.vue` | 评论区组件 |
| 25 | `ruoyi-ui/src/views/campus/report/ReportDialog.vue` | 举报弹窗组件 |
| **修改文件** (4个) |||
| 26 | `CwPostMapper.java` | 新增3个计数器方法 |
| 27 | `CwPostMapper.xml` | 新增3条SQL |
| 28 | `detail.vue` | 集成评论区+点赞+举报 |
| 29 | `index.vue` | 帖子列表增加点赞交互 |

**总新增：** 25个文件 | **修改：** 4个文件

---

## 二、API 端点清单

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/campus/comment/list/{postId}` | 获取帖子评论树 | campus:comment:query |
| POST | `/campus/comment` | 发表评论(含AI审核) | campus:comment:add |
| DELETE | `/campus/comment/{commentId}` | 删除评论(仅作者) | campus:comment:remove |
| POST | `/campus/like` | 点赞/取消切换 | campus:like:operate |
| GET | `/campus/like/status` | 查询点赞状态 | — |
| POST | `/campus/like/batch-status` | 批量查询点赞状态 | — |
| POST | `/campus/report` | 提交举报 | campus:report:add |
| GET | `/campus/report/my` | 我的举报记录 | campus:report:query |

---

## 三、现有模块连通性

| 模块 | 联通方式 |
|------|---------|
| **CwPost** (Phase 6) | 评论/点赞/举报时增量更新帖子计数器（commentCount/likeCount/reportCount）|
| **ContentAuditService** (Phase 6) | 评论发布时调用 `audit(content, "comment")` 审核评论内容 |
| **CwPostMapper** (Phase 6) | 新增 `incrementPostLikeCount` / `incrementCommentCount` / `incrementReportCount` |
| **SecurityUtils** | 获取当前用户ID和用户名 |
| **BaseController** | 继承使用 `startPage` / `getDataTable` / `success` / `error` |

---

## 四、数据库设计

### 4.1 评论表 (cw_comment)

- 支持一级评论 + 嵌套回复（parent_id/reply_to_id/reply_to_uid/reply_to_name）
- AI审核字段 audit_status/audit_reason/ai_audit_score
- 软删除机制

### 4.2 点赞表 (cw_like)

- user_id + target_type(0帖子/1评论) + target_id 唯一约束
- 状态切换：0已赞 ↔ 1已取消（复用记录）
- 计数同步：点赞/取消时增量更新目标计数器

### 4.3 举报表 (cw_report)

- 6类举报原因：spam/harassment/porn/fake/privacy/other
- 5种处理状态：0待处理/1已忽略/2已警告/3已删除/4已封禁
- 同一用户对同一目标只能举报一次

---

## 五、业务流程

### 5.1 评论发布流程

```
用户发表评论 → CwCommentController.add()
  → CwCommentServiceImpl.publishComment()
    → 1. 设置 userId / createBy
    → 2. contentAuditService.audit("comment")
    → 3. 通过(auditStatus=1) / 驳回(auditStatus=2)
    → 4. 保存评论
    → 5. 通过时 incrementCommentCount(+1)
    → 6. 返回评论对象
```

### 5.2 点赞切换流程

```
用户点击点赞 → CwLikeController.toggle()
  → CwLikeServiceImpl.toggleLike()
    → 查询是否已有记录
    ├── 无记录 → insert(status=0) → liked=true
    ├── 已取消 → update(status=0) → liked=true
    └── 已点赞 → update(status=1) → liked=false
    → 更新目标计数 (+1/-1)
    → 返回 {liked, delta}
```

### 5.3 举报提交流程

```
用户点击举报 → CwReportController.add()
  → CwReportServiceImpl.submitReport()
    → 检查是否已举报(同一用户同一目标)
    → 校验举报原因(6种合法代码)
    → 保存举报记录
    → 帖子举报时 incrementReportCount(+1)
    → 返回举报对象
```

---

## 六、前端交互

### 6.1 评论区 (CommentSection.vue)

- 嵌套树形结构：一级评论 + 子回复
- 发布评论/回复（textarea输入）
- 评论点赞（实时切换动画）
- 删除评论（仅作者可见删除按钮）
- 举报入口
- 批量加载点赞状态

### 6.2 举报弹窗 (ReportDialog.vue)

- 6种举报原因单选（含描述）
- 详细描述选填
- 防重复提交

### 6.3 集成点

- `detail.vue`：已集成CommentSection + 帖子点赞 + 帖子举报
- `index.vue`：帖子卡片增加点赞按钮（阻止冒泡）

---

## ⚠️ 您需要手动执行的操作

### 步骤 1: 执行建表脚本
```sql
-- 在MySQL中执行
source sql/cw_interaction.sql
```

### 步骤 2: 执行菜单权限脚本
```sql
-- 创建互动功能菜单权限
source sql/cw_interaction_menu.sql
```

### 步骤 3: 重启后端服务
- 停止当前运行的服务
- 重新启动 `ry.bat` 或通过IDE启动

### 步骤 4: 重新构建前端
```bash
cd ruoyi-ui
npm run build:prod
```

### 步骤 5: 功能验证

| 验证项 | 操作 | 预期结果 |
|--------|------|----------|
| 发表评论 | 进入帖子详情 → 输入评论 → 发表 | 评论出现在列表中 |
| 评论审核 | 发表含敏感词的评论 | 前端提示审核驳回 |
| 回复评论 | 点击回复 → 输入内容 → 回复 | 子回复出现在父评论下 |
| 删除评论 | 自己的评论 → 点击删除 | 评论消失，评论数-1 |
| 帖子点赞 | 帖子详情/列表 → 点击点赞 | 图标变色，数字+1/-1 |
| 评论点赞 | 评论区 → 点击点赞 | 图标变色，数字变化 |
| 举报帖子 | 帖子详情 → 点击举报 → 选择原因 → 提交 | 提示举报成功 |
| 举报评论 | 评论区 → 点击举报 → 选择原因 → 提交 | 提示举报成功 |
| 重复举报 | 对同一目标再次举报 | 提示"已举报过" |
| 我的举报 | 访问举报记录 | 显示已提交的举报列表 |

---

## 七、文件变更总览

```
新增：
  sql/cw_interaction.sql               建表脚本
  sql/cw_interaction_menu.sql          权限脚本
  domain/CwComment.java                评论实体
  domain/CwLike.java                   点赞实体
  domain/CwReport.java                 举报实体
  mapper/CwCommentMapper.java          评论Mapper接口
  mapper/CwLikeMapper.java             点赞Mapper接口
  mapper/CwReportMapper.java           举报Mapper接口
  mapper/system/CwCommentMapper.xml    评论XML映射
  mapper/system/CwLikeMapper.xml       点赞XML映射
  mapper/system/CwReportMapper.xml     举报XML映射
  service/ICwCommentService.java       评论Service
  service/ICwLikeService.java          点赞Service
  service/ICwReportService.java        举报Service
  service/impl/CwCommentServiceImpl.java
  service/impl/CwLikeServiceImpl.java
  service/impl/CwReportServiceImpl.java
  controller/system/CwCommentController.java
  controller/system/CwLikeController.java
  controller/system/CwReportController.java
  api/campus/comment.js                前端API
  api/campus/like.js                   前端API
  api/campus/report.js                 前端API
  views/campus/comment/CommentSection.vue
  views/campus/report/ReportDialog.vue

修改：
  mapper/CwPostMapper.java             +3个计数器方法
  mapper/system/CwPostMapper.xml        +3条SQL
  views/campus/post/detail.vue         集成评论区+点赞+举报
  views/campus/post/index.vue          帖子列表点赞交互
```

**Phase 7 开发完毕，编译通过，零Lint错误。请执行上述操作验证。**
