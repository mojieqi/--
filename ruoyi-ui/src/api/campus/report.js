import request from '@/utils/request'

// 提交举报
export function addReport(data) {
  return request({
    url: '/campus/report',
    method: 'post',
    data: data
  })
}

// 我的举报记录
export function myReports(query) {
  return request({
    url: '/campus/report/my',
    method: 'get',
    params: query
  })
}
