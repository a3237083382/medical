import request from '@/utils/request'

// 查询保险公司列表
export function listCompany(query) {
  return request({
    url: '/business/company/list',
    method: 'get',
    params: query
  })
}

// 查询保险公司详细
export function getCompany(id) {
  return request({
    url: '/business/company/' + id,
    method: 'get'
  })
}

// 新增保险公司
export function addCompany(data) {
  return request({
    url: '/business/company',
    method: 'post',
    data: data
  })
}

// 修改保险公司
export function updateCompany(data) {
  return request({
    url: '/business/company',
    method: 'put',
    data: data
  })
}

// 删除保险公司
export function delCompany(id) {
  return request({
    url: '/business/company/' + id,
    method: 'delete'
  })
}

// 保险公司充值
export function recharge(id, amount) {
  return request({
    url: '/business/company/recharge',
    method: 'put',
    params: { id: id, amount: amount }
  })
}

// 修改保险公司状态
export function changeStatus(id, status) {
  return request({
    url: '/business/company/changeStatus',
    method: 'put',
    params: { id: id, status: status }
  })
}
