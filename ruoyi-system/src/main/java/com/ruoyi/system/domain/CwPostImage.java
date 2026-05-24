package com.ruoyi.system.domain;

import java.util.Date;

/**
 * 校园墙帖子图片对象 cw_post_image
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class CwPostImage {
    private static final long serialVersionUID = 1L;

    /** 图片ID */
    private Long imageId;

    /** 帖子ID */
    private Long postId;

    /** 图片URL */
    private String imageUrl;

    /** 缩略图URL */
    private String thumbnailUrl;

    /** 排序 */
    private Integer sort;

    /** 创建时间 */
    private Date createTime;

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "CwPostImage{" +
                "imageId=" + imageId +
                ", postId=" + postId +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
