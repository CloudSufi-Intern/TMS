import { useEffect } from 'react';
import { Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import Landing from './pages/Landing';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import TicketDetail from './pages/ticketDetails';
import Analytics from './pages/Analytics';
import ProtectedRoute from './components/ProtectedRoute';
import { removeToken } from './utils/auth';
import { useToast } from './hooks/useToast';
import { useTicketContext } from './context/TicketContext';
import Toast from './components/Toast';
import './dashboard.css'; // For Toast styling

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
        <Route path="/"         element={<Landing />} />
        <Route path="/home"     element={<Landing />} />
        <Route path="/login"    element={<Login />} />
        <Route path="/register" element={<Signup />} />

        <Route path="/dashboard" element={
          <ProtectedRoute><Dashboard /></ProtectedRoute>
        } />
        <Route path="/tickets/:id" element={
          <ProtectedRoute><TicketDetail /></ProtectedRoute>
        } />
        <Route path="/analytics" element={
          <ProtectedRoute><Analytics /></ProtectedRoute>
        } />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <Toast visible={toast.visible} message={toast.message} isError={toast.isError} />
    </>
  );
}

export default App;
