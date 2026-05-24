package com.ruoyi.system.service.impl;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.system.service.IContentAuditService;

/**
 * AI内容审核Service实现 — 基于关键词匹配
 *
 * 与 ContentAuditTool 共享相同的敏感词库逻辑，
 * 但作为 Spring Bean 注入，供业务Service直接调用。
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@Service
public class ContentAuditServiceImpl implements IContentAuditService {

    private static final Logger log = LoggerFactory.getLogger(ContentAuditServiceImpl.class);

    /** 内容最大审核长度 */
    private static final int MAX_CONTENT_LENGTH = 5000;

    /** 敏感词库：分类 → 敏感词列表 */
    private static final Map<String, List<String>> SENSITIVE_WORDS = new LinkedHashMap<>();

    /** 联系人信息模式（正则） */
    private static final List<String> CONTACT_PATTERNS = Arrays.asList(
            "1[3-9]\\d{9}",                                     // 手机号
            "\\d{3}-\\d{8}",                                    // 座机
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}" // 邮箱
    );

    static {
        SENSITIVE_WORDS.put("违法信息", Arrays.asList(
                "毒品", "吸毒", "贩毒", "赌博", "赌场", "博彩", "六合彩",
                "诈骗", "骗钱", "传销", "洗钱", "高利贷", "裸贷"
        ));
        SENSITIVE_WORDS.put("人身攻击", Arrays.asList(
                "傻逼", "脑残", "智障", "废物", "垃圾人", "去死",
                "人渣", "畜生", "狗日的", "他妈的", "操你", "fuck"
        ));
        SENSITIVE_WORDS.put("色情低俗", Arrays.asList(
                "约炮", "嫖娼", "卖淫", "裸聊", "性交", "做爱",
                "一夜情", "包养", "援交", "色情", "黄色", "av"
        ));
        SENSITIVE_WORDS.put("广告营销", Arrays.asList(
                "加微信", "扫码加", "刷单", "兼职赚钱", "日赚",
                "代理", "微商", "代购", "加群", "点击链接"
        ));
        SENSITIVE_WORDS.put("校园安全", Arrays.asList(
                "打架", "群殴", "霸凌", "欺凌", "校园暴力",
                "跳楼", "自杀", "自残", "割腕"
        ));
    }

    @Override
    public Map<String, Object> audit(String content, String scene) {
        if (scene == null || scene.isEmpty()) {
            scene = "post";
        }
        if (content == null || content.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("passed", false);
            result.put("score", 0.0);
            result.put("reason", "内容为空");
            return result;
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            content = content.substring(0, MAX_CONTENT_LENGTH);
        }

        String lowerContent = content.toLowerCase();

        Map<String, List<String>> violations = new LinkedHashMap<>();
        int totalHits = 0;

        for (Map.Entry<String, List<String>> entry : SENSITIVE_WORDS.entrySet()) {
            String category = entry.getKey();
            List<String> hitWords = new ArrayList<>();

            for (String word : entry.getValue()) {
                if (lowerContent.contains(word.toLowerCase())) {
                    hitWords.add(word);
                    totalHits++;
                }
            }

            if (!hitWords.isEmpty()) {
                violations.put(category, hitWords);
            }
        }

        // 检测联系人信息
        List<String> contacts = new ArrayList<>();
        for (String pattern : CONTACT_PATTERNS) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(content);
            while (m.find() && contacts.size() < 3) {
                contacts.add(m.group());
            }
        }

        Map<String, Object> result = new HashMap<>();

        if (violations.isEmpty() && contacts.isEmpty()) {
            result.put("passed", true);
            result.put("score", 100.0);
            result.put("reason", "内容合规");
            result.put("violations", new HashMap<>());
            log.info("审核通过: scene={}, contentLength={}", scene, content.length());
            return result;
        }

        // 有违规内容
        StringBuilder reason = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : violations.entrySet()) {
            if (reason.length() > 0) reason.append("; ");
            reason.append("【").append(entry.getKey()).append("】命中: ")
                  .append(String.join("、", entry.getValue()));
        }
        if (!contacts.isEmpty()) {
            if (reason.length() > 0) reason.append("; ");
            reason.append("【个人信息】检测到联系方式");
        }

        // 计算分数：每个违规词扣20分，最低0分
        double score = Math.max(0, 100 - totalHits * 20);

        // 达标判断：2个及以上违规 → 不通过
        boolean passed = totalHits < 2;

        result.put("passed", passed);
        result.put("score", score);
        result.put("reason", reason.toString());
        result.put("violations", violations);

        log.info("审核结果: scene={}, passed={}, score={}, hits={}", scene, passed, score, totalHits);
        return result;
    }
}
