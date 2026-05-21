import request from '@/utils/request'

export function listPrice(query) {
  return request({
    url: '/business/company-price/list',
    method: 'get',
    params: query
  })
}

export function listCompanyPriceItems(companyId) {
  return request({
    url: '/business/company-price/company/' + companyId + '/items',
    method: 'get'
  })
}

export function getPrice(id) {
  return request({
    url: '/business/company-price/' + id,
    method: 'get'
  })
}

export function addPrice(data) {
  return request({
    url: '/business/company-price',
    method: 'post',
    data: data
  })
}

export function updatePrice(data) {
  return request({
    url: '/business/company-price',
    method: 'put',
    data: data
  })
}

export function delPrice(id) {
  return request({
    url: '/business/company-price/' + id,
    method: 'delete'
  })
}
