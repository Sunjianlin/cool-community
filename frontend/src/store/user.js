import { defineStore } from 'pinia'
import userApi from '../api/userApi'

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

export const useUserStore = defineStore('user', {
  state: () => ({
    isLoggedIn: false,
    user: null,
    userAvatar: DEFAULT_AVATAR,
    isInitialized: false,
    tokenCheckInterval: null
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
      this.userAvatar = updatedUser.avatar || DEFAULT_AVATAR
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
        this.userAvatar = user.avatar || DEFAULT_AVATAR
        
        localStorage.setItem('user', JSON.stringify(user))
        localStorage.setItem('token', user.token)
        localStorage.setItem('refreshToken', user.refreshToken)
        
        // 启动token检测
        this.startTokenCheck()
        
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
    
    async logout() {
      try {
        await userApi.logout()
      } catch (error) {
        console.error('退出登录失败:', error)
      } finally {
        // 停止token检测
        this.stopTokenCheck()
        
        this.user = null
        this.isLoggedIn = false
        this.userAvatar = DEFAULT_AVATAR
        localStorage.removeItem('user')
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
      }
    },
    
    clearAuth() {
      // 停止token检测
      this.stopTokenCheck()
      
      this.user = null
      this.isLoggedIn = false
      this.userAvatar = DEFAULT_AVATAR
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
    },
    
    setLoggedIn(userData) {
      this.user = userData
      this.isLoggedIn = true
      this.userAvatar = userData.avatar || DEFAULT_AVATAR
    },
    
    async validateToken() {
      const token = localStorage.getItem('token')
      const userStr = localStorage.getItem('user')
      
      if (!token) {
        this.clearAuth()
        this.isInitialized = true
        return false
      }
      
      try {
        const response = await userApi.getUserInfo()
        if (response.code === 200 && response.data) {
          this.setLoggedIn(response.data)
          localStorage.setItem('user', JSON.stringify(response.data))
          this.isInitialized = true
          return true
        } else {
          // 如果后端返回错误，清除认证信息
          this.clearAuth()
          this.isInitialized = true
          return false
        }
      } catch (error) {
        console.error('Token验证失败:', error)
        // 如果请求失败，清除认证信息
        this.clearAuth()
        this.isInitialized = true
        return false
      }
    },
    
    async initAuth() {
      this.isInitialized = false
      const isValid = await this.validateToken()
      if (isValid) {
        // 如果token有效，启动token检测
        this.startTokenCheck()
      }
    },
    
    // 启动token检测
    startTokenCheck() {
      // 清除之前的检测间隔
      this.stopTokenCheck()
      
      // 每5分钟检测一次token有效性
      this.tokenCheckInterval = setInterval(async () => {
        await this.validateToken()
      }, 5 * 60 * 1000)
    },
    
    // 停止token检测
    stopTokenCheck() {
      if (this.tokenCheckInterval) {
        clearInterval(this.tokenCheckInterval)
        this.tokenCheckInterval = null
      }
    }
  }
})
