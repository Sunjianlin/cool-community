import axios from './axios'

const messageCenterApi = {
  getNotificationSummary: () => axios.get('/message-center/summary'),
  
  getCommentNotifications: (params) => axios.get('/message-center/comment', { params }),
  
  getLikeNotifications: (params) => axios.get('/message-center/like', { params }),
  
  getFollowNotifications: (params) => axios.get('/message-center/follow', { params }),
  
  getSystemNotifications: (params) => axios.get('/message-center/system', { params }),
  
  markCommentReplyAsRead: (id) => axios.put(`/message-center/comment-reply/${id}/read`),
  
  markPostCommentAsRead: (id) => axios.put(`/message-center/post-comment/${id}/read`),
  
  markAllCommentAsRead: () => axios.put('/message-center/comment/read-all'),
  
  markLikeAsRead: (id) => axios.put(`/message-center/like/${id}/read`),
  
  markAllLikeAsRead: () => axios.put('/message-center/like/read-all'),
  
  markFollowAsRead: (id) => axios.put(`/message-center/follow/${id}/read`),
  
  markAllFollowAsRead: () => axios.put('/message-center/follow/read-all'),
  
  markSystemAsRead: (id) => axios.put(`/message-center/system/${id}/read`),
  
  markAllSystemAsRead: () => axios.put('/message-center/system/read-all'),
  
  markAllAsRead: () => axios.put('/message-center/read-all')
}

export default messageCenterApi
