import Badge from './Badge';

const statusDot = {
  open:        'bg-blue-500',
  in_progress: 'bg-amber-500',
  on_hold:     'bg-orange-500',
  resolved:    'bg-green-500',
  closed:      'bg-slate-400',
};

const TicketRow = ({ ticket, onClick }) => {
  const { title, description, status, priority, assignedTo, createdBy, commentCount, attachmentCount } = ticket;
  const statusKey   = status?.toLowerCase();
  const dotCls      = statusDot[statusKey] || 'bg-slate-400';
  const assigneeInitial = assignedTo && assignedTo !== 'Unassigned'
    ? assignedTo.charAt(0).toUpperCase()
    : null;
  const creatorName = typeof createdBy === 'object' ? createdBy?.name : (createdBy || 'Unknown');

  return (
    <div
      className="grid grid-cols-[1fr_120px_110px_160px_90px] items-center gap-4 px-4 py-3.5 border-b border-slate-100 hover:bg-slate-50 cursor-pointer transition-colors group"
      onClick={onClick}
    >
      {/* Title + meta */}
      <div className="min-w-0">
        <div className="flex items-center gap-2 mb-0.5">
          <span className={`w-2 h-2 rounded-full flex-shrink-0 ${dotCls}`} />
          <span className="text-sm font-semibold text-slate-900 truncate group-hover:text-indigo-700 transition-colors">
            {title}
          </span>
        </div>
        {description && (
          <p className="text-xs text-slate-400 truncate ml-4">{description}</p>
        )}
        <p className="text-xs text-slate-400 ml-4 mt-0.5">by {creatorName}</p>
      </div>

      {/* Status */}
      <div><Badge type={statusKey} /></div>

      {/* Priority */}
      <div><Badge type={priority?.toLowerCase()} /></div>

      {/* Assigned to */}
      <div className="flex items-center gap-2 min-w-0">
        {assigneeInitial ? (
          <>
            <div className="w-6 h-6 rounded-full bg-indigo-100 text-indigo-700 text-xs font-bold flex items-center justify-center flex-shrink-0">
              {assigneeInitial}
            </div>
            <span className="text-sm text-slate-700 truncate">{assignedTo}</span>
          </>
        ) : (
          <span className="text-sm text-slate-400 italic">Unassigned</span>
        )}
      </div>

      {/* Activity */}
      <div className="flex items-center gap-3">
        <span className="flex items-center gap-1 text-xs text-slate-400">
          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
          </svg>
          {commentCount || 0}
        </span>
        <span className="flex items-center gap-1 text-xs text-slate-400">
          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
          </svg>
          {attachmentCount || 0}
        </span>
      </div>
    </div>
  );
};

export default TicketRow;
