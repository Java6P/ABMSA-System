import request from '@/utils/request'

export function getOverview() {
  return request.get('/dashboard/overview')
}

export function getSentimentTrend(keywordId, period) {
  const params = { period }
  if (keywordId != null) {
    params.keywordId = keywordId
  }
  return request.get('/dashboard/trend', { params })
}

export function getKeywordStats(id) {
  return request.get(`/dashboard/keyword/${id}/stats`)
}

export function getTargetCompare(keywordId) {
  const params = {}
  if (keywordId != null) {
    params.keywordId = keywordId
  }
  return request.get('/dashboard/target-compare', { params })
}

export function getWordCloud(keywordId) {
  const params = {}
  if (keywordId != null) {
    params.keywordId = keywordId
  }
  return request.get('/dashboard/wordcloud', { params })
}
