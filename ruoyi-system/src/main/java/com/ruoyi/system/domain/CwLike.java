package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 校园墙点赞对象 cw_like
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class CwLike extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 点赞ID */
    private Long likeId;

    /** 用户ID */
    private Long userId;

    /** 目标类型(0帖子 1评论) */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 状态(0已赞 1已取消) */
    private String status;

    // ===== getter/setter =====

    public Long getLikeId() { return likeId; }
    public void setLikeId(Long likeId) { this.likeId = likeId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "CwLike{" +
                "likeId=" + likeId +
                ", userId=" + userId +
                ", targetType='" + targetType + '\'' +
                ", targetId=" + targetId +
                ", status='" + status + '\'' +
                '}';
    }
}
