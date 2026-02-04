import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: '/api', 
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

// Request interceptor to add auth info if needed
axiosInstance.interceptors.request.use(
  config => {
    console.log('Request to:', config.url, 'withCredentials:', config.withCredentials);
    return config;
  },
  error => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor for debugging
axiosInstance.interceptors.response.use(
  response => {
    console.log('Response from:', response.config.url, 'status:', response.status);
    return response;
  },
  error => {
    console.error('Response error:', error.response?.status, error.response?.data);
    return Promise.reject(error);
  }
);

export default axiosInstance;

