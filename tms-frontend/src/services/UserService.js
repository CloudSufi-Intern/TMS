import apiClient from '../utils/apiClient';

export const getUserDetails = async (username) => {
  const res = await apiClient.get('/api/user', { params: { username } });
  return res.data;
};

export const searchUsers = async (username) => {
  const res = await apiClient.get('/api/user/search', { params: { username } });
  return res.data;
};

export const updateMe = async (payload) => {
  const res = await apiClient.put('/api/user', payload);
  return res.data;
};
