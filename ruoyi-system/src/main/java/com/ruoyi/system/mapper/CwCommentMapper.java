package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.CwComment;

/**
 * 校园墙评论Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface CwCommentMapper {

    /**
     * 查询帖子的一级评论列表(不含子回复)
     */
    public List<CwComment> selectCwCommentList(CwComment comment);

    /**
     * 查询某条评论的子回复列表
     */
    public List<CwComment> selectChildComments(Long parentId);

    /**
     * 查询单个评论
     */
    public CwComment selectCwCommentById(Long commentId);

    /**
     * 新增评论
     */
    public int insertCwComment(CwComment comment);

    /**
     * 修改评论
     */
    public int updateCwComment(CwComment comment);

    /**
     * 软删除评论
     */
    public int deleteCwCommentById(Long commentId);

    /**
     * 增加评论点赞数
     */
    public int incrementLikeCount(@Param("commentId") Long commentId, @Param("delta") int delta);

    /**
     * 查询帖子总评论数(含子回复)
     */
    public int countCommentsByPostId(Long postId);
}
