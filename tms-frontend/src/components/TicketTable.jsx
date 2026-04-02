import TicketRow from './TicketRow';

/**
 * Full ticket table with column headers and rows
 * @param {array} tickets - Filtered list of ticket objects
 * @param {function} onTicketClick - Handler when a row is clicked
 *@author-Smriti Bajpai
 */
const TicketTable = ({ tickets, onTicketClick }) => (
  <div className="ticket-table">
    {/* Column Headers */}
    <div className="table-header">
      <div className="th">Ticket</div>
      <div className="th">Status</div>
      <div className="th">Priority</div>
      <div className="th">Assigned To</div>
      <div className="th">Activity</div>
    </div>

    {/* Rows or Empty State */}
    {tickets.length === 0 ? (
      <div className="empty-state">
        <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <p>No tickets found</p>
      </div>
    ) : (
      tickets.map((ticket) => (
        <TicketRow
          key={ticket.id}
          ticket={ticket}
          onClick={() => onTicketClick(ticket)}
        />
      ))
    )}
  </div>
);

export default TicketTable;
