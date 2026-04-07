/**
 * Service module for user-related API calls.
 *
 * @author Ansh Parnami
 */

const BASE_URL = 'http://localhost:8080';

/**
 * Fetches user details by username.
 *
 * @param {string} username - The username of the user to fetch.
 * @returns {Promise<Object>} The user details.
 */
export const getUserDetails = async (username) => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${BASE_URL}/api/user?username=${username}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || 'Failed to fetch user details');
  }

  return await response.json();
};
