import request from '@/utils/request'

export function listLog(query) {
  return request({
    url: '/business/log/list',
    method: 'get',
    params: query
  })
}

export function getLog(id) {
  return request({
    url: '/business/log/' + id,
    method: 'get'
  })
}

