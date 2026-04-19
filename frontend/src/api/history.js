import request from '@/utils/request'

export function listHistory(params) {
  return request.get('/history/list', { params })
}

export function getRecord(id) {
  return request.get(`/history/${id}`)
}

export function deleteRecord(id) {
  return request.delete(`/history/${id}`)
}

export function exportCsv() {
  return request.get('/history/export/csv', { responseType: 'blob' })
}

export function exportExcel() {
  return request.get('/history/export/excel', { responseType: 'blob' })
}
