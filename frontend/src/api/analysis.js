import request from '@/utils/request'

export function predict(text, target, imageUrl) {
  return request.post('/analysis/predict', { text, target, imageUrl })
}

export function batchAnalyze(formData) {
  return request.post('/analysis/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getHistory(params) {
  return request.get('/analysis/history', { params })
}

export function exportCsv() {
  return request.get('/analysis/export/csv', { responseType: 'blob' })
}

export function exportExcel() {
  return request.get('/analysis/export/excel', { responseType: 'blob' })
}
