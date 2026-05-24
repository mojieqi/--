<template>
  <div :class="['message-bubble', role]">
    <!-- 用户/助手头像 -->
    <div v-if="role !== 'tool'" class="message-avatar">
      <i :class="role === 'user' ? 'el-icon-user' : 'el-icon-cpu'"></i>
    </div>
    <div class="message-body">
      <div class="message-role">
        {{ role === 'user' ? '我' : role === 'tool' ? '工具' : 'AI助手' }}
      </div>
      <!-- AI消息使用Markdown渲染 -->
      <div v-if="role === 'assistant'" class="message-content markdown-body" v-html="renderedContent"></div>
      <!-- 工具消息: 带工具图标的结果文本 -->
      <div v-else-if="role === 'tool'" class="message-content tool-content">
        <i class="el-icon-set-up"></i>
        <span>{{ content }}</span>
      </div>
      <!-- 用户消息纯文本 -->
      <div v-else class="message-content">{{ content }}</div>
      <!-- 工具调用卡片 (历史消息中 assistant 的 tool_calls 数据) -->
      <ToolResultCard v-if="role === 'assistant' && toolCalls" :tool-calls="toolCalls" />
      <div class="message-time">{{ formatTime(createTime) }}</div>
    </div>
  </div>
</template>

<script>
import ToolResultCard from './ToolResultCard.vue'

export default {
  name: 'MessageBubble',
  components: { ToolResultCard },
  props: {
    role: { type: String, required: true },
    content: { type: String, default: '' },
    createTime: { type: String, default: '' },
    toolCalls: { type: Object, default: null }
  },
  computed: {
    renderedContent() {
      return this.simpleMarkdown(this.content)
    }
  },
  methods: {
    simpleMarkdown(text) {
      if (!text) return ''
      let html = text
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.+?)\*/g, '<em>$1</em>')
        .replace(/`{3}(\w*)\n?([\s\S]*?)`{3}/g, '<pre><code>$2</code></pre>')
        .replace(/`(.+?)`/g, '<code>$1</code>')
        .replace(/\n/g, '<br>')
      return html
    },
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const h = String(d.getHours()).padStart(2, '0')
      const m = String(d.getMinutes()).padStart(2, '0')
      return h + ':' + m
    }
  }
}
</script>

<style scoped>
.message-bubble {
  display: flex;
  margin-bottom: 20px;
  padding: 0 16px;
}
.message-bubble.user { flex-direction: row-reverse; }
.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin: 0 12px;
  font-size: 18px;
}
.user .message-avatar { background: #d0e8ff; color: #409eff; }
.assistant .message-avatar { background: #e8f5e9; color: #67c23a; }
/* Phase 4.6: 工具消息样式 */
.tool .message-body {
  max-width: 85%;
  margin-left: 48px;
}
.tool .message-role { color: #e6a23c; }
.tool .tool-content {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  background: #fdf6ec;
  color: #866026;
  border: 1px solid #fae3b7;
  font-size: 13px;
}
.tool .tool-content i { color: #e6a23c; margin-top: 2px; flex-shrink: 0; }
.message-body {
  max-width: 75%;
  min-width: 100px;
}
.message-role {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.user .message-role { text-align: right; }
.message-content {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
.user .message-content {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.assistant .message-content {
  background: #f5f5f5;
  color: #333;
  border-bottom-left-radius: 4px;
}
.message-content.markdown-body pre {
  background: #282c34;
  color: #abb2bf;
  padding: 12px 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 8px 0;
}
.message-content.markdown-body code {
  background: rgba(0,0,0,0.06);
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 13px;
}
.message-time {
  font-size: 11px;
  color: #ccc;
  margin-top: 4px;
}
.user .message-time { text-align: right; }
</style>
