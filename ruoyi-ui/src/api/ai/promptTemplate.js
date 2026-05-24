import request from '@/utils/request'

// 查询提示词模板列表
export function listPrompt(query) {
  return request({
    url: '/ai/prompt/list',
    method: 'get',
    params: query
  })
}

// 查询提示词模板详细
export function getPrompt(templateId) {
  return request({
    url: '/ai/prompt/' + templateId,
    method: 'get'
  })
}

// 新增提示词模板
export function addPrompt(data) {
  return request({
    url: '/ai/prompt',
    method: 'post',
    data: data
  })
}

// 修改提示词模板
export function updatePrompt(data) {
  return request({
    url: '/ai/prompt',
    method: 'put',
    data: data
  })
}

// 删除提示词模板
export function delPrompt(templateIds) {
  return request({
    url: '/ai/prompt/' + templateIds,
    method: 'delete'
  })
}

// 获取启用的系统提示词
export function listSystemPrompts() {
  return request({
    url: '/ai/prompt/systemPrompts',
    method: 'get'
  })
}

// AI生成/润色提示词
export function generatePrompt(data) {
  return request({
    url: '/ai/prompt/generate',
    method: 'post',
    data: data
  })
}
