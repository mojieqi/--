package com.ruoyi.system.domain;

/**
 * 用户长期记忆对象 ai_user_memory
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class AiUserMemory {

    private static final long serialVersionUID = 1L;

    /** 记忆ID */
    private Long memoryId;

    /** 用户ID */
    private Long userId;

    /** 记忆键(话题/偏好/关键信息) */
    private String memoryKey;

    /** 记忆内容 */
    private String memoryValue;

    /** 重要性(1-5) */
    private Integer importance;

    /** 最后访问时间 */
    private String lastAccess;

    /** 访问次数 */
    private Integer accessCount;

    /** 创建时间 */
    private String createTime;

    public Long getMemoryId() { return memoryId; }
    public void setMemoryId(Long memoryId) { this.memoryId = memoryId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMemoryKey() { return memoryKey; }
    public void setMemoryKey(String memoryKey) { this.memoryKey = memoryKey; }
    public String getMemoryValue() { return memoryValue; }
    public void setMemoryValue(String memoryValue) { this.memoryValue = memoryValue; }
    public Integer getImportance() { return importance; }
    public void setImportance(Integer importance) { this.importance = importance; }
    public String getLastAccess() { return lastAccess; }
    public void setLastAccess(String lastAccess) { this.lastAccess = lastAccess; }
    public Integer getAccessCount() { return accessCount; }
    public void setAccessCount(Integer accessCount) { this.accessCount = accessCount; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "AiUserMemory{" +
                "memoryId=" + memoryId +
                ", userId=" + userId +
                ", memoryKey='" + memoryKey + '\'' +
                ", importance=" + importance +
                '}';
    }
}
