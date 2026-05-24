package com.ruoyi.system.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 校园墙帖子对象 cw_post
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class CwPost extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 帖子ID */
    private Long postId;

    /** 发布用户ID */
    private Long userId;

    /** 分类ID */
    private Long categoryId;

    /** 帖子标题 */
    private String title;

    /** 帖子内容 */
    private String content;

    /** 封面图片URL */
    private String coverImage;

    /** 是否匿名(0否 1是) */
    private String isAnonymous;

    /** 浏览数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 被举报次数 */
    private Integer reportCount;

    /** 审核状态(0待审核 1通过 2驳回) */
    private String auditStatus;

    /** 审核原因 */
    private String auditReason;

    /** AI分类结果 */
    private String aiCategory;

    /** AI审核分数 */
    private Double aiAuditScore;

    /** 是否置顶(0否 1是) */
    private String isTop;

    /** 是否热门(0否 1是) */
    private String isHot;

    /** 状态(0正常 1已删除 2违规下架) */
    private String status;

    // ===== 查询扩展字段 =====

    /** 发布用户昵称 */
    private String nickName;

    /** 发布用户头像 */
    private String avatar;

    /** 分类名称 */
    private String categoryName;

    /** 帖子图片列表 */
    private List<CwPostImage> images;

    /** 当前用户是否已点赞 */
    private Boolean isLiked;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(String isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditReason() {
        return auditReason;
    }

    public void setAuditReason(String auditReason) {
        this.auditReason = auditReason;
    }

    public String getAiCategory() {
        return aiCategory;
    }

    public void setAiCategory(String aiCategory) {
        this.aiCategory = aiCategory;
    }

    public Double getAiAuditScore() {
        return aiAuditScore;
    }

    public void setAiAuditScore(Double aiAuditScore) {
        this.aiAuditScore = aiAuditScore;
    }

    public String getIsTop() {
        return isTop;
    }

    public void setIsTop(String isTop) {
        this.isTop = isTop;
    }

    public String getIsHot() {
        return isHot;
    }

    public void setIsHot(String isHot) {
        this.isHot = isHot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<CwPostImage> getImages() {
        return images;
    }

    public void setImages(List<CwPostImage> images) {
        this.images = images;
    }

    public Boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }

    @Override
    public String toString() {
        return "CwPost{" +
                "postId=" + postId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", auditStatus='" + auditStatus + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
