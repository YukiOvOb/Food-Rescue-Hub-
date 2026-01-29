import axios from './axiosConfig';

const authService = {

  login: async (data) => {
    const res = await axios.post('/auth/login', data);
    return res.data;
  },

  register: async (data) => {
    const res = await axios.post('/auth/register', data);
    return res.data;
  },

  logout: async () => {
    await axios.post('/auth/logout');
  },

  getCurrentUser: async () => {
    const res = await axios.get('/auth/me');
    return res.data;
  },

  isAuthenticated: async () => {
    try {
      await axios.get('/auth/me');
      return true;
    } catch {
      return false;
    }
  }
};

export default authService;
