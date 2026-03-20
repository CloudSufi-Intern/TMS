/**
 * Toast notification component
 * @param {boolean} visible - Controls visibility
 * @param {string} message - Message to display
 * @param {boolean} isError - Error styling toggle
 * @author- Smriti Bajpai
 */
const Toast = ({ visible, message, isError }) => (
  <div className={`toast ${visible ? 'show' : ''} ${isError ? 'error' : ''}`}>
    {!isError && (
      <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
      </svg>
    )}
    <span>{message}</span>
  </div>
);

export default Toast;
