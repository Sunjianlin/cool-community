import axios from './axios'

const backgroundApi = {
  getBackgroundList: () => {
    return axios.get('/background/list')
  },
  
  getCurrentBackground: () => {
    return axios.get('/background/current')
  },
  
  setCurrentBackground: (backgroundId) => {
    return axios.post('/background/set-current', null, {
      params: { backgroundId }
    })
  }
}

export default backgroundApi
