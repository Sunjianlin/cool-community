import { defineStore } from 'pinia'
import userApi from '../api/userApi'

export const useUserStore = defineStore('user', {
  state: () => ({
    isLoggedIn: false,
    user: null,
    userAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }),
  
  getters: {
    getUser: (state) => state.user,
    getIsLoggedIn: (state) => state.isLoggedIn,
    getUserAvatar: (state) => state.userAvatar,
    isAdmin: (state) => {
      if (!state.user) return false
      return state.user.role >= 1
    }
  },
  
  actions: {
    updateUser(updatedUser) {
      this.user = { ...this.user, ...updatedUser }
      this.userAvatar = updatedUser.avatar || this.userAvatar
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    
    updateAvatar(avatarUrl) {
      if (this.user) {
        this.user.avatar = avatarUrl
        this.userAvatar = avatarUrl
        localStorage.setItem('user', JSON.stringify(this.user))
      }
    },
    
    async login(username, password) {
      try {
        const response = await userApi.login({ username, password })
        const user = response.data
        
        this.user = user
        this.isLoggedIn = true
        this.userAvatar = user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
        
        localStorage.setItem('user', JSON.stringify(user))
        localStorage.setItem('token', user.token)
        
        return user
      } catch (error) {
        console.error('登录失败:', error)
        throw error
      }
    },
    
    async register(data) {
      try {
        await userApi.register(data)
      } catch (error) {
        console.error('注册失败:', error)
        throw error
      }
    },
    
    logout() {
      this.user = null
      this.isLoggedIn = false
      this.userAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    },
    
    loadUserFromStorage() {
      const userStr = localStorage.getItem('user')
      const tokenStr = localStorage.getItem('token')
      if (userStr && tokenStr) {
        try {
          const user = JSON.parse(userStr)
          this.user = user
          this.isLoggedIn = true
          this.userAvatar = user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
        } catch (error) {
          console.error('加载用户信息失败:', error)
          localStorage.removeItem('user')
          localStorage.removeItem('token')
        }
      }
    }
  }
})
