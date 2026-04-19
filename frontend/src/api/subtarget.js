import request from '@/utils/request'

export function listSubTargets(keywordId) {
  return request.get('/subtarget/list', { params: { keywordId } })
}

export function createSubTarget(data) {
  return request.post('/subtarget/create', data)
}

export function updateSubTarget(id, data) {
  return request.put(`/subtarget/${id}`, data)
}

export function deleteSubTarget(id) {
  return request.delete(`/subtarget/${id}`)
}

export function toggleSubTarget(id, enabled) {
  return request.put(`/subtarget/${id}/toggle`, { enabled })
}
