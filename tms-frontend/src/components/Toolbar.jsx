/**
 * Toolbar with search input, status filter, and create ticket CTA
 * @param {string} search - Current search value
 * @param {function} onSearch - Search change handler
 * @param {string} statusFilter - Current status filter value
 * @param {function} onStatusFilter - Filter change handler
 * @param {function} onCreateClick - Opens create ticket modal
 *@author - Smriti Bajpai
 */
const Toolbar = ({ search, onSearch, statusFilter, onStatusFilter, onCreateClick }) => (
  <div className="toolbar">
    <div className="search-wrap">
      <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <circle cx="11" cy="11" r="8" />
        <path strokeLinecap="round" d="M21 21l-4.35-4.35" />
      </svg>
      <input
        className="search-input"
        type="text"
        placeholder="Search tickets..."
        value={search}
        onChange={(e) => onSearch(e.target.value)}
      />
    </div>

    <div className="filter-wrap">
      <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round"
          d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2a1 1 0 01-.293.707L13 13.414V19a1 1 0 01-.553.894l-4 2A1 1 0 017 21v-7.586L3.293 6.707A1 1 0 013 6V4z" />
      </svg>
      <select
        className="filter-select"
        value={statusFilter}
        onChange={(e) => onStatusFilter(e.target.value)}
      >
        <option value="">All Status</option>
        <option value="open">Open</option>
        <option value="in_progress">In Progress</option>
        <option value="pending_approval">Pending Approval</option>
        <option value="resolved">Resolved</option>
      </select>
    </div>

    <button className="btn-create" onClick={onCreateClick}>
      <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
      </svg>
      Create Ticket
    </button>
  </div>
);

export default Toolbar;
