import request from '@/utils/request'

// 查询LLM大模型配置列表
export function listConfig(query) {
  return request({
    url: '/ai/llm/config/list',
    method: 'get',
    params: query
  })
}

// 查询LLM大模型配置详细
export function getConfig(configId) {
  return request({
    url: '/ai/llm/config/' + configId,
    method: 'get'
  })
}

// 新增LLM大模型配置
export function addConfig(data) {
  return request({
    url: '/ai/llm/config',
    method: 'post',
    data: data
  })
}

// 修改LLM大模型配置
export function updateConfig(data) {
  return request({
    url: '/ai/llm/config',
    method: 'put',
    data: data
  })
}

// 删除LLM大模型配置
export function delConfig(configId) {
  return request({
    url: '/ai/llm/config/' + configId,
    method: 'delete'
  })
}

// 设置默认配置
export function setDefault(configId) {
  return request({
    url: '/ai/llm/config/default/' + configId,
    method: 'put'
  })
}

// 测试接入 - 获取模型列表
export function testConnection(configId) {
  return request({
    url: '/ai/llm/config/test/' + configId,
    method: 'post'
  })
}

// 导出LLM大模型配置
export function exportConfig(query) {
  return request({
    url: '/ai/llm/config/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
