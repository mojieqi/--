package com.ruoyi.system.agent.tool;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.system.domain.AiTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 内容审核工具 — 基于关键词匹配审核校园墙帖子/评论内容
 *
 * 内置校园场景敏感词库，覆盖以下类别:
 * - 违法信息(毒品/赌博/诈骗)
 * - 人身攻击(辱骂/歧视)
 * - 色情低俗
 * - 广告营销
 * - 校园安全(暴力/欺凌)
 *
 * 参数:
 * - content (必填): 待审核文本
 * - scene (可选): 审核场景 [post, comment, profile]，默认 post
 *
 * Phase 4.4: 基于关键词的轻量审核
 * Phase 5.x: 可升级为 LLM-based 审核（复用 Phase 1 的 content_audit 提示词模板）
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class ContentAuditTool extends AbstractTool {

    private static final Logger log = LoggerFactory.getLogger(ContentAuditTool.class);

    /** 内容最大审核长度 */
    private static final int MAX_CONTENT_LENGTH = 5000;

    /** 敏感词库 */
    private static final Map<String, List<String>> SENSITIVE_WORDS = new LinkedHashMap<>();

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

    /** 联系人信息模式（正则） */
    private static final List<String> CONTACT_PATTERNS = Arrays.asList(
            "1[3-9]\\d{9}",           // 手机号
            "\\d{3}-\\d{8}",          // 座机
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"  // 邮箱
    );

    public ContentAuditTool(AiTool toolMeta) {
        super(toolMeta);
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        requireParams(arguments, "content");
        String content = getStringParam(arguments, "content");
        String scene = getStringParam(arguments, "scene");
        if (scene == null || scene.isEmpty()) {
            scene = "post";
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            content = content.substring(0, MAX_CONTENT_LENGTH) + "...";
        }

        log.info("ContentAuditTool 开始审核: scene={}, content_length={}", scene, content.length());

        try {
            return audit(content, scene);
        } catch (Exception e) {
            log.error("ContentAuditTool 审核异常: {}", e.getMessage());
            return JSON.toJSONString(Collections.singletonMap("error",
                    "审核处理失败: " + e.getMessage()));
        }
    }

    /**
     * 执行审核
     */
    private String audit(String content, String scene) {
        // 统一转为小写进行匹配
        String lowerContent = content.toLowerCase();

        Map<String, List<String>> violations = new LinkedHashMap<>();
        int totalHits = 0;

        // 遍历敏感词库
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

        // 构建审核结果
        StringBuilder result = new StringBuilder();

        if (violations.isEmpty() && contacts.isEmpty()) {
            result.append("审核结果: ✅ 通过\n");
            result.append("审核场景: ").append(getSceneLabel(scene)).append("\n");
            result.append("审核内容长度: ").append(content.length()).append(" 字\n");
            result.append("未检测到违规内容。\n");
            return result.toString();
        }

        // 有违规内容
        result.append("审核结果: ⚠️ 存在违规内容\n");
        result.append("审核场景: ").append(getSceneLabel(scene)).append("\n");
        result.append("违规项数: ").append(totalHits).append("\n\n");

        for (Map.Entry<String, List<String>> entry : violations.entrySet()) {
            result.append("【").append(entry.getKey()).append("】命中: ");
            result.append(String.join("、", entry.getValue()));
            result.append("\n");
        }

        if (!contacts.isEmpty()) {
            result.append("\n【个人信息】检测到: ");
            result.append(String.join("、", contacts));
            result.append("\n");
            result.append("提示: 公开内容中不建议包含个人联系方式。\n");
        }

        // 给出处理建议
        result.append("\n处理建议: ");
        if (totalHits >= 3) {
            result.append("🔴 严重违规，建议拒绝发布并进行人工复核。");
        } else if (totalHits >= 2) {
            result.append("🟡 中等违规，建议修改后重新提交。");
        } else {
            result.append("🟢 轻微违规，建议提示用户修改后发布。");
        }

        return result.toString();
    }

    private String getSceneLabel(String scene) {
        switch (scene.toLowerCase()) {
            case "comment": return "评论";
            case "profile": return "个人资料";
            default: return "帖子";
        }
    }
}
