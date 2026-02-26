import axios from 'axios'
import { ElMessage } from 'element-plus'

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8082/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

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
        // Token过期，清除本地存储并跳转到登录页面
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        ElMessage.error('登录已过期，请重新登录')
        // 延迟跳转，确保消息能够显示
        setTimeout(() => {
          window.location.href = '/login'
        }, 1000)
        return Promise.reject(new Error('Token过期'))
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
