package com.ruoyi.system.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.CwReport;
import com.ruoyi.system.service.ICwReportService;
import com.ruoyi.system.service.impl.CwReportServiceImpl;

/**
 * 校园墙举报Controller
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@RestController
@RequestMapping("/campus/report")
public class CwReportController extends BaseController {

    @Autowired
    private ICwReportService reportService;

    /**
     * 提交举报
     */
    @Log(title = "校园墙举报", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CwReport report) {
        try {
            if (report.getTargetType() == null || report.getTargetType().isEmpty()) {
                return error("举报目标类型不能为空");
            }
            if (report.getTargetId() == null || report.getTargetId() <= 0) {
                return error("举报目标ID无效");
            }
            if (report.getReportReason() == null || report.getReportReason().isEmpty()) {
                return error("请选择举报原因");
            }

            CwReport result = reportService.submitReport(report);

            if (result.getReportId() != null && result.getReportId() == -1) {
                return error("您已举报过该内容，请勿重复举报");
            }
            if (result.getReportId() != null && result.getReportId() == -2) {
                return error("无效的举报原因");
            }

            return success(result).put("msg", "举报提交成功，我们将尽快处理");
        } catch (Exception e) {
            logger.error("提交举报失败", e);
            return error("举报失败: " + e.getMessage());
        }
    }

    /**
     * 我的举报记录
     */
    @GetMapping("/my")
    public TableDataInfo myReports(CwReport report) {
        startPage();
        List<CwReport> list = reportService.selectMyReportList(report);
        // 填充举报原因中文名
        for (CwReport r : list) {
            if (r.getReportReasonName() == null) {
                r.setReportReasonName(CwReportServiceImpl.getReasonName(r.getReportReason()));
            }
        }
        return getDataTable(list);
    }
}
