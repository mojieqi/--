package com.ruoyi.system.controller.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.CwPost;
import com.ruoyi.system.service.ICwRecommendService;

/**
 * 校园墙智能推荐Controller
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@RestController
@RequestMapping("/campus/recommend")
public class CwRecommendController extends BaseController {

    @Autowired
    private ICwRecommendService recommendService;

    /**
     * 个性化推荐Feed流
     */
    @GetMapping("/feed")
    public AjaxResult feed(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<CwPost> list = recommendService.getFeed(pageNum, pageSize);
        // 手动构建分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("rows", list);
        result.put("total", list.size());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return success(result);
    }

    /**
     * 热门榜单
     */
    @GetMapping("/hot")
    public AjaxResult hot(
            @RequestParam(defaultValue = "day") String type,
            @RequestParam(defaultValue = "20") int limit) {
        List<CwPost> list = recommendService.getHot(type, limit);
        // 清除临时热度数据再返回
        for (CwPost p : list) {
            p.getParams().remove("_heat");
        }
        return success(list);
    }

    /**
     * 相关推荐（帖子详情页使用）
     */
    @GetMapping("/related/{postId}")
    public AjaxResult related(
            @PathVariable("postId") Long postId,
            @RequestParam(defaultValue = "6") int limit) {
        List<CwPost> list = recommendService.getRelated(postId, limit);
        for (CwPost p : list) {
            p.getParams().remove("_relScore");
        }
        return success(list);
    }

    /**
     * 热门关键词
     */
    @GetMapping("/keywords")
    public AjaxResult keywords(@RequestParam(defaultValue = "20") int limit) {
        List<Map<String, Object>> list = recommendService.getKeywords(limit);
        return success(list);
    }
}
