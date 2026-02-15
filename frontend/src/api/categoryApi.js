import axios from './axios'

const categoryApi = {
  getAllCategories: () => {
    return axios.get('/category/list')
  }
}

export default categoryApi
