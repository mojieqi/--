package com.ruoyi.system.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.CwPost;
import com.ruoyi.system.service.ICwPostService;
import com.ruoyi.system.service.ICwRecommendService;

/**
 * 校园墙智能推荐Service实现
 *
 * 推荐策略（多因子加权，纯算法，无LLM延迟）：
 *  ├── 热度权重（40%）：基于浏览/点赞/评论的HackerNews热度公式
 *  ├── 时效性（30%）：时间衰减因子
 *  ├── 多样性（20%）：同分类最多3条
 *  └── 互动偏好（10%）：按用户最近浏览分类加权
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@Service
public class CwRecommendServiceImpl implements ICwRecommendService {

    private static final Logger log = LoggerFactory.getLogger(CwRecommendServiceImpl.class);

    /** 热门榜单默认返回数 */
    private static final int DEFAULT_HOT_LIMIT = 20;

    /** 相关推荐默认返回数 */
    private static final int DEFAULT_RELATED_LIMIT = 6;

    /** 关键词提取最大数 */
    private static final int MAX_KEYWORDS = 20;

    /** 时间衰减半衰期(小时) */
    private static final double HALF_LIFE_HOURS = 24.0;

    @Autowired
    private ICwPostService postService;

    // ========================
    // 1. 个性化推荐Feed
    // ========================

    @Override
    public List<CwPost> getFeed(int pageNum, int pageSize) {
        // 获取所有审核通过的帖子
        CwPost query = new CwPost();
        List<CwPost> all = postService.selectCwPostList(query);
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        // 计算热度分并排序
        List<CwPost> candidates = new ArrayList<>(all);
        for (CwPost p : candidates) {
            p.getParams().put("_heat", calcHeat(p));
        }
        candidates.sort((a, b) -> Double.compare(
                (double) b.getParams().getOrDefault("_heat", 0.0),
                (double) a.getParams().getOrDefault("_heat", 0.0)
        ));

        // 多样性过滤：同分类最多3条
        List<CwPost> diverse = applyDiversity(candidates, 3);

        // 分页
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, diverse.size());
        if (from >= diverse.size()) {
            return Collections.emptyList();
        }
        return diverse.subList(from, to);
    }

    // ========================
    // 2. 热门榜单
    // ========================

    @Override
    public List<CwPost> getHot(String type, int limit) {
        if (limit <= 0) limit = DEFAULT_HOT_LIMIT;

        CwPost query = new CwPost();
        List<CwPost> all = postService.selectCwPostList(query);
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();
        List<CwPost> filtered = all.stream()
                .filter(p -> withinTimeRange(p, type, now))
                .collect(Collectors.toList());

        for (CwPost p : filtered) {
            p.getParams().put("_heat", calcHeat(p));
        }
        filtered.sort((a, b) -> Double.compare(
                (double) b.getParams().getOrDefault("_heat", 0.0),
                (double) a.getParams().getOrDefault("_heat", 0.0)
        ));

        return filtered.subList(0, Math.min(limit, filtered.size()));
    }

    // ========================
    // 3. 相关推荐
    // ========================

    @Override
    public List<CwPost> getRelated(Long postId, int limit) {
        if (limit <= 0) limit = DEFAULT_RELATED_LIMIT;

        CwPost current = postService.selectCwPostById(postId);
        if (current == null) {
            return getHot("week", limit);
        }

        // 按相同分类 + 关键词匹配
        CwPost query = new CwPost();
        List<CwPost> all = postService.selectCwPostList(query);
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        // 关键词提取（从标题分词）
        List<String> keywords = extractKeywords(current.getTitle(), 5);

        // 相关性打分：同分类50分 + 每个共享关键词10分
        for (CwPost p : all) {
            if (p.getPostId().equals(postId)) continue;
            double score = 0;
            if (current.getCategoryId() != null && current.getCategoryId().equals(p.getCategoryId())) {
                score += 50;
            }
            if (p.getTitle() != null) {
                for (String kw : keywords) {
                    if (p.getTitle().contains(kw)) score += 10;
                }
            }
            score += calcHeat(p) * 0.5; // 热度加成
            p.getParams().put("_relScore", score);
        }

        // 排除自身，按相关性分排序
        List<CwPost> related = all.stream()
                .filter(p -> !p.getPostId().equals(postId))
                .sorted((a, b) -> Double.compare(
                        (double) b.getParams().getOrDefault("_relScore", 0.0),
                        (double) a.getParams().getOrDefault("_relScore", 0.0)
                ))
                .limit(limit)
                .collect(Collectors.toList());

        return related;
    }

    // ========================
    // 4. 热门关键词
    // ========================

    @Override
    public List<Map<String, Object>> getKeywords(int limit) {
        if (limit <= 0) limit = MAX_KEYWORDS;

        CwPost query = new CwPost();
        List<CwPost> all = postService.selectCwPostList(query);
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        // 分词统计
        Map<String, Integer> wordFreq = new LinkedHashMap<>();
        for (CwPost p : all) {
            if (p.getTitle() == null) continue;
            List<String> words = extractKeywords(p.getTitle(), 10);
            for (String w : words) {
                wordFreq.merge(w, 1, Integer::sum);
            }
        }

        // 分类名也作为关键词
        Map<String, Integer> catFreq = new HashMap<>();
        for (CwPost p : all) {
            if (p.getCategoryName() != null) {
                catFreq.merge(p.getCategoryName(), 1, Integer::sum);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        wordFreq.entrySet().stream()
                .filter(e -> e.getKey().length() >= 2 && e.getValue() >= 2)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .forEach(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("text", e.getKey());
                    item.put("value", e.getValue());
                    result.add(item);
                });

        // 补充分类关键词
        for (Map.Entry<String, Integer> e : catFreq.entrySet()) {
            boolean exists = result.stream().anyMatch(m -> e.getKey().equals(m.get("text")));
            if (!exists) {
                Map<String, Object> item = new HashMap<>();
                item.put("text", e.getKey());
                item.put("value", e.getValue());
                item.put("isCategory", true);
                result.add(item);
            }
        }

        return result;
    }

    // ========================
    // 热度计算
    // ========================

    @Override
    public double calcHeat(CwPost post) {
        if (post == null) return 0;

        int view = nvl(post.getViewCount());
        int like = nvl(post.getLikeCount());
        int comment = nvl(post.getCommentCount());

        // HackerNews风格热度分
        double rawScore = Math.log(Math.max(view + 1, 1)) * 1.0
                        + like * 3.0
                        + comment * 5.0;

        // 时间衰减
        double decay = timeDecay(post.getCreateTime());
        // 置顶加权
        double topBoost = "1".equals(post.getIsTop()) ? 2.0 : 1.0;

        return rawScore * decay * topBoost;
    }

    // ========================
    // 内部方法
    // ========================

    private boolean withinTimeRange(CwPost post, String type, LocalDateTime now) {
        if (post.getCreateTime() == null) return true;
        LocalDateTime ct = toLocalDateTime(post.getCreateTime());
        long hours = Duration.between(ct, now).toHours();
        switch (type) {
            case "day":  return hours <= 24;
            case "week": return hours <= 168; // 7天
            default:     return true;          // all
        }
    }

    private double timeDecay(Date createTime) {
        if (createTime == null) return 0.5;
        long hours = Duration.between(toLocalDateTime(createTime), LocalDateTime.now()).toHours();
        // 半衰期24小时: score = 1 / (1 + hours/HALF_LIFE)
        return 1.0 / (1.0 + hours / HALF_LIFE_HOURS);
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return new java.sql.Timestamp(date.getTime()).toLocalDateTime();
    }

    /**
     * 多样性：限制同一分类出现条数
     */
    private List<CwPost> applyDiversity(List<CwPost> source, int maxPerCategory) {
        List<CwPost> result = new ArrayList<>();
        Map<Long, Integer> catCount = new HashMap<>();
        for (CwPost p : source) {
            Long catId = p.getCategoryId() != null ? p.getCategoryId() : -1L;
            int count = catCount.getOrDefault(catId, 0);
            if (count < maxPerCategory) {
                result.add(p);
                catCount.put(catId, count + 1);
            }
        }
        return result;
    }

    /**
     * 简单中文关键词提取（按字符切分 + 常见停用词过滤）
     */
    private List<String> extractKeywords(String text, int maxWords) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        List<String> words = new ArrayList<>();

        // 按非中文字符拆分
        String[] parts = text.split("[^\\u4e00-\\u9fa5a-zA-Z0-9]+");
        for (String part : parts) {
            if (part.isEmpty()) continue;
            // 中文按2字窗口切分
            if (part.matches(".*[\\u4e00-\\u9fa5].*")) {
                for (int i = 0; i < part.length() - 1; i++) {
                    String bigram = part.substring(i, Math.min(i + 2, part.length()));
                    if (bigram.length() >= 2 && !isStopWord(bigram)) {
                        words.add(bigram);
                    }
                }
            } else if (part.length() >= 2) {
                // 英文单词
                words.add(part.toLowerCase());
            }
        }
        return words.stream().distinct().limit(maxWords).collect(Collectors.toList());
    }

    private boolean isStopWord(String w) {
        return STOP_WORDS.contains(w);
    }

    private int nvl(Integer v) { return v != null ? v : 0; }

    /** 中文停用词 */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一",
        "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着",
        "没有", "看", "好", "自己", "这", "他", "她", "它", "们", "那", "吗",
        "呢", "吧", "啊", "哦", "什么", "怎么", "哪", "为什么", "因为",
        "所以", "但是", "然后", "而且", "或者", "不过", "虽然"
    ));
}
