/**
 * Service module for ticket-related API calls.
 *
 * @author Ansh Parnami
 */

const BASE_URL = 'http://localhost:8080';

/**
 * Partially updates a ticket by its ID.
 *
 * @param {number|string} ticketId - The ID of the ticket to update.
 * @param {Object} updatePayload - The fields to update (status, priority, assigneeEmail).
 * @returns {Promise<Object>} The updated ticket response.
 */
export const updateTicket = async (ticketId, updatePayload) => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${BASE_URL}/api/tickets/${ticketId}`, {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(updatePayload)
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || 'Failed to update ticket');
  }

  return await response.json();
};

/**
 * Fetches ticket details by ID.
 *
 * @param {number|string} ticketId - The ID of the ticket to fetch.
 * @returns {Promise<Object>} The ticket details.
 */
export const getTicketById = async (ticketId) => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${BASE_URL}/api/tickets/${ticketId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || 'Failed to fetch ticket');
  }

  return await response.json();
};
