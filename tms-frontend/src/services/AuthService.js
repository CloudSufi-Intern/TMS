import apiClient from '../utils/apiClient';

/**
 * Auth API: signup + login.
 * Uses the central axios client so 401 handling and token attachment are uniform.
 */

export const signUp = (payload) => apiClient.post('/api/auth/signup', payload);

export const login = (payload) => apiClient.post('/api/auth/login', payload);
