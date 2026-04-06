import { useState } from 'react';
import { getUserDetails } from '../services/UserService';

/**
 * App header with brand, user profile, and logout
 *@author-Smriti Bajpai
 */
const Header = ({ onLogout }) => {

  const userName = localStorage.getItem("userName");
  const [userDetails, setUserDetails] = useState(null);
  const [isCardVisible, setIsCardVisible] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleUserClick = async () => {
    if (isCardVisible) {
      setIsCardVisible(false);
      return;
    }

    setLoading(true);
    try {
      const details = await getUserDetails(userName);
      setUserDetails(details);
      setIsCardVisible(true);
    } catch (error) {
      console.error("Error fetching user details:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <header className="header">
      <div className="header-brand">
        <h1>Ticket Management</h1>
        <h2>
          Welcome back, {userName || "Employee"}
        </h2>
      </div>

      <div className="header-actions">
        <button className="btn-user" onClick={handleUserClick} disabled={loading}>
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round"
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>

          {loading ? "Loading..." : (userName || "Employee")}
        </button>

        <button className="btn-logout" onClick={onLogout}>
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round"
              d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          Logout
        </button>
      </div>

      {isCardVisible && userDetails && (
        <div className="user-card">
          <div className="user-card-header">
            <div className="user-card-avatar">
              {userDetails.username.charAt(0).toUpperCase()}
            </div>
            <div className="user-card-info">
              <h3>{userDetails.username}</h3>
              <p>{userDetails.role}</p>
            </div>
          </div>
          <div className="user-card-details">
            <div className="user-detail-item">
              <span className="user-detail-label">Email</span>
              <span className="user-detail-value">{userDetails.email}</span>
            </div>
            <div className="user-detail-item">
              <span className="user-detail-label">Phone</span>
              <span className="user-detail-value">{userDetails.phoneNo || 'N/A'}</span>
            </div>
            <div className="user-detail-item">
              <span className="user-detail-label">User ID</span>
              <span className="user-detail-value">#{userDetails.id}</span>
            </div>
          </div>
        </div>
      )}
    </header>
  );
};

export default Header;