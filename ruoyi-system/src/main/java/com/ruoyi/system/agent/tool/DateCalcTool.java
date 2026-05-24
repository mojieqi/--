package com.ruoyi.system.agent.tool;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.system.domain.AiTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 日期计算工具 — 纯本地计算，无外部依赖
 *
 * 支持三种操作:
 * - diff:   计算两个日期的差值(天数)
 * - weekday:获取某天是星期几
 * - add_days:日期加减天数
 *
 * 参数:
 * - date1 (必填): 第一个日期，格式 yyyy-MM-dd
 * - operation (必填): 操作类型 [diff, weekday, add_days]
 * - date2 (可选): 第二个日期，diff 操作时使用
 * - days (可选): 加减天数，add_days 操作时使用
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class DateCalcTool extends AbstractTool {

    private static final Logger log = LoggerFactory.getLogger(DateCalcTool.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] WEEKDAY_NAMES = {
            "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"
    };

    public DateCalcTool(AiTool toolMeta) {
        super(toolMeta);
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        requireParams(arguments, "date1", "operation");
        String operation = getStringParam(arguments, "operation");

        try {
            LocalDate date1 = parseDate(getStringParam(arguments, "date1"));

            switch (operation.toLowerCase()) {
                case "diff":
                    return executeDiff(date1, arguments);
                case "weekday":
                    return executeWeekday(date1);
                case "add_days":
                    return executeAddDays(date1, arguments);
                default:
                    return JSON.toJSONString(Collections.singletonMap("error",
                            "不支持的操作: " + operation + "，支持的操作: diff, weekday, add_days"));
            }
        } catch (DateTimeParseException e) {
            return JSON.toJSONString(Collections.singletonMap("error",
                    "日期格式错误，请使用 yyyy-MM-dd 格式，例如 2026-05-24"));
        } catch (Exception e) {
            log.error("DateCalcTool 计算异常: {}", e.getMessage());
            return JSON.toJSONString(Collections.singletonMap("error",
                    "日期计算失败: " + e.getMessage()));
        }
    }

    /**
     * 计算两个日期的差值(天数)
     */
    private String executeDiff(LocalDate date1, Map<String, Object> arguments) {
        String date2Str = getStringParam(arguments, "date2");
        if (date2Str == null || date2Str.isEmpty()) {
            return JSON.toJSONString(Collections.singletonMap("error",
                    "diff 操作需要提供 date2 参数"));
        }
        LocalDate date2 = parseDate(date2Str);

        long daysBetween = Math.abs(ChronoUnit.DAYS.between(date1, date2));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date1", date1.format(DATE_FMT));
        result.put("date2", date2.format(DATE_FMT));
        result.put("days_between", daysBetween);

        // 附加信息
        result.put("weeks_between", String.format("%.1f 周", daysBetween / 7.0));
        if (daysBetween >= 30) {
            result.put("months_between", String.format("%.1f 月", daysBetween / 30.44));
        }
        if (daysBetween >= 365) {
            result.put("years_between", String.format("%.1f 年", daysBetween / 365.25));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("日期差值计算结果:\n");
        sb.append("- ").append(date1.format(DATE_FMT)).append(" 到 ");
        sb.append(date2.format(DATE_FMT)).append(" 间隔 ");
        sb.append(daysBetween).append(" 天");

        boolean date1Before = date1.isBefore(date2);
        if (!date1Before) {
            sb.append(" (").append(date2.format(DATE_FMT)).append("在前)");
        }

        if (daysBetween >= 7) {
            sb.append("，约 ").append(String.format("%.1f", daysBetween / 7.0)).append(" 周");
        }
        if (daysBetween >= 30) {
            sb.append("，约 ").append(String.format("%.1f", daysBetween / 30.44)).append(" 月");
        }

        return sb.toString();
    }

    /**
     * 获取某天是星期几
     */
    private String executeWeekday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String weekdayName = WEEKDAY_NAMES[dayOfWeek.getValue() - 1];

        return date.format(DATE_FMT) + " 是 " + weekdayName;
    }

    /**
     * 日期加减天数
     */
    private String executeAddDays(LocalDate date, Map<String, Object> arguments) {
        Integer days = getIntParam(arguments, "days");
        if (days == null) {
            return JSON.toJSONString(Collections.singletonMap("error",
                    "add_days 操作需要提供 days 参数"));
        }

        LocalDate newDate = date.plusDays(days);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("original_date", date.format(DATE_FMT));
        result.put("days_added", days);
        result.put("result_date", newDate.format(DATE_FMT));
        result.put("result_weekday", WEEKDAY_NAMES[newDate.getDayOfWeek().getValue() - 1]);

        StringBuilder sb = new StringBuilder();
        sb.append(date.format(DATE_FMT));
        if (days >= 0) {
            sb.append(" + ").append(days).append(" 天 = ");
        } else {
            sb.append(" - ").append(Math.abs(days)).append(" 天 = ");
        }
        sb.append(newDate.format(DATE_FMT)).append(" (");
        sb.append(WEEKDAY_NAMES[newDate.getDayOfWeek().getValue() - 1]).append(")");

        return sb.toString();
    }

    /**
     * 解析日期字符串
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new DateTimeParseException("日期为空", dateStr, 0);
        }

        // 支持多种日期格式
        dateStr = dateStr.trim();

        // yyyy-MM-dd
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(dateStr, DATE_FMT);
        }

        // yyyy/MM/dd
        if (dateStr.matches("\\d{4}/\\d{2}/\\d{2}")) {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        }

        // yyyyMMdd
        if (dateStr.matches("\\d{8}")) {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        // today / tomorrow / yesterday
        switch (dateStr.toLowerCase()) {
            case "today":
            case "今天":
                return LocalDate.now();
            case "tomorrow":
            case "明天":
                return LocalDate.now().plusDays(1);
            case "yesterday":
            case "昨天":
                return LocalDate.now().minusDays(1);
            default:
                return LocalDate.parse(dateStr, DATE_FMT);
        }
    }
}
