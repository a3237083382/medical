import request from '@/utils/request'

export function getDashboardSummary() {
  return request({
    url: '/business/dashboard/summary',
    method: 'get'
  })
}

export function getMonthlyTrend() {
  return request({
    url: '/business/dashboard/trend',
    method: 'get'
  })
}

export function getQueryTypeStats() {
  return request({
    url: '/business/dashboard/query-type',
    method: 'get'
  })
}

export function getCompanyRank() {
  return request({
    url: '/business/dashboard/company-rank',
    method: 'get'
  })
}

