import axios from './axios'

const brandApi = {
  getAllBrands: () => {
    return axios.get('/brand/list')
  },
  
  getBrandById: (id) => {
    return axios.get(`/brand/${id}`)
  },
  
  createBrand: (data) => {
    return axios.post('/admin/brand/create', data)
  },
  
  updateBrand: (data) => {
    return axios.put('/admin/brand/update', data)
  },
  
  deleteBrand: (id) => {
    return axios.delete(`/admin/brand/${id}`)
  }
}

export default brandApi
