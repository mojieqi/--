package com.ruoyi.system.service;

/**
 * 文档解析Service接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface IAiDocumentParserService {

    /**
     * 从文件路径解析文本内容
     *
     * @param filePath 文件路径
     * @param fileType 文件类型(txt/pdf/docx/xlsx/md)
     * @return 提取的文本内容
     */
    String extractText(String filePath, String fileType) throws Exception;

    /**
     * 将文本分块
     *
     * @param text 原文
     * @param kbId 知识库ID
     * @param docId 文档ID
     * @return 分块数量
     */
    int chunkText(String text, Long kbId, Long docId);

    /**
     * 估算Token数量(粗略: 中文1字≈1token, 英文1词≈1.3token)
     */
    int estimateTokens(String text);
}
