package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CwReport;

/**
 * 校园墙举报Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface CwReportMapper {

    /**
     * 查询举报列表
     */
    public List<CwReport> selectCwReportList(CwReport report);

    /**
     * 查询用户自己对某目标的举报记录
     */
    public CwReport selectUserReport(Long userId, String targetType, Long targetId);

    /**
     * 新增举报
     */
    public int insertCwReport(CwReport report);

    /**
     * 更新举报处理状态
     */
    public int updateHandleStatus(CwReport report);

    /**
     * 统计某目标被举报次数
     */
    public int countReportsByTarget(String targetType, Long targetId);
}
