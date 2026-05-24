package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AiDocumentChunk;
import com.ruoyi.system.mapper.AiDocumentChunkMapper;
import com.ruoyi.system.service.IAiDocumentParserService;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档解析Service实现
 *
 * @author ruoyi
 * @date 2026-05-23
 */
@Service
public class AiDocumentParserServiceImpl implements IAiDocumentParserService {

    /** 分块大小(字符数) */
    private static final int CHUNK_SIZE = 500;

    /** 分块重叠(字符数) */
    private static final int CHUNK_OVERLAP = 50;

    @Autowired
    private AiDocumentChunkMapper aiDocumentChunkMapper;

    @Override
    public String extractText(String filePath, String fileType) throws Exception {
        if (fileType == null) {
            throw new Exception("文件类型不能为空");
        }
        switch (fileType.toLowerCase()) {
            case "txt":
            case "md":
                return extractPlainText(filePath);
            case "pdf":
                return extractPdfText(filePath);
            case "docx":
                return extractDocxText(filePath);
            case "xlsx":
                return extractXlsxText(filePath);
            default:
                throw new Exception("不支持的文件类型: " + fileType);
        }
    }

    @Override
    public int chunkText(String text, Long kbId, Long docId) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        List<AiDocumentChunk> chunks = new ArrayList<>();
        int length = text.length();
        int index = 0;
        int start = 0;

        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            String chunkContent = text.substring(start, end);

            AiDocumentChunk chunk = new AiDocumentChunk();
            chunk.setDocId(docId);
            chunk.setKbId(kbId);
            chunk.setChunkIndex(index);
            chunk.setChunkContent(chunkContent.trim());
            chunk.setTokenCount(estimateTokens(chunkContent));

            chunks.add(chunk);
            index++;
            start += (CHUNK_SIZE - CHUNK_OVERLAP); // 重叠滑动
        }

        if (!chunks.isEmpty()) {
            aiDocumentChunkMapper.batchInsertAiDocumentChunk(chunks);
        }

        return chunks.size();
    }

    @Override
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // 粗略估算: 中文字符≈1token, 英文单词≈1.3token
        int chineseChars = 0;
        int englishWords = 0;
        boolean inWord = false;

        for (char c : text.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                chineseChars++;
                inWord = false;
            } else if (Character.isLetter(c)) {
                if (!inWord) {
                    englishWords++;
                    inWord = true;
                }
            } else {
                inWord = false;
            }
        }
        return (int)(chineseChars + englishWords * 1.3);
    }

    // ==================== 内部解析方法 ====================

    private String extractPlainText(String filePath) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String extractPdfText(String filePath) throws Exception {
        try (PDDocument document = Loader.loadPDF(Paths.get(filePath).toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    private String extractDocxText(String filePath) throws Exception {
        try (InputStream is = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractXlsxText(String filePath) throws Exception {
        try (InputStream is = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(is)) {
            StringBuilder sb = new StringBuilder();
            workbook.forEach(sheet -> {
                sb.append("【工作表:").append(sheet.getSheetName()).append("】\n");
                sheet.forEach(row -> {
                    row.forEach(cell -> {
                        switch (cell.getCellType()) {
                            case STRING:
                                sb.append(cell.getStringCellValue()).append("\t");
                                break;
                            case NUMERIC:
                                sb.append(cell.getNumericCellValue()).append("\t");
                                break;
                            case BOOLEAN:
                                sb.append(cell.getBooleanCellValue()).append("\t");
                                break;
                            default:
                                sb.append("\t");
                        }
                    });
                    sb.append("\n");
                });
                sb.append("\n");
            });
            return sb.toString();
        }
    }
}
