import { createContext, useContext, useState } from 'react';

/**
 * Global ticket state. Keeps the master list in one place so the dashboard,
 * filters, and any future views all read from a single source of truth.
 *
 * Initial state is an EMPTY array — real data is loaded by the useTickets
 * hook on mount via the backend API. The previous mock seed is gone.
 */
const TicketContext = createContext();

export const TicketProvider = ({ children }) => {
  const [tickets, setTickets] = useState([]);

  return (
    <TicketContext.Provider value={{ tickets, setTickets }}>
      {children}
    </TicketContext.Provider>
  );
};

export const useTicketContext = () => useContext(TicketContext);
