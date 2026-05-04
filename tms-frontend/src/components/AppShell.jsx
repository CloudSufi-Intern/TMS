import { useRef, useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { removeToken } from '../utils/auth';
import { getUserDetails } from '../services/UserService';

const AppShell = ({ children, title, onCreateClick }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const userName = localStorage.getItem('userName') || 'User';
  const role     = localStorage.getItem('role') || '';
  const initial  = userName.charAt(0).toUpperCase();

  const [profileOpen, setProfileOpen]     = useState(false);
  const [userDetails, setUserDetails]     = useState(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const dropdownRef = useRef(null);

  const handleLogout = () => {
    removeToken();
    navigate('/login');
  };

  const handleProfileClick = async () => {
    if (profileOpen) { setProfileOpen(false); return; }
    setProfileLoading(true);
    try {
      const d = await getUserDetails(userName);
      setUserDetails(d);
    } catch { /* non-fatal */ }
    finally { setProfileLoading(false); }
    setProfileOpen(true);
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handler = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setProfileOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      {/* Top header */}
      <header className="sticky top-0 z-20 bg-white border-b border-slate-200 h-14 flex items-center px-6 gap-4">
        {/* Brand */}
        <Link to="/" className="flex items-center gap-2 flex-shrink-0">
          <div className="w-7 h-7 bg-indigo-600 rounded-lg flex items-center justify-center">
            <svg className="w-3.5 h-3.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <span className="text-sm font-semibold text-slate-900 tracking-tight">TMS</span>
        </Link>

        {/* Nav links */}
        <nav className="flex items-center gap-1 ml-1">
          {[
            { label: 'Dashboard', to: '/dashboard' },
            { label: 'Analytics', to: '/analytics' },
          ].map(({ label, to }) => {
            const active = location.pathname === to;
            return (
              <Link
                key={to}
                to={to}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                  active
                    ? 'bg-indigo-50 text-indigo-700'
                    : 'text-slate-500 hover:text-slate-800 hover:bg-slate-100'
                }`}
              >
                {label}
              </Link>
            );
          })}
        </nav>

        {/* Divider + page title */}
        {title && (
          <>
            <span className="text-slate-300 select-none">|</span>
            <span className="text-sm font-medium text-slate-600 truncate">{title}</span>
          </>
        )}

        <div className="flex-1" />

        {/* New Ticket button */}
        {onCreateClick && (
          <button
            onClick={onCreateClick}
            className="flex items-center gap-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold px-4 py-2 rounded-lg transition-colors shadow-sm flex-shrink-0"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
            </svg>
            New Ticket
          </button>
        )}

        {/* User profile */}
        <div className="relative flex-shrink-0" ref={dropdownRef}>
          <button
            onClick={handleProfileClick}
            disabled={profileLoading}
            className="flex items-center gap-2.5 pl-2 pr-3 py-1.5 rounded-lg hover:bg-slate-100 transition-colors"
          >
            <div className="w-7 h-7 rounded-full bg-indigo-600 text-white text-xs font-bold flex items-center justify-center">
              {initial}
            </div>
            <div className="hidden sm:block text-left">
              <div className="text-xs font-semibold text-slate-800 leading-tight">{userName}</div>
              <div className="text-xs text-slate-400 leading-tight">{role}</div>
            </div>
            <svg className="w-3.5 h-3.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
            </svg>
          </button>

          {/* Dropdown */}
          {profileOpen && (
            <div className="absolute right-0 top-full mt-2 w-60 bg-white border border-slate-200 rounded-xl shadow-xl z-30">
              <div className="p-4 border-b border-slate-100">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-indigo-600 text-white text-sm font-bold flex items-center justify-center flex-shrink-0">
                    {initial}
                  </div>
                  <div className="min-w-0">
                    <div className="text-sm font-semibold text-slate-900 truncate">{userDetails?.username || userName}</div>
                    <div className="text-xs text-slate-500">{userDetails?.role || role}</div>
                  </div>
                </div>
              </div>

              {userDetails && (
                <div className="p-3 border-b border-slate-100 space-y-2">
                  {[
                    { label: 'Email',   value: userDetails.email },
                    { label: 'Phone',   value: userDetails.phoneNo || 'N/A' },
                    { label: 'User ID', value: `#${userDetails.id}` },
                  ].map(({ label, value }) => (
                    <div key={label} className="flex justify-between gap-3 text-xs">
                      <span className="text-slate-400">{label}</span>
                      <span className="text-slate-700 font-medium text-right truncate max-w-[160px]">{value}</span>
                    </div>
                  ))}
                </div>
              )}

              <div className="p-2">
                <button
                  onClick={handleLogout}
                  className="w-full flex items-center gap-2 px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                >
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  Sign Out
                </button>
              </div>
            </div>
          )}
        </div>
      </header>

      {/* Page content */}
      <main className="flex-1 p-6">
        {children}
      </main>
    </div>
  );
};

export default AppShell;
