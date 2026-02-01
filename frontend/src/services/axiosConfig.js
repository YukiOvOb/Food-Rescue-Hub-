import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://172.26.235.205:8081/api',
  // baseURL: 'http://localhost:8080/api',
  withCredentials: true
});

export default axiosInstance;
