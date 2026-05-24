package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 校园墙举报对象 cw_report
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class CwReport extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 举报ID */
    private Long reportId;

    /** 举报人用户ID */
    private Long userId;

    /** 目标类型(0帖子 1评论) */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 举报原因代码 */
    private String reportReason;

    /** 举报详细描述 */
    private String reportDesc;

    /** 处理状态(0待处理 1已忽略 2已警告 3已删除 4已封禁) */
    private String handleStatus;

    /** 处理结果描述 */
    private String handleResult;

    /** 处理人 */
    private String handleBy;

    /** 处理时间 */
    private java.util.Date handleTime;

    // ===== 查询扩展字段 =====

    /** 举报人昵称 */
    private String nickName;

    /** 被举报内容摘要 */
    private String targetContent;

    /** 举报原因中文名 */
    private String reportReasonName;

    // ===== getter/setter =====

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getReportReason() { return reportReason; }
    public void setReportReason(String reportReason) { this.reportReason = reportReason; }

    public String getReportDesc() { return reportDesc; }
    public void setReportDesc(String reportDesc) { this.reportDesc = reportDesc; }

    public String getHandleStatus() { return handleStatus; }
    public void setHandleStatus(String handleStatus) { this.handleStatus = handleStatus; }

    public String getHandleResult() { return handleResult; }
    public void setHandleResult(String handleResult) { this.handleResult = handleResult; }

    public String getHandleBy() { return handleBy; }
    public void setHandleBy(String handleBy) { this.handleBy = handleBy; }

    public java.util.Date getHandleTime() { return handleTime; }
    public void setHandleTime(java.util.Date handleTime) { this.handleTime = handleTime; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getTargetContent() { return targetContent; }
    public void setTargetContent(String targetContent) { this.targetContent = targetContent; }

    public String getReportReasonName() { return reportReasonName; }
    public void setReportReasonName(String reportReasonName) { this.reportReasonName = reportReasonName; }

    @Override
    public String toString() {
        return "CwReport{" +
                "reportId=" + reportId +
                ", userId=" + userId +
                ", targetType='" + targetType + '\'' +
                ", targetId=" + targetId +
                ", reportReason='" + reportReason + '\'' +
                ", handleStatus='" + handleStatus + '\'' +
                '}';
    }
}
