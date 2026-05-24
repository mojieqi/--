import request from '@/utils/request'

// 获取帖子评论列表(含嵌套回复)
export function listComment(postId) {
  return request({
    url: '/campus/comment/list/' + postId,
    method: 'get'
  })
}

// 发表评论
export function addComment(data) {
  return request({
    url: '/campus/comment',
    method: 'post',
    data: data
  })
}

// 删除评论
export function delComment(commentId) {
  return request({
    url: '/campus/comment/' + commentId,
    method: 'delete'
  })
}
