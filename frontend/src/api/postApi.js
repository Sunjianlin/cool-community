import axios from './axios'

const postApi = {
  getPostList: (params) => {
    return axios.get('/post/list', { params })
  },
  
  getPostDetail: (id) => {
    return axios.get(`/post/detail/${id}`)
  },
  
  createPost: (data) => {
    return axios.post('/post/create', data)
  },
  
  deletePost: (id) => {
    return axios.delete(`/post/delete/${id}`)
  },
  
  likePost: (id) => {
    return axios.post(`/post/like/${id}`)
  },
  
  unlikePost: (id) => {
    return axios.delete(`/post/unlike/${id}`)
  },
  
  collectPost: (id) => {
    return axios.post(`/post/collect/${id}`)
  },
  
  uncollectPost: (id) => {
    return axios.delete(`/post/uncollect/${id}`)
  },
  
  approvePost: (id) => {
    return axios.post(`/post/approve/${id}`)
  },
  
  rejectPost: (id) => {
    return axios.post(`/post/reject/${id}`)
  },
  
  setTop: (id) => {
    return axios.post(`/post/top/${id}`)
  },
  
  cancelTop: (id) => {
    return axios.delete(`/post/top/${id}`)
  },
  
  setEssence: (id) => {
    return axios.post(`/post/essence/${id}`)
  },
  
  cancelEssence: (id) => {
    return axios.delete(`/post/essence/${id}`)
  }
}

export default postApi
