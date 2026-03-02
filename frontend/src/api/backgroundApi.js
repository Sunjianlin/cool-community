import axios from './axios'

const backgroundApi = {
  // 获取用户背景图列表
  getBackgroundList: () => {
    return axios.get('/api/background/list')
  },
  
  // 获取用户当前背景图
  getCurrentBackground: () => {
    return axios.get('/api/background/current')
  },
  
  // 设置当前背景图
  setCurrentBackground: (backgroundId) => {
    return axios.post('/api/background/set-current', { backgroundId })
  }
}

export default backgroundApi
