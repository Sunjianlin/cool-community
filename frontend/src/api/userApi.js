import axios from './axios'

const userApi = {
  login: (data) => {
    return axios.post('/user/login', data)
  },
  
  register: (data) => {
    return axios.post('/user/register', data)
  },
  
  logout: () => {
    return axios.post('/user/logout')
  },
  
  getUserInfo: () => {
    return axios.get('/user/info')
  },
  
  getUserInfoById: (id) => {
    return axios.get(`/user/info/${id}`)
  },
  
  updateUserInfo: (data) => {
    return axios.put('/user/update', data)
  },
  
  uploadAvatar: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  followUser: (id) => {
    return axios.post(`/user/follow/${id}`)
  },
  
  unfollowUser: (id) => {
    return axios.delete(`/user/unfollow/${id}`)
  },
  
  getFollowingList: (id, params) => {
    return axios.get(`/user/following/${id}`, { params })
  },
  
  getFollowerList: (id, params) => {
    return axios.get(`/user/followers/${id}`, { params })
  },
  
  getUserList: (params) => {
    return axios.get('/user/list', { params })
  },
  
  banUser: (id) => {
    return axios.post(`/user/ban/${id}`)
  },
  
  unbanUser: (id) => {
    return axios.post(`/user/unban/${id}`)
  },
  
  deleteUser: (id) => {
    return axios.delete(`/user/${id}`)
  }
}

export default userApi
