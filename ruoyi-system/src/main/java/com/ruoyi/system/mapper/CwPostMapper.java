package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CwPost;

/**
 * 校园墙帖子Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface CwPostMapper {

    /**
     * 查询帖子列表(前端)
     */
    public List<CwPost> selectCwPostList(CwPost post);

    /**
     * 查询帖子详情
     */
    public CwPost selectCwPostById(Long postId);

    /**
     * 查询用户自己的帖子列表
     */
    public List<CwPost> selectMyCwPostList(CwPost post);

    /**
     * 新增帖子
     */
    public int insertCwPost(CwPost post);

    /**
     * 修改帖子
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
     * 更新帖子审核状态
     */
    public int updateAuditStatus(CwPost post);

    /**
     * 增加浏览量
     */
    public int incrementViewCount(Long postId);

    /**
     * 更新点赞数
     */
    public int updateLikeCount(Long postId);

    /**
     * 更新评论数
     */
    public int updateCommentCount(Long postId);
}
