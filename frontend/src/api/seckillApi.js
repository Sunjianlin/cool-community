import axios from './axios'

const seckillApi = {
  // 获取活动详情
  getActivityDetail: (id) => {
    return axios.get(`/api/seckill/detail/${id}`)
  },
  
  // 执行秒杀
  doSeckill: (id) => {
    return axios.post(`/api/seckill/do/${id}`)
  },
  
  // 获取次日活动
  getNextDayActivity: () => {
    return axios.get('/api/seckill/next-day')
  },
  
  // 检查是否有次日活动
  hasNextDayActivity: () => {
    return axios.get('/api/seckill/has-next-day')
  },
  
  // 获取秒杀活动列表
  getSeckillList: () => {
    return axios.get('/admin/seckill/list')
  },
  
  // 创建秒杀活动
  createSeckill: (activity) => {
    return axios.post('/admin/seckill/create', activity)
  },
  
  // 更新秒杀活动
  updateSeckill: (activity) => {
    return axios.put('/admin/seckill/update', activity)
  },
  
  // 删除秒杀活动
  deleteSeckill: (id) => {
    return axios.delete(`/admin/seckill/delete/${id}`)
  },
  
  // 上传背景图
  uploadBackgroundImage: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post('/admin/seckill/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

export default seckillApi
