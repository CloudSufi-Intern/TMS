import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Dashboard from "./pages/Dashboard";
import TicketDetail from "./pages/ticketDetails";

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
          <Route path="/" element={<Signup />} />
          <Route path="/login" element={<Login />} />
          <Route path="/dashboard" element={<Dashboard/>}/>
          <Route path="/tickets/:id" element={<TicketDetail />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
