import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signUp } from '../services/AuthService';
import { isLoggedIn } from '../utils/auth';

const roles = ['IT', 'ENGINEERING', 'HR', 'MANAGER', 'LEAD', 'ARCHITECT', 'DEVOPS', 'DEVSECOPS', 'INTERN'];

const Signup = () => {
  const navigate = useNavigate();

  useEffect(() => {
    if (isLoggedIn()) navigate('/dashboard', { replace: true });
  }, [navigate]);

  const [formData, setFormData] = useState({
    fullName: '', email: '', password: '', role: 'IT', phoneNo: '',
  });
  const [error, setError]     = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (error) setError('');
  };

  const validate = () => {
    if (!formData.fullName.trim()) { setError('Full name is required.'); return false; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) { setError('Please enter a valid email address.'); return false; }
    const digits = formData.phoneNo.replace(/[\s\-+(). ]*/g, '');
    if (digits.length < 10) { setError('Phone number must be at least 10 digits.'); return false; }
    if (digits.length > 15) { setError('Phone number must be no more than 15 digits.'); return false; }
    if (!/^\d+$/.test(digits)) { setError('Phone number may only contain digits, spaces, +, or dashes.'); return false; }
    if (!formData.password)    { setError('Password is required.'); return false; }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setIsLoading(true);
    setError('');
    try {
      await signUp({
        username: formData.fullName,
        email:    formData.email,
        password: formData.password,
        phoneNo:  formData.phoneNo,
        role:     formData.role,
      });
      navigate('/login');
    } catch (err) {
      const status  = err.response?.status;
      const message = err.response?.data?.message;
      if (status === 409)    setError(message || 'An account with this email or username already exists.');
      else if (status === 400) setError(message || 'Please check your details and try again.');
      else if (err.request)  setError('Unable to connect to the server. Please check your connection.');
      else                   setError('Something went wrong. Please try again later.');
    } finally {
      setIsLoading(false);
    }
  };

  const inputCls = 'w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm text-slate-900 placeholder-slate-400 outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors bg-white';
  const labelCls = 'block text-xs font-semibold text-slate-700 mb-1.5 uppercase tracking-wide';

  return (
    <div className="min-h-screen flex bg-slate-50">
      {/* Left panel */}
      <div className="hidden lg:flex lg:w-1/2 bg-indigo-600 flex-col justify-between p-12">
        <Link to="/home" className="flex items-center gap-2.5">
          <div className="w-8 h-8 bg-white/20 rounded-lg flex items-center justify-center">
            <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <span className="text-white font-semibold text-sm">Ticket Management System</span>
        </Link>

        <div>
          <h2 className="text-3xl font-bold text-white leading-tight mb-4">
            Resolve faster,<br />track everything.
          </h2>
          <p className="text-indigo-200 text-sm leading-relaxed max-w-sm">
            A centralized platform for IT support ticket management with full audit trails, role-based access, and automated notifications.
          </p>

          <div className="mt-10 space-y-4">
            {[
              'Real-time status tracking across all teams',
              'Priority triage — Low to Urgent',
              'Automatic email notifications on updates',
            ].map(item => (
              <div key={item} className="flex items-start gap-3">
                <div className="w-5 h-5 rounded-full bg-white/20 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <svg className="w-3 h-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <span className="text-indigo-100 text-sm">{item}</span>
              </div>
            ))}
          </div>
        </div>

        <p className="text-indigo-300 text-xs">Enterprise IT Support Platform</p>
      </div>

      {/* Right panel */}
      <div className="flex-1 flex items-center justify-center p-8 overflow-y-auto">
        <div className="w-full max-w-sm py-8">
          <div className="mb-8">
            <Link to="/home" className="flex items-center gap-2 mb-8 lg:hidden">
              <div className="w-7 h-7 bg-indigo-600 rounded-lg flex items-center justify-center">
                <svg className="w-3.5 h-3.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <span className="text-sm font-semibold text-slate-900">TMS</span>
            </Link>
            <h1 className="text-2xl font-bold text-slate-900 mb-1">Create your account</h1>
            <p className="text-sm text-slate-500">Fill in your details to get started.</p>
          </div>

          {error && (
            <div className="mb-5 flex items-start gap-2.5 p-3.5 bg-red-50 border border-red-200 rounded-lg">
              <svg className="w-4 h-4 text-red-500 mt-0.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="text-sm text-red-600">{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className={labelCls}>Full Name</label>
              <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} placeholder="Jane Smith" required className={inputCls} />
            </div>
            <div>
              <label className={labelCls}>Email Address</label>
              <input type="text" name="email" value={formData.email} onChange={handleChange} placeholder="you@company.com" required className={inputCls} />
            </div>
            <div>
              <label className={labelCls}>Phone Number</label>
              <input type="tel" name="phoneNo" value={formData.phoneNo} onChange={handleChange} placeholder="+1 (555) 000-0000" required className={inputCls} />
            </div>
            <div>
              <label className={labelCls}>Organizational Role</label>
              <select name="role" value={formData.role} onChange={handleChange} required
                className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm text-slate-900 outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors bg-white"
              >
                {roles.map(r => {
                  const acronyms = new Set(['IT', 'HR']);
                  const label = acronyms.has(r) ? r : r.charAt(0) + r.slice(1).toLowerCase();
                  return <option key={r} value={r}>{label}</option>;
                })}
              </select>
            </div>
            <div>
              <label className={labelCls}>Password</label>
              <input type="password" name="password" value={formData.password} onChange={handleChange} placeholder="••••••••" required className={inputCls} />
            </div>
            <button type="submit" disabled={isLoading}
              className="w-full bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-semibold py-2.5 rounded-lg text-sm transition-colors mt-1 shadow-sm"
            >
              {isLoading ? 'Creating Account...' : 'Create Account'}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-slate-500">
            Already have an account?{' '}
            <Link to="/login" className="text-indigo-600 font-semibold hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Signup;
