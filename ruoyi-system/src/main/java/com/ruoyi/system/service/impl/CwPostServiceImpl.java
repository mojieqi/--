package com.ruoyi.system.service.impl;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.CwCategory;
import com.ruoyi.system.domain.CwPost;
import com.ruoyi.system.domain.CwPostImage;
import com.ruoyi.system.mapper.CwCategoryMapper;
import com.ruoyi.system.mapper.CwPostMapper;
import com.ruoyi.system.mapper.CwPostImageMapper;
import com.ruoyi.system.service.IContentAuditService;
import com.ruoyi.system.service.IContentClassifyService;
import com.ruoyi.system.service.ICwPostService;

/**
 * 校园墙帖子Service实现
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@Service
public class CwPostServiceImpl implements ICwPostService {

    private static final Logger log = LoggerFactory.getLogger(CwPostServiceImpl.class);

    @Autowired
    private CwPostMapper postMapper;

    @Autowired
    private CwPostImageMapper postImageMapper;

    @Autowired
    private CwCategoryMapper categoryMapper;

    @Autowired
    private IContentAuditService contentAuditService;

    @Autowired
    private IContentClassifyService contentClassifyService;

    @Override
    public List<CwPost> selectCwPostList(CwPost post) {
        if (post == null) {
            post = new CwPost();
        }
        List<CwPost> list = postMapper.selectCwPostList(post);
        // 匿名帖子隐藏发布者信息
        maskAnonymousPosts(list);
        return list;
    }

    @Override
    public CwPost selectCwPostById(Long postId) {
        // 自动增加浏览量
        postMapper.incrementViewCount(postId);
        CwPost post = postMapper.selectCwPostById(postId);
        if (post != null) {
            // 加载帖子图片
            List<CwPostImage> images = postImageMapper.selectImagesByPostId(postId);
            post.setImages(images);
            // 匿名帖子隐藏发布者信息
            if ("1".equals(post.getIsAnonymous())) {
                post.setNickName("匿名用户");
                post.setCreateBy("匿名用户");
                post.setUserId(null);
            }
        }
        return post;
    }

    @Override
    public List<CwPost> selectMyCwPostList(CwPost post) {
        String username = SecurityUtils.getUsername();
        post.setCreateBy(username);
        return postMapper.selectMyCwPostList(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CwPost publishPost(CwPost post) {
        // 1. 获取当前用户信息
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        post.setUserId(userId);
        post.setCreateBy(username);

        // 2. 如果没有指定分类，进行AI智能分类
        if (post.getCategoryId() == null) {
            IContentClassifyService.ClassificationResult classifyResult =
                    contentClassifyService.classify(post.getTitle(), post.getContent());

            // 根据分类代码查找category_id
            CwCategory category = categoryMapper.selectCwCategoryByCode(classifyResult.getCategoryCode());
            if (category != null) {
                post.setCategoryId(category.getCategoryId());
                post.setAiCategory(classifyResult.getCategoryName());
            } else {
                // 兜底：默认使用生活吐槽
                CwCategory defaultCategory = categoryMapper.selectCwCategoryByCode("life");
                if (defaultCategory != null) {
                    post.setCategoryId(defaultCategory.getCategoryId());
                    post.setAiCategory("生活吐槽(默认)");
                }
            }
        }

        // 3. AI内容审核
        Map<String, Object> auditResult = contentAuditService.audit(post.getContent(), "post");
        boolean passed = (boolean) auditResult.get("passed");
        double score = (double) auditResult.getOrDefault("score", 0.0);

        if (passed) {
            post.setAuditStatus("1"); // 审核通过
            post.setAiAuditScore(score);
        } else {
            post.setAuditStatus("2"); // 审核驳回
            post.setAuditReason((String) auditResult.get("reason"));
            post.setAiAuditScore(score);
        }

        // 4. 保存帖子
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setReportCount(0);
        post.setStatus("0");
        postMapper.insertCwPost(post);

        // 5. 保存帖子图片
        if (post.getImages() != null && !post.getImages().isEmpty()) {
            int sort = 0;
            for (CwPostImage image : post.getImages()) {
                image.setPostId(post.getPostId());
                image.setSort(sort++);
            }
            postImageMapper.batchInsertImages(post.getImages());

            // 设置第一张图片为封面
            if (post.getCoverImage() == null) {
                CwPost updatePost = new CwPost();
                updatePost.setPostId(post.getPostId());
                updatePost.setCoverImage(post.getImages().get(0).getImageUrl());
                postMapper.updateCwPost(updatePost);
                post.setCoverImage(updatePost.getCoverImage());
            }
        }

        // 6. 重新加载帖子(含关联数据)
        CwPost savedPost = postMapper.selectCwPostById(post.getPostId());
        if (savedPost != null) {
            savedPost.setImages(postImageMapper.selectImagesByPostId(post.getPostId()));
        }

        log.info("帖子发布成功: postId={}, auditStatus={}, aiCategory={}, userId={}",
                post.getPostId(), post.getAuditStatus(), post.getAiCategory(), userId);

        return savedPost != null ? savedPost : post;
    }

    @Override
    public int updateCwPost(CwPost post) {
        post.setUpdateBy(SecurityUtils.getUsername());
        return postMapper.updateCwPost(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCwPostById(Long postId) {
        // 软删除帖子
        int result = postMapper.deleteCwPostById(postId);
        // 删除关联图片
        postImageMapper.deleteImagesByPostId(postId);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCwPostByIds(Long[] postIds) {
        for (Long postId : postIds) {
            deleteCwPostById(postId);
        }
        return postIds.length;
    }

    @Override
    public int auditPost(Long postId, String auditStatus, String auditReason) {
        CwPost post = new CwPost();
        post.setPostId(postId);
        post.setAuditStatus(auditStatus);
        post.setAuditReason(auditReason);
        return postMapper.updateAuditStatus(post);
    }

    /**
     * 匿名帖子隐藏发布者信息
     */
    private void maskAnonymousPosts(List<CwPost> posts) {
        for (CwPost post : posts) {
            if ("1".equals(post.getIsAnonymous())) {
                post.setNickName("匿名用户");
                post.setCreateBy("匿名用户");
                post.setUserId(null);
            }
        }
    }
}
