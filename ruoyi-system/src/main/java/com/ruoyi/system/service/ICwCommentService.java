package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CwComment;

/**
 * 校园墙评论Service接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface ICwCommentService {

    /**
     * 查询帖子评论列表(含嵌套回复)
     */
    public List<CwComment> selectCommentTree(Long postId);

    /**
     * 查询单个评论
     */
    public CwComment selectCwCommentById(Long commentId);

    /**
     * 发表评论(含AI审核)
     */
    public CwComment publishComment(CwComment comment);

    /**
     * 删除评论(软删除，仅作者可删除)
     */
    public int deleteCwCommentById(Long commentId);
}
