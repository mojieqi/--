package com.ruoyi.system.service.impl;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.CwComment;
import com.ruoyi.system.mapper.CwCommentMapper;
import com.ruoyi.system.mapper.CwPostMapper;
import com.ruoyi.system.service.IContentAuditService;
import com.ruoyi.system.service.ICwCommentService;

/**
 * 校园墙评论Service实现
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@Service
public class CwCommentServiceImpl implements ICwCommentService {

    private static final Logger log = LoggerFactory.getLogger(CwCommentServiceImpl.class);

    @Autowired
    private CwCommentMapper commentMapper;

    @Autowired
    private CwPostMapper postMapper;

    @Autowired
    private IContentAuditService contentAuditService;

    @Override
    public List<CwComment> selectCommentTree(Long postId) {
        // 1. 查询一级评论
        CwComment query = new CwComment();
        query.setPostId(postId);
        List<CwComment> rootComments = commentMapper.selectCwCommentList(query);

        // 2. 查询每个一级评论的子回复
        for (CwComment parent : rootComments) {
            List<CwComment> children = commentMapper.selectChildComments(parent.getCommentId());
            parent.setChildren(children != null ? children : new ArrayList<>());
        }

        return rootComments;
    }

    @Override
    public CwComment selectCwCommentById(Long commentId) {
        return commentMapper.selectCwCommentById(commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CwComment publishComment(CwComment comment) {
        // 1. 获取当前用户
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        comment.setUserId(userId);
        comment.setCreateBy(username);

        // 2. AI内容审核
        Map<String, Object> auditResult = contentAuditService.audit(comment.getContent(), "comment");
        boolean passed = (boolean) auditResult.get("passed");
        double score = (double) auditResult.getOrDefault("score", 0.0);

        if (passed) {
            comment.setAuditStatus("1"); // 审核通过
            comment.setAiAuditScore(score);
        } else {
            comment.setAuditStatus("2"); // 审核驳回
            comment.setAuditReason((String) auditResult.get("reason"));
            comment.setAiAuditScore(score);
        }

        // 3. 处理父评论/回复目标
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }

        // 4. 保存评论
        comment.setLikeCount(0);
        comment.setStatus("0");
        commentMapper.insertCwComment(comment);

        // 5. 审核通过时更新帖子评论数
        if ("1".equals(comment.getAuditStatus())) {
            try {
                postMapper.incrementCommentCount(comment.getPostId(), 1);
            } catch (Exception e) {
                log.warn("更新帖子评论数失败: postId={}", comment.getPostId(), e);
            }
        }

        log.info("评论发表成功: commentId={}, postId={}, auditStatus={}, userId={}",
                comment.getCommentId(), comment.getPostId(), comment.getAuditStatus(), userId);

        return comment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCwCommentById(Long commentId) {
        CwComment comment = commentMapper.selectCwCommentById(commentId);
        if (comment == null) {
            return 0;
        }

        // 权限校验：仅作者可删除
        Long currentUserId = SecurityUtils.getUserId();
        if (!currentUserId.equals(comment.getUserId())) {
            log.warn("非评论作者尝试删除: commentId={}, currentUser={}, author={}",
                    commentId, currentUserId, comment.getUserId());
            return -1;
        }

        // 软删除
        commentMapper.deleteCwCommentById(commentId);

        // 更新帖子评论数
        try {
            int totalComments = commentMapper.countCommentsByPostId(comment.getPostId());
            postMapper.updateCommentCount(comment.getPostId());
        } catch (Exception e) {
            log.warn("更新帖子评论数失败: postId={}", comment.getPostId(), e);
        }

        log.info("评论删除: commentId={}, postId={}", commentId, comment.getPostId());
        return 1;
    }
}
