<template>
  <div class="tool-result-card" v-if="parsedCalls && parsedCalls.length">
    <div class="tool-header">
      <i class="el-icon-set-up"></i>
      <span>工具调用 ({{ parsedCalls.length }})</span>
    </div>
    <div v-for="(item, idx) in parsedCalls" :key="idx" class="tool-item">
      <div class="tool-name">
        <i class="el-icon-link"></i>
        {{ getToolName(item) }}
      </div>
      <div v-if="getToolArgs(item)" class="tool-args">
        <pre>{{ formatArgs(getToolArgs(item)) }}</pre>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ToolResultCard',
  props: {
    toolCalls: { type: [Object, Array, String], default: null }
  },
  computed: {
    parsedCalls() {
      if (!this.toolCalls) return null
      try {
        let data = this.toolCalls
        if (typeof data === 'string') {
          data = JSON.parse(data)
        }
        return Array.isArray(data) ? data : [data]
      } catch {
        return null
      }
    }
  },
  methods: {
    /** 兼容 function.name / name 两种嵌套格式 */
    getToolName(item) {
      return (item.function && item.function.name) || item.name || 'unknown'
    },
    /** 兼容 function.arguments / arguments 两种嵌套格式 */
    getToolArgs(item) {
      return (item.function && item.function.arguments) || item.arguments || ''
    },
    formatArgs(args) {
      if (!args) return ''
      try {
        const obj = typeof args === 'string' ? JSON.parse(args) : args
        return JSON.stringify(obj, null, 2)
      } catch {
        return String(args)
      }
    }
  }
}
</script>

<style scoped>
.tool-result-card {
  background: #f9fafc;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 10px 12px;
  margin-top: 8px;
  font-size: 13px;
}
.tool-header {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-weight: 600;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid #ebeef5;
}
.tool-item {
  background: #fff;
  border-radius: 6px;
  padding: 8px 10px;
  margin-bottom: 6px;
  border: 1px solid #ebeef5;
}
.tool-item:last-child { margin-bottom: 0; }
.tool-name {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #409eff;
  font-weight: 600;
  margin-bottom: 4px;
}
.tool-args pre {
  margin: 4px 0 0;
  padding: 6px 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 11px;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 150px;
  overflow-y: auto;
}
</style>
