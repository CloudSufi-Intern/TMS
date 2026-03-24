import axios from 'axios';

/**
 * Service module for authentication API calls.
 * Centralises the backend base URL and all auth-related HTTP requests.
 *
 * @author Yashas Yadav
 */

const BASE_URL = 'http://localhost:8080';

const authClient = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

/**
 * Sends a sign-up request to POST /api/auth/signup.
 *
 * @param {Object} payload - { username, email, password, phoneNo, role }
 * @returns {Promise} Axios response
 */
export const signUp = (payload) => authClient.post('/api/auth/signup', payload);

/**
 * Sends a login request to POST /api/auth/login.
 *
 * @param {Object} payload - { email, password }
 * @returns {Promise} Axios response
 */
export const login = (payload) => authClient.post('/api/auth/login', payload);
