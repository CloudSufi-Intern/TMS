import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { signUp } from '../services/AuthService';

/**
 * Signup page component
 * Allows new users to create an account via POST /api/auth/signup.
 * Maps the "Full Name" field to the backend's "username" field.
 * Displays meaningful error messages for validation failures and conflicts.
 *
 * @author Vedanshu Garg
 * @author Yashas Yadav (API integration)
 *@author Smriti Bajpai(Changed the form handling)
 * @returns Signup form UI
 */
const Signup = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    role: 'IT',
    phoneNo: ''
  });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const roles = [
    'IT', 'ENGINEERING', 'HR', 'MANAGER', 'LEAD',
    'ARCHITECT', 'DEVOPS', 'DEVSECOPS', 'INTERN'
  ];

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (error) setError('');
  };

  const validateForm = () => {
    /*
     * Validate email format using standard email regex.
     * Checks for: localpart@domain.tld structure.
     */
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      setError("Please enter a valid email address.");
      return false;
    }

    /*
     * Extract only digits from phone number before validating length.
     *
     */
    const digitsOnly = formData.phoneNo.replace(/[\s\-+(). ]*/g, '');

    if (digitsOnly.length < 10) {
      setError("Phone number is too short. Please enter at least 10 digits.");
      return false;
    }

    if (digitsOnly.length > 15) {
      setError("Phone number is too long. Please enter no more than 15 digits.");
      return false;
    }

    if (!/^\d+$/.test(digitsOnly)) {
      setError("Phone number can only contain digits, spaces, +, or dashes.");
      return false;
    }

    setError('');
    return true;
  };

  const handleSignup = async (e) => {
    e.preventDefault();

    if (!validateForm()) return;

    setIsLoading(true);
    setError('');

    try {
      await signUp({
        username: formData.fullName,
        email: formData.email,
        password: formData.password,
        phoneNo: formData.phoneNo,
        role: formData.role,
      });

      // registration successful — redirect to login
      navigate('/login');

    } catch (err) {
      const status = err.response?.status;
      const message = err.response?.data?.message;

      if (status === 409) {
        setError(message || 'An account with this email or username already exists.');
      } else if (status === 400) {
        setError(message || 'Please check your details and try again.');
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
    <div className="bg-[#f4f6fb] min-h-screen flex flex-col font-sans">
      <main className="flex-1 flex items-center justify-center p-4 py-12">
        <div className="bg-white rounded-[20px] shadow-sm p-10 w-full max-w-[480px]">

          <div className="w-16 h-16 bg-[#554af0] rounded-2xl flex items-center justify-center mx-auto mb-6">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>
          </div>

          <h1 className="text-[26px] font-semibold text-center text-gray-900 mb-2 tracking-tight">Ticket Management System</h1>
          <p className="text-center text-gray-500 text-[15px] mb-6">Create your account</p>

          {error && (
            <div className="mb-4 p-3 text-sm text-red-600 bg-red-50 rounded-lg border border-red-100 flex items-start gap-2">
              <svg className="w-4 h-4 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              <span>{error}</span>
            </div>
          )}

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
              <input
                type="text"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="you@company.com"
                className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-1 focus:ring-[#554af0] focus:border-[#554af0] outline-none text-[15px] placeholder-gray-400"
                required
              />
            </div>

            <div>
              <label className="block text-[13px] font-medium text-gray-600 mb-1.5">Phone Number</label>
              <input
                type="tel"
                name="phoneNo"
                value={formData.phoneNo}
                onChange={handleChange}
                placeholder="+1 (555) 000-0000"
                className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-1 focus:ring-[#554af0] focus:border-[#554af0] outline-none text-[15px] placeholder-gray-400"
                required
              />
            </div>

            <div>
              <label className="block text-[13px] font-medium text-gray-600 mb-1.5">Organizational Role</label>
              <select
                name="role"
                value={formData.role}
                onChange={handleChange}
                className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-1 focus:ring-[#554af0] focus:border-[#554af0] outline-none text-[15px] bg-white text-gray-700"
                required
              >
                {roles.map((role) => (
                  <option key={role} value={role}>
                    {role}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-[13px] font-medium text-gray-600 mb-1.5">Password</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="••••••••"
                className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-1 focus:ring-[#554af0] focus:border-[#554af0] outline-none text-[15px] placeholder-gray-400"
                required
              />
            </div>

            <button disabled={isLoading} type="submit" className="w-full bg-[#554af0] hover:bg-[#463bce] disabled:bg-indigo-300 text-white font-medium py-3 rounded-lg mt-2 text-[15px] transition">
              {isLoading ? 'Creating Account...' : 'Sign Up'}
            </button>
          </form>



          <p className="mt-8 text-center text-[14px] text-[#554af0] font-medium">
            <Link to="/login" className="hover:underline">Already have an account? Sign In</Link>
          </p>
        </div>
      </main>
    </div>
  );
}

export default Signup;
