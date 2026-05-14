import request from '@/utils/request'

export function listFee(query) {
  return request({
    url: '/business/fee/list',
    method: 'get',
    params: query
  })
}

export function getFee(id) {
  return request({
    url: '/business/fee/' + id,
    method: 'get'
  })
}

