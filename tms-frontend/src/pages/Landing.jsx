import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { isLoggedIn } from '../utils/auth';

/* ─── Data ─────────────────────────────────────────────────────────────── */

const capabilities = [
  {
    icon: (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
      </svg>
    ),
    title: 'Unified Ticket Lifecycle',
    desc: 'Track every issue from initial submission through assignment, escalation, resolution, and formal closure — all in one place.',
  },
  {
    icon: (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
      </svg>
    ),
    title: 'Role-Based Access Control',
    desc: 'Nine distinct roles — from IT administrators to interns — each with precisely scoped permissions and ticket visibility.',
  },
  {
    icon: (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
    title: 'Immutable Audit Trail',
    desc: 'Every action — status changes, reassignments, comments, and file uploads — is timestamped and permanently recorded.',
  },
  {
    icon: (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
      </svg>
    ),
    title: 'Agent Assignment',
    desc: 'IT administrators can assign or reassign any ticket to any team member, including self-assignment with a single click.',
  },
  {
    icon: (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
      </svg>
    ),
    title: 'Automated Notifications',
    desc: 'Ticket creators and assignees receive email updates on every status transition — no manual follow-up required.',
  },
  {
    icon: (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
      </svg>
    ),
    title: 'Attachments and Comments',
    desc: 'Attach files to tickets and comments, mention team members with @username, and keep all context in one thread.',
  },
  {
    icon: (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
    ),
    title: 'Role-Based Analytics',
    desc: 'Interactive charts for ticket volume by status, priority, assignee workload, and weekly trends — scoped to your role automatically.',
  },
];

const steps = [
  {
    number: '01',
    title: 'Submit a Ticket',
    desc: 'Any team member can raise a ticket with a title, description, priority level, and file attachments. It lands immediately in the queue.',
  },
  {
    number: '02',
    title: 'Assign and Triage',
    desc: 'IT administrators review incoming tickets, set priority, and assign them to the appropriate agent. Status moves to In Progress.',
  },
  {
    number: '03',
    title: 'Resolve and Close',
    desc: 'The assignee works through the issue, communicates via comments, and marks it Resolved. The creator can confirm and close it.',
  },
  {
    number: '04',
    title: 'Review Analytics',
    desc: 'Track ticket trends, workload distribution, and priority breakdowns over time — with data scoped automatically to your role.',
  },
];

const allRoles = [
  { name: 'IT',          desc: 'Full administrative access across all tickets and users' },
  { name: 'Engineering', desc: 'Raise and track software and infrastructure issues' },
  { name: 'HR',          desc: 'Manage HR-related support requests and escalations' },
  { name: 'Manager',     desc: 'Approve, monitor, and escalate tickets across teams' },
  { name: 'Lead',        desc: 'Oversee team tickets and coordinate technical resolutions' },
  { name: 'Architect',   desc: 'Submit architecture decisions and design-level issues' },
  { name: 'DevOps',      desc: 'Handle CI/CD, infrastructure, and deployment incidents' },
  { name: 'DevSecOps',   desc: 'Track security vulnerabilities and compliance issues' },
  { name: 'Intern',      desc: 'Submit and monitor your own support requests' },
];

/* ─── Product UI mockup ─────────────────────────────────────────────────── */
const previewTickets = [
  { title: 'VPN gateway unreachable from remote offices',  status: 'Open',        sBg: 'bg-blue-50 text-blue-700 border border-blue-200',     pBg: 'bg-orange-50 text-orange-700 border border-orange-200', priority: 'High',   dot: 'bg-blue-400' },
  { title: 'Database backup job failed on prod server',     status: 'In Progress', sBg: 'bg-amber-50 text-amber-700 border border-amber-200',   pBg: 'bg-red-50 text-red-700 border border-red-200',          priority: 'Urgent', dot: 'bg-amber-400' },
  { title: 'SSL certificate expiry on staging environment', status: 'On Hold',     sBg: 'bg-orange-50 text-orange-700 border border-orange-200', pBg: 'bg-blue-50 text-blue-700 border border-blue-200',       priority: 'Medium', dot: 'bg-orange-400' },
  { title: 'Email routing misconfiguration — Exchange',     status: 'Resolved',    sBg: 'bg-green-50 text-green-700 border border-green-200',   pBg: 'bg-slate-100 text-slate-600 border border-slate-200',   priority: 'Low',    dot: 'bg-green-400' },
];

const DashboardPreview = () => (
  <div className="bg-white rounded-xl border border-slate-200 shadow-xl overflow-hidden text-left select-none">
    {/* Header */}
    <div className="border-b border-slate-200 px-4 h-11 flex items-center gap-2.5 bg-white">
      <div className="w-5 h-5 bg-indigo-600 rounded flex items-center justify-center flex-shrink-0">
        <svg className="w-2.5 h-2.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      </div>
      <span className="text-xs font-bold text-slate-900">TMS</span>
      <span className="text-xs font-semibold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded">Dashboard</span>
      <span className="text-xs font-semibold text-slate-400 px-2 py-0.5 rounded">Analytics</span>
      <span className="text-slate-300 text-xs">|</span>
      <span className="text-xs text-slate-500 font-medium">Dashboard</span>
      <div className="flex-1" />
      <div className="h-6 px-2.5 bg-indigo-600 rounded text-white text-xs font-semibold flex items-center gap-1">
        <span>+</span><span>New Ticket</span>
      </div>
      <div className="w-6 h-6 rounded-full bg-indigo-100 text-indigo-700 text-xs font-bold flex items-center justify-center ml-1">A</div>
    </div>

    {/* Stat cards */}
    <div className="grid grid-cols-4 gap-2 p-3 bg-slate-50 border-b border-slate-200">
      {[
        { label: 'All',         bg: 'bg-indigo-100' },
        { label: 'Open',        bg: 'bg-blue-100' },
        { label: 'In Progress', bg: 'bg-amber-100' },
        { label: 'Resolved',    bg: 'bg-green-100' },
      ].map(c => (
        <div key={c.label} className="bg-white rounded-lg border border-slate-200 p-2.5">
          <div className={`w-5 h-5 rounded mb-2 ${c.bg}`} />
          <div className="h-3 bg-slate-100 rounded w-5 mb-1.5" />
          <div className="text-xs text-slate-400 truncate">{c.label}</div>
        </div>
      ))}
    </div>

    {/* Toolbar skeleton */}
    <div className="flex items-center gap-2 px-3 py-2.5 border-b border-slate-200 bg-white">
      <div className="flex-1 h-7 bg-slate-100 rounded-lg" />
      <div className="w-24 h-7 bg-slate-100 rounded-lg" />
      <div className="w-24 h-7 bg-slate-100 rounded-lg" />
    </div>

    {/* Table header */}
    <div className="grid grid-cols-[1fr_80px_70px] gap-3 px-3 py-2 bg-slate-50 border-b border-slate-200">
      <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Ticket</span>
      <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Status</span>
      <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Priority</span>
    </div>

    {/* Ticket rows */}
    <div className="divide-y divide-slate-100">
      {previewTickets.map((t, i) => (
        <div key={i} className="grid grid-cols-[1fr_80px_70px] gap-3 items-center px-3 py-2.5">
          <div className="flex items-center gap-2 min-w-0">
            <span className={`w-2 h-2 rounded-full flex-shrink-0 ${t.dot}`} />
            <span className="text-xs text-slate-700 truncate font-medium">{t.title}</span>
          </div>
          <span className={`text-xs font-semibold px-1.5 py-0.5 rounded-full ${t.sBg}`}>{t.status}</span>
          <span className={`text-xs font-semibold px-1.5 py-0.5 rounded-full ${t.pBg}`}>{t.priority}</span>
        </div>
      ))}
    </div>
  </div>
);

/* ─── Page ──────────────────────────────────────────────────────────────── */
const Landing = () => {
  const navigate = useNavigate();
  useEffect(() => {
    if (isLoggedIn()) navigate('/dashboard', { replace: true });
  }, [navigate]);

  return (
    <div className="min-h-screen bg-white flex flex-col text-slate-900">

      {/* ── Navbar ── */}
      <nav className="sticky top-0 z-30 bg-white/95 backdrop-blur-sm border-b border-slate-200">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center">
              <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <span className="text-sm font-bold text-slate-900 tracking-tight">Ticket Management System</span>
          </div>
          <div className="flex items-center gap-2">
            <Link to="/login" className="text-sm font-medium text-slate-600 hover:text-slate-900 px-4 py-2 rounded-lg hover:bg-slate-50 transition-colors">
              Sign In
            </Link>
            <Link to="/register" className="text-sm font-semibold bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg transition-colors shadow-sm">
              Get Started
            </Link>
          </div>
        </div>
      </nav>

      {/* ── Hero ── */}
      <section className="bg-white border-b border-slate-200">
        <div className="max-w-6xl mx-auto px-6 py-16 grid lg:grid-cols-2 gap-14 items-center">
          <div>
            <div className="inline-flex items-center text-xs font-semibold text-indigo-700 bg-indigo-50 border border-indigo-200 px-3 py-1.5 rounded-full uppercase tracking-wider mb-6">
              Enterprise IT Support Platform
            </div>
            <h1 className="text-4xl lg:text-5xl font-bold text-slate-900 leading-tight tracking-tight mb-5">
              IT support that scales<br />with your team
            </h1>
            <p className="text-base text-slate-500 leading-relaxed mb-8 max-w-md">
              A structured ticket management system for engineering organizations.
              Submit, assign, track, and resolve issues with a complete audit trail, role-based access control, and built-in analytics.
            </p>
            <div className="flex flex-wrap gap-3 mb-8">
              <Link to="/register" className="bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-6 py-2.5 rounded-lg transition-colors text-sm shadow-sm">
                Create Account
              </Link>
              <Link to="/login" className="border border-slate-300 hover:border-indigo-300 bg-white text-slate-700 hover:text-indigo-700 font-semibold px-6 py-2.5 rounded-lg transition-colors text-sm">
                Sign In
              </Link>
            </div>
            <div className="flex flex-wrap gap-2 items-center">
              <span className="text-xs text-slate-400 font-medium">Statuses:</span>
              {[
                { label: 'Open',        cls: 'bg-blue-50 text-blue-700 border-blue-200' },
                { label: 'In Progress', cls: 'bg-amber-50 text-amber-700 border-amber-200' },
                { label: 'On Hold',     cls: 'bg-orange-50 text-orange-700 border-orange-200' },
                { label: 'Resolved',    cls: 'bg-green-50 text-green-700 border-green-200' },
                { label: 'Closed',      cls: 'bg-slate-100 text-slate-600 border-slate-300' },
              ].map(s => (
                <span key={s.label} className={`text-xs font-semibold px-2.5 py-1 rounded-full border ${s.cls}`}>{s.label}</span>
              ))}
            </div>
          </div>

          <div className="relative">
            <div className="absolute -inset-3 bg-indigo-50 rounded-2xl border border-indigo-100 -z-10" />
            <DashboardPreview />
          </div>
        </div>
      </section>

      {/* ── How it works ── */}
      <section className="bg-slate-50 border-b border-slate-200">
        <div className="max-w-6xl mx-auto px-6 py-16">
          <div className="text-center mb-12">
            <h2 className="text-2xl font-bold text-slate-900 mb-3 tracking-tight">How it works</h2>
            <p className="text-slate-500 text-sm max-w-md mx-auto leading-relaxed">A structured three-step process that keeps every issue visible, assigned, and actioned.</p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
            {steps.map((s) => (
              <div key={s.number} className="bg-white rounded-xl border border-slate-200 p-6">
                <div className="w-9 h-9 rounded-lg bg-indigo-600 text-white text-sm font-bold flex items-center justify-center mb-4">
                  {s.number}
                </div>
                <h3 className="font-semibold text-slate-900 mb-2 text-sm">{s.title}</h3>
                <p className="text-sm text-slate-500 leading-relaxed">{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Capabilities ── */}
      <section className="bg-white border-b border-slate-200">
        <div className="max-w-6xl mx-auto px-6 py-16">
          <div className="text-center mb-12">
            <h2 className="text-2xl font-bold text-slate-900 mb-3 tracking-tight">Platform capabilities</h2>
            <p className="text-slate-500 text-sm max-w-md mx-auto leading-relaxed">Everything required to manage support operations at the team and organization level.</p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            {capabilities.map((c) => (
              <div key={c.title} className="bg-white border border-slate-200 rounded-xl p-5 hover:border-indigo-300 hover:shadow-sm transition-all">
                <div className="w-9 h-9 bg-indigo-50 text-indigo-600 rounded-lg flex items-center justify-center mb-4 border border-indigo-100">
                  {c.icon}
                </div>
                <h3 className="font-semibold text-slate-900 mb-1.5 text-sm">{c.title}</h3>
                <p className="text-sm text-slate-500 leading-relaxed">{c.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Roles ── */}
      <section className="bg-slate-50 border-b border-slate-200">
        <div className="max-w-6xl mx-auto px-6 py-16">
          <div className="text-center mb-12">
            <h2 className="text-2xl font-bold text-slate-900 mb-3 tracking-tight">Nine roles, one platform</h2>
            <p className="text-slate-500 text-sm max-w-md mx-auto leading-relaxed">Access and permissions are defined per role at registration. Everyone sees exactly what they need to do their job.</p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {allRoles.map((r) => (
              <div key={r.name} className="bg-white rounded-xl border border-slate-200 p-4 flex items-start gap-3 hover:border-indigo-300 hover:shadow-sm transition-all">
                <div className="w-8 h-8 rounded-lg bg-indigo-50 border border-indigo-100 text-indigo-600 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <div>
                  <div className="text-sm font-semibold text-slate-900">{r.name}</div>
                  <div className="text-xs text-slate-500 mt-0.5 leading-relaxed">{r.desc}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── CTA ── */}
      <section className="bg-indigo-600">
        <div className="max-w-6xl mx-auto px-6 py-14 flex flex-col sm:flex-row items-center justify-between gap-6">
          <div>
            <h2 className="text-xl font-bold text-white mb-1">Ready to get started?</h2>
            <p className="text-indigo-200 text-sm">Create an account and your team can start submitting tickets immediately.</p>
          </div>
          <div className="flex gap-3 flex-shrink-0">
            <Link to="/register" className="bg-white text-indigo-600 font-semibold px-6 py-2.5 rounded-lg hover:bg-indigo-50 transition-colors text-sm shadow-sm">
              Create Account
            </Link>
            <Link to="/login" className="border border-indigo-400 hover:border-white text-white font-semibold px-6 py-2.5 rounded-lg hover:bg-indigo-500 transition-colors text-sm">
              Sign In
            </Link>
          </div>
        </div>
      </section>

      {/* ── Footer ── */}
      <footer className="bg-white border-t border-slate-200">
        <div className="max-w-6xl mx-auto px-6 py-7 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 bg-indigo-600 rounded flex items-center justify-center">
              <svg className="w-3 h-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <span className="text-sm font-bold text-slate-900">Ticket Management System</span>
          </div>
          <p className="text-xs text-slate-400">Enterprise IT Support Platform</p>
        </div>
      </footer>
    </div>
  );
};

export default Landing;
