import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8081/api',
  withCredentials: true
});

export default axiosInstance;
