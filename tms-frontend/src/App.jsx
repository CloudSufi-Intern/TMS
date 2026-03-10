import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Login from "./pages/Login";
import Signup from "./pages/Signup";

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
      </Routes>
    </BrowserRouter>
  );
}

export default App;
