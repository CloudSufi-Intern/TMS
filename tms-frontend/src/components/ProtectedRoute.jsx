import { useEffect } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { isLoggedIn, msUntilExpiry, removeToken } from '../utils/auth';

/**
 * Guards routes that require authentication.
 *
 * Schedules a logout timer aligned with the JWT's `exp` claim so the user
 * is redirected to /login?expired=1 exactly when the token expires mid-session.
 */
const ProtectedRoute = ({ children }) => {
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    const ms = msUntilExpiry(token);
    if (ms <= 0) {
      removeToken();
      navigate('/login?expired=1', { replace: true });
      return;
    }
    const timer = setTimeout(() => {
      removeToken();
      navigate('/login?expired=1', { replace: true });
    }, ms);

    return () => clearTimeout(timer);
  }, [navigate]);

  if (!isLoggedIn()) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

export default ProtectedRoute;
