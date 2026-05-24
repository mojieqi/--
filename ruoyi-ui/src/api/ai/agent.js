import request from '@/utils/request'
import { getToken } from '@/utils/auth'

// ==================== 会话管理 ====================

/** 会话列表 */
export function listConversations(query) {
  return request({ url: '/ai/agent/conversation/list', method: 'get', params: query })
}

/** 新建会话 */
export function createConversation(data) {
  return request({ url: '/ai/agent/conversation', method: 'post', data })
}

/** 删除会话 */
export function deleteConversation(ids) {
  return request({ url: '/ai/agent/conversation/' + ids, method: 'delete' })
}

/** 清空会话上下文 */
export function clearConversation(id) {
  return request({ url: '/ai/agent/conversation/' + id + '/clear', method: 'post' })
}

// ==================== 消息管理 ====================

/** 获取会话消息列表 */
export function getMessages(conversationId) {
  return request({ url: '/ai/agent/message/list/' + conversationId, method: 'get' })
}

/**
 * 发送消息 — SSE流式 (使用fetch实现POST流式)
 * 返回一个可读取的 ReadableStream reader
 */
export function chatStream(conversationId, content) {
  const baseURL = process.env.VUE_APP_BASE_API
  const url = baseURL + '/ai/agent/chat'

  return fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + getToken()
    },
    body: JSON.stringify({ conversationId, content })
  })
}

/** 停止生成 */
export function stopChat() {
  return request({ url: '/ai/agent/chat/stop', method: 'post' })
}

// ==================== 知识库检索 ====================

/** 知识库语义搜索 */
export function searchKnowledge(data) {
  return request({ url: '/ai/agent/search', method: 'post', data })
}

// ==================== 记忆管理 ====================

/** 长期记忆列表 */
export function listMemories(query) {
  return request({ url: '/ai/agent/memory/list', method: 'get', params: query })
}

/** 删除记忆 */
export function deleteMemory(ids) {
  return request({ url: '/ai/agent/memory/' + ids, method: 'delete' })
}
