package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CwReport;

/**
 * 校园墙举报Service接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface ICwReportService {

    /**
     * 提交举报
     */
    public CwReport submitReport(CwReport report);

    /**
     * 查询用户的举报记录
     */
    public List<CwReport> selectMyReportList(CwReport report);

    /**
     * 查询举报详情
     */
    public CwReport selectCwReportById(Long reportId);
}
