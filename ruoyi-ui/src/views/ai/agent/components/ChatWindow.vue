<template>
  <div class="chat-window">
    <!-- 消息列表 -->
    <div class="message-list" ref="messageList" @scroll="handleScroll">
      <div v-if="messages.length === 0 && !streaming" class="empty-hint">
        <i class="el-icon-chat-dot-round"></i>
        <p>开始与 AI Agent 对话</p>
        <p class="sub-hint">选择左侧会话或新建对话</p>
      </div>

      <MessageBubble
        v-for="msg in displayMessages"
        :key="msg.messageId || msg._tempId"
        :role="msg.role"
        :content="msg.content"
        :createTime="msg.createTime"
        :toolCalls="msg.toolCalls"
      />

      <!-- 流式生成中的临时气泡 -->
      <div v-if="streaming" class="message-bubble assistant">
        <div class="message-avatar"><i class="el-icon-cpu"></i></div>
        <div class="message-body">
          <div class="message-role">AI助手</div>
          <!-- Phase 4.6: 工具调用中提示 -->
          <div v-if="streamToolCalling" class="stream-tool-calling">
            <i class="el-icon-loading"></i>
            <span>{{ streamToolCallingMsg }}</span>
          </div>
          <!-- Phase 4.6: 工具执行结果信息卡片 -->
          <div v-for="(tr, idx) in streamToolResults" :key="'tr-'+idx"
               :class="['stream-tool-result', tr.success ? 'success' : 'error']">
            <div class="tool-result-header">
              <i :class="tr.success ? 'el-icon-success' : 'el-icon-error'"></i>
              <span class="tool-code">{{ tr.code }}</span>
              <span class="tool-status-tag">{{ tr.success ? '成功' : '失败' }}</span>
            </div>
            <div v-if="tr.arguments" class="tool-args-inline">
              <pre>{{ formatToolArgs(tr.arguments) }}</pre>
            </div>
            <div class="tool-result-preview">{{ truncateText(tr.result, 300) }}</div>
          </div>
          <!-- 流式文本 -->
          <div v-if="streamContent" class="message-content markdown-body" v-html="renderedStreaming"></div>
          <span class="typing-dot"></span>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="2"
        placeholder="输入消息... (Enter发送, Shift+Enter换行)"
        :disabled="streaming"
        @keydown.native.enter.exact="handleSend"
      ></el-input>
      <div class="input-actions">
        <span class="input-hint" v-if="streaming">AI正在回复中...</span>
        <el-button
          v-if="streaming"
          type="danger"
          size="small"
          icon="el-icon-close"
          @click="$emit('stop')"
        >停止</el-button>
        <el-button
          v-else
          type="primary"
          size="small"
          icon="el-icon-s-promotion"
          :disabled="!inputText.trim()"
          @click="handleSend"
        >发送</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import MessageBubble from './MessageBubble.vue'

export default {
  name: 'ChatWindow',
  components: { MessageBubble },
  props: {
    messages: { type: Array, default: () => [] },
    streaming: { type: Boolean, default: false },
    streamContent: { type: String, default: '' },
    /** Phase 4.6: 流式工具执行结果列表 */
    streamToolResults: { type: Array, default: () => [] },
    /** Phase 4.6: 是否正在调用工具 */
    streamToolCalling: { type: Boolean, default: false },
    /** Phase 4.6: 工具调用提示文本 */
    streamToolCallingMsg: { type: String, default: '' }
  },
  data() {
    return {
      inputText: ''
    }
  },
  computed: {
    displayMessages() {
      return this.messages
    },
    renderedStreaming() {
      return this.simpleMarkdown(this.streamContent)
    }
  },
  watch: {
    messages: {
      handler() { this.$nextTick(() => this.scrollToBottom()) },
      deep: true
    },
    streamContent() {
      this.$nextTick(() => this.scrollToBottom())
    }
  },
  methods: {
    handleSend() {
      if (this.streaming) return
      const text = this.inputText.trim()
      if (!text) return
      this.inputText = ''
      this.$emit('send', text)
    },
    scrollToBottom() {
      const el = this.$refs.messageList
      if (el) {
        el.scrollTop = el.scrollHeight
      }
    },
    handleScroll() {},
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
    /** Phase 4.6: 格式化工具调用参数 (JSON美化) */
    formatToolArgs(args) {
      if (!args) return ''
      try {
        const obj = typeof args === 'string' ? JSON.parse(args) : args
        return JSON.stringify(obj, null, 2)
      } catch {
        return String(args)
      }
    },
    /** Phase 4.6: 截断工具结果超长文本 */
    truncateText(text, maxLen) {
      if (!text) return ''
      return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
    }
  }
}
</script>

<style scoped>
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-width: 0;
}
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
}
.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #ccc;
}
.empty-hint i { font-size: 48px; margin-bottom: 12px; }
.empty-hint p { font-size: 15px; margin: 4px 0; }
.empty-hint .sub-hint { font-size: 12px; color: #ddd; }
.input-area {
  padding: 12px 16px;
  border-top: 1px solid #e8e8e8;
  background: #fff;
}
.input-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 8px;
  gap: 8px;
}
.input-hint { font-size: 12px; color: #e6a23c; }
.typing-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409eff;
  animation: blink 1s infinite;
  margin-left: 4px;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
/* ===== Phase 4.6: 流式工具状态样式 ===== */
.stream-tool-calling {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  margin-bottom: 8px;
  background: #f0f7ff;
  border: 1px solid #b3d8ff;
  border-radius: 6px;
  font-size: 13px;
  color: #409eff;
}
.stream-tool-calling i { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.stream-tool-result {
  padding: 10px 12px;
  margin-bottom: 8px;
  border-radius: 6px;
  font-size: 13px;
}
.stream-tool-result.success {
  background: #f0fff4;
  border: 1px solid #b7ebd0;
}
.stream-tool-result.error {
  background: #fff5f5;
  border: 1px solid #fbc4c4;
}
.tool-result-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  font-weight: 600;
}
.stream-tool-result.success .tool-result-header { color: #67c23a; }
.stream-tool-result.error .tool-result-header { color: #f56c6c; }
.tool-status-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 3px;
  font-weight: 500;
}
.stream-tool-result.success .tool-status-tag {
  background: #e1f3e4;
  color: #67c23a;
}
.stream-tool-result.error .tool-status-tag {
  background: #fde2e2;
  color: #f56c6c;
}
.tool-args-inline pre {
  margin: 4px 0;
  padding: 6px 8px;
  background: rgba(0,0,0,0.04);
  border-radius: 4px;
  font-size: 11px;
  color: #666;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 120px;
  overflow-y: auto;
}
.tool-result-preview {
  color: #555;
  line-height: 1.5;
  max-height: 100px;
  overflow-y: auto;
  word-break: break-word;
}
</style>
