import request from '@/utils/request'

// 保险公司登录
export function companyLogin(data) {
  return request({
    url: '/company/login',
    method: 'post',
    data
  })
}

// 提交充值申请
export function submitRecharge(data) {
  return request({
    url: '/company/api/recharge/submit',
    method: 'post',
    data
  })
}

// 充值记录列表
export function listRecharge() {
  return request({
    url: '/company/api/recharge/list',
    method: 'get'
  })
}
