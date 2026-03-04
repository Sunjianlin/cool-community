import axios from 'axios'
import { ElMessage } from 'element-plus'
import userApi from './userApi'

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

axiosInstance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
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
        
        // 如果不是刷新令牌的请求且没有正在刷新
        if (!originalRequest._retry && !isRefreshing) {
          originalRequest._retry = true
          isRefreshing = true
          
          const refreshToken = localStorage.getItem('refreshToken')
          if (!refreshToken) {
            // 没有刷新令牌，跳转到登录页
            localStorage.clear()
            ElMessage.error('登录已过期，请重新登录')
            setTimeout(() => {
              window.location.href = '/login'
            }, 1000)
            return Promise.reject(new Error('Token过期'))
          }
          
          // 尝试刷新令牌
          return userApi.refreshToken(refreshToken)
            .then(response => {
              const data = response.data
              if (data) {
                // 存储新的令牌
                storeTokens(data)
                // 更新请求头
                axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${data.token}`
                originalRequest.headers.Authorization = `Bearer ${data.token}`
                // 处理等待的请求
                processQueue(null)
                // 重新发送原始请求
                return axiosInstance(originalRequest)
              } else {
                throw new Error('刷新令牌失败')
              }
            })
            .catch(refreshError => {
              // 刷新令牌失败，跳转到登录页
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
          // 正在刷新令牌，将请求加入队列
          return new Promise((resolve, reject) => {
            refreshSubscribers.push(error => {
              if (error) {
                reject(error)
              } else {
                // 刷新成功，更新请求头并重试
                originalRequest.headers.Authorization = `Bearer ${localStorage.getItem('token')}`
                resolve(axiosInstance(originalRequest))
              }
            })
          })
        }
      }
      const message = error.response.data?.message || error.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    ElMessage.error('网络错误，请检查网络连接')
    return Promise.reject(error)
  }
)

export default axiosInstance
