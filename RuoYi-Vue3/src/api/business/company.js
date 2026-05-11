import request from '@/utils/request'

export function listCompany(query) {
  return request({
    url: '/business/company/list',
    method: 'get',
    params: query
  })
}

export function getCompany(id) {
  return request({
    url: '/business/company/' + id,
    method: 'get'
  })
}

export function addCompany(data) {
  return request({
    url: '/business/company',
    method: 'post',
    data: data
  })
}

export function updateCompany(data) {
  return request({
    url: '/business/company',
    method: 'put',
    data: data
  })
}

export function delCompany(id) {
  return request({
    url: '/business/company/' + id,
    method: 'delete'
  })
}

export function recharge(id, amount) {
  return request({
    url: '/business/company/recharge',
    method: 'put',
    params: { id: id, amount: amount }
  })
}

export function changeStatus(id, status) {
  return request({
    url: '/business/company/changeStatus',
    method: 'put',
    params: { id: id, status: status }
  })
}
