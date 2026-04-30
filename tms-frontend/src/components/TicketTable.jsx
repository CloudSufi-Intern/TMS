import TicketRow from './TicketRow';

const thCls = 'py-2.5 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider';

const TicketTable = ({ tickets, onTicketClick }) => (
  <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
    {/* Column headers */}
    <div className="grid grid-cols-[1fr_90px] md:grid-cols-[1fr_110px_100px_80px] lg:grid-cols-[1fr_120px_110px_160px_130px_130px_90px] gap-4 px-4 border-b border-slate-200 bg-slate-50">
      <div className={thCls}>Ticket</div>
      <div className={thCls}>Status</div>
      <div className={`${thCls} hidden md:block`}>Priority</div>
      <div className={`${thCls} hidden lg:block`}>Assigned To</div>
      <div className={`${thCls} hidden lg:block`}>Created</div>
      <div className={`${thCls} hidden lg:block`}>Updated</div>
      <div className={`${thCls} hidden md:block`}>Activity</div>
    </div>

    {tickets.length === 0 ? (
      <div className="py-20 flex flex-col items-center justify-center text-slate-400">
        <svg className="w-10 h-10 mb-3 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <p className="text-sm font-medium text-slate-500">No tickets found</p>
        <p className="text-xs text-slate-400 mt-1">Try adjusting your filters or create a new ticket.</p>
      </div>
    ) : (
      <div>
        {tickets.map((ticket) => (
          <TicketRow key={ticket.id} ticket={ticket} onClick={() => onTicketClick(ticket)} />
        ))}
      </div>
    )}
  </div>
);

export default TicketTable;
