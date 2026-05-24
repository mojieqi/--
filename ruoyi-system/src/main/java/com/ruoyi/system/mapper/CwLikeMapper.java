package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CwLike;

/**
 * 校园墙点赞Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface CwLikeMapper {

    /**
     * 查询用户对某目标的点赞记录
     */
    public CwLike selectCwLike(CwLike like);

    /**
     * 查询用户对多个目标的点赞状态
     */
    public List<CwLike> selectLikeStatusList(Long userId, String targetType, List<Long> targetIds);

    /**
     * 新增点赞记录
     */
    public int insertCwLike(CwLike like);

    /**
     * 更新点赞状态
     */
    public int updateCwLike(CwLike like);
}
