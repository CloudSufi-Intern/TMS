import { createContext, useContext, useState } from "react";
import { initialTickets } from "../data/tickets";
/**
 * This file creates a global state for managing tickets across the application.
 *
 * Previously, different components (Dashboard & Ticket Details)
 * were using separate states (like initialTickets), causing inconsistency.
 * Updating ticket status in one component did NOT reflect in others.
 *
 * Use React Context API to maintain a single source of truth.
 * All components consume the same ticket state.
 *
 * Stores tickets in global state
 * Provides updateTicketStatus() to modify ticket status
 * Ensures UI updates everywhere automatically
 *
 *@author-Smriti Bajpai
 */
const TicketContext = createContext();

export const TicketProvider = ({ children }) => {
  const [tickets, setTickets] = useState(initialTickets);


/**
 * Updates the status of a specific ticket
 *
 * @param {string|number} id - Unique ID of the ticket
 * @param {string} newStatus - New status (Pending, Resolved, etc.)
 *
 * Iterates over existing tickets
 * Finds the matching ticket using ID
 * Returns a NEW updated object (immutability)
 *
 */
  const updateTicketStatus = (id, newStatus) => {
    setTickets(prev =>
      prev.map(t =>
        String(t.id) === String(id)
          ? { ...t, status: newStatus }
          : t
      )
    );
  };

  return (
    <TicketContext.Provider value={{ tickets, setTickets, updateTicketStatus }}>
      {children}
    </TicketContext.Provider>
  );
};

export const useTicketContext = () => useContext(TicketContext);