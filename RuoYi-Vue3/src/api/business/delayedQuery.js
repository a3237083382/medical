import request from '@/utils/request'

export function listDelayedQuery(query) {
  return request({
    url: '/business/delayed-query/list',
    method: 'get',
    params: query
  })
}

export function getDelayedQuery(id) {
  return request({
    url: '/business/delayed-query/' + id,
    method: 'get'
  })
}

export function saveDelayedQuery(id, data) {
  return request({
    url: '/business/delayed-query/' + id + '/save',
    method: 'post',
    data
  })
}

export function completeDelayedQuery(id, data) {
  return request({
    url: '/business/delayed-query/' + id + '/complete',
    method: 'post',
    data
  })
}

export function updateDelayedQueryResult(id, data) {
  return request({
    url: '/business/delayed-query/' + id + '/result',
    method: 'put',
    data
  })
}

export function importDelayedQueryExcel(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/business/delayed-query/' + id + '/import',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false },
    data: formData
  })
}

export function getCompanyDelayedLogs(companyId) {
  return request({
    url: '/business/delayed-query/company/' + companyId + '/logs',
    method: 'get'
  })
}
