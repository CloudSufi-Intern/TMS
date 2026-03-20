/**
 * Badge component for status and priority labels
 * @param {string} type - The badge value (open, resolved, high, etc.)
 * @param {string} label - Optional display label override
 *
 *@Author-Smriti Bajpai
 */
const Badge = ({ type, label }) => {
  const display = label || type?.replace('_', ' ');
  return (
    <span className={`badge ${type}`}>{display}</span>
  );
};

export default Badge;
