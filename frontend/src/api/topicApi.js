import axios from './axios'

const topicApi = {
  getTopicList: (params) => {
    return axios.get('/topic/list', { params })
  },
  
  getHotTopics: (params) => {
    return axios.get('/topic/hot', { params })
  },
  
  getTopicDetail: (id) => {
    return axios.get(`/topic/detail/${id}`)
  },
  
  createTopic: (data) => {
    return axios.post('/topic/create', data)
  },
  
  updateTopic: (data) => {
    return axios.put('/topic/update', data)
  },
  
  deleteTopic: (id) => {
    return axios.delete(`/topic/${id}`)
  },
  
  followTopic: (id) => {
    return axios.post(`/topic/follow/${id}`)
  },
  
  unfollowTopic: (id) => {
    return axios.delete(`/topic/unfollow/${id}`)
  }
}

export default topicApi
