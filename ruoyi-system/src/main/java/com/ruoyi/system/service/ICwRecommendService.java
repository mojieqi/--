package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.CwPost;

/**
 * 校园墙智能推荐Service接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface ICwRecommendService {

    /**
     * 个性化推荐Feed流
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 推荐帖子列表
     */
    List<CwPost> getFeed(int pageNum, int pageSize);

    /**
     * 热门榜单
     *
     * @param type 榜单类型(day/week/all)
     * @param limit 返回条数
     * @return 热帖列表
     */
    List<CwPost> getHot(String type, int limit);

    /**
     * 相关推荐(基于分类+关键词匹配)
     *
     * @param postId 当前帖子ID
     * @param limit  返回条数
     * @return 相关帖子列表
     */
    List<CwPost> getRelated(Long postId, int limit);

    /**
     * 热门关键词
     *
     * @param limit 返回数量
     * @return 关键词列表(含频次)
     */
    List<Map<String, Object>> getKeywords(int limit);

    /**
     * 计算帖子热度分
     */
    double calcHeat(CwPost post);
}
