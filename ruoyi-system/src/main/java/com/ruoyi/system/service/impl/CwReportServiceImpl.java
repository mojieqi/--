package com.ruoyi.system.service.impl;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.CwReport;
import com.ruoyi.system.mapper.CwPostMapper;
import com.ruoyi.system.mapper.CwReportMapper;
import com.ruoyi.system.service.ICwReportService;

/**
 * 校园墙举报Service实现
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@Service
public class CwReportServiceImpl implements ICwReportService {

    private static final Logger log = LoggerFactory.getLogger(CwReportServiceImpl.class);

    /** 举报原因映射 */
    private static final Map<String, String> REASON_MAP = new LinkedHashMap<>();
    static {
        REASON_MAP.put("spam", "垃圾广告");
        REASON_MAP.put("harassment", "人身攻击");
        REASON_MAP.put("porn", "色情低俗");
        REASON_MAP.put("fake", "虚假信息");
        REASON_MAP.put("privacy", "侵犯隐私");
        REASON_MAP.put("other", "其他");
    }

    @Autowired
    private CwReportMapper reportMapper;

    @Autowired
    private CwPostMapper postMapper;

    /**
     * 获取举报原因中文名
     */
    public static String getReasonName(String code) {
        return REASON_MAP.getOrDefault(code, code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CwReport submitReport(CwReport report) {
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();

        // 校验：是否已举报过同一目标
        CwReport existing = reportMapper.selectUserReport(userId, report.getTargetType(), report.getTargetId());
        if (existing != null) {
            log.info("用户重复举报: userId={}, targetType={}, targetId={}", userId, report.getTargetType(), report.getTargetId());
            report.setReportId(-1L); // 标记重复举报
            return report;
        }

        // 校验举报原因
        if (report.getReportReason() == null || !REASON_MAP.containsKey(report.getReportReason())) {
            log.warn("无效的举报原因: {}", report.getReportReason());
            report.setReportId(-2L); // 标记无效原因
            return report;
        }

        report.setUserId(userId);
        report.setCreateBy(username);

        // 设置举报原因中文名(供前端展示)
        report.setReportReasonName(REASON_MAP.get(report.getReportReason()));

        reportMapper.insertCwReport(report);

        // 更新帖子被举报次数
        if ("0".equals(report.getTargetType())) {
            try {
                postMapper.incrementReportCount(report.getTargetId());
            } catch (Exception e) {
                log.warn("更新帖子举报数失败: postId={}", report.getTargetId(), e);
            }
        }

        log.info("举报提交成功: reportId={}, userId={}, targetType={}, targetId={}, reason={}",
                report.getReportId(), userId, report.getTargetType(), report.getTargetId(), report.getReportReason());

        return report;
    }

    @Override
    public List<CwReport> selectMyReportList(CwReport report) {
        Long userId = SecurityUtils.getUserId();
        if (report == null) {
            report = new CwReport();
        }
        report.setUserId(userId);
        return reportMapper.selectCwReportList(report);
    }

    @Override
    public CwReport selectCwReportById(Long reportId) {
        return null; // 暂由Controller直接调用Mapper
    }
}
