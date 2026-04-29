const configs = {
  open:        { cls: 'bg-blue-50 text-blue-700 border-blue-200',    label: 'Open' },
  in_progress: { cls: 'bg-amber-50 text-amber-700 border-amber-200',  label: 'In Progress' },
  on_hold:     { cls: 'bg-orange-50 text-orange-700 border-orange-200', label: 'On Hold' },
  resolved:    { cls: 'bg-green-50 text-green-700 border-green-200',   label: 'Resolved' },
  closed:      { cls: 'bg-slate-100 text-slate-600 border-slate-300',  label: 'Closed' },
  low:         { cls: 'bg-green-50 text-green-700 border-green-200',   label: 'Low' },
  medium:      { cls: 'bg-blue-50 text-blue-700 border-blue-200',      label: 'Medium' },
  high:        { cls: 'bg-orange-50 text-orange-700 border-orange-200', label: 'High' },
  urgent:      { cls: 'bg-red-50 text-red-700 border-red-200',         label: 'Urgent' },
};

const Badge = ({ type, label }) => {
  const key = type?.toLowerCase();
  const cfg = configs[key] || { cls: 'bg-slate-100 text-slate-600 border-slate-300', label: type || '' };
  const display = label || cfg.label;
  return (
    <span className={`inline-flex items-center text-xs font-semibold px-2 py-0.5 rounded-full border ${cfg.cls}`}>
      {display}
    </span>
  );
};

export default Badge;
