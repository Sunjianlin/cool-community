import axios from 'axios';

const pointsApi = {
  // 获取用户积分
  getUserPoints: async (userId) => {
    try {
      const response = await axios.get(`/api/points/${userId}`);
      return response.data;
    } catch (error) {
      console.error('获取用户积分失败:', error);
      throw error;
    }
  },
  
  // 获取用户积分记录
  getPointsRecords: async (userId, page = 1, size = 10) => {
    try {
      const response = await axios.get(`/api/points/${userId}/records`, {
        params: { page, size }
      });
      return response.data;
    } catch (error) {
      console.error('获取积分记录失败:', error);
      throw error;
    }
  }
};

export default pointsApi;