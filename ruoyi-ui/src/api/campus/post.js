import request from '@/utils/request'

// 查询帖子列表(首页)
export function listPost(query) {
  return request({
    url: '/campus/post/list',
    method: 'get',
    params: query
  })
}

// 查询帖子详情
export function getPost(postId) {
  return request({
    url: '/campus/post/' + postId,
    method: 'get'
  })
}

// 发布帖子
export function addPost(data) {
  return request({
    url: '/campus/post',
    method: 'post',
    data: data
  })
}

// 编辑帖子
export function updatePost(data) {
  return request({
    url: '/campus/post',
    method: 'put',
    data: data
  })
}

// 删除帖子
export function delPost(postIds) {
  return request({
    url: '/campus/post/' + postIds,
    method: 'delete'
  })
}

// 我的帖子列表
export function myPosts(query) {
  return request({
    url: '/campus/post/my',
    method: 'get',
    params: query
  })
}

// 分类列表
export function categoryList() {
  return request({
    url: '/campus/post/category/list',
    method: 'get'
  })
}
