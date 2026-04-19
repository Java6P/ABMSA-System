import { defineStore } from 'pinia'
import { listKeywords } from '@/api/keyword'

export const useKeywordStore = defineStore('keyword', {
  state: () => ({
    keywords: [],
    selectedKeyword: null
  }),
  actions: {
    async fetchKeywords() {
      const data = await listKeywords()
      this.keywords = Array.isArray(data) ? data : (data?.list || [])
    },
    selectKeyword(keyword) {
      this.selectedKeyword = keyword
    }
  }
})
