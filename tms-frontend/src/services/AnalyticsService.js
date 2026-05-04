import apiClient from '../utils/apiClient';

export const getAnalytics = async () => {
  const res = await apiClient.get('/api/analytics');
  return res.data;
};
