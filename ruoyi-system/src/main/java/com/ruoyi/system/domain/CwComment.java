package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 校园墙评论对象 cw_comment
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class CwComment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 评论ID */
    private Long commentId;

    /** 帖子ID */
    private Long postId;

    /** 评论用户ID */
    private Long userId;

    /** 父评论ID(0表示一级评论) */
    private Long parentId;

    /** 回复目标评论ID */
    private Long replyToId;

    /** 回复目标用户ID */
    private Long replyToUid;

    /** 回复目标用户昵称 */
    private String replyToName;

    /** 评论内容 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 审核状态(0待审核 1通过 2驳回) */
    private String auditStatus;

    /** 审核驳回原因 */
    private String auditReason;

    /** AI审核分数(0-100) */
    private Double aiAuditScore;

    /** 状态(0正常 1已删除) */
    private String status;

    // ===== 查询扩展字段 =====

    /** 评论用户昵称 */
    private String nickName;

    /** 评论用户头像 */
    private String avatar;

    /** 当前用户是否已点赞 */
    private Boolean isLiked;

    /** 子评论列表(嵌套回复) */
    private java.util.List<CwComment> children;

    // ===== getter/setter =====

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public Long getReplyToId() { return replyToId; }
    public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }

    public Long getReplyToUid() { return replyToUid; }
    public void setReplyToUid(Long replyToUid) { this.replyToUid = replyToUid; }

    public String getReplyToName() { return replyToName; }
    public void setReplyToName(String replyToName) { this.replyToName = replyToName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }

    public String getAuditReason() { return auditReason; }
    public void setAuditReason(String auditReason) { this.auditReason = auditReason; }

    public Double getAiAuditScore() { return aiAuditScore; }
    public void setAiAuditScore(Double aiAuditScore) { this.aiAuditScore = aiAuditScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Boolean getIsLiked() { return isLiked; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }

    public java.util.List<CwComment> getChildren() { return children; }
    public void setChildren(java.util.List<CwComment> children) { this.children = children; }

    @Override
    public String toString() {
        return "CwComment{" +
                "commentId=" + commentId +
                ", postId=" + postId +
                ", userId=" + userId +
                ", parentId=" + parentId +
                ", status='" + status + '\'' +
                '}';
    }
}
