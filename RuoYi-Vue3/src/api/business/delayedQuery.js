import request from '@/utils/request'

export function listDelayedQuery(query) {
  return request({
    url: '/business/delayed-query/list',
    method: 'get',
    params: query
  })
}

export function listDelayedQueryBatch(query) {
  return request({
    url: '/business/delayed-query/batches/list',
    method: 'get',
    params: query
  })
}

export function getDelayedQueryBatch(id) {
  return request({
    url: `/business/delayed-query/batches/${id}`,
    method: 'get'
  })
}

export function getDelayedQuery(id) {
  return request({
    url: `/business/delayed-query/${id}`,
    method: 'get'
  })
}

export function startDelayedQuery(id) {
  return request({
    url: `/business/delayed-query/${id}/start`,
    method: 'post'
  })
}

export function importDelayedResult(id, file) {
  const data = new FormData()
  data.append('file', file)
  return request({
    url: `/business/delayed-query/${id}/result/import-preview`,
    method: 'post',
    data
  })
}

export function saveDelayedDraft(id, data) {
  return request({
    url: `/business/delayed-query/${id}/result/draft`,
    method: 'put',
    data
  })
}

export function completeDelayedQuery(id, data) {
  return request({
    url: `/business/delayed-query/${id}/complete`,
    method: 'post',
    data
  })
}

export function updateDelayedResult(id, data) {
  return request({
    url: `/business/delayed-query/${id}/result`,
    method: 'put',
    data
  })
}
