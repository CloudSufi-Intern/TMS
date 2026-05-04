import { useEffect, useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
  AreaChart, Area,
} from 'recharts';
import AppShell from '../components/AppShell';
import { getAnalytics } from '../services/AnalyticsService';

const STATUS_COLORS = {
  OPEN: '#3b82f6',
  IN_PROGRESS: '#f59e0b',
  ON_HOLD: '#f97316',
  RESOLVED: '#22c55e',
  CLOSED: '#94a3b8',
};

const PRIORITY_COLORS = {
  LOW: '#22c55e',
  MEDIUM: '#3b82f6',
  HIGH: '#f59e0b',
  URGENT: '#ef4444',
};

const ASSIGNEE_COLOR = '#6366f1';

const cardCls = 'bg-white rounded-xl border border-slate-200 p-5';
const sectionTitle = 'text-sm font-semibold text-slate-700 mb-4';

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-white border border-slate-200 rounded-lg shadow-lg px-3 py-2 text-xs">
      <p className="font-semibold text-slate-700 mb-1">{label}</p>
      {payload.map((p) => (
        <p key={p.name} style={{ color: p.color ?? p.fill }}>
          {p.value} ticket{p.value !== 1 ? 's' : ''}
        </p>
      ))}
    </div>
  );
};

const Analytics = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getAnalytics()
      .then(setData)
      .catch((e) => setError(e.response?.data?.message || 'Failed to load analytics'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <AppShell title="Analytics">
        <div className="flex items-center justify-center py-32 text-slate-400">
          <svg className="w-6 h-6 animate-spin mr-3 text-indigo-500" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth={4} />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
          <span className="text-sm">Loading analytics...</span>
        </div>
      </AppShell>
    );
  }

  if (error) {
    return (
      <AppShell title="Analytics">
        <div className="flex items-center gap-2.5 p-4 bg-red-50 border border-red-200 rounded-xl text-sm text-red-600">
          {error}
        </div>
      </AppShell>
    );
  }

  const statusData = (data?.byStatus ?? []).map((d) => ({
    ...d,
    fill: STATUS_COLORS[d.label] ?? '#94a3b8',
  }));

  const priorityData = (data?.byPriority ?? []).map((d) => ({
    ...d,
    fill: PRIORITY_COLORS[d.label] ?? '#94a3b8',
  }));

  const totalTickets = statusData.reduce((s, d) => s + d.count, 0);

  return (
    <AppShell title="Analytics">
      {/* Summary row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-6">
        {[
          { label: 'Total Tickets', value: totalTickets, color: 'text-indigo-600', bg: 'bg-indigo-50' },
          { label: 'Open', value: statusData.find((d) => d.label === 'OPEN')?.count ?? 0, color: 'text-blue-600', bg: 'bg-blue-50' },
          { label: 'In Progress', value: statusData.find((d) => d.label === 'IN_PROGRESS')?.count ?? 0, color: 'text-amber-600', bg: 'bg-amber-50' },
          { label: 'Resolved', value: statusData.find((d) => d.label === 'RESOLVED')?.count ?? 0, color: 'text-green-600', bg: 'bg-green-50' },
        ].map((s) => (
          <div key={s.label} className={`${cardCls} ${s.bg} border-0`}>
            <div className={`text-2xl font-bold ${s.color}`}>{s.value}</div>
            <div className="text-xs font-medium text-slate-500 mt-1">{s.label}</div>
          </div>
        ))}
      </div>

      {/* Charts row 1 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5 mb-5">
        {/* Tickets by status */}
        <div className={cardCls}>
          <p className={sectionTitle}>Tickets by Status</p>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={statusData} barSize={28} margin={{ top: 4, right: 8, left: -20, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="label" tick={{ fontSize: 11, fill: '#64748b' }} />
              <YAxis tick={{ fontSize: 11, fill: '#64748b' }} allowDecimals={false} />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                {statusData.map((entry) => (
                  <Cell key={entry.label} fill={entry.fill} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Tickets by priority */}
        <div className={cardCls}>
          <p className={sectionTitle}>Tickets by Priority</p>
          {priorityData.length === 0 ? (
            <div className="flex items-center justify-center h-[220px] text-sm text-slate-400">No data</div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie
                  data={priorityData}
                  dataKey="count"
                  nameKey="label"
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={3}
                >
                  {priorityData.map((entry) => (
                    <Cell key={entry.label} fill={entry.fill} />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend
                  formatter={(value) => (
                    <span style={{ fontSize: 11, color: '#64748b' }}>{value}</span>
                  )}
                />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* Charts row 2 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* Weekly ticket creation trend */}
        <div className={cardCls}>
          <p className={sectionTitle}>Tickets Created (Last 8 Weeks)</p>
          <ResponsiveContainer width="100%" height={220}>
            <AreaChart data={data?.weeklyCreated ?? []} margin={{ top: 4, right: 8, left: -20, bottom: 0 }}>
              <defs>
                <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366f1" stopOpacity={0.2} />
                  <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="label" tick={{ fontSize: 10, fill: '#64748b' }} />
              <YAxis tick={{ fontSize: 11, fill: '#64748b' }} allowDecimals={false} />
              <Tooltip content={<CustomTooltip />} />
              <Area
                type="monotone"
                dataKey="count"
                stroke="#6366f1"
                strokeWidth={2}
                fill="url(#areaGrad)"
                dot={{ r: 3, fill: '#6366f1' }}
                activeDot={{ r: 5 }}
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Workload by assignee */}
        <div className={cardCls}>
          <p className={sectionTitle}>Workload by Assignee</p>
          {(data?.byAssignee ?? []).length === 0 ? (
            <div className="flex items-center justify-center h-[220px] text-sm text-slate-400">No assigned tickets</div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart
                layout="vertical"
                data={data?.byAssignee ?? []}
                barSize={16}
                margin={{ top: 4, right: 16, left: 8, bottom: 0 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" horizontal={false} />
                <XAxis type="number" tick={{ fontSize: 11, fill: '#64748b' }} allowDecimals={false} />
                <YAxis type="category" dataKey="label" tick={{ fontSize: 11, fill: '#64748b' }} width={80} />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="count" fill={ASSIGNEE_COLOR} radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    </AppShell>
  );
};

export default Analytics;
