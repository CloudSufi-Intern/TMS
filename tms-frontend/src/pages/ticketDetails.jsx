import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getTicketById, updateTicket } from '../services/TicketService';
import '../ticketDetails.css';

/**
 * TicketDetail page — fetches full ticket info from GET /api/tickets/{id}.
 * Shows ticket metadata, history audit log, and attachment metadata.
 *
 * @author Smriti Bajpai
 * [API Integration] Replaced context read with real API call — Priyanshu Gupta
 * [Merge] download handler fix — Yashas Yadav
 */
const TicketDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();

  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');

  const [comment, setComment] = useState('');
  const [comments, setComments] = useState([]);
  const [status, setStatus] = useState('');
  const [priority, setPriority] = useState('');
  const [toast, setToast] = useState({ show: false, message: '' });
  const [attachments, setAttachments] = useState([]);
  const [isUpdating, setIsUpdating] = useState(false);
  const [assigneeEmail, setAssigneeEmail] = useState('');

  useEffect(() => {
    const fetchTicket = async () => {
      setLoading(true);
      setFetchError('');
      try {
        const data = await getTicketById(id);
        setTicket(data);
        setStatus(data.status || '');
        setPriority(data.priority || '');
        setAttachments(data.attachments || []);
      } catch (err) {
        setFetchError(err.message || 'Failed to load ticket.');
      } finally {
        setLoading(false);
      }
    };

    fetchTicket();
  }, [id]);

  /** Shows a toast notification that auto-hides after 2.5s */
  const showToast = (message) => {
    setToast({ show: true, message });
    setTimeout(() => setToast({ show: false, message: '' }), 2500);
  };

  /** Handles ticket assignment to a specific agent email */
  const handleAssignAgent = async () => {
    if (!assigneeEmail.trim()) return;
    setIsUpdating(true);
    try {
      await updateTicket(id, { assigneeEmail: assigneeEmail.trim() });
      showToast(`Ticket assigned to ${assigneeEmail}`);
      setAssigneeEmail('');
      const updatedTicket = await getTicketById(id);
      setTicket(updatedTicket);
    } catch (error) {
      console.error("Error assigning agent:", error);
      showToast(error.message || 'Failed to assign agent');
    } finally {
      setIsUpdating(false);
    }
  };

  /** Closing the ticket and redirecting to the dashboard page */
  const closeTicketHandler = async () => {
    try {
      setIsUpdating(true);
      await updateTicket(id, { status: 'CLOSED' });
      showToast('Ticket closed successfully');
      setTimeout(() => navigate('/dashboard'), 1000);
    } catch (error) {
      console.error("Error closing ticket:", error);
      showToast('Failed to close ticket');
    } finally {
      setIsUpdating(false);
    }
  };

  const formatDate = (dt) => {
    if (!dt) return 'N/A';
    return new Date(dt).toLocaleString();
  };

  const formatBytes = (bytes) => {
    if (!bytes) return '0 KB';
    return bytes > 1024 * 1024
      ? `${(bytes / (1024 * 1024)).toFixed(1)} MB`
      : `${Math.round(bytes / 1024)} KB`;
  };

  if (loading) {
    return (
      <div className="td-page">
        <header className="td-header">
          <button className="td-back-btn" onClick={() => navigate('/dashboard')}>
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
            </svg>
            Back to Dashboard
          </button>
        </header>
        <div style={{ textAlign: 'center', padding: '80px 24px', color: '#9ca3af' }}>
          <p style={{ fontSize: '15px' }}>Loading ticket...</p>
        </div>
      </div>
    );
  }

  if (fetchError || !ticket) {
    return (
      <div className="td-page">
        <header className="td-header">
          <button className="td-back-btn" onClick={() => navigate('/dashboard')}>
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
            </svg>
            Back to Dashboard
          </button>
        </header>
        <div style={{ textAlign: 'center', padding: '80px 24px', color: '#9ca3af' }}>
          <p style={{ fontSize: '18px', fontWeight: '600', color: '#111827' }}>
            {fetchError || 'Ticket not found'}
          </p>
          <p style={{ fontSize: '14px', marginTop: '8px' }}>
            The ticket may not exist or you may not have access.
          </p>
        </div>
      </div>
    );
  }

  /** Adds a new comment to the local list */
  const handleAddComment = () => {
    if (!comment.trim()) return;
    const newComment = {
      id: Date.now(),
      author: ticket.createdBy,
      text: comment.trim(),
      time: new Date().toLocaleString(),
    };
    setComments((prev) => [...prev, newComment]);
    setComment('');
    showToast('Comment added successfully');
  };

  /** Updates ticket status in backend and locally */
  const handleStatusChange = async (e) => {
    const newStatus = e.target.value;
    setIsUpdating(true);
    try {
      await updateTicket(id, { status: newStatus });
      setStatus(newStatus);
      showToast(`Status updated to "${newStatus}"`);
      
      // Refresh ticket data to get updated history
      const updatedTicket = await getTicketById(id);
      setTicket(updatedTicket);
    } catch (error) {
      console.error("Error updating status:", error);
      showToast('Failed to update status');
    } finally {
      setIsUpdating(false);
    }
  };

  /** Updates ticket priority in backend and locally */
  const handlePriorityChange = async (e) => {
    const newPriority = e.target.value;
    setIsUpdating(true);
    try {
      await updateTicket(id, { priority: newPriority });
      setPriority(newPriority);
      showToast(`Priority updated to "${newPriority}"`);
      
      // Refresh ticket data to get updated history
      const updatedTicket = await getTicketById(id);
      setTicket(updatedTicket);
    } catch (error) {
      console.error("Error updating priority:", error);
      showToast('Failed to update priority');
    } finally {
      setIsUpdating(false);
    }
  };

  /** Handles file selection — appends to local attachments state */
  const handleAttachFile = (e) => {
    const selectedFiles = Array.from(e.target.files);
    if (!selectedFiles.length) return;

    const newAttachments = selectedFiles.map((file) => ({
      id: Date.now() + Math.random(),
      name: file.name,
      fileSizeInBytes: file.size,
      fileType: file.type.startsWith('image/') ? 'IMAGE' : 'PDF',
      localUrl: URL.createObjectURL(file),
    }));

    setAttachments((prev) => [...prev, ...newAttachments]);
    showToast(`${selectedFiles.length} file(s) attached successfully`);
    e.target.value = '';
  };

  /** Downloads a file — opens local blob URL or fetches from backend */
  const handleDownload = async (file) => {
    try {
      if (file.localUrl) {
        window.open(file.localUrl, '_blank');
        return;
      }

      const token = localStorage.getItem('token');
      const response = await fetch(
        `http://localhost:8080/api/attachments/${file.id}/download`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (!response.ok) throw new Error('Download failed');

      const blob = await response.blob();
      window.open(window.URL.createObjectURL(blob));
    } catch (err) {
      console.error(err);
      showToast('Failed to download file');
    }
  };

  return (
    <div className={`td-page ${isUpdating ? 'td-updating' : ''}`}>

      {/* TOP NAV */}
      <header className="td-header">
        <button className="td-back-btn" onClick={() => navigate('/dashboard')}>
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
          Back to Dashboard
        </button>
        <span className="td-ticket-id">#{String(ticket.id).padStart(6, '0')}</span>
      </header>

      <div className="td-layout">

        {/* LEFT COLUMN */}
        <div className="td-left">

          {/* Ticket Info Card */}
          <div className="td-card">
            <h1 className="td-title">{ticket.title}</h1>
            <div className="td-badges">
              <span className={`td-badge td-status-${ticket.status?.toLowerCase()}`}>
                {ticket.status?.replace(/_/g, ' ')}
              </span>
              <span className={`td-badge td-priority-${ticket.priority?.toLowerCase()}`}>
                {ticket.priority} priority
              </span>
            </div>
            <p className="td-description">{ticket.description}</p>
          </div>

          {/* History Card — real audit log from API */}
          <div className="td-card">
            <h2 className="td-section-title">History ({ticket.history?.length || 0})</h2>
            {(!ticket.history || ticket.history.length === 0) ? (
              <p style={{ fontSize: '13px', color: '#9ca3af' }}>No history on this ticket.</p>
            ) : (
              <div className="td-comments-list">
                {ticket.history.map((entry) => (
                  <div className="td-comment" key={entry.id}>
                    <div className="td-avatar">
                      <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                        <path strokeLinecap="round" strokeLinejoin="round"
                          d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                    <div className="td-comment-body">
                      <div className="td-comment-header">
                        <span className="td-comment-author">{entry.createdBy}</span>
                        <span className="td-comment-time">{formatDate(entry.createdAt)}</span>
                      </div>
                      <p className="td-comment-text">{entry.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Attachments Card */}
          <div className="td-card">
            <h2 className="td-section-title">Attachments ({attachments.length})</h2>
            {attachments.length === 0 ? (
              <p style={{ fontSize: '13px', color: '#9ca3af' }}>No attachments on this ticket.</p>
            ) : (
              <div className="td-attachments">
                {attachments.map((file, i) => (
                  <div className="td-attachment-row" key={file.id ?? i}>
                    <div className="td-attachment-left">
                      <div className="td-attachment-icon">
                        <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                          <path strokeLinecap="round" strokeLinejoin="round"
                            d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                        </svg>
                      </div>
                      <div>
                        <div className="td-attachment-name">
                          {file.name || `Attachment #${file.id} (${file.fileType})`}
                        </div>
                        <div className="td-attachment-size">{formatBytes(file.fileSizeInBytes)}</div>
                      </div>
                    </div>
                    <button
                      className="td-download-btn"
                      onClick={() => handleDownload(file)}
                    >
                      <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round"
                          d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            )}

            <div style={{ marginTop: '12px' }}>
              <input
                type="file"
                id="attachFileInput"
                style={{ display: 'none' }}
                multiple
                accept=".png,.jpg,.jpeg,.pdf"
                onChange={handleAttachFile}
              />
              <button
                className="td-attach-link"
                onClick={() => document.getElementById('attachFileInput').click()}
              >
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round"
                    d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                </svg>
                Attach files
              </button>
            </div>
          </div>

          {/* Comments Card */}
          <div className="td-card">
            <h2 className="td-section-title">Comments ({comments.length})</h2>
            <div className="td-comments-list">
              {comments.length === 0 ? (
                <p style={{ fontSize: '13px', color: '#9ca3af', paddingBottom: '16px' }}>
                  No comments yet. Be the first to comment.
                </p>
              ) : (
                comments.map((c) => (
                  <div className="td-comment" key={c.id}>
                    <div className="td-avatar">
                      <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                        <path strokeLinecap="round" strokeLinejoin="round"
                          d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                    <div className="td-comment-body">
                      <div className="td-comment-header">
                        <span className="td-comment-author">{c.author}</span>
                        <span className="td-comment-time">{c.time}</span>
                      </div>
                      <p className="td-comment-text">{c.text}</p>
                    </div>
                  </div>
                ))
              )}
            </div>
            <div className="td-add-comment">
              <label className="td-label">Add a comment</label>
              <div className="td-comment-input-wrap">
                <textarea
                  className="td-comment-input"
                  placeholder="Type your comment here..."
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && e.ctrlKey) handleAddComment();
                  }}
                />
                <button className="td-send-btn" onClick={handleAddComment}>
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                  </svg>
                </button>
              </div>
            </div>
          </div>

        </div>

        {/* RIGHT COLUMN */}
        <div className="td-right">

          {/* Update Status Card */}
          <div className="td-card">
            <h2 className="td-section-title">Update Status</h2>
            <select className="td-status-select" value={status} onChange={handleStatusChange} disabled={isUpdating}>
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="ON_HOLD">On Hold</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>
          </div>

          {/* Update Priority Card */}
          <div className="td-card">
            <h2 className="td-section-title">Update Priority</h2>
            <select className="td-status-select" value={priority} onChange={handlePriorityChange} disabled={isUpdating}>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>

          {/* Assign Agent Card */}
          <div className="td-card">
            <h2 className="td-section-title">
              {ticket.assignedTo && ticket.assignedTo !== 'Unassigned' ? 'Change Agent' : 'Assign Agent'}
            </h2>
            
            {ticket.assignedTo && ticket.assignedTo !== 'Unassigned' && (
              <div style={{ marginBottom: '12px', fontSize: '13px', color: '#4b5563' }}>
                Currently assigned to: <strong style={{ color: '#111827' }}>{ticket.assignedTo}</strong>
              </div>
            )}

            <div className="td-assign-input-wrap">
              <input 
                type="email" 
                className="td-assign-input" 
                placeholder="Agent email..." 
                value={assigneeEmail}
                onChange={(e) => setAssigneeEmail(e.target.value)}
              />
              <button 
                className="td-assign-btn" 
                onClick={handleAssignAgent}
                disabled={isUpdating || !assigneeEmail.trim()}
              >
                {ticket.assignedTo && ticket.assignedTo !== 'Unassigned' ? 'Update' : 'Assign'}
              </button>
            </div>
          </div>

          {/* Ticket Information Card */}

          <div className="td-card">
            <h2 className="td-section-title">Ticket Information</h2>
            <div className="td-info-list">

              <div className="td-info-item">
                <div className="td-info-icon">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round"
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <div>
                  <div className="td-info-label">Created By</div>
                  <div className="td-info-email">{ticket.createdBy || 'Unknown'}</div>
                </div>
              </div>

              <div className="td-info-item">
                <div className="td-info-icon">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round"
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <div>
                  <div className="td-info-label">Assigned To</div>
                  <div className="td-info-email">{ticket.assignedTo || 'Unassigned'}</div>
                </div>
              </div>

              {ticket.isApprovalRequired && (
                <div className="td-info-item">
                  <div className="td-info-icon">
                    <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                      <path strokeLinecap="round" strokeLinejoin="round"
                        d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div>
                    <div className="td-info-label">Approver</div>
                    <div className="td-info-email">{ticket.approver || 'N/A'}</div>
                    <div style={{ fontSize: '11px', color: '#9ca3af', marginTop: '2px' }}>
                      {ticket.approvalStatus?.replace(/_/g, ' ')}
                    </div>
                  </div>
                </div>
              )}

              <div className="td-info-item">
                <div className="td-info-icon">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                    <line x1="16" y1="2" x2="16" y2="6" />
                    <line x1="8" y1="2" x2="8" y2="6" />
                    <line x1="3" y1="10" x2="21" y2="10" />
                  </svg>
                </div>
                <div>
                  <div className="td-info-label">Created</div>
                  <div className="td-info-name">{formatDate(ticket.createdAt)}</div>
                </div>
              </div>

              <div className="td-info-item">
                <div className="td-info-icon">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <circle cx="12" cy="12" r="10" />
                    <polyline points="12 6 12 12 16 14" />
                  </svg>
                </div>
                <div>
                  <div className="td-info-label">Last Updated</div>
                  <div className="td-info-name">{formatDate(ticket.updatedAt)}</div>
                </div>
              </div>

              <div className="td-info-item">
                <div className="td-info-icon">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round"
                      d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A2 2 0 013 12V7a4 4 0 014-4z" />
                  </svg>
                </div>
                <div>
                  <div className="td-info-label">Ticket ID</div>
                  <div className="td-info-name td-ticket-id-bold">
                    #{String(ticket.id).padStart(6, '0')}
                  </div>
                </div>
              </div>

            </div>
          </div>

          {/* Quick Actions */}
          <div className="td-card">
            <h2 className="td-section-title">Quick Actions</h2>
            <div className="td-actions-list">
              <button className="td-action-btn">Edit Ticket</button>
              <button className="td-action-btn td-action-danger" onClick={closeTicketHandler} disabled={isUpdating}>
                Close Ticket
              </button>
            </div>
          </div>

        </div>
      </div>

      {/* Toast notification */}
      <div className={`td-toast ${toast.show ? 'td-toast-show' : ''}`}>
        <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
        </svg>
        {toast.message}
      </div>

    </div>
  );
};

export default TicketDetail;