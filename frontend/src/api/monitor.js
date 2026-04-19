import request from '@/utils/request'

export function listTasks(keywordId) {
  return request.get('/monitor/tasks', { params: { keywordId } })
}

export function getTask(id) {
  return request.get(`/monitor/tasks/${id}`)
}

export function getTaskTweets(taskId, params) {
  return request.get(`/monitor/tasks/${taskId}/tweets`, { params })
}

export function getTaskResults(taskId) {
  return request.get(`/monitor/tasks/${taskId}/results`)
}

export function triggerCrawl(keywordId) {
  return request.post('/monitor/crawl', { keywordId })
}

export function getLogs(params) {
  return request.get('/monitor/logs', { params })
}
