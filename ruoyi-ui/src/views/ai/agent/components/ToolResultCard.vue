<template>
  <div class="tool-result-card" v-if="parsedCalls">
    <div class="tool-header">
      <i class="el-icon-setting"></i> 工具调用
    </div>
    <div v-for="(item, idx) in parsedCalls" :key="idx" class="tool-item">
      <div class="tool-name">{{ item.name || 'unknown' }}</div>
      <div v-if="item.arguments" class="tool-args">
        <pre>{{ formatJson(item.arguments) }}</pre>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ToolResultCard',
  props: {
    toolCalls: { type: Object, default: null }
  },
  computed: {
    parsedCalls() {
      if (!this.toolCalls) return null
      try {
        if (typeof this.toolCalls === 'string') {
          return JSON.parse(this.toolCalls)
        }
        return Array.isArray(this.toolCalls) ? this.toolCalls : [this.toolCalls]
      } catch {
        return null
      }
    }
  },
  methods: {
    formatJson(obj) {
      if (typeof obj === 'string') return obj
      return JSON.stringify(obj, null, 2)
    }
  }
}
</script>

<style scoped>
.tool-result-card {
  background: #f0f7ff;
  border: 1px solid #d0e8ff;
  border-radius: 6px;
  padding: 10px 12px;
  margin-top: 8px;
  font-size: 12px;
}
.tool-header {
  color: #409eff;
  font-weight: bold;
  margin-bottom: 8px;
}
.tool-item {
  background: #fff;
  border-radius: 4px;
  padding: 8px;
  margin-bottom: 6px;
}
.tool-name {
  color: #67c23a;
  font-weight: bold;
  margin-bottom: 4px;
}
.tool-args pre {
  margin: 0;
  font-size: 11px;
  color: #666;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
