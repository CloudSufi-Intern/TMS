const selectCls = 'px-3 py-2 border border-slate-300 rounded-lg text-sm text-slate-700 bg-white outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors';

const Toolbar = ({
  search, onSearch,
  statusFilter, onStatusFilter,
  sortBy, onSortBy,
  sortDir, onSortDir,
}) => (
  <div className="flex flex-wrap items-center gap-3 p-4 border-b border-slate-200">
    {/* Search */}
    <div className="flex items-center gap-2 flex-1 min-w-[180px] max-w-xs border border-slate-300 rounded-lg px-3 py-2 bg-white focus-within:ring-2 focus-within:ring-indigo-500 focus-within:border-indigo-500 transition-colors">
      <svg className="w-4 h-4 text-slate-400 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <circle cx="11" cy="11" r="8" />
        <path strokeLinecap="round" d="M21 21l-4.35-4.35" />
      </svg>
      <input
        type="text"
        placeholder="Search tickets..."
        value={search}
        onChange={(e) => onSearch(e.target.value)}
        className="flex-1 text-sm text-slate-900 placeholder-slate-400 outline-none bg-transparent"
      />
    </div>

    <div className="flex items-center gap-2 flex-wrap">
      <select value={statusFilter} onChange={(e) => onStatusFilter(e.target.value)} className={selectCls}>
        <option value="">All Status</option>
        <option value="open">Open</option>
        <option value="in_progress">In Progress</option>
        <option value="on_hold">On Hold</option>
        <option value="resolved">Resolved</option>
        <option value="closed">Closed</option>
      </select>

      <select value={sortBy} onChange={(e) => onSortBy(e.target.value)} className={selectCls}>
        <option value="createdAt">Created date</option>
        <option value="updatedAt">Last updated</option>
        <option value="priority">Priority</option>
        <option value="status">Status</option>
        <option value="title">Title</option>
      </select>

      <select value={sortDir} onChange={(e) => onSortDir(e.target.value)} className={selectCls}>
        <option value="desc">Newest first</option>
        <option value="asc">Oldest first</option>
      </select>
    </div>
  </div>
);

export default Toolbar;
