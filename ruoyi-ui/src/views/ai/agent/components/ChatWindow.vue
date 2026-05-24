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
          <div class="message-content markdown-body" v-html="renderedStreaming"></div>
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
    streamContent: { type: String, default: '' }
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
</style>
