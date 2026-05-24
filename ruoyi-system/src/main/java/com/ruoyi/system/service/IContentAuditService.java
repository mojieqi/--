package com.ruoyi.system.service;

import java.util.Map;

/**
 * AI内容审核Service接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface IContentAuditService {

    /**
     * 审核文本内容
     *
     * @param content 待审核文本
     * @param scene   审核场景(post/comment/profile)
     * @return 审核结果
     *   - passed: boolean 是否通过
     *   - score: double 信任度分数(0-100)，通过则为100，否则按违规程度扣分
     *   - reason: String 审核原因(驳回时填写)
     *   - violations: Map<String, List<String>> 违规词分类
     */
    public Map<String, Object> audit(String content, String scene);
}
