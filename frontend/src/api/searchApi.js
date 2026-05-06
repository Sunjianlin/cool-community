import axios from './axios'

const searchApi = {
  searchAll: (keyword, page = 1, pageSize = 10) => 
    axios.get('/search/all', { params: { keyword, page, pageSize } }),
    
  searchPosts: (keyword, page = 1, pageSize = 10) => 
    axios.get('/search/posts', { params: { keyword, page, pageSize } }),
    
  searchUsers: (keyword, page = 1, pageSize = 10) => 
    axios.get('/search/users', { params: { keyword, page, pageSize } }),
    
  searchTopics: (keyword, page = 1, pageSize = 10) => 
    axios.get('/search/topics', { params: { keyword, page, pageSize } }),
    
  getSuggestions: (prefix) => 
    axios.get('/search/suggest', { params: { prefix } }),
    
  getHotKeywords: () => 
    axios.get('/search/hot-keywords')
}

export default searchApi
