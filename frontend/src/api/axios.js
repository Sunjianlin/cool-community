import axios from 'axios'
import { ElMessage } from 'element-plus'
import userApi from './userApi'
import { useUserStore } from '../store/user'

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8082/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 标记是否正在刷新令牌
let isRefreshing = false
// 存储等待刷新令牌的请求
let refreshSubscribers = []

// 处理等待刷新令牌的请求
function processQueue(error) {
  refreshSubscribers.forEach(cb => cb(error))
  refreshSubscribers = []
}

// 存储令牌的方法
function storeTokens(data) {
  localStorage.setItem('token', data.token)
  localStorage.setItem('refreshToken', data.refreshToken)
  localStorage.setItem('user', JSON.stringify(data))
}

const NO_AUTH_URLS = ['/user/login', '/user/register', '/user/refresh']

axiosInstance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    const isNoAuth = NO_AUTH_URLS.some(url => config.url.includes(url))
    if (token && !isNoAuth) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

axiosInstance.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    return Promise.reject(new Error(res.message || 'Error'))
  },
  error => {
    console.error('API Error:', error)
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        const originalRequest = error.config

        if (!originalRequest._retry && !isRefreshing) {
          originalRequest._retry = true
          isRefreshing = true

          const refreshToken = localStorage.getItem('refreshToken')
          if (!refreshToken) {
            localStorage.clear()
            ElMessage.error('登录已过期，请重新登录')
            setTimeout(() => {
              window.location.href = '/login'
            }, 1000)
            return Promise.reject(new Error('Token过期'))
          }

          return userApi.refreshToken(refreshToken)
            .then(response => {
              const data = response.data
              if (data) {
                storeTokens(data)
                
                const userStore = useUserStore()
                userStore.user = data
                userStore.userAvatar = data.avatar || DEFAULT_AVATAR
                
                axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${data.token}`
                originalRequest.headers.Authorization = `Bearer ${data.token}`
                processQueue(null)
                return axiosInstance(originalRequest)
              } else {
                throw new Error('刷新令牌失败')
              }
            })
            .catch(refreshError => {
              processQueue(refreshError)
              localStorage.clear()
              ElMessage.error('登录已过期，请重新登录')
              setTimeout(() => {
                window.location.href = '/login'
              }, 1000)
              return Promise.reject(refreshError)
            })
            .finally(() => {
              isRefreshing = false
            })
        } else {
          return new Promise((resolve, reject) => {
            refreshSubscribers.push(error => {
              if (error) {
                reject(error)
              } else {
                originalRequest.headers.Authorization = `Bearer ${localStorage.getItem('token')}`
                resolve(axiosInstance(originalRequest))
              }
            })
          })
        }
      }
      const message = error.response.data?.message || error.message || '请求失败'
      return Promise.reject(new Error(message))
    }
    ElMessage.error('网络错误，请检查网络连接')
    return Promise.reject(error)
  }
)

export default axiosInstance
