import axios from 'axios';
import { removeToken } from './auth';

/**
 * Central axios instance used by every service in the app.
 *
 * - Base URL comes from VITE_API_BASE_URL so deployments don't need source changes.
 * - Request interceptor attaches the JWT bearer token from localStorage.
 * - Response interceptor catches 401 (expired/invalid token) and forces logout
 *   so the UI redirects to /login instead of showing a confusing error state.
 */
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081',
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      removeToken();
      // Avoid redirect loop if we're already on a public page
      const pathname = window.location.pathname;
      if (pathname !== '/login' && pathname !== '/') {
        window.location.replace('/login?expired=1');
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
