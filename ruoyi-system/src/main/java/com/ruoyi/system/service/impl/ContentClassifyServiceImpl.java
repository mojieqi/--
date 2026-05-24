package com.ruoyi.system.service.impl;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.system.service.IContentClassifyService;

/**
 * AI智能分类Service实现
 *
 * 策略: 关键词优先(快速准确) + LLM兜底(Phase 6先实现关键词，LLM增强后续迭代)
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@Service
public class ContentClassifyServiceImpl implements IContentClassifyService {

    private static final Logger log = LoggerFactory.getLogger(ContentClassifyServiceImpl.class);

    /** 分类关键词映射 */
    private static final Map<String, ClassificationRule> RULES = new LinkedHashMap<>();

    static {
        RULES.put("confession", new ClassificationRule("表白墙", 80, Arrays.asList(
            "表白", "喜欢", "暗恋", "心动了", "一见钟情", "好感", "想认识", "crush",
            "偷偷喜欢", "忍不住", "关注你", "每天都能看到", "想对你说"
        )));
        RULES.put("lost_found", new ClassificationRule("寻物启事", 90, Arrays.asList(
            "丢失", "寻找", "不见", "谁看到", "谁拿了", "归还", "寻物", "焦急",
            "帮忙找", "借了忘了", "落在", "遗忘", "急寻", "失物招领"
        )));
        RULES.put("help", new ClassificationRule("求助问答", 70, Arrays.asList(
            "求助", "请问", "有没有人知道", "怎么办", "帮帮我", "求解",
            "谁可以", "谁知道", "怎么解决", "请教", "想问一下"
        )));
        RULES.put("news", new ClassificationRule("校园资讯", 85, Arrays.asList(
            "通知", "公告", "活动", "讲座", "比赛", "考试安排", "选课",
            "教务处", "学生会", "社团", "招新"
        )));
        RULES.put("life", new ClassificationRule("生活吐槽", 60, Arrays.asList(
            "吐槽", "搞笑", "无语", "日常", "今天", "食堂", "宿舍", "快递",
            "外卖", "天气", "不想上课"
        )));
        RULES.put("trade", new ClassificationRule("二手交易", 95, Arrays.asList(
            "出售", "转让", "二手", "便宜出", "闲置", "求购", "想买",
            "卖", "价格", "交易", "面交", "自提", "买来没用"
        )));
        RULES.put("study", new ClassificationRule("学习交流", 80, Arrays.asList(
            "学习", "考试", "复习", "刷题", "考研", "四六级", "论文",
            "期末", "绩点", "课程", "图书馆", "自习"
        )));
    }

    /**
     * 分类规则
     */
    private static class ClassificationRule {
        final String categoryName;
        final int weight;
        final List<String> keywords;

        ClassificationRule(String categoryName, int weight, List<String> keywords) {
            this.categoryName = categoryName;
            this.weight = weight;
            this.keywords = keywords;
        }
    }

    @Override
    public ClassificationResult classify(String title, String content) {
        String text = (title != null ? title + " " : "") + (content != null ? content : "");
        text = text.toLowerCase();

        // 关键词匹配
        String bestCategoryCode = null;
        String bestCategoryName = null;
        double bestScore = 0;

        for (Map.Entry<String, ClassificationRule> entry : RULES.entrySet()) {
            ClassificationRule rule = entry.getValue();
            int hitCount = 0;
            for (String keyword : rule.keywords) {
                if (text.contains(keyword.toLowerCase())) {
                    hitCount++;
                }
            }
            if (hitCount > 0) {
                // 得分 = 命中数/关键词总数 * 权重
                double score = (double) hitCount / rule.keywords.size() * rule.weight / 100.0;
                if (score > bestScore) {
                    bestScore = score;
                    bestCategoryCode = entry.getKey();
                    bestCategoryName = rule.categoryName;
                }
            }
        }

        if (bestCategoryCode != null && bestScore > 0.05) {
            log.info("AI分类结果: code={}, name={}, source=keyword, score={:.2f}",
                    bestCategoryCode, bestCategoryName, bestScore);
            return new ClassificationResult(bestCategoryCode, bestCategoryName,
                    Math.min(bestScore * 1.2, 0.95), "keyword");
        }

        // 兜底：归入生活吐槽
        log.info("AI分类结果: 无匹配关键词，兜底为【生活吐槽】");
        return new ClassificationResult("life", "生活吐槽", 0.5, "keyword");
    }
}
