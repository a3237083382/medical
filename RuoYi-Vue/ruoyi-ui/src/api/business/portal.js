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

// 保险公司登录
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

// 提交充值申请
export function submitRecharge(data) {
  return request({
    url: '/company/api/recharge/submit',
    method: 'post',
    data,
    headers: companyHeaders()
  })
}

// 充值记录列表
export function listRecharge() {
  return request({
    url: '/company/api/recharge/list',
    method: 'get',
    headers: companyHeaders()
  })
}

// 查询可用医疗接口类型
export function listMedicalQueryTypes() {
  return request({
    url: '/company/api/medical/query-types',
    method: 'get',
    headers: companyHeaders()
  })
}

// 公司端医疗信息查询
export function queryMedical(data) {
  return request({
    url: '/company/api/medical/query',
    method: 'post',
    data,
    headers: companyHeaders()
  })
}
