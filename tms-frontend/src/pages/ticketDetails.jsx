import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTicketContext } from "../context/TicketContext";
import '../ticketDetails.css';

/**
 * TicketDetail page , shows full ticket info, comments, attachments,
 * status update, and quick actions.
 * Reads ticket data from initialTickets using the id from the URL params.
 *
 * @author Smriti Bajpai
 */
const TicketDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { tickets,updateTicketStatus } = useTicketContext();
  const foundTicket = tickets.find(
    (t) => String(t.id) === String(id)
  );
  const [comment, setComment] = useState('');
  const [comments, setComments] = useState(foundTicket?.commentsList || []);
  const [status, setStatus] = useState(foundTicket?.status || 'open');
  const [toast, setToast] = useState({ show: false, message: '' });


  const [attachments, setAttachments] = useState(foundTicket?.attachments || []);

  /** Shows a toast notification that auto-hides after 2.5s */
  const showToast = (message) => {
    setToast({ show: true, message });
    setTimeout(() => setToast({ show: false, message: '' }), 2500);
  };

  /** Closing the ticket and redirecting to the dashboard page */
  const closeTicketHandler = () => {
    setTimeout(() => navigate('/dashboard'), 100);
  };

  /*
   * Handle case where no ticket matches the URL id.
   */
  if (!foundTicket) {
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
          <p style={{ fontSize: '18px', fontWeight: '600', color: '#111827' }}>Ticket not found</p>
          <p style={{ fontSize: '14px', marginTop: '8px' }}>The ticket you are looking for does not exist.</p>
        </div>
      </div>
    );
  }

  const ticket = foundTicket;

  /** Adds a new comment to the local list — wire to POST /api/tickets/:id/comments */
  const handleAddComment = () => {
    if (!comment.trim()) return;
    const newComment = {
      id: Date.now(),
      author: ticket.creator,
      text: comment.trim(),
      time: new Date().toLocaleString(),
    };
    setComments((prev) => [...prev, newComment]);
    setComment('');
    showToast('Comment added successfully');
  };

  /** Updates ticket status locally — wire to PATCH /api/tickets/:id when backend ready */
const handleStatusChange = (e) => {
  const newStatus = e.target.value;

  setStatus(newStatus);
  updateTicketStatus(id, newStatus);

  showToast(`Status updated to "${newStatus}"`);
};

  /** Quick action handlers — wire to respective API calls when backend ready */
  const handleQuickAction = (action) => {
    showToast(`${action} — this is for backend`);
  };

  /**
   * Handles file selection from local computer.
   * Reads selected files, converts size to KB/MB, and appends to attachments state.
   * TODO: Replace with API call when backend is ready:
   * const formData = new FormData();
   * files.forEach(file => formData.append('files', file));
   * fetch(`/api/tickets/${id}/attachments`, { method: 'POST', body: formData });
   */
  const handleAttachFile = (e) => {
    const selectedFiles = Array.from(e.target.files);
    if (!selectedFiles.length) return;

    const newAttachments = selectedFiles.map((file) => ({
      name: file.name,

      /* Convert bytes to KB or MB depending on file size */
      size: file.size > 1024 * 1024
        ? `${(file.size / (1024 * 1024)).toFixed(1)} MB`
        : `${Math.round(file.size / 1024)} KB`,

      fileType: file.type.startsWith('image/') ? 'image' : 'pdf',
      uploadedBy: ticket.creator,
      uploadedAt: new Date().toLocaleString(),

      /*
       * Creates a temporary local URL so the file can be previewed
       * in the browser without needing a backend.
       * This URL is only valid for the current browser session.
       */
      localUrl: URL.createObjectURL(file),
    }));

    setAttachments((prev) => [...prev, ...newAttachments]);
    showToast(`${selectedFiles.length} file(s) attached successfully`);

    /* Reset input value so the same file can be re-selected if needed */
    e.target.value = '';
  };

  return (
    <div className="td-page">

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
              <span className={`td-badge td-status-${ticket.status}`}>
                {ticket.status.replace(/_/g, ' ')}
              </span>
              <span className={`td-badge td-priority-${ticket.priority}`}>
                {ticket.priority} priority
              </span>
              <span className="td-badge td-category">{ticket.category}</span>
            </div>
            <p className="td-description">{ticket.description}</p>
          </div>

          {/* Attachments Card — now uses local attachments state instead of ticket.attachments */}
          <div className="td-card">
            <h2 className="td-section-title">
              Attachments ({attachments.length})
            </h2>

            {attachments.length === 0 ? (
              <p style={{ fontSize: '13px', color: '#9ca3af' }}>No attachments on this ticket.</p>
            ) : (
              <div className="td-attachments">
                {attachments.map((file, i) => (
                  <div className="td-attachment-row" key={i}>
                    <div className="td-attachment-left">
                      <div className="td-attachment-icon">
                        <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                          <path strokeLinecap="round" strokeLinejoin="round"
                            d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                        </svg>
                      </div>
                      <div>
                        <div className="td-attachment-name">{file.name}</div>
                        <div className="td-attachment-size">{file.size}</div>
                      </div>
                    </div>


                    <button
                      className="td-download-btn"
                      onClick={() => {
                        if (file.localUrl) {
                          window.open(file.localUrl, '_blank');
                        } else {
                          showToast(`Downloading ${file.name}...`);
                        }
                      }}
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

            {/* Add comment input */}
            <div className="td-add-comment">
              <label className="td-label">Add a comment</label>
              <div className="td-comment-input-wrap">
                <textarea
                  className="td-comment-input"
                  placeholder="Type your comment here..."
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  onKeyDown={(e) => {
                    /* Ctrl + Enter submits the comment */
                    if (e.key === 'Enter' && e.ctrlKey) handleAddComment();
                  }}
                />
                <button className="td-send-btn" onClick={handleAddComment}>
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                  </svg>
                </button>
              </div>

              {/*
               * Hidden file input — opens computer file picker when triggered.
               * Accepts PNG, JPG, JPEG and PDF files only.
               * multiple allows selecting more than one file at once.
               */}
              <input
                type="file"
                id="attachFileInput"
                style={{ display: 'none' }}
                multiple
                accept=".png,.jpg,.jpeg,.pdf"
                onChange={handleAttachFile}
              />

              {/* Visible button that triggers the hidden file input above */}
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

        </div>

        {/* RIGHT COLUMN */}
        <div className="td-right">

          {/* Update Status Card */}
          <div className="td-card">
            <h2 className="td-section-title">Update Status</h2>
            <select className="td-status-select" value={status} onChange={handleStatusChange}>
              <option value="open">Open</option>
              <option value="in_progress">In Progress</option>
              <option value="pending_approval">Pending Approval</option>
              <option value="resolved">Resolved</option>
            </select>
          </div>

          {/* Ticket Information Card */}
          <div className="td-card">
            <h2 className="td-section-title">Ticket Information</h2>
            <div className="td-info-list">

              {/* Created By */}
              <div className="td-info-item">
                <div className="td-info-icon">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round"
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <div>
                  <div className="td-info-label">Created By</div>
                  <div className="td-info-name">{ticket.createdBy.name}</div>
                  <div className="td-info-email">{ticket.createdBy.email}</div>
                </div>
              </div>

              {/* Assigned To */}
              <div className="td-info-item">
                <div className="td-info-icon">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round"
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <div>
                  <div className="td-info-label">Assigned To</div>
                  <div className="td-info-name">{ticket.assignedToDetail.name}</div>
                  <div className="td-info-email">{ticket.assignedToDetail.email}</div>
                </div>
              </div>

              {/* Created At */}
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
                  <div className="td-info-name">{ticket.createdAt}</div>
                </div>
              </div>

              {/* Last Updated */}
              <div className="td-info-item">
                <div className="td-info-icon">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <circle cx="12" cy="12" r="10" />
                    <polyline points="12 6 12 12 16 14" />
                  </svg>
                </div>
                <div>
                  <div className="td-info-label">Last Updated</div>
                  <div className="td-info-name">{ticket.updatedAt}</div>
                </div>
              </div>

              {/* Ticket ID */}
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

          {/* Quick Actions Card */}
          <div className="td-card">
              <button className="td-action-btn td-action-danger" onClick={closeTicketHandler}>
                Close Ticket
              </button>
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