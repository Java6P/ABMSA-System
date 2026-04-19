import { defineStore } from 'pinia'
import { login as apiLogin, getUserInfo } from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    token: localStorage.getItem('token') || null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    async login(username, password) {
      const data = await apiLogin(username, password)
      this.token = data.token
      this.user = data.user || { username }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      ElMessage.success('登录成功')
    },
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
    },
    async fetchUserInfo() {
      const data = await getUserInfo()
      this.user = data
      localStorage.setItem('user', JSON.stringify(data))
    }
  }
})
