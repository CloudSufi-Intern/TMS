import { useState,useEffect } from 'react';
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

  const showToast = (message) => {
    setToast({ show: true, message });
    setTimeout(() => setToast({ show: false, message: '' }), 2500);
  };

  const closeTicketHandler = () => {
    setTimeout(() => navigate('/dashboard'), 100);
  };

  if (!foundTicket) {
    return <div>Ticket not found</div>;
  }

  const ticket = foundTicket;

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

  const handleAttachFile = (e) => {
    const selectedFiles = Array.from(e.target.files);
    if (!selectedFiles.length) return;

    const newAttachments = selectedFiles.map((file) => ({
      id: Date.now() + Math.random(),

      name: file.name,

      size:
        file.size > 1024 * 1024
          ? `${(file.size / (1024 * 1024)).toFixed(1)} MB`
          : `${Math.round(file.size / 1024)} KB`,

      fileType: file.type.startsWith('image/') ? 'image' : 'pdf',
      uploadedBy: ticket.creator,
      uploadedAt: new Date().toLocaleString(),

      localUrl: URL.createObjectURL(file), // local preview
    }));

    setAttachments((prev) => [...prev, ...newAttachments]);
    showToast(`${selectedFiles.length} file(s) attached successfully`);

    e.target.value = '';
  };

  const handleDownload = async (file) => {
    try {
      //  LOCAL FILE
      if (file.localUrl) {
        window.open(file.localUrl, '_blank');
        return;
      }

      //BACKEND FILE
      const token = localStorage.getItem("token");

      const response = await fetch(
        `http://localhost:8080/api/attachments/${file.id}/download`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Download failed");
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);

      window.open(url);

    } catch (err) {
      console.error(err);
      showToast("Failed to download file");
    }
  };

  return (
    <div className="td-page">
      {/* ── HEADER ── */}
      <header className="td-header">
        <button className="td-back-btn" onClick={() => navigate('/dashboard')}>
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back
        </button>
        <span className="td-ticket-id">#{String(ticket.id).padStart(6, '0')}</span>
      </header>

      {/* ── MAIN LAYOUT (The Grid) ── */}
      <main className="td-layout">

        {/* LEFT COLUMN: Ticket Content & Comments */}
        <div className="td-left">
          <div className="td-card">
            <h1 className="td-title">{ticket.title}</h1>
            <div className="td-badges">
              {/* Status Badge - Dynamic Class */}
              <span className={`td-badge td-status-${status.toLowerCase()}`}>
                {status.replace('_', ' ')}
              </span>
              {/* Priority Badge */}
              <span className={`td-badge td-priority-${ticket.priority?.toLowerCase()}`}>
                {ticket.priority}
              </span>
              <span className="td-badge td-category">{ticket.category}</span>
            </div>
            <p className="td-description">{ticket.description}</p>
          </div>

          {/* ATTACHMENTS */}
          <div className="td-card">
            <h2 className="td-section-title">Attachments ({attachments.length})</h2>
            <div className="td-attachments">
              {attachments.map((file) => (
                <div key={file.id} className="td-attachment-row">
                  <div className="td-attachment-left">
                    <div className="td-attachment-icon">
                      <svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" /></svg>
                    </div>
                    <div>
                      <div className="td-attachment-name">{file.name}</div>
                      <div className="td-attachment-size">{file.size}</div>
                    </div>
                  </div>
                  <button className="td-download-btn" onClick={() => handleDownload(file)}>
                    <svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path d="M4 16v1a2 2 0 002 2h12a2 2 0 002-2v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
                  </button>
                </div>
              ))}
            </div>
          </div>

          {/* COMMENTS SECTION */}
          <div className="td-card">
            <h2 className="td-section-title">Comments</h2>
            <div className="td-comments-list">
              {comments.map((c) => (
                <div key={c.id} className="td-comment">
                  <div className="td-avatar">
                    <svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
                  </div>
                  <div className="td-comment-body">
                    <div className="td-comment-header">
                      <span className="td-comment-author">{c.author}</span>
                      <span className="td-comment-time">{c.time}</span>
                    </div>
                    <p className="td-comment-text">{c.text}</p>
                  </div>
                </div>
              ))}
            </div>

            {/* ADD COMMENT INPUT */}
            <div className="td-add-comment">
              <label className="td-label">Post a Response</label>
              <div className="td-comment-input-wrap">
                <textarea
                  className="td-comment-input"
                  placeholder="Type your message..."
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                />
                <button className="td-send-btn" onClick={handleAddComment}>
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" /></svg>
                </button>
              </div>
              <button className="td-attach-link">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" /></svg>
                <input type="file" onChange={handleAttachFile} style={{display:'none'}} id="file-up" />
                <label htmlFor="file-up">Add Attachment</label>
              </button>
            </div>
          </div>
        </div>

        {/* RIGHT COLUMN: Sidebar */}
        <aside className="td-right">
          <div className="td-card">
            <h2 className="td-section-title">Status Control</h2>
            <select className="td-status-select" value={status} onChange={handleStatusChange}>
              <option value="open">Open</option>
              <option value="in_progress">In Progress</option>
              <option value="resolved">Resolved</option>
            </select>
          </div>

          <div className="td-card">
            <h2 className="td-section-title">Ticket Info</h2>
            <div className="td-info-list">
              <div className="td-info-item">
                <div className="td-info-icon">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
                </div>
                <div>
                  <div className="td-info-label">Created By</div>
                  <div className="td-info-name">{ticket.creator}</div>
                </div>
              </div>
            </div>
          </div>

          <div className="td-card">
            <h2 className="td-section-title">Quick Actions</h2>
            <div className="td-actions-list">
              <button className="td-action-btn">Edit Ticket</button>
              <button className="td-action-btn td-action-danger" onClick={closeTicketHandler}>
                Close Ticket
              </button>
            </div>
          </div>
        </aside>
      </main>

      {/* TOAST NOTIFICATION */}
      <div className={`td-toast ${toast.show ? 'td-toast-show' : ''}`}>
        <svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>
        {toast.message}
      </div>
    </div>
  );
};

export default TicketDetail;