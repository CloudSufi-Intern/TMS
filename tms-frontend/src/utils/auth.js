/**
 * Utility functions for JWT token management.
 * Centralizes all localStorage token operations so they
 * are consistent across the entire frontend.
 *
 * @author Smriti Bajpai
 */

/**
 * Checks if a user is currently logged in by verifying
 * that a token exists in localStorage.
 * @returns {boolean} true if token exists, false otherwise
 */
 export const isLoggedIn = () => {
   /*
    * localStorage.getItem returns null if key does not exist.
    * We check for null, undefined and empty string to cover
    * all cases where the user is considered logged out.
    */
   const token = localStorage.getItem('token');
   return token !== null && token !== undefined && token !== '';
 };

 /**
  * Removes the JWT token from localStorage on logout.
  * After this call the user is considered logged out.
  */
 export const removeToken = () => {
   localStorage.removeItem('token');
   localStorage.removeItem('tokenType');
 };