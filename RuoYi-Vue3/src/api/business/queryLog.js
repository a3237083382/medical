import request from '@/utils/request'

export function listQueryLog(query) {
  return request({
    url: '/business/log/list',
    method: 'get',
    params: query
  })
}

export function getQueryLog(id) {
  return request({
    url: '/business/log/' + id,
    method: 'get'
  })
}
