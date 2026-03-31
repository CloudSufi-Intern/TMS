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
  const foundTicket = initialTickets.find((t) => String(t.id) === String(id));

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

      <header className="td-header">
        <button onClick={() => navigate('/dashboard')}>Back</button>
        <span>#{String(ticket.id).padStart(6, '0')}</span>
      </header>

      <div className="td-layout">

        {/* LEFT */}
        <div className="td-left">

          <div className="td-card">
            <h1>{ticket.title}</h1>
            <p>{ticket.description}</p>
          </div>

          {/*  ATTACHMENTS */}
          <div className="td-card">
            <h2>Attachments ({attachments.length})</h2>

            {attachments.map((file) => (
              <div key={file.id} className="td-attachment-row">
                <span>{file.name}</span>

                <button onClick={() => handleDownload(file)}>
                  Download
                </button>
              </div>
            ))}
          </div>

          {/* COMMENTS */}
          <div className="td-card">
            <h2>Comments ({comments.length})</h2>
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
                 <div className="td-info-name">{ticket.creator || "Unknown"}</div>
                 <div className="td-info-email">N/A</div>
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
               <div className="td-info-name">{ticket.assignedTo || "Unassigned"}</div>
               <div className="td-info-email">N/A</div>
                </div>
              </div>

            {comments.map((c) => (
              <div key={c.id}>{c.text}</div>
            ))}

            <textarea
              value={comment}
              onChange={(e) => setComment(e.target.value)}
            />

            <button onClick={handleAddComment}>Add</button>

            <input
              type="file"
              multiple
              onChange={handleAttachFile}
            />
          {/* Quick Actions Card */}
          <div className="td-card">
              <button className="td-action-btn td-action-danger" onClick={closeTicketHandler}>
                Close Ticket
              </button>
          </div>
        </div>

        {/* RIGHT */}
        <div className="td-right">
          <select value={status} onChange={handleStatusChange}>
            <option value="open">Open</option>
            <option value="resolved">Resolved</option>
          </select>
        </div>
      </div>

      {/* TOAST */}
      {toast.show && <div>{toast.message}</div>}
    </div>
  );
};

export default TicketDetail;