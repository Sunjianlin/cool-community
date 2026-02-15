import axios from './axios'

const productReviewApi = {
  // 获取产品的评价
  getReviewsByProductId: (productId) => {
    return axios.get(`/api/product-reviews/product/${productId}`)
  },
  
  // 创建产品评价
  createReview: (review) => {
    return axios.post('/api/product-reviews', review)
  },
  
  // 标记评价为有用
  markAsUseful: (reviewId) => {
    return axios.post(`/api/product-reviews/${reviewId}/useful`)
  }
}

export default productReviewApi
