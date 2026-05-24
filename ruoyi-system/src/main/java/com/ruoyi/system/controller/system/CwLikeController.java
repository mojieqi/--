package com.ruoyi.system.controller.system;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.ICwLikeService;

/**
 * 校园墙点赞Controller
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@RestController
@RequestMapping("/campus/like")
public class CwLikeController extends BaseController {

    @Autowired
    private ICwLikeService likeService;

    /**
     * 点赞/取消点赞
     * @param params: targetType(0帖子/1评论), targetId
     */
    @PostMapping
    public AjaxResult toggle(@RequestBody Map<String, Object> params) {
        try {
            String targetType = (String) params.getOrDefault("targetType", "0");
            Long targetId = Long.valueOf(params.get("targetId").toString());

            if (targetId == null || targetId <= 0) {
                return error("目标ID无效");
            }

            Map<String, Object> result = likeService.toggleLike(targetType, targetId);
            boolean liked = (boolean) result.get("liked");
            return success(result).put("msg", liked ? "点赞成功" : "已取消点赞");
        } catch (Exception e) {
            logger.error("点赞操作失败", e);
            return error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户对某目标的点赞状态
     */
    @GetMapping("/status")
    public AjaxResult status(@RequestParam String targetType, @RequestParam Long targetId) {
        try {
            boolean liked = likeService.isLiked(targetType, targetId);
            return success().put("liked", liked);
        } catch (Exception e) {
            logger.error("查询点赞状态失败", e);
            return success().put("liked", false);
        }
    }

    /**
     * 批量查询用户对多个目标的点赞状态
     * 用于帖子列表渲染时批量获取点赞状态
     */
    @PostMapping("/batch-status")
    public AjaxResult batchStatus(@RequestBody Map<String, Object> params) {
        try {
            String targetType = (String) params.getOrDefault("targetType", "0");
            @SuppressWarnings("unchecked")
            List<Long> targetIds = (List<Long>) params.get("targetIds");

            if (targetIds == null || targetIds.isEmpty()) {
                return success(new java.util.HashMap<>());
            }

            Map<Long, Boolean> result = likeService.batchIsLiked(targetType, targetIds);
            return success(result);
        } catch (Exception e) {
            logger.error("批量查询点赞状态失败", e);
            return success(new java.util.HashMap<>());
        }
    }
}
