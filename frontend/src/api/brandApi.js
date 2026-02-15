import axios from './axios'

const brandApi = {
  getAllBrands: () => {
    return axios.get('/brand/list')
  },
  
  getBrandById: (id) => {
    return axios.get(`/brand/${id}`)
  },
  
  createBrand: (data) => {
    return axios.post('/brand/create', data)
  },
  
  updateBrand: (data) => {
    return axios.put('/brand/update', data)
  },
  
  deleteBrand: (id) => {
    return axios.delete(`/brand/${id}`)
  }
}

export default brandApi
