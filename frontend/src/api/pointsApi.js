import axios from './axios';

const pointsApi = {
  // 获取用户积分
  getUserPoints: async () => {
    try {
      const response = await axios.get('/points/');
      return response.data;
    } catch (error) {
      console.error('获取用户积分失败:', error);
      throw error;
    }
  },

  // 增加用户积分
  addPoints: async (points, type) => {
    try {
      const response = await axios.post('/points/add', { points, type });
      return response.data;
    } catch (error) {
      console.error('增加用户积分失败:', error);
      throw error;
    }
  },

  // 减少用户积分
  reducePoints: async (points, type) => {
    try {
      const response = await axios.post('/points/reduce', { points, type });
      return response.data;
    } catch (error) {
      console.error('减少用户积分失败:', error);
      throw error;
    }
  }
};

export default pointsApi;
