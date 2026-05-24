package com.ruoyi.system.service;

/**
 * AI智能分类Service接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface IContentClassifyService {

    /**
     * 对帖子内容进行AI智能分类
     *
     * @param title   帖子标题
     * @param content 帖子内容
     * @return 分类结果
     *   - categoryCode: String 分类代码
     *   - categoryName: String 分类名称
     *   - confidence: double 置信度(0-1)
     *   - source: String "ai"|"keyword"
     */
    public ClassificationResult classify(String title, String content);

    /**
     * 分类结果
     */
    class ClassificationResult {
        private String categoryCode;
        private String categoryName;
        private double confidence;
        private String source;

        public ClassificationResult() {}

        public ClassificationResult(String categoryCode, String categoryName, double confidence, String source) {
            this.categoryCode = categoryCode;
            this.categoryName = categoryName;
            this.confidence = confidence;
            this.source = source;
        }

        public String getCategoryCode() { return categoryCode; }
        public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}
