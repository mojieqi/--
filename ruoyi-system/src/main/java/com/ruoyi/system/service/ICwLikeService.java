package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.CwLike;

/**
 * 校园墙点赞Service接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface ICwLikeService {

    /**
     * 点赞/取消点赞
     * @return Map包含 liked(boolean) 和 likeCount(int)
     */
    public Map<String, Object> toggleLike(String targetType, Long targetId);

    /**
     * 查询用户对目标的点赞状态
     * @return true=已点赞，false=未点赞
     */
    public boolean isLiked(String targetType, Long targetId);

    /**
     * 批量查询用户对多个目标的点赞状态
     * @return targetId → true/false
     */
    public Map<Long, Boolean> batchIsLiked(String targetType, List<Long> targetIds);
}
