import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { initialTickets } from '../data/tickets';
import '../ticketDetails.css';

/**
 * TicketDetail page , shows full ticket info, comments, attachments,
 * status update, and quick actions.
 * Reads ticket data from initialTickets using the id from the URL params.
 *
 * @author Smriti Bajpai
 */
import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { initialTickets } from '../data/tickets';
import '../ticketDetails.css';

const TicketDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const foundTicket = initialTickets.find((t) => String(t.id) === String(id));

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
    showToast('Ticket closing successfully');
    setTimeout(() => navigate('/dashboard'), 1000);
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

  const handleStatusChange = (e) => {
    setStatus(e.target.value);
    showToast(`Status updated to "${e.target.value}"`);
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