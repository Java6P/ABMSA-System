import request from '@/utils/request'

export function getOverview() {
  return request.get('/dashboard/overview')
}

export function getSentimentTrend(keywordId, period) {
  return request.get('/dashboard/trend', { params: { keywordId, period } })
}

export function getKeywordStats(id) {
  return request.get(`/dashboard/keyword/${id}/stats`)
}

export function getTargetCompare(keywordId) {
  return request.get('/dashboard/target-compare', { params: { keywordId } })
}

export function getWordCloud(keywordId) {
  return request.get('/dashboard/wordcloud', { params: { keywordId } })
}
