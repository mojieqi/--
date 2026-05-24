package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CwPostImage;

/**
 * 校园墙帖子图片Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface CwPostImageMapper {

    /**
     * 查询帖子的图片列表
     */
    public List<CwPostImage> selectImagesByPostId(Long postId);

    /**
     * 批量插入图片
     */
    public int batchInsertImages(List<CwPostImage> images);

    /**
     * 删除帖子的所有图片
     */
    public int deleteImagesByPostId(Long postId);
}
