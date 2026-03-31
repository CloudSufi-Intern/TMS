import { useState } from "react";
import { Link, useNavigate } from 'react-router-dom';
import { login } from '../services/AuthService';

/**
 * Login page component
 * Handles user authentication via POST /api/auth/login.
 * On success, stores the JWT token in localStorage and redirects to dashboard.
 * Displays meaningful error messages for invalid credentials or server errors.
 *
 * @author Vedanshu Garg
 * @author Yashas Yadav (API integration)
 * @returns Login form UI
 */
const Login = () => {
  const navigate = useNavigate();

  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    setCredentials({ ...credentials, [e.target.name]: e.target.value });
    if (error) setError('');
  };

  /**
   * Validates login form fields before making the API call.
   * Runs client-side checks so we avoid unnecessary API calls
   * for obviously invalid inputs like empty fields or bad email format.
   *
   * @returns {boolean} true if all fields are valid, false otherwise
   */
  const validateForm = () => {
    /*
     * Check email is not empty before testing format.
     * trim() ensures whitespace-only input is treated as empty.
     */
    if (!credentials.email.trim()) {
      setError('Please enter your email address.');
      return false;
    }

    /*
     * Validate email format using standard email regex.
     * Checks for: localpart@domain.tld structure.
     *
     */
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(credentials.email)) {
      setError('Please enter a valid email address.');
      return false;
    }

    /*
     * Check password is not empty.
     *
     */
    if (!credentials.password.trim()) {
      setError('Please enter your password.');
      return false;
    }

    setError('');
    return true;
  };

  /**
   * Handles login form submission.
   * Runs frontend validation first, then calls the backend API.
   * Stores JWT token in localStorage on success.
   */
  const handleLogin = async (e) => {
    e.preventDefault();

    /* Run frontend validation before hitting the API */
    if (!validateForm()) return;

    setIsLoading(true);
    setError('');

    try {
      const response = await login(credentials);

      // store JWT token for use in secured API calls
     localStorage.setItem('token', response.data.token);
     localStorage.setItem('tokenType', response.data.tokenType);
     localStorage.setItem('role', response.data.role);

      localStorage.setItem("userName", response.data.user.username);

      navigate('/dashboard');

    } catch (err) {
      const status = err.response?.status;
      const message = err.response?.data?.message;

      if (status === 401) {
        setError('Invalid email or password. Please try again.');
      } else if (status === 400) {
        setError(message || 'Please fill in all required fields.');
      } else if (err.request) {
        setError('Unable to connect to the server. Please check your connection.');
      } else {
        setError('Something went wrong. Please try again later.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="bg-[#f4f6fb] min-h-screen flex flex-col bg-blue-100 font-sans">
      <main className="flex-1 flex items-center justify-center p-4">
        <div className="bg-white rounded-[20px] shadow-sm p-10 w-full max-w-[440px]">

          <div className="w-16 h-16 bg-[#554af0] rounded-2xl flex items-center justify-center mx-auto mb-6">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>
          </div>

          <h1 className="text-[26px] font-semibold text-center text-gray-900 mb-2 tracking-tight">Ticket Management System</h1>
          <p className="text-center text-gray-500 text-[15px] mb-6">Welcome back</p>

          {/* Display Login Errors */}
          {error && (
            <div className="mb-4 p-3 text-sm text-red-600 bg-red-50 rounded-lg border border-red-100 flex items-start gap-2">
              <svg className="w-4 h-4 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-[13px] font-medium text-gray-600 mb-1.5">Email Address</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                  <svg className="h-[18px] w-[18px] text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>
                </div>
                <input
                  type="text"
                  name="email"
                  value={credentials.email}
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
                  value={credentials.password}
                  onChange={handleChange}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:ring-1 focus:ring-[#554af0] focus:border-[#554af0] outline-none text-[15px] placeholder-gray-400"
                  required
                />
              </div>
            </div>

            <button disabled={isLoading} type="submit" className="w-full bg-[#554af0] hover:bg-[#463bce] disabled:bg-indigo-300 text-white font-medium py-3 rounded-lg mt-2 text-[15px] transition">
              {isLoading ? 'Signing In...' : 'Sign In'}
            </button>
          </form>



          <p className="mt-8 text-center text-[14px] text-[#554af0] font-medium">
            <Link to="/" className="hover:underline">Don't have an account? Sign Up</Link>
          </p>
        </div>
      </main>
    </div>
  );
}

export default Login;