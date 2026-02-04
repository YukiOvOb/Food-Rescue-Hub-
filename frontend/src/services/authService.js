import axios from './axiosConfig';

const authService = {

  login: async (data) => {
    const res = await axios.post('/auth/login', data);
    
    if (res.data) {
      localStorage.setItem('user', JSON.stringify(res.data));
      localStorage.setItem('isLoggedIn', 'true');
    }
    return res.data;
  },

  register: async (data) => {
    const res = await axios.post('/auth/register', data);
    return res.data;
  },

  logout: async () => {
    await axios.post('/auth/logout');
    localStorage.removeItem('user');
    localStorage.removeItem('isLoggedIn');
  },

  getCurrentUser: async () => {
   
    const user = localStorage.getItem('user');
    if (user) {
      try {
        return JSON.parse(user);
      } catch (e) {
        console.error('Failed to parse user from localStorage:', e);
      }
    }
    
    
    const res = await axios.get('/auth/me');
    if (res.data) {
      localStorage.setItem('user', JSON.stringify(res.data));
      localStorage.setItem('isLoggedIn', 'true');
    }
    return res.data;
  },

  isAuthenticated: async () => {
    
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    if (isLoggedIn === 'true') {
      return true;
    }
    
    
    try {
      await axios.get('/auth/me');
      return true;
    } catch {
      localStorage.removeItem('user');
      localStorage.removeItem('isLoggedIn');
      return false;
    }
  },

  getStoredUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }
};

export default authService;
