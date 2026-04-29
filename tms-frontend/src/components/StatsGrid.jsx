const cards = (stats, role) => {
  const total = (stats.open || 0) + (stats.in_progress || 0) + (stats.pending_approval || 0) + (stats.resolved || 0) + (stats.closed || 0);
  const items = [
    {
      label: 'All Tickets',
      value: total,
      filterValue: '',
      iconCls: 'bg-indigo-100 text-indigo-600',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 10h16M4 14h16M4 18h16" />
        </svg>
      ),
    },
    {
      label: 'Open',
      value: stats.open ?? 0,
      filterValue: 'open',
      iconCls: 'bg-blue-100 text-blue-600',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
          <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
        </svg>
      ),
    },
    {
      label: 'In Progress',
      value: stats.in_progress ?? 0,
      filterValue: 'in_progress',
      iconCls: 'bg-amber-100 text-amber-600',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
      ),
    },
    {
      label: 'On Hold',
      value: stats.pending_approval ?? 0,
      filterValue: 'on_hold',
      iconCls: 'bg-orange-100 text-orange-600',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M10 9v6m4-6v6m7-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
    },
    ...(role === 'IT' ? [{
      label: 'Assigned to Me',
      value: stats.assigned ?? 0,
      filterValue: 'assigned',
      iconCls: 'bg-violet-100 text-violet-600',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>
      ),
    }] : []),
    {
      label: 'Resolved',
      value: stats.resolved ?? 0,
      filterValue: 'resolved',
      iconCls: 'bg-green-100 text-green-600',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
    },
    {
      label: 'Closed',
      value: stats.closed ?? 0,
      filterValue: 'closed',
      iconCls: 'bg-slate-100 text-slate-500',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
          <circle cx="12" cy="12" r="10" />
          <line x1="15" y1="9" x2="9" y2="15" /><line x1="9" y1="9" x2="15" y2="15" />
        </svg>
      ),
    },
  ];
  return items;
};

const StatsGrid = ({ stats, role, onFilter, activeFilter }) => {
  const handleClick = (filterValue) => {
    if (!onFilter) return;
    onFilter(filterValue === '' || activeFilter === filterValue ? '' : filterValue);
  };

  const isActive = (fv) => fv === '' ? activeFilter === '' : activeFilter === fv;

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 mb-6">
      {cards(stats, role).map((c) => (
        <button
          key={c.label}
          onClick={() => handleClick(c.filterValue)}
          className={`text-left p-4 rounded-xl border transition-all ${
            isActive(c.filterValue)
              ? 'bg-indigo-50 border-indigo-300 ring-1 ring-indigo-300 shadow-sm'
              : 'bg-white border-slate-200 hover:border-slate-300 hover:shadow-sm'
          }`}
        >
          <div className={`w-8 h-8 rounded-lg flex items-center justify-center mb-3 ${c.iconCls}`}>
            {c.icon}
          </div>
          <div className={`text-2xl font-bold leading-none mb-1 ${isActive(c.filterValue) ? 'text-indigo-700' : 'text-slate-900'}`}>
            {c.value}
          </div>
          <div className={`text-xs font-medium leading-tight ${isActive(c.filterValue) ? 'text-indigo-600' : 'text-slate-500'}`}>
            {c.label}
          </div>
        </button>
      ))}
    </div>
  );
};

export default StatsGrid;
