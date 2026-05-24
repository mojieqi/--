package com.ruoyi.system.agent;

import com.ruoyi.system.domain.AiConversationMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 记忆管理器 — 短期记忆窗口管理与摘要压缩
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class MemoryManager {

    /** 默认保留最近 N 轮对话(一轮 = 用户消息 + 助手回复) */
    private static final int DEFAULT_MAX_ROUNDS = 10;

    /** 摘要压缩阈值: 超过此轮数时触发压缩 */
    private static final int COMPRESS_THRESHOLD = 10;

    /** 压缩后保留的最近轮数 */
    private static final int KEEP_RECENT_ROUNDS = 4;

    /** 摘要压缩的提示词 */
    private static final String SUMMARIZE_PROMPT =
        "请将以下对话历史压缩为一段简洁的摘要，保留关键信息、用户偏好和重要事实，不超过200字：";

    private final int maxRounds;

    public MemoryManager() {
        this(DEFAULT_MAX_ROUNDS);
    }

    public MemoryManager(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    /**
     * 获取窗口内的对话消息
     * 如果超过阈值，需要压缩早期消息
     */
    public List<AiConversationMessage> getWindowMessages(List<AiConversationMessage> allMessages) {
        if (allMessages == null || allMessages.isEmpty()) {
            return new ArrayList<>();
        }

        int rounds = countRounds(allMessages);

        if (rounds <= COMPRESS_THRESHOLD) {
            // 未超过阈值，直接返回所有消息
            return allMessages;
        }

        // 超过阈值，保留最近的 KEEP_RECENT_ROUNDS 轮，早期消息返回给调用者进行摘要
        return keepRecentMessages(allMessages, KEEP_RECENT_ROUNDS);
    }

    /**
     * 生成需要摘要的早期对话文本
     */
    public String getEarlyMessagesForSummary(List<AiConversationMessage> allMessages) {
        if (allMessages == null || allMessages.isEmpty()) {
            return null;
        }

        int rounds = countRounds(allMessages);
        if (rounds <= COMPRESS_THRESHOLD) {
            return null;
        }

        List<AiConversationMessage> earlyMessages = getEarlyMessages(allMessages);
        if (earlyMessages.isEmpty()) {
            return null;
        }

        return earlyMessages.stream()
                .map(m -> m.getRole() + ": " + truncateContent(m.getContent(), 300))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 构建摘要消息
     */
    public String buildSummaryPrompt(String earlyMessagesText) {
        if (earlyMessagesText == null || earlyMessagesText.isEmpty()) {
            return null;
        }
        return SUMMARIZE_PROMPT + "\n\n" + earlyMessagesText;
    }

    /**
     * 计算对话轮数
     */
    public int countRounds(List<AiConversationMessage> messages) {
        int userCount = 0;
        for (AiConversationMessage msg : messages) {
            if ("user".equals(msg.getRole())) {
                userCount++;
            }
        }
        return userCount;
    }

    /**
     * 是否需要压缩
     */
    public boolean needCompression(List<AiConversationMessage> messages) {
        return countRounds(messages) > COMPRESS_THRESHOLD;
    }

    /**
     * 估算消息列表的Token数(粗略: 1字符≈0.5 token)
     */
    public int estimateTokens(List<AiConversationMessage> messages) {
        if (messages == null) return 0;
        int totalChars = 0;
        for (AiConversationMessage msg : messages) {
            if (msg.getContent() != null) {
                totalChars += msg.getContent().length();
            }
        }
        return totalChars / 2;
    }

    /**
     * 估算单条消息的Token数
     */
    public int estimateTokens(String content) {
        if (content == null || content.isEmpty()) return 0;
        return content.length() / 2;
    }

    // --- private helpers ---

    private List<AiConversationMessage> keepRecentMessages(List<AiConversationMessage> messages, int keepRounds) {
        int totalRounds = countRounds(messages);
        if (totalRounds <= keepRounds) return messages;

        int skipRounds = totalRounds - keepRounds;
        int skippedUserMsgCount = 0;
        int startIdx = 0;

        for (int i = 0; i < messages.size(); i++) {
            if ("user".equals(messages.get(i).getRole())) {
                skippedUserMsgCount++;
                if (skippedUserMsgCount > skipRounds) {
                    startIdx = i;
                    break;
                }
            }
        }

        return messages.subList(startIdx, messages.size());
    }

    private List<AiConversationMessage> getEarlyMessages(List<AiConversationMessage> messages) {
        int totalRounds = countRounds(messages);
        int keepRounds = KEEP_RECENT_ROUNDS;
        int skipRounds = totalRounds - keepRounds;

        int skippedUserMsgCount = 0;
        int endIdx = 0;

        for (int i = 0; i < messages.size(); i++) {
            if ("user".equals(messages.get(i).getRole())) {
                skippedUserMsgCount++;
                if (skippedUserMsgCount > skipRounds) {
                    endIdx = i;
                    break;
                }
            }
        }

        if (endIdx == 0) endIdx = messages.size();
        return messages.subList(0, endIdx);
    }

    private String truncateContent(String content, int maxLen) {
        if (content == null) return "";
        if (content.length() <= maxLen) return content;
        return content.substring(0, maxLen) + "...";
    }
}
