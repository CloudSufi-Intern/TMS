import { useEffect, useRef, useState } from 'react';

const fmt = (val) =>
  val ? new Date(val + 'T00:00:00').toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : null;

const DateRangeFilter = ({ dateFrom, onDateFrom, dateTo, onDateTo }) => {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    const handler = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const label = (() => {
    const f = fmt(dateFrom), t = fmt(dateTo);
    if (f && t) return `${f} – ${t}`;
    if (f) return `From ${f}`;
    if (t) return `Until ${t}`;
    return 'Date Range';
  })();

  const hasValue = dateFrom || dateTo;

  const clear = (e) => {
    e.stopPropagation();
    onDateFrom('');
    onDateTo('');
  };

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen((o) => !o)}
        className={`flex items-center gap-1.5 px-3 py-2 border rounded-lg text-sm transition-colors outline-none focus:ring-2 focus:ring-indigo-500 ${
          hasValue
            ? 'border-indigo-400 bg-indigo-50 text-indigo-700'
            : 'border-slate-300 bg-white text-slate-700 hover:border-slate-400'
        }`}
      >
        <svg className="w-3.5 h-3.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
          <line x1="16" y1="2" x2="16" y2="6" /><line x1="8" y1="2" x2="8" y2="6" /><line x1="3" y1="10" x2="21" y2="10" />
        </svg>
        <span>{label}</span>
        {hasValue ? (
          <span onClick={clear} className="ml-0.5 text-indigo-400 hover:text-indigo-600 leading-none">✕</span>
        ) : (
          <svg className="w-3 h-3 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
          </svg>
        )}
      </button>

      {open && (
        <div className="absolute top-full mt-1.5 left-0 z-20 bg-white border border-slate-200 rounded-xl shadow-lg p-4 w-64">
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-3">Filter by date</p>
          <div className="flex flex-col gap-3">
            <label className="flex flex-col gap-1">
              <span className="text-xs text-slate-500">From</span>
              <input
                type="date"
                value={dateFrom}
                max={dateTo || undefined}
                onChange={(e) => onDateFrom(e.target.value)}
                className="px-2.5 py-1.5 border border-slate-300 rounded-lg text-sm text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              />
            </label>
            <label className="flex flex-col gap-1">
              <span className="text-xs text-slate-500">To</span>
              <input
                type="date"
                value={dateTo}
                min={dateFrom || undefined}
                onChange={(e) => onDateTo(e.target.value)}
                className="px-2.5 py-1.5 border border-slate-300 rounded-lg text-sm text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              />
            </label>
          </div>
          {hasValue && (
            <button onClick={clear} className="mt-3 w-full text-xs text-slate-500 hover:text-red-500 transition-colors">
              Clear range
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default DateRangeFilter;
