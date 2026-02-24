import axios from './axios'

const adminApi = {
  getUserList: (params) => {
    return axios.get('/admin/user/list', { params })
  },
  
  banUser: (id) => {
    return axios.post(`/admin/user/ban/${id}`)
  },
  
  unbanUser: (id) => {
    return axios.post(`/admin/user/unban/${id}`)
  },
  
  deleteUser: (id) => {
    return axios.delete(`/admin/user/${id}`)
  },
  
  getPostList: (params) => {
    return axios.get('/admin/post/list', { params })
  },
  
  approvePost: (id) => {
    return axios.post(`/admin/post/approve/${id}`)
  },
  
  rejectPost: (id) => {
    return axios.post(`/admin/post/reject/${id}`)
  },
  
  setTop: (id) => {
    return axios.post(`/admin/post/top/${id}`)
  },
  
  cancelTop: (id) => {
    return axios.delete(`/admin/post/top/${id}`)
  },
  
  setEssence: (id) => {
    return axios.post(`/admin/post/essence/${id}`)
  },
  
  cancelEssence: (id) => {
    return axios.delete(`/admin/post/essence/${id}`)
  },
  
  deletePost: (id) => {
    return axios.delete(`/admin/post/${id}`)
  },
  
  getTopicList: (params) => {
    return axios.get('/admin/topic/list', { params })
  },
  
  createTopic: (data) => {
    return axios.post('/admin/topic/create', data)
  },
  
  updateTopic: (data) => {
    return axios.put('/admin/topic/update', data)
  },
  
  deleteTopic: (id) => {
    return axios.delete(`/admin/topic/${id}`)
  },
  
  setHotTopic: (id) => {
    return axios.post(`/admin/topic/setHot/${id}`)
  },
  
  cancelHotTopic: (id) => {
    return axios.delete(`/admin/topic/setHot/${id}`)
  },
  
  getProductList: (params) => {
    return axios.get('/admin/product/list', { params })
  },
  
  createProduct: (data) => {
    return axios.post('/admin/product/create', data)
  },
  
  updateProduct: (data) => {
    return axios.put('/admin/product/update', data)
  },
  
  deleteProduct: (id) => {
    return axios.delete(`/admin/product/${id}`)
  },
  
  getDashboardStats: () => {
    return axios.get('/admin/dashboard/stats')
  }
}

export default adminApi
