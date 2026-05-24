import request from '@/utils/request'

// 个性化推荐Feed流
export function getFeed(params) {
  return request({
    url: '/campus/recommend/feed',
    method: 'get',
    params
  })
}

// 热门榜单 (type=day/week/all)
export function getHot(params) {
  return request({
    url: '/campus/recommend/hot',
    method: 'get',
    params
  })
}

// 相关推荐
export function getRelated(postId, params) {
  return request({
    url: '/campus/recommend/related/' + postId,
    method: 'get',
    params
  })
}

// 热门关键词
export function getKeywords(params) {
  return request({
    url: '/campus/recommend/keywords',
    method: 'get',
    params
  })
}
