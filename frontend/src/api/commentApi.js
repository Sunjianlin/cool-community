import axios from './axios'

const commentApi = {
  // 获取帖子的评论
  getCommentsByPostId: (postId) => {
    return axios.get(`/api/comments/post/${postId}`)
  },
  
  // 创建评论
  createComment: (comment) => {
    return axios.post('/api/comments', comment)
  },
  
  // 点赞评论
  likeComment: (id) => {
    return axios.post(`/api/comments/${id}/like`)
  }
}

export default commentApi
