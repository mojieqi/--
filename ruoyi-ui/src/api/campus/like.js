import request from '@/utils/request'

// 点赞/取消点赞
export function toggleLike(data) {
  return request({
    url: '/campus/like',
    method: 'post',
    data: data
  })
}

// 查询点赞状态
export function likeStatus(params) {
  return request({
    url: '/campus/like/status',
    method: 'get',
    params: params
  })
}

// 批量查询点赞状态
export function batchLikeStatus(data) {
  return request({
    url: '/campus/like/batch-status',
    method: 'post',
    data: data
  })
}
