package com.ruoyi.system.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.*;
import com.ruoyi.system.service.IAiAgentService;
import com.ruoyi.system.service.IAiConversationService;
import com.ruoyi.system.service.IAiMemoryService;
import com.ruoyi.system.service.IEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI Agent 控制台 Controller
 *
 * @author ruoyi
 * @date 2026-05-23
 */
@RestController
@RequestMapping("/ai/agent")
public class AiAgentController extends BaseController {

    @Autowired
    private IAiConversationService conversationService;

    @Autowired
    private IAiAgentService agentService;

    @Autowired
    private IAiMemoryService memoryService;

    @Autowired
    private IEmbeddingService embeddingService;

    // ==================== 会话管理 ====================

    /**
     * 会话列表
     */
    @PreAuthorize("@ss.hasPermi('ai:agent:list')")
    @GetMapping("/conversation/list")
    public TableDataInfo list(AiConversation conversation) {
        startPage();
        List<AiConversation> list = conversationService.selectAiConversationList(conversation);
        return getDataTable(list);
    }

    /**
     * 新建会话
     */
    @PreAuthorize("@ss.hasPermi('ai:agent:add')")
    @Log(title = "AI Agent会话", businessType = BusinessType.INSERT)
    @PostMapping("/conversation")
    public AjaxResult add(@RequestBody AiConversation conversation) {
        return toAjax(conversationService.insertAiConversation(conversation));
    }

    /**
     * 删除会话
     */
    @PreAuthorize("@ss.hasPermi('ai:agent:remove')")
    @Log(title = "AI Agent会话", businessType = BusinessType.DELETE)
    @DeleteMapping("/conversation/{conversationIds}")
    public AjaxResult remove(@PathVariable Long[] conversationIds) {
        return toAjax(conversationService.deleteAiConversationByIds(conversationIds));
    }

    /**
     * 清空会话上下文
     */
    @PreAuthorize("@ss.hasPermi('ai:agent:chat')")
    @Log(title = "AI Agent会话", businessType = BusinessType.UPDATE)
    @PostMapping("/conversation/{conversationId}/clear")
    public AjaxResult clear(@PathVariable Long conversationId) {
        return toAjax(conversationService.clearConversation(conversationId));
    }

    // ==================== 消息管理 ====================

    /**
     * 获取会话消息列表
     */
    @PreAuthorize("@ss.hasPermi('ai:agent:query')")
    @GetMapping("/message/list/{conversationId}")
    public AjaxResult getMessages(@PathVariable Long conversationId) {
        List<AiConversationMessage> messages = agentService.getMessages(conversationId);
        return success(messages);
    }

    /**
     * 发送消息 (SSE流式响应)
     */
    @PreAuthorize("@ss.hasPermi('ai:agent:chat')")
    @Log(title = "AI Agent对话", businessType = BusinessType.OTHER)
    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody AiConversationMessage message) {
        return agentService.chat(message.getConversationId(), message.getContent());
    }

    /**
     * 停止生成
     */
    @PreAuthorize("@ss.hasPermi('ai:agent:chat')")
    @PostMapping("/chat/stop")
    public AjaxResult stop() {
        // SSE 连接由前端 EventSource.close() 处理
        return success("已停止");
    }

    // ==================== 知识库检索 ====================

    /**
     * 知识库语义搜索
     */
    @PreAuthorize("@ss.hasPermi('ai:agent:query')")
    @PostMapping("/search")
    public AjaxResult search(@RequestBody AiDocumentChunk searchParam) {
        if (searchParam.getKbId() == null) {
            return error("知识库ID不能为空");
        }
        List<IEmbeddingService.ChunkSearchResult> results =
                embeddingService.searchInBase(searchParam.getKbId(),
                        searchParam.getChunkContent(), 10);
        return success(results);
    }

    // ==================== 记忆管理 (Phase 3 基础CRUD) ====================

    /**
     * 长期记忆列表
     */
    @PreAuthorize("@ss.hasPermi('ai:agent:query')")
    @GetMapping("/memory/list")
    public TableDataInfo memoryList(AiUserMemory memory) {
        startPage();
        List<AiUserMemory> list = memoryService.selectAiUserMemoryList(memory);
        return getDataTable(list);
    }

    /**
     * 删除记忆
     */
    @PreAuthorize("@ss.hasPermi('ai:agent:remove')")
    @Log(title = "AI Agent记忆", businessType = BusinessType.DELETE)
    @DeleteMapping("/memory/{memoryIds}")
    public AjaxResult removeMemory(@PathVariable Long[] memoryIds) {
        return toAjax(memoryService.deleteAiUserMemoryByIds(memoryIds));
    }
}
