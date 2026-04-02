import {Navigate} from 'react-router-dom';
import {isLoggedIn} from '../utils/auth';

/**
 * ProtectedRoute component — guards routes that require authentication.
 * If user is not logged in, redirects to login page immediately.
 * If user is logged in, renders the requested page normally.
 *
 * Used in App.jsx to wrap any route that needs authentication.
 *
 * @param {JSX.Element} children - The page component to render if authenticated
 * @returns {JSX.Element} The page or a redirect to /login
 * @author-Smriti Bajpai
 */
 const ProtectedRoute =({children})=>{
     if(!isLoggedIn()){
         return <Navigate to="/login" replace/>
         }
     return children;
     }
export default ProtectedRoute;