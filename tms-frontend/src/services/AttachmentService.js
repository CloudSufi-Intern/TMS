import apiClient from '../utils/apiClient';

/**
 * Attachment download. We pull the bytes as a Blob so we can save the file
 * with its original name (extracted from Content-Disposition).
 */
export const downloadAttachment = async (attachmentId, fallbackName) => {
  const res = await apiClient.get(`/api/attachments/${attachmentId}/download`, {
    responseType: 'blob',
  });

  // Pull the original filename out of the Content-Disposition header.
  let finalName = fallbackName || `file_${attachmentId}`;
  const disposition = res.headers['content-disposition'] || '';
  const match = disposition.match(/filename="?([^"]+)"?/);
  if (match && match[1]) finalName = match[1];

  const url = window.URL.createObjectURL(new Blob([res.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', finalName);
  document.body.appendChild(link);
  link.click();
  link.parentNode.removeChild(link);
  window.URL.revokeObjectURL(url);
};
