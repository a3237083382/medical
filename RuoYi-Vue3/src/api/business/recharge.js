import request from '@/utils/request'

export function listRecharge(query) {
  return request({
    url: '/business/recharge/list',
    method: 'get',
    params: query
  })
}

export function approveRecharge(data) {
  return request({
    url: '/business/recharge/approve',
    method: 'put',
    data
  })
}

export function rejectRecharge(data) {
  return request({
    url: '/business/recharge/reject',
    method: 'put',
    data
  })
}
