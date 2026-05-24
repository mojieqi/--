import request from '@/utils/request'

// 工具列表
export function listTool(query) {
  return request({ url: '/ai/tool/list', method: 'get', params: query })
}

// 工具详情
export function getTool(toolId) {
  return request({ url: '/ai/tool/' + toolId, method: 'get' })
}

// 新增工具
export function addTool(data) {
  return request({ url: '/ai/tool', method: 'post', data })
}

// 修改工具
export function updateTool(data) {
  return request({ url: '/ai/tool', method: 'put', data })
}

// 删除工具
export function delTool(toolIds) {
  return request({ url: '/ai/tool/' + toolIds, method: 'delete' })
}

// 批量变更工具状态
export function changeToolStatus(data) {
  return request({ url: '/ai/tool/changeStatus', method: 'put', data })
}
