<template>
  <div class="agent-container">
    <!-- 左侧会话列表 -->
    <ConversationSidebar
      :conversations="conversationList"
      :activeId="activeConversationId"
      :loading="convLoading"
      @select="handleSelectConversation"
      @delete="handleDeleteConversation"
      @new-conversation="handleNewConversation"
    />

    <!-- 中间对话区 -->
    <ChatWindow
      ref="chatWindow"
      :messages="messages"
      :streaming="streaming"
      :streamContent="streamContent"
      :streamToolResults="streamToolResults"
      :streamToolCalling="streamToolCalling"
      :streamToolCallingMsg="streamToolCallingMsg"
      @send="handleSendMessage"
      @stop="handleStop"
    />

    <!-- 右侧设置面板 -->
    <AgentSettings
      :conversation="activeConversation"
      :configs="llmConfigs"
      :prompts="systemPrompts"
      :knowledgeBases="knowledgeBases"
      @update-settings="handleUpdateSettings"
      @clear-context="handleClearContext"
    />
  </div>
</template>

<script>
import ConversationSidebar from './components/ConversationSidebar.vue'
import ChatWindow from './components/ChatWindow.vue'
import AgentSettings from './components/AgentSettings.vue'
import {
  listConversations, createConversation, deleteConversation,
  getMessages, chatStream, stopChat, clearConversation
} from '@/api/ai/agent'
import { listConfig } from '@/api/ai/llmConfig'
import { listSystemPrompts } from '@/api/ai/promptTemplate'
import { listKnowledgeBase } from '@/api/ai/knowledge'

export default {
  name: 'AiAgent',
  components: { ConversationSidebar, ChatWindow, AgentSettings },
  data() {
    return {
      // 会话列表
      conversationList: [],
      convLoading: false,
      activeConversationId: null,
      activeConversation: {},

      // 消息
      messages: [],

      // 流式状态
      streaming: false,
      streamContent: '',
      streamToolResults: [],      // Phase 4.6: 流式工具执行结果列表
      streamToolCalling: false,   // Phase 4.6: 是否正在调用工具
      streamToolCallingMsg: '',   // Phase 4.6: 工具调用提示文本
      currentReader: null,
      abortController: null,

      // 选项数据
      llmConfigs: [],
      systemPrompts: [],
      knowledgeBases: []
    }
  },
  created() {
    this.loadOptions()
    this.loadConversations()
  },
  methods: {
    // ==================== 初始化加载 ====================
    async loadOptions() {
      try {
        const [configRes, promptRes, kbRes] = await Promise.all([
          listConfig({ status: '0' }),
          listSystemPrompts(),
          listKnowledgeBase()
        ])
        this.llmConfigs = configRes.rows || []
        this.systemPrompts = promptRes.data || []
        this.knowledgeBases = kbRes.rows || []
      } catch (e) {
        console.error('加载选项失败', e)
      }
    },

    async loadConversations() {
      this.convLoading = true
      try {
        const res = await listConversations({})
        this.conversationList = res.rows || []
        if (this.conversationList.length > 0 && !this.activeConversationId) {
          this.handleSelectConversation(this.conversationList[0])
        }
      } finally {
        this.convLoading = false
      }
    },

    // ==================== 会话操作 ====================
    async handleNewConversation() {
      // 默认使用第一个LLM配置
      const defaultConfig = this.llmConfigs.length > 0 ? this.llmConfigs[0].configId : null
      if (!defaultConfig) {
        this.$message.warning('没有可用的LLM配置，请先在【LLM大模型配置】中添加')
        return
      }

      try {
        const res = await createConversation({
          llmConfigId: defaultConfig,
          title: '新对话',
          memoryMode: '1',
          maxTokens: 4096
        })
        this.$message.success('新建会话成功')
        await this.loadConversations()
        // 选中新会话
        const newConv = this.conversationList.find(c => c.conversationId === res.data?.conversationId)
        if (newConv) {
          this.handleSelectConversation(newConv)
        }
      } catch (e) {
        this.$message.error('新建会话失败')
      }
    },

    async handleSelectConversation(conv) {
      this.activeConversationId = conv.conversationId
      this.activeConversation = conv
      this.messages = []
      try {
        const res = await getMessages(conv.conversationId)
        this.messages = res.data || []
        this.$nextTick(() => {
          this.$refs.chatWindow?.scrollToBottom()
        })
      } catch (e) {
        console.error('加载消息失败', e)
      }
    },

    async handleDeleteConversation(id) {
      try {
        await deleteConversation(id)
        this.$message.success('删除成功')
        if (this.activeConversationId === id) {
          this.activeConversationId = null
          this.activeConversation = {}
          this.messages = []
        }
        await this.loadConversations()
      } catch (e) {
        this.$message.error('删除失败')
      }
    },

    async handleClearContext() {
      if (!this.activeConversationId) return
      try {
        await this.$confirm('确定清空当前会话的所有消息吗？', '提示', { type: 'warning' })
        await clearConversation(this.activeConversationId)
        this.messages = []
        this.$message.success('上下文已清空')
      } catch (e) {
        // 取消
      }
    },

    async handleUpdateSettings(settings) {
      if (!this.activeConversationId) return
      // 仅在前端更新 activeConversation，下次对话时后端会读取最新设置
      Object.assign(this.activeConversation, settings)
    },

    // ==================== 消息发送 ====================
    async handleSendMessage(text) {
      if (!this.activeConversationId) {
        this.$message.warning('请先选择或创建一个会话')
        return
      }
      if (this.streaming) return

      // 添加用户消息到界面
      this.messages.push({
        _tempId: Date.now(),
        role: 'user',
        content: text,
        createTime: new Date().toISOString()
      })

      // 开始流式
      this.streaming = true
      this.streamContent = ''

      try {
        const response = await chatStream(this.activeConversationId, text)

        if (!response.ok) {
          throw new Error('请求失败: ' + response.status)
        }

        const reader = response.body.getReader()
        this.currentReader = reader
        const decoder = new TextDecoder()
        let buffer = ''
        let streamDone = false

        // SSE 协议变量: 追踪当前事件类型和数据
        let currentEvent = 'message'
        let eventData = ''

        while (!streamDone) {
          const { done, value } = await reader.read()
          if (done) break

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            // 1) event: 行 → 记录事件类型
            if (line.startsWith('event:')) {
              currentEvent = line.substring(6).trim() || 'message'
            }
            // 2) data: 行 → 累积数据 (检测 [DONE] 结束标记)
            else if (line.startsWith('data:')) {
              const data = line.substring(5).trim()
              if (data === '[DONE]') {
                streamDone = true
                break
              }
              if (eventData) eventData += '\n'
              eventData += data
            }
            // 3) 空行 → SSE 事件边界,处理当前累积事件
            else if (line.trim() === '') {
              if (eventData) {
                this.processStreamEvent(currentEvent, eventData)
              }
              currentEvent = 'message'
              eventData = ''
            }
          }
        }

        // 流结束后添加 AI 消息 (非工具调用场景)
        if (this.streamContent) {
          this.messages.push({
            _tempId: Date.now(),
            role: 'assistant',
            content: this.streamContent,
            createTime: new Date().toISOString()
          })
        }
      } catch (e) {
        if (e.name !== 'AbortError') {
          this.$message.error('对话失败: ' + e.message)
        }
      } finally {
        this.streaming = false
        this.streamContent = ''
        this.streamToolResults = []
        this.streamToolCalling = false
        this.streamToolCallingMsg = ''
        this.currentReader = null
        // 刷新消息列表和会话列表
        if (this.activeConversationId) {
          const res = await getMessages(this.activeConversationId)
          this.messages = res.data || []
        }
        // 异步刷新侧边栏(不影响主流程)
        this.loadConversations()
      }
    },

    handleStop() {
      if (this.currentReader) {
        this.currentReader.cancel()
        this.currentReader = null
      }
      this.streaming = false
      this.streamToolResults = []
      this.streamToolCalling = false
      this.streamToolCallingMsg = ''
      if (this.streamContent) {
        this.messages.push({
          _tempId: Date.now(),
          role: 'assistant',
          content: this.streamContent + '(已停止生成)',
          createTime: new Date().toISOString()
        })
      }
      this.streamContent = ''
    },

    /**
     * Phase 4.6: 处理 SSE 事件分发
     * 根据 event: 行的事件类型，将 data 路由到正确的状态
     * 
     * SSE 事件类型:
     * - message    → 追加到流式文本 (streamContent)
     * - tool_call  → 标记工具调用中 (streamToolCalling)
     * - tool_result → 添加工具执行结果卡片 (streamToolResults)
     * - error      → 显示错误并终止流式
     * - done       → 流结束 ([DONE] 在前端 data 行已提前捕获)
     */
    processStreamEvent(eventType, data) {
      switch (eventType) {
        case 'message':
          // 普通文本增量: 追加到流式显示内容
          this.streamContent += data
          break

        case 'tool_call':
          // 工具调用开始: 显示调用中状态
          this.streamToolCalling = true
          try {
            const info = JSON.parse(data)
            this.streamToolCallingMsg = info.message || '正在调用工具...'
          } catch {
            this.streamToolCallingMsg = '正在调用工具...'
          }
          break

        case 'tool_result':
          // 工具执行结果: 添加到结果列表 (实时展示)
          try {
            const result = JSON.parse(data)
            this.streamToolResults.push({
              code: result.code,
              arguments: result.arguments,
              success: result.success !== false, // 默认 true
              result: result.result || '(无返回内容)'
            })
            // 工具调用完成,隐藏"调用中"提示
            this.streamToolCalling = false
          } catch {
            console.warn('tool_result 事件解析失败:', data)
          }
          break

        case 'error':
          // 后端错误: 显示错误消息并终止
          this.$message.error(data || '对话出现异常')
          this.streaming = false
          break

        default:
          // 未知事件类型,静默忽略 (兼容未来扩展)
          break
      }
    }
  }
}
</script>

<style scoped>
.agent-container {
  display: flex;
  height: calc(100vh - 84px);
  background: #fff;
}
</style>
