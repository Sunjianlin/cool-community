import axios from './axios'

const productApi = {
  getProductList: (params) => {
    return axios.get('/product/list', { params })
  },
  
  getProductDetail: (id) => {
    return axios.get(`/product/detail/${id}`)
  },
  
  createProduct: (data) => {
    return axios.post('/product/create', data)
  },
  
  updateProduct: (data) => {
    return axios.put('/product/update', data)
  },
  
  deleteProduct: (id) => {
    return axios.delete(`/product/${id}`)
  }
}

export default productApi
