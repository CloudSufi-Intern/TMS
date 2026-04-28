import Badge from './Badge';

const statusIcons = {
  open: (
    <svg className="ticket-icon" style={{ color: '#3b82f6' }} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
    </svg>
  ),
  in_progress: (
    <svg className="ticket-icon" style={{ color: '#ca8a04' }} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
    </svg>
  ),
  pending_approval: (
    <svg className="ticket-icon" style={{ color: '#f97316' }} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
    </svg>
  ),
  resolved: (
    <svg className="ticket-icon" style={{ color: '#22c55e' }} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  ),
};

/**
 * Single ticket row in the ticket list table
 * @param {object} ticket - Ticket data object
 * @param {function} onClick - Row click handler
 * @author-Smriti Bajpai
 */
const TicketRow = ({ ticket, onClick }) => {
  const { title, description, status, priority, category, assignedTo, createdBy, commentCount, attachmentCount, hasNotif } = ticket;

  return (
    <div className="ticket-row" onClick={onClick}>
      {/* Title + description */}
      <div className="ticket-info">
        <div className="ticket-title-wrap">
          {statusIcons[status?.toLowerCase()]}
          <span className="ticket-title">{title}</span>
        </div>
        <div className="ticket-desc">{description || "Not Available" }</div>
        <div className="ticket-creator">Created by {typeof createdBy === 'object' ? createdBy.name : (createdBy || "Unassigned")}</div>
      </div>

      {/* Status */}
      <div className="col-status">
        <Badge type={status?.toLowerCase()} />
      </div>

      {/* Priority */}
      <div className="col-priority">
        <Badge type={priority?.toLowerCase()} />
      </div>

     {/* Assigned To */}
     <div className="col-assigned">
       {assignedTo || "Unassigned"}
     </div>


      {/* Activity */}
      <div className="col-activity">
        <span className="activity-item">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round"
              d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
          </svg>
          {commentCount || 0} comments
        </span>
        <span className="activity-item">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round"
              d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
          </svg>
          {attachmentCount || 0} attachments
        </span>
      </div>
    </div>
  );
};

export default TicketRow;
