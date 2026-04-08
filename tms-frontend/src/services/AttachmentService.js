/**
 * Service module for attachment-related API calls.
 */

const BASE_URL = 'http://localhost:8080';

/**
 * Downloads an attachment by its ID.
 *
 * @param {number|string} attachmentId - The ID of the attachment to download.
 * @param {string} fileName - The name of the file to save as (optional).
 */
export const downloadAttachment = async (attachmentId, fileName) => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${BASE_URL}/api/attachments/${attachmentId}/download`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Failed to download attachment');
  }

  // Try to get filename from Content-Disposition header
  let finalFileName = fileName;
  const contentDisposition = response.headers.get('Content-Disposition');
  if (contentDisposition) {
    const fileNameMatch = contentDisposition.match(/filename="?([^"]+)"?/);
    if (fileNameMatch && fileNameMatch[1]) {
      finalFileName = fileNameMatch[1];
    }
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', finalFileName || `file_${attachmentId}`);
  document.body.appendChild(link);
  link.click();
  link.parentNode.removeChild(link);
  window.URL.revokeObjectURL(url);
};
