import request from '@/utils/request'

function getCompanyToken() {
  return sessionStorage.getItem('companyToken') || localStorage.getItem('companyToken') || ''
}

function companyHeaders() {
  const token = getCompanyToken()
  return {
    isToken: false,
    Authorization: token ? 'Bearer ' + token : ''
  }
}

export function companyLogin(data) {
  return request({
    url: '/company/login',
    method: 'post',
    data,
    headers: {
      isToken: false
    }
  })
}

export function submitRecharge(data) {
  return request({
    url: '/company/api/recharge/submit',
    method: 'post',
    data,
    headers: companyHeaders()
  })
}

export function listRecharge() {
  return request({
    url: '/company/api/recharge/list',
    method: 'get',
    headers: companyHeaders()
  })
}

export function getProfile() {
  return request({
    url: '/company/api/profile',
    method: 'get',
    headers: companyHeaders()
  })
}

export function regenerateAppKey() {
  return request({
    url: '/company/api/profile/app-key',
    method: 'post',
    headers: companyHeaders()
  })
}

export function listMonthlyBill(query) {
  return request({
    url: '/company/api/monthly-bill/list',
    method: 'get',
    params: query,
    headers: companyHeaders()
  })
}

export function getMonthlyBill(id) {
  return request({
    url: '/company/api/monthly-bill/' + id,
    method: 'get',
    headers: companyHeaders()
  })
}

export function listQueryLogs(query) {
  return request({
    url: '/company/api/query-log/list',
    method: 'get',
    params: query,
    headers: companyHeaders()
  })
}

export function listMedicalQueryTypes() {
  return request({
    url: '/company/api/medical/query-types',
    method: 'get',
    headers: companyHeaders()
  })
}

export function queryMedical(data) {
  return request({
    url: '/company/api/medical/query',
    method: 'post',
    data,
    headers: companyHeaders()
  })
}
