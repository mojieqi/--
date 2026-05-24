package com.ruoyi.system.agent.tool;

import com.ruoyi.system.domain.AiTool;
import com.ruoyi.system.service.IEmbeddingService;
import com.ruoyi.system.service.IEmbeddingService.ChunkSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 知识库查询工具 — 在项目知识库中进行语义搜索
 *
 * 对接现有的 IEmbeddingService.searchInBase()，基于向量余弦相似度匹配。
 * 参数 keyword 为必填，topK 可选(默认3)。
 * kbId 通过 arguments 中的 kbId 或默认搜索全部已向量化的知识库。
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class KbQueryTool extends AbstractTool {

    private static final Logger log = LoggerFactory.getLogger(KbQueryTool.class);

    private static final int DEFAULT_TOP_K = 3;
    private static final int MAX_TOP_K = 10;

    public KbQueryTool(AiTool toolMeta) {
        super(toolMeta);
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        requireParams(arguments, "keyword");
        String keyword = getStringParam(arguments, "keyword");
        Integer topK = getIntParam(arguments, "topK");
        if (topK == null || topK <= 0) {
            topK = DEFAULT_TOP_K;
        }
        if (topK > MAX_TOP_K) {
            topK = MAX_TOP_K;
        }

        // 可选: 指定知识库ID
        Long kbId = null;
        Object kbIdObj = arguments.get("kbId");
        if (kbIdObj != null) {
            try {
                kbId = Long.valueOf(kbIdObj.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        log.info("KbQueryTool 开始查询: keyword={}, kbId={}, topK={}", keyword, kbId, topK);

        try {
            IEmbeddingService embeddingService = ToolSpringContext.getEmbeddingService();

            List<ChunkSearchResult> results;
            if (kbId != null) {
                results = embeddingService.searchInBase(kbId, keyword, topK);
            } else {
                // 未指定 kbId 时，搜索所有可用知识库（这里需要遍历）
                // 实际场景中 kbId 由 LLM 或前端提供，此处作降级处理
                log.warn("KbQueryTool 未指定 kbId，返回提示信息");
                return "请指定知识库ID(kbId)进行查询。如果不知道可用知识库，请提示用户提供。";
            }

            if (results == null || results.isEmpty()) {
                return "未在知识库中找到与「" + keyword + "」相关的内容。";
            }

            return formatKnowledgeResults(keyword, results);
        } catch (IllegalStateException e) {
            log.error("KbQueryTool EmbeddingService 未初始化: {}", e.getMessage());
            return "知识库查询服务未就绪，请联系管理员。";
        } catch (Exception e) {
            log.error("KbQueryTool 查询异常: {}", e.getMessage());
            return "知识库查询失败: " + e.getMessage();
        }
    }

    /**
     * 格式化知识库搜索结果
     */
    private String formatKnowledgeResults(String keyword, List<ChunkSearchResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("知识库搜索结果 (查询: \"").append(keyword).append("\"):\n\n");

        for (int i = 0; i < results.size(); i++) {
            ChunkSearchResult r = results.get(i);
            sb.append("--- 结果 ").append(i + 1);
            if (r.getSimilarityScore() != null) {
                sb.append(" (相关度: ").append(String.format("%.1f%%", r.getSimilarityScore() * 100)).append(")");
            }
            sb.append(" ---\n");
            sb.append(r.getChunkContent());
            sb.append("\n\n");
        }

        return sb.toString();
    }
}
