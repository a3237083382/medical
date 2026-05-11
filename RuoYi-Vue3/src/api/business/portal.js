import request from '@/utils/request'

export function companyLogin(data) {
  return request({
    url: '/company/login',
    method: 'post',
    data
  })
}

export function submitRecharge(data) {
  return request({
    url: '/company/api/recharge/submit',
    method: 'post',
    data
  })
}

export function listRechargeRecords() {
  return request({
    url: '/company/api/recharge/list',
    method: 'get'
  })
}
