import axios from './axios'

const messageApi = {
  getConversationList: () => {
    return axios.get('/message/conversations')
  },
  
  getConversation: (toUserId, params) => {
    return axios.get(`/message/conversation/${toUserId}`, { params })
  },
  
  sendMessage: (data) => {
    return axios.post('/message/send', data)
  },
  
  getMessageList: (params) => {
    return axios.get('/message/list', { params })
  },
  
  getUnreadCount: () => {
    return axios.get('/message/unread')
  },
  
  markAsRead: (id) => {
    return axios.post(`/message/read/${id}`)
  },
  
  markAllAsRead: () => {
    return axios.post('/message/read/all')
  },
  
  markConversationAsRead: (toUserId) => {
    return axios.post(`/message/conversation/read/${toUserId}`)
  },
  
  deleteMessage: (id) => {
    return axios.delete(`/message/delete/${id}`)
  }
}

export default messageApi
