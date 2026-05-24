package com.ruoyi.system.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.AiDocumentChunk;
import com.ruoyi.system.domain.AiKnowledgeBase;
import com.ruoyi.system.domain.AiKnowledgeDocument;
import com.ruoyi.system.mapper.AiDocumentChunkMapper;
import com.ruoyi.system.service.IAiKnowledgeBaseService;
import com.ruoyi.system.service.IAiKnowledgeDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI知识库Controller
 *
 * @author ruoyi
 * @date 2026-05-23
 */
@RestController
@RequestMapping("/ai/knowledge")
public class AiKnowledgeController extends BaseController {

    @Autowired
    private IAiKnowledgeBaseService aiKnowledgeBaseService;

    @Autowired
    private IAiKnowledgeDocumentService aiKnowledgeDocumentService;

    @Autowired
    private AiDocumentChunkMapper aiDocumentChunkMapper;

    // ==================== 知识库 CRUD ====================

    @PreAuthorize("@ss.hasPermi('ai:knowledge:list')")
    @GetMapping("/base/list")
    public TableDataInfo baseList(AiKnowledgeBase knowledgeBase) {
        startPage();
        List<AiKnowledgeBase> list = aiKnowledgeBaseService.selectAiKnowledgeBaseList(knowledgeBase);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:query')")
    @GetMapping("/base/{kbId}")
    public AjaxResult getInfo(@PathVariable Long kbId) {
        return success(aiKnowledgeBaseService.selectAiKnowledgeBaseById(kbId));
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:add')")
    @Log(title = "AI知识库", businessType = BusinessType.INSERT)
    @PostMapping("/base")
    public AjaxResult add(@RequestBody AiKnowledgeBase knowledgeBase) {
        knowledgeBase.setCreateBy(getUsername());
        return toAjax(aiKnowledgeBaseService.insertAiKnowledgeBase(knowledgeBase));
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:edit')")
    @Log(title = "AI知识库", businessType = BusinessType.UPDATE)
    @PutMapping("/base")
    public AjaxResult edit(@RequestBody AiKnowledgeBase knowledgeBase) {
        knowledgeBase.setUpdateBy(getUsername());
        return toAjax(aiKnowledgeBaseService.updateAiKnowledgeBase(knowledgeBase));
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:remove')")
    @Log(title = "AI知识库", businessType = BusinessType.DELETE)
    @DeleteMapping("/base/{kbIds}")
    public AjaxResult remove(@PathVariable Long[] kbIds) {
        return toAjax(aiKnowledgeBaseService.deleteAiKnowledgeBaseByIds(kbIds));
    }

    // ==================== 文档管理 ====================

    @PreAuthorize("@ss.hasPermi('ai:knowledge:doc:list')")
    @GetMapping("/doc/list")
    public TableDataInfo docList(AiKnowledgeDocument document) {
        startPage();
        List<AiKnowledgeDocument> list = aiKnowledgeDocumentService.selectAiKnowledgeDocumentList(document);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:doc:upload')")
    @Log(title = "AI知识库文档", businessType = BusinessType.INSERT)
    @PostMapping("/doc/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file,
                             @RequestParam("kbId") Long kbId) {
        try {
            AiKnowledgeDocument doc = aiKnowledgeDocumentService.uploadDocument(file, kbId);
            Map<String, Object> result = new HashMap<>();
            result.put("docId", doc.getDocId());
            result.put("docName", doc.getDocName());
            result.put("status", doc.getStatus());
            result.put("chunkCount", doc.getChunkCount());
            if ("2".equals(doc.getStatus())) {
                result.put("errorMsg", doc.getErrorMsg());
            }
            return success(result);
        } catch (Exception e) {
            return error("上传失败: " + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:doc:remove')")
    @Log(title = "AI知识库文档", businessType = BusinessType.DELETE)
    @DeleteMapping("/doc/{docIds}")
    public AjaxResult removeDoc(@PathVariable Long[] docIds) {
        return toAjax(aiKnowledgeDocumentService.deleteAiKnowledgeDocumentByIds(docIds));
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:doc:reparse')")
    @Log(title = "AI知识库文档", businessType = BusinessType.UPDATE)
    @PostMapping("/doc/reparse/{docId}")
    public AjaxResult reparse(@PathVariable Long docId) {
        try {
            AiKnowledgeDocument doc = aiKnowledgeDocumentService.reparseDocument(docId);
            return success(doc);
        } catch (Exception e) {
            return error("重新解析失败: " + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:doc:query')")
    @GetMapping("/doc/{docId}")
    public AjaxResult getDocInfo(@PathVariable Long docId) {
        AiKnowledgeDocument doc = aiKnowledgeDocumentService.selectAiKnowledgeDocumentById(docId);
        if (doc != null) {
            // 同时返回分块列表
            List<AiDocumentChunk> chunks = aiDocumentChunkMapper.selectAiDocumentChunkByDocId(docId);
            Map<String, Object> result = new HashMap<>();
            result.put("document", doc);
            result.put("chunks", chunks);
            return success(result);
        }
        return success(doc);
    }

    // ==================== 检索 ====================

    @PreAuthorize("@ss.hasPermi('ai:knowledge:search')")
    @PostMapping("/search")
    public AjaxResult search(@RequestBody Map<String, Object> params) {
        Long kbId = params.get("kbId") != null ? Long.valueOf(params.get("kbId").toString()) : null;
        String keyword = params.get("keyword") != null ? params.get("keyword").toString() : "";

        AiDocumentChunk searchParam = new AiDocumentChunk();
        searchParam.setKbId(kbId);
        searchParam.setChunkContent(keyword);

        List<AiDocumentChunk> chunks = aiDocumentChunkMapper.searchChunks(searchParam);
        return success(chunks);
    }
}
