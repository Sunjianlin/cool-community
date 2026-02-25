import axios from './axios'

const messageApi = {
  // 获取聊天列表
  getChatList: () => {
    return axios.get('/chat/list')
  },
  
  // 获取或创建会话
  getOrCreateSession: (userId) => {
    return axios.get(`/chat/session/${userId}`)
  },
  
  // 获取会话的消息列表
  getSessionMessages: (sessionId, params) => {
    return axios.get(`/chat/messages/${sessionId}`, { params })
  },
  
  // 发送消息
  sendMessages: (userId, data) => {
    return axios.post('/chat/send', { ...data, toUserId: userId })
  },
  
  // 标记会话消息为已读
  markSessionAsRead: (sessionId) => {
    return axios.post(`/chat/read/${sessionId}`)
  },
  
  // 获取未读消息数量
  getUnreadCount: () => {
    return axios.get('/chat/unread')
  },
  
  // 删除会话
  deleteSession: (sessionId) => {
    return axios.delete(`/chat/session/${sessionId}`)
  },
  
  // 关闭会话
  closeSession: (sessionId) => {
    return axios.post(`/chat/close/${sessionId}`)
  },
  
  // 获取消息列表（系统消息）
  getMessageList: (params) => {
    return axios.get('/message/list', { params })
  },
  
  // 标记消息为已读（系统消息）
  markAsRead: (id) => {
    return axios.post(`/message/read/${id}`)
  },
  
  // 标记所有消息为已读（系统消息）
  markAllAsRead: () => {
    return axios.post('/message/read/all')
  },
  
  // 删除消息（系统消息）
  deleteMessage: (id) => {
    return axios.delete(`/message/delete/${id}`)
  }
}

export default messageApi
