import axios from './axios';

const pointsApi = {
  getUserPoints: async () => {
    const response = await axios.get('/points/');
    return response;
  },

  addPoints: async (points, type) => {
    const response = await axios.post('/points/add', { points, type });
    return response;
  },

  reducePoints: async (points, type) => {
    const response = await axios.post('/points/reduce', { points, type });
    return response;
  }
};

export default pointsApi;
