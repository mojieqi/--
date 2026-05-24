package com.ruoyi.system.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.CwComment;
import com.ruoyi.system.service.ICwCommentService;

/**
 * 校园墙评论Controller
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@RestController
@RequestMapping("/campus/comment")
public class CwCommentController extends BaseController {

    @Autowired
    private ICwCommentService commentService;

    /**
     * 获取帖子评论列表(含嵌套回复树)
     */
    @GetMapping("/list/{postId}")
    public AjaxResult list(@PathVariable("postId") Long postId) {
        List<CwComment> list = commentService.selectCommentTree(postId);
        return success(list);
    }

    /**
     * 发表评论
     */
    @Log(title = "校园墙评论", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CwComment comment) {
        try {
            // 校验必填字段
            if (comment.getPostId() == null) {
                return error("帖子ID不能为空");
            }
            if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
                return error("评论内容不能为空");
            }
            if (comment.getContent().length() > 1000) {
                return error("评论内容不能超过1000字");
            }

            CwComment result = commentService.publishComment(comment);

            // 审核驳回时给用户提示
            if ("2".equals(result.getAuditStatus())) {
                return success(result).put("msg", "评论涉及敏感内容，已被系统驳回: " + result.getAuditReason());
            }

            return success(result);
        } catch (Exception e) {
            logger.error("发表评论失败", e);
            return error("发表失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论(仅作者可删除)
     */
    @Log(title = "校园墙评论", businessType = BusinessType.DELETE)
    @DeleteMapping("/{commentId}")
    public AjaxResult remove(@PathVariable Long commentId) {
        try {
            int result = commentService.deleteCwCommentById(commentId);
            if (result == -1) {
                return error("只能删除自己的评论");
            }
            if (result == 0) {
                return error("评论不存在");
            }
            return success();
        } catch (Exception e) {
            logger.error("删除评论失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }
}
