import request from '@/utils/request'

// ==================== 知识库 ====================

// 知识库列表
export function listKnowledgeBase(query) {
  return request({ url: '/ai/knowledge/base/list', method: 'get', params: query })
}

// 知识库详情
export function getKnowledgeBase(kbId) {
  return request({ url: '/ai/knowledge/base/' + kbId, method: 'get' })
}

// 新增知识库
export function addKnowledgeBase(data) {
  return request({ url: '/ai/knowledge/base', method: 'post', data })
}

// 修改知识库
export function updateKnowledgeBase(data) {
  return request({ url: '/ai/knowledge/base', method: 'put', data })
}

// 删除知识库
export function delKnowledgeBase(kbIds) {
  return request({ url: '/ai/knowledge/base/' + kbIds, method: 'delete' })
}

// ==================== 文档 ====================

// 文档列表
export function listDocument(query) {
  return request({ url: '/ai/knowledge/doc/list', method: 'get', params: query })
}

// 文档详情(含分块)
export function getDocument(docId) {
  return request({ url: '/ai/knowledge/doc/' + docId, method: 'get' })
}

// 上传文档
export function uploadDocument(data) {
  return request({ url: '/ai/knowledge/doc/upload', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
}

// 删除文档
export function delDocument(docIds) {
  return request({ url: '/ai/knowledge/doc/' + docIds, method: 'delete' })
}

// 重新解析文档
export function reparseDocument(docId) {
  return request({ url: '/ai/knowledge/doc/reparse/' + docId, method: 'post' })
}

// ==================== 检索 ====================

// 知识库检索
export function searchKnowledge(data) {
  return request({ url: '/ai/knowledge/search', method: 'post', data })
}
