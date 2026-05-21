import request from '@/utils/request'

export function listMonthlyBill(query) {
  return request({
    url: '/business/monthly-bill/list',
    method: 'get',
    params: query
  })
}

export function getMonthlyBill(id) {
  return request({
    url: '/business/monthly-bill/' + id,
    method: 'get'
  })
}

export function generateMonthlyBill(billingMonth) {
  return request({
    url: '/business/monthly-bill/generate/' + billingMonth,
    method: 'post'
  })
}
