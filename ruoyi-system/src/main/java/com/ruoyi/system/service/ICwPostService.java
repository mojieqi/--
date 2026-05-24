package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CwPost;

/**
 * 校园墙帖子Service接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface ICwPostService {

    /**
     * 查询帖子列表(首页展示，仅审核通过的)
     */
    public List<CwPost> selectCwPostList(CwPost post);

    /**
     * 查询帖子详情(含图片，自动+1浏览量)
     */
    public CwPost selectCwPostById(Long postId);

    /**
     * 查询用户自己的帖子列表
     */
    public List<CwPost> selectMyCwPostList(CwPost post);

    /**
     * 发布帖子(含AI审核+智能分类)
     */
    public CwPost publishPost(CwPost post);

    /**
     * 编辑帖子(仅作者可编辑)
     */
    public int updateCwPost(CwPost post);

    /**
     * 删除帖子(软删除)
     */
    public int deleteCwPostById(Long postId);

    /**
     * 批量删除帖子
     */
    public int deleteCwPostByIds(Long[] postIds);

    /**
     * 管理员审核帖子
     */
    public int auditPost(Long postId, String auditStatus, String auditReason);
}
