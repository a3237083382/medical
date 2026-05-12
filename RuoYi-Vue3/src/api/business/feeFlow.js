import request from '@/utils/request'

export function listFeeFlow(query) {
  return request({
    url: '/business/fee/list',
    method: 'get',
    params: query
  })
}

export function getFeeFlow(id) {
  return request({
    url: '/business/fee/' + id,
    method: 'get'
  })
}
