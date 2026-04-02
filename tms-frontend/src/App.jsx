import { BrowserRouter, Routes, Route , Navigate } from 'react-router-dom'
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Dashboard from "./pages/Dashboard";
import TicketDetail from "./pages/ticketDetails";
import ProtectedRoute from './components/ProtectedRoute';

/**
 * Root component handling application routing
 *
 * @author Vedanshu Garg
 * @returns Application routes
 */
function App() {
  return (
    <BrowserRouter>
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
    </BrowserRouter>
  );
}

export default App;