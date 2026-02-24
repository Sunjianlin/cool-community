import axios from './axios'

const commentApi = {
  getCommentsByPostId: (postId, params) => {
    return axios.get(`/comment/list/${postId}`, { params })
  },
  
  createComment: (comment) => {
    return axios.post('/comment/create', comment)
  },
  
  likeComment: (id) => {
    return axios.post(`/comment/like/${id}`)
  },
  
  unlikeComment: (id) => {
    return axios.delete(`/comment/unlike/${id}`)
  },
  
  deleteComment: (id) => {
    return axios.delete(`/comment/delete/${id}`)
  }
}

export default commentApi
