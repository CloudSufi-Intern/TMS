import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';

/**
 * Signup page component
 * Allows new users to create an account
 *
 * @author Vedanshu Garg
 * @returns Signup form UI
 */
const Signup = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSignup = (e) => {
      e.preventDefault();
      console.log("Frontend captured this Signup Data:", formData);
      navigate('/login');
  };

  return (
    <div className="bg-[#f4f6fb] min-h-screen flex flex-col bg-blue-100 font-sans">
      <main className="flex-1 flex items-center justify-center p-4">
        <div className="bg-white rounded-[20px] shadow-sm p-10 w-full max-w-[440px]">

          <div className="w-16 h-16 bg-[#554af0] rounded-2xl flex items-center justify-center mx-auto mb-6">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>
          </div>

          <h1 className="text-[26px] font-semibold text-center text-gray-900 mb-2 tracking-tight">Ticket Management System</h1>
          <p className="text-center text-gray-500 text-[15px] mb-6">Create your account</p>

          {/* Display Backend Errors Here */}
          {error && <div className="mb-4 p-3 text-sm text-red-600 bg-red-50 rounded-lg border border-red-100">{error}</div>}

          <form onSubmit={handleSignup} className="space-y-4">
            <div>
              <label className="block text-[13px] font-medium text-gray-600 mb-1.5">Full Name</label>
              <input
                type="text"
                name="fullName"
                value={formData.fullName}
                onChange={handleChange}
                placeholder="John Doe"
                className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-1 focus:ring-[#554af0] focus:border-[#554af0] outline-none text-[15px] placeholder-gray-400"
                required
              />
            </div>

            <div>
              <label className="block text-[13px] font-medium text-gray-600 mb-1.5">Email Address</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                  <svg className="h-[18px] w-[18px] text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>
                </div>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="you@company.com"
                  className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:ring-1 focus:ring-[#554af0] focus:border-[#554af0] outline-none text-[15px] placeholder-gray-400"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-[13px] font-medium text-gray-600 mb-1.5">Password</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                  <svg className="h-[18px] w-[18px] text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>
                </div>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:ring-1 focus:ring-[#554af0] focus:border-[#554af0] outline-none text-[15px] placeholder-gray-400"
                  required
                />
              </div>
            </div>

            <button disabled={isLoading} type="submit" className="w-full bg-[#554af0] hover:bg-[#463bce] disabled:bg-indigo-300 text-white font-medium py-3 rounded-lg mt-2 text-[15px] transition">
              {isLoading ? 'Creating Account...' : 'Sign Up'}
            </button>
          </form>

          <div className="mt-8 flex items-center justify-center">
            <div className="border-t border-gray-200 w-full"></div>
            <span className="px-4 text-[13px] text-gray-400 bg-white whitespace-nowrap">Or continue with</span>
            <div className="border-t border-gray-200 w-full"></div>
          </div>

          <button className="mt-6 w-full flex justify-center items-center gap-2 bg-white border border-gray-200 hover:bg-gray-50 text-gray-700 font-medium py-3 rounded-lg text-[15px] shadow-sm transition">
            <svg className="w-[18px] h-[18px]" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
            Google
          </button>

          <p className="mt-8 text-center text-[14px] text-[#554af0] font-medium">
            <Link to="/login" className="hover:underline">Already have an account? Sign In</Link>
          </p>
        </div>
      </main>
    </div>
  );
}

export default Signup;
