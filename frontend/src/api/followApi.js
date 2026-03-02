import request from './axios'

const followApi = {
  // 关注用户
  followUser: (userId) => {
    return request({
      url: `/follow/user/${userId}`,
      method: 'post'
    })
  },
  
  // 取消关注用户
  unfollowUser: (userId) => {
    return request({
      url: `/follow/user/${userId}`,
      method: 'delete'
    })
  },
  
  // 关注话题
  followTopic: (topicId) => {
    return request({
      url: `/follow/topic/${topicId}`,
      method: 'post'
    })
  },
  
  // 取消关注话题
  unfollowTopic: (topicId) => {
    return request({
      url: `/follow/topic/${topicId}`,
      method: 'delete'
    })
  },
  
  // 关注产品
  followProduct: (productId) => {
    return request({
      url: `/follow/product/${productId}`,
      method: 'post'
    })
  },
  
  // 取消关注产品
  unfollowProduct: (productId) => {
    return request({
      url: `/follow/product/${productId}`,
      method: 'delete'
    })
  },
  
  // 检查是否关注
  checkFollow: (targetId, targetType) => {
    return request({
      url: '/follow/check',
      method: 'get',
      params: {
        targetId,
        targetType
      }
    })
  },
  
  // 获取关注列表
  getFollowingList: (userId, targetType, page = 1, pageSize = 10) => {
    return request({
      url: `/follow/following/${userId}`,
      method: 'get',
      params: {
        targetType,
        page,
        pageSize
      }
    })
  },
  
  // 获取粉丝/关注者列表
  getFollowerList: (targetId, targetType, page = 1, pageSize = 10) => {
    return request({
      url: `/follow/followers/${targetId}`,
      method: 'get',
      params: {
        targetType,
        page,
        pageSize
      }
    })
  },
  
  // 获取关注数量
  getFollowCount: (targetId, targetType) => {
    return request({
      url: `/follow/count/${targetId}`,
      method: 'get',
      params: {
        targetType
      }
    })
  },
  
  // 获取被关注数量
  getFollowerCount: (userId, targetType) => {
    return request({
      url: `/follow/follower/count/${userId}`,
      method: 'get',
      params: {
        targetType
      }
    })
  }
}

export default followApi
