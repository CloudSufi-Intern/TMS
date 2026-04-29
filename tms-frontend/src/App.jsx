import { useEffect } from 'react';
import { Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Dashboard from "./pages/Dashboard";
import TicketDetail from "./pages/ticketDetails";
import ProtectedRoute from './components/ProtectedRoute';
import { removeToken } from './utils/auth';
import { useToast } from './hooks/useToast';
import { useTicketContext } from './context/TicketContext';
import Toast from './components/Toast';
import './dashboard.css'; // For Toast styling

/**
 * Root component handling application routing
 *
 * @author Vedanshu Garg
 * @returns Application routes
 */
function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const { toast, showToast } = useToast();
  const { setTickets } = useTicketContext();

  useEffect(() => {
    let timer;
    
    const checkSession = () => {
      const token = localStorage.getItem('token');
      if (token) {
        const expiry = localStorage.getItem('sessionExpiry');
        if (expiry) {
          const timeLeft = parseInt(expiry) - Date.now();
          
          if (timeLeft <= 0) {
            handleSessionExpired();
          } else {
            // Set a timer to logout when session expires
            timer = setTimeout(handleSessionExpired, timeLeft);
          }
        }
      }
    };

    const handleSessionExpired = () => {
      const currentTheme = localStorage.getItem('theme');
      
      // Clear all storage
      localStorage.clear(); 
      
      // Restore theme
      if (currentTheme) localStorage.setItem('theme', currentTheme);
      
      // Clear token via utility
      removeToken();
      
      // Reset React State
      if (setTickets) setTickets([]);
      
      // Set a flag in sessionStorage to notify Login page
      sessionStorage.setItem('sessionExpired', 'true');
      // Redirect to login
      navigate('/login');
    };

    checkSession();

    return () => {
      if (timer) clearTimeout(timer);
    };
  }, [location.pathname, navigate, setTickets]);

  return (
    <>
      <Routes>
          {/* Public routes — accessible without login */}
                  <Route path="/"      element={<Signup />} />
                  <Route path="/login" element={<Login />} />

                  {/* Protected routes — redirect to /login if not authenticated */}
                  <Route path="/dashboard" element={
                    <ProtectedRoute>
                      <Dashboard />
                    </ProtectedRoute>
                  } />
                  <Route path="/tickets/:id" element={
                    <ProtectedRoute>
                      <TicketDetail />
                    </ProtectedRoute>
                  } />

                  {/* redirect unknown URLs to login */}
                  <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
      <Toast visible={toast.visible} message={toast.message} isError={toast.isError} />
    </>
  );
}

export default App;