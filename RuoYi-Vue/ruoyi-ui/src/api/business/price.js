import request from '@/utils/request'

// 查询价目列表
export function listPrice(query) {
  return request({
    url: '/business/price/list',
    method: 'get',
    params: query
  })
}

// 查询价目详细
export function getPrice(id) {
  return request({
    url: '/business/price/' + id,
    method: 'get'
  })
}

// 新增价目
export function addPrice(data) {
  return request({
    url: '/business/price',
    method: 'post',
    data: data
  })
}

// 修改价目
export function updatePrice(data) {
  return request({
    url: '/business/price',
    method: 'put',
    data: data
  })
}

// 删除价目
export function delPrice(id) {
  return request({
    url: '/business/price/' + id,
    method: 'delete'
  })
}
