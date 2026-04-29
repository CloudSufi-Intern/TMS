import apiClient from '../utils/apiClient';

/**
 * Ticket API. All calls go through the central axios client which:
 * - Injects the JWT
 * - Performs auto-logout on 401 (token expiry)
 */

/** Returns the authenticated user's tickets. Supports sort. */
export const getMyTickets = async ({ sortBy = 'createdAt', sortDir = 'desc' } = {}) => {
  const res = await apiClient.get('/api/tickets/my', { params: { sortBy, sortDir } });
  return res.data;
};

export const getTicketById = async (ticketId) => {
  const res = await apiClient.get(`/api/tickets/${ticketId}`);
  return res.data;
};

export const updateTicket = async (ticketId, payload) => {
  const res = await apiClient.patch(`/api/tickets/${ticketId}`, payload);
  return res.data;
};

/** Raise a ticket. Sends multipart so attachments are supported. */
export const raiseTicket = async ({ title, description, priority, files }) => {
  const formData = new FormData();
  formData.append('title', title);
  formData.append('description', description);
  formData.append('priority', priority || 'MEDIUM');
  if (files && files.length > 0) {
    for (const f of files) formData.append('attachments', f);
  }
  const res = await apiClient.post('/api/tickets', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
};

/**
 * Get comments. Supports sort (asc/desc) and an optional author username filter.
 */
export const getComments = async (ticketId, { sortDir = 'asc', author = '' } = {}) => {
  const params = { sortDir };
  if (author) params.author = author;
  const res = await apiClient.get(`/api/tickets/${ticketId}/comments`, { params });
  return res.data;
};

/**
 * Add a comment, optionally with attachments. Sent as multipart so the same
 * endpoint covers both text-only and attached cases.
 */
export const addComment = async (ticketId, content, files = []) => {
  const formData = new FormData();
  formData.append('content', content);
  for (const f of files) formData.append('attachments', f);
  const res = await apiClient.post(`/api/tickets/${ticketId}/comments`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
};
