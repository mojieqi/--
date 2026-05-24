package com.ruoyi.system.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.CwCategory;
import com.ruoyi.system.domain.CwPost;
import com.ruoyi.system.service.ICwCategoryService;
import com.ruoyi.system.service.ICwPostService;

/**
 * 校园墙帖子Controller
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@RestController
@RequestMapping("/campus/post")
public class CwPostController extends BaseController {

    @Autowired
    private ICwPostService postService;

    @Autowired
    private ICwCategoryService categoryService;

    // ==================== 帖子相关 ====================

    /**
     * 帖子列表(首页展示)
     */
    @GetMapping("/list")
    public TableDataInfo list(CwPost post) {
        startPage();
        List<CwPost> list = postService.selectCwPostList(post);
        return getDataTable(list);
    }

    /**
     * 帖子详情
     */
    @GetMapping("/{postId}")
    public AjaxResult getInfo(@PathVariable("postId") Long postId) {
        CwPost post = postService.selectCwPostById(postId);
        if (post == null) {
            return error("帖子不存在或已删除");
        }
        return success(post);
    }

    /**
     * 发布帖子
     */
    @Log(title = "校园墙帖子", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CwPost post) {
        try {
            // 校验必填字段
            if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
                return error("帖子标题不能为空");
            }
            if (post.getContent() == null || post.getContent().trim().isEmpty()) {
                return error("帖子内容不能为空");
            }
            if (post.getTitle().length() > 200) {
                return error("标题长度不能超过200字");
            }
            if (post.getContent().length() > 5000) {
                return error("内容长度不能超过5000字");
            }

            CwPost result = postService.publishPost(post);
            return success(result);
        } catch (Exception e) {
            logger.error("发布帖子失败", e);
            return error("发布失败: " + e.getMessage());
        }
    }

    /**
     * 编辑帖子(仅作者可编辑)
     */
    @Log(title = "校园墙帖子", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CwPost post) {
        try {
            if (post.getPostId() == null) {
                return error("帖子ID不能为空");
            }
            // 权限校验: 仅作者可编辑
            CwPost existing = postService.selectCwPostById(post.getPostId());
            if (existing == null) {
                return error("帖子不存在");
            }
            if (!getUsername().equals(existing.getCreateBy())) {
                return error("只能编辑自己的帖子");
            }
            postService.updateCwPost(post);
            return success();
        } catch (Exception e) {
            logger.error("编辑帖子失败", e);
            return error("编辑失败: " + e.getMessage());
        }
    }

    /**
     * 删除帖子
     */
    @Log(title = "校园墙帖子", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    public AjaxResult remove(@PathVariable Long[] postIds) {
        try {
            postService.deleteCwPostByIds(postIds);
            return success();
        } catch (Exception e) {
            logger.error("删除帖子失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }

    // ==================== 用户中心 ====================

    /**
     * 我的帖子列表
     */
    @GetMapping("/my")
    public TableDataInfo myPosts(CwPost post) {
        startPage();
        List<CwPost> list = postService.selectMyCwPostList(post);
        return getDataTable(list);
    }

    // ==================== 分类相关 ====================

    /**
     * 分类列表(用户端选择用)
     */
    @GetMapping("/category/list")
    public AjaxResult categoryList() {
        List<CwCategory> list = categoryService.selectCwCategoryList(null);
        return success(list);
    }
}
