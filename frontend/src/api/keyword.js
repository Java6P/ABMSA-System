import request from '@/utils/request'

export function listKeywords() {
  return request.get('/keyword/list')
}

export function getKeyword(id) {
  return request.get(`/keyword/${id}`)
}

export function createKeyword(data) {
  return request.post('/keyword/create', data)
}

export function updateKeyword(id, data) {
  return request.put(`/keyword/${id}`, data)
}

export function deleteKeyword(id) {
  return request.delete(`/keyword/${id}`)
}

export function toggleKeyword(id, enabled) {
  return request.put(`/keyword/${id}/toggle`, { enabled })
}
