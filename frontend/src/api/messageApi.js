import axios from './axios'

const messageApi = {
  // 获取两个用户之间的消息
  getMessagesBetweenUsers: (senderId, receiverId) => {
    return axios.get(`/api/messages/conversation/${senderId}/${receiverId}`)
  },
  
  // 获取未读消息
  getUnreadMessages: (receiverId) => {
    return axios.get(`/api/messages/unread/${receiverId}`)
  },
  
  // 发送消息
  sendMessage: (message) => {
    return axios.post('/api/messages', message)
  },
  
  // 标记消息为已读
  markAsRead: (messageId) => {
    return axios.post(`/api/messages/${messageId}/read`)
  }
}

export default messageApi
