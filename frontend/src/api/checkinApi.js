import axios from './axios';

const checkinApi = {
  // 执行签到
  checkin: async (userId) => {
    try {
      const response = await axios.post('/checkin', { userId });
      return response.data;
    } catch (error) {
      console.error('签到失败:', error);
      throw error;
    }
  },

  // 检查今天是否已签到
  hasCheckedInToday: async (userId) => {
    try {
      const response = await axios.get(`/checkin/${userId}/status`);
      return response.data;
    } catch (error) {
      console.error('检查签到状态失败:', error);
      throw error;
    }
  }
};

export default checkinApi;
