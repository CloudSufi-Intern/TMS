/**
 * Token & session utilities.
 *
 * The JWT carries an `exp` claim (seconds since epoch). We decode it locally
 * to detect expiry on app boot and proactively kick the user out, so they
 * don't see a stale dashboard before the first 401 fires.
 */

export const decodeJwt = (token) => {
  try {
    const payload = token.split('.')[1];
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(json);
  } catch {
    return null;
  }
};

export const isTokenExpired = (token) => {
  const payload = decodeJwt(token);
  if (!payload || !payload.exp) return true;
  return payload.exp * 1000 <= Date.now();
};

/**
 * Returns the milliseconds until the token expires, or 0 if already expired.
 */
export const msUntilExpiry = (token) => {
  const payload = decodeJwt(token);
  if (!payload || !payload.exp) return 0;
  return Math.max(0, payload.exp * 1000 - Date.now());
};

export const isLoggedIn = () => {
  const token = localStorage.getItem('token');
  if (!token) return false;
  if (isTokenExpired(token)) {
    removeToken();
    return false;
  }
  return true;
};

export const removeToken = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('tokenType');
  localStorage.removeItem('role');
  localStorage.removeItem('email');
  localStorage.removeItem('userName');
  localStorage.removeItem('sessionExpiry');
};
