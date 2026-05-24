package com.ruoyi.system.service.impl;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.CwLike;
import com.ruoyi.system.mapper.CwCommentMapper;
import com.ruoyi.system.mapper.CwLikeMapper;
import com.ruoyi.system.mapper.CwPostMapper;
import com.ruoyi.system.service.ICwLikeService;

/**
 * 校园墙点赞Service实现
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@Service
public class CwLikeServiceImpl implements ICwLikeService {

    private static final Logger log = LoggerFactory.getLogger(CwLikeServiceImpl.class);

    @Autowired
    private CwLikeMapper likeMapper;

    @Autowired
    private CwPostMapper postMapper;

    @Autowired
    private CwCommentMapper commentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleLike(String targetType, Long targetId) {
        Long userId = SecurityUtils.getUserId();

        CwLike query = new CwLike();
        query.setUserId(userId);
        query.setTargetType(targetType);
        query.setTargetId(targetId);

        CwLike existing = likeMapper.selectCwLike(query);
        boolean liked;
        int delta;

        if (existing == null) {
            // 首次点赞
            CwLike like = new CwLike();
            like.setUserId(userId);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            like.setStatus("0");
            likeMapper.insertCwLike(like);
            liked = true;
            delta = 1;
            log.info("点赞成功: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        } else if ("1".equals(existing.getStatus())) {
            // 之前取消过，重新点赞
            existing.setStatus("0");
            likeMapper.updateCwLike(existing);
            liked = true;
            delta = 1;
            log.info("重新点赞: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        } else {
            // 已点赞，取消点赞
            existing.setStatus("1");
            likeMapper.updateCwLike(existing);
            liked = false;
            delta = -1;
            log.info("取消点赞: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        }

        // 更新目标点赞计数
        if ("0".equals(targetType)) {
            // 帖子点赞
            postMapper.incrementPostLikeCount(targetId, delta);
        } else {
            // 评论点赞
            commentMapper.incrementLikeCount(targetId, delta);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("delta", delta);
        return result;
    }

    @Override
    public boolean isLiked(String targetType, Long targetId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }

        CwLike query = new CwLike();
        query.setUserId(userId);
        query.setTargetType(targetType);
        query.setTargetId(targetId);

        CwLike existing = likeMapper.selectCwLike(query);
        return existing != null && "0".equals(existing.getStatus());
    }

    @Override
    public Map<Long, Boolean> batchIsLiked(String targetType, List<Long> targetIds) {
        Map<Long, Boolean> result = new HashMap<>();
        Long userId = SecurityUtils.getUserId();
        if (userId == null || targetIds == null || targetIds.isEmpty()) {
            // 未登录用户全部返回false
            for (Long id : targetIds) {
                result.put(id, false);
            }
            return result;
        }

        // 先全部初始化为false
        for (Long id : targetIds) {
            result.put(id, false);
        }

        try {
            List<CwLike> likes = likeMapper.selectLikeStatusList(userId, targetType, targetIds);
            if (likes != null) {
                for (CwLike like : likes) {
                    result.put(like.getTargetId(), true);
                }
            }
        } catch (Exception e) {
            log.warn("批量查询点赞状态异常", e);
        }

        return result;
    }
}
