import request from '@/utils/request'

export function listPrice(query) {
  return request({
    url: '/business/price/list',
    method: 'get',
    params: query
  })
}

export function getPrice(id) {
  return request({
    url: '/business/price/' + id,
    method: 'get'
  })
}

export function addPrice(data) {
  return request({
    url: '/business/price',
    method: 'post',
    data: data
  })
}

export function updatePrice(data) {
  return request({
    url: '/business/price',
    method: 'put',
    data: data
  })
}

export function delPrice(id) {
  return request({
    url: '/business/price/' + id,
    method: 'delete'
  })
}
