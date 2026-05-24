package com.ruoyi.system.service.impl;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.system.domain.AiKnowledgeDocument;
import com.ruoyi.system.mapper.AiDocumentChunkMapper;
import com.ruoyi.system.mapper.AiKnowledgeBaseMapper;
import com.ruoyi.system.mapper.AiKnowledgeDocumentMapper;
import com.ruoyi.system.service.IAiDocumentParserService;
import com.ruoyi.system.service.IAiKnowledgeDocumentService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * AI知识库文档Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-23
 */
@Service
public class AiKnowledgeDocumentServiceImpl implements IAiKnowledgeDocumentService {

    @Autowired
    private AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;

    @Autowired
    private AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    @Autowired
    private AiDocumentChunkMapper aiDocumentChunkMapper;

    @Autowired
    private IAiDocumentParserService documentParserService;

    /** 支持的文件类型 */
    private static final String[] ALLOWED_EXTENSIONS = {"txt", "md", "pdf", "docx", "xlsx"};

    @Override
    public AiKnowledgeDocument selectAiKnowledgeDocumentById(Long docId) {
        return aiKnowledgeDocumentMapper.selectAiKnowledgeDocumentById(docId);
    }

    @Override
    public List<AiKnowledgeDocument> selectAiKnowledgeDocumentList(AiKnowledgeDocument document) {
        return aiKnowledgeDocumentMapper.selectAiKnowledgeDocumentList(document);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiKnowledgeDocument uploadDocument(MultipartFile file, Long kbId) throws Exception {
        // 1. 验证文件类型
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new Exception("文件名不能为空");
        }
        String extension = FilenameUtils.getExtension(originalName).toLowerCase();
        boolean allowed = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (ext.equals(extension)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new Exception("不支持的文件类型: " + extension + "，仅支持 " + String.join("/", ALLOWED_EXTENSIONS));
        }

        // 2. 上传文件到 knowledge 子目录
        String knowledgePath = RuoYiConfig.getProfile() + "/knowledge/" + kbId;
        File dir = new File(knowledgePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = FileUploadUtils.upload(knowledgePath, file);

        // 3. 创建文档记录(状态: 处理中)
        AiKnowledgeDocument document = new AiKnowledgeDocument();
        document.setKbId(kbId);
        document.setDocName(originalName);
        document.setDocType(extension);
        document.setFilePath(filePath);
        document.setFileSize(file.getSize());
        document.setStatus("0"); // 处理中
        document.setCreateTime(new Date());
        aiKnowledgeDocumentMapper.insertAiKnowledgeDocument(document);

        // 4. 解析文本(异步可优化，当前同步)
        try {
            String text = documentParserService.extractText(filePath, extension);
            int chunkCount = documentParserService.chunkText(text, kbId, document.getDocId());
            document.setContentText(text);
            document.setChunkCount(chunkCount);
            document.setStatus("1"); // 完成
        } catch (Exception e) {
            document.setStatus("2"); // 失败
            document.setErrorMsg(e.getMessage());
        }
        aiKnowledgeDocumentMapper.updateAiKnowledgeDocument(document);

        // 5. 更新知识库统计
        aiKnowledgeBaseMapper.updateDocCount(kbId);

        return document;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiKnowledgeDocument reparseDocument(Long docId) throws Exception {
        AiKnowledgeDocument document = aiKnowledgeDocumentMapper.selectAiKnowledgeDocumentById(docId);
        if (document == null) {
            throw new Exception("文档不存在");
        }

        // 删除旧分块
        aiDocumentChunkMapper.deleteAiDocumentChunkByDocId(docId);

        // 重新解析
        document.setStatus("0");
        aiKnowledgeDocumentMapper.updateAiKnowledgeDocument(document);

        try {
            String text = documentParserService.extractText(document.getFilePath(), document.getDocType());
            int chunkCount = documentParserService.chunkText(text, document.getKbId(), docId);
            document.setContentText(text);
            document.setChunkCount(chunkCount);
            document.setStatus("1");
        } catch (Exception e) {
            document.setStatus("2");
            document.setErrorMsg(e.getMessage());
        }
        aiKnowledgeDocumentMapper.updateAiKnowledgeDocument(document);
        aiKnowledgeBaseMapper.updateDocCount(document.getKbId());

        return document;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAiKnowledgeDocumentByIds(Long[] docIds) {
        // 先收集所有涉及的kbId，用于后续更新统计
        java.util.Set<Long> kbIdSet = new java.util.HashSet<>();
        for (Long docId : docIds) {
            AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectAiKnowledgeDocumentById(docId);
            if (doc != null) {
                kbIdSet.add(doc.getKbId());
            }
            aiDocumentChunkMapper.deleteAiDocumentChunkByDocId(docId);
        }
        int result = aiKnowledgeDocumentMapper.deleteAiKnowledgeDocumentByIds(docIds);

        // 更新所有涉及的知识库统计
        for (Long kbId : kbIdSet) {
            aiKnowledgeBaseMapper.updateDocCount(kbId);
        }
        return result;
    }
}
