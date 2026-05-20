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

export function getCompanyProfile() {
  return request({
    url: '/company/api/profile',
    method: 'get'
  })
}

export function updateCompanyProfile(data) {
  return request({
    url: '/company/api/profile',
    method: 'put',
    data
  })
}

export function updateCompanyPassword(data) {
  return request({
    url: '/company/api/profile/password',
    method: 'put',
    data
  })
}

export function regenerateCompanyAppKey() {
  return request({
    url: '/company/api/profile/app-key',
    method: 'post'
  })
}

export function listQueryLogs(query) {
  return request({
    url: '/company/api/query-log/list',
    method: 'get',
    params: query
  })
}

export function listMedicalQueryTypes() {
  return request({
    url: '/company/api/medical/query-types',
    method: 'get'
  })
}

export function queryMedical(data) {
  return request({
    url: '/company/api/medical/query',
    method: 'post',
    data
  })
}

export function listFeeFlows(query) {
  return request({
    url: '/company/api/fee-flow/list',
    method: 'get',
    params: query
  })
}
