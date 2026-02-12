import axios from './axiosConfig';

const toStoredUser = (user) => {
  if (!user) return null;
  return {
    userId: user.userId ?? user.supplierId ?? user.id ?? null,
    supplierId: user.supplierId ?? user.userId ?? user.id ?? null,
    email: user.email ?? null,
    displayName: user.displayName ?? null,
    role: user.role ?? null
  };
};

const persistUser = (user) => {
  const safeUser = toStoredUser(user);
  if (!safeUser || !safeUser.userId) {
    localStorage.removeItem('user');
    localStorage.removeItem('isLoggedIn');
    return null;
  }
  localStorage.setItem('user', JSON.stringify(safeUser));
  localStorage.setItem('isLoggedIn', 'true');
  return safeUser;
};

const normalizeUser = (user) => {
  if (!user) return user;
  const resolvedUserId = user.userId ?? user.supplierId ?? user.id ?? null;
  return {
    ...user,
    userId: resolvedUserId,
    supplierId: user.supplierId ?? resolvedUserId
  };
};

const authService = {

  login: async (data) => {
    const payload = {
      ...data,
      role: 'SUPPLIER'
    };
    const res = await axios.post('/auth/login', payload);
    
    if (res.data) {
      const normalized = normalizeUser(res.data);
      if ((normalized.role || '').toUpperCase() !== 'SUPPLIER') {
        throw new Error('Please log in with a supplier account.');
      }
      persistUser(normalized);
      return normalized;
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
        const normalized = normalizeUser(JSON.parse(user));
        if ((normalized.role || '').toUpperCase() !== 'SUPPLIER') {
          localStorage.removeItem('user');
          localStorage.removeItem('isLoggedIn');
          throw new Error('Please log in with a supplier account.');
        }
        persistUser(normalized);
        return normalized;
      } catch (e) {
        localStorage.removeItem('user');
        localStorage.removeItem('isLoggedIn');
      }
    }
    
    
    const res = await axios.get('/auth/me');
    if (res.data) {
      const normalized = normalizeUser(res.data);
      if ((normalized.role || '').toUpperCase() !== 'SUPPLIER') {
        localStorage.removeItem('user');
        localStorage.removeItem('isLoggedIn');
        throw new Error('Please log in with a supplier account.');
      }
      persistUser(normalized);
      return normalized;
    }
    return res.data;
  },

  isAuthenticated: async () => {
    
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    if (isLoggedIn === 'true') {
      return true;
    }
    
    
    try {
      const response = await axios.get('/auth/me');
      if ((response?.data?.role || '').toUpperCase() !== 'SUPPLIER') {
        localStorage.removeItem('user');
        localStorage.removeItem('isLoggedIn');
        return false;
      }
      return true;
    } catch {
      localStorage.removeItem('user');
      localStorage.removeItem('isLoggedIn');
      return false;
    }
  },

  getStoredUser: () => {
    const user = localStorage.getItem('user');
    if (!user) return null;
    try {
      const normalized = normalizeUser(JSON.parse(user));
      if ((normalized.role || '').toUpperCase() !== 'SUPPLIER') {
        localStorage.removeItem('user');
        localStorage.removeItem('isLoggedIn');
        return null;
      }
      persistUser(normalized);
      return normalized;
    } catch {
      localStorage.removeItem('user');
      localStorage.removeItem('isLoggedIn');
      return null;
    }
  },

  persistUser
};

export default authService;
