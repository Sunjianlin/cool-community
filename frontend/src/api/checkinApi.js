import axios from './axios';

const checkinApi = {
  checkin: async () => {
    const response = await axios.post('/checkin');
    return response;
  },

  hasCheckedInToday: async () => {
    const response = await axios.get('/checkin/status');
    return response;
  }
};

export default checkinApi;
