/**
 * @description Custom React hook for managing all ticket-related state and logic.
 * Handles ticket listing, real-time search filtering, status filtering,
 * and ticket creation.
 *
 * @author Smriti Bajpai
 */

import { useState } from 'react';
import { initialTickets } from '../data/tickets';
import { useTicketContext } from "../context/TicketContext";

/**
 * @property {Ticket[]}  tickets        - The filtered list of tickets to display in the UI
 * @property {Object}    stats          - Counts of tickets grouped by status
 * @property {number}    stats.open              - Count of open tickets
 * @property {number}    stats.in_progress       - Count of in-progress tickets
 * @property {number}    stats.pending_approval  - Count of tickets pending approval
 * @property {number}    stats.resolved          - Count of resolved tickets
 * @property {string}    search         - Current search query string
 * @property {Function}  setSearch      - Setter to update the search query
 * @property {string}    statusFilter   - Currently active status filter value
 * @property {Function}  setStatusFilter - Setter to update the status filter
 * @property {Function}  createTicket   - Function to add a new ticket to the list
 */

export const useTickets = () => {

  /**
   * Master list of all tickets in the system.
   * Initialized with mock data.
   */
  const { tickets } = useTicketContext();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  /**
   * Derives the filtered ticket list from the master tickets array.
   * Applies both the search query and the status filter simultaneously.
   * Runs on every render when search or statusFilter values change.
   */
  const filteredTickets = tickets.filter((ticket) => {
    const query = search.toLowerCase();

    /*
     * Check if the ticket matches the search query.
     * If search is empty, all tickets pass this check.
     */
    const matchesSearch =
      !query ||
      ticket.title.toLowerCase().includes(query) ||
      ticket.desc.toLowerCase().includes(query) ||
      ticket.category.toLowerCase().includes(query);

    /*
     * Check if the ticket matches the selected status filter.
     * If no filter is selected (empty string), all tickets pass.
     */
    const matchesStatus = !statusFilter || ticket.status === statusFilter;

    return matchesSearch && matchesStatus;
  });


  const stats = {
    open:             tickets.filter((t) => t.status === 'open').length,
    in_progress:      tickets.filter((t) => t.status === 'in_progress').length,
    pending_approval: tickets.filter((t) => t.status === 'pending_approval').length,
    resolved:         tickets.filter((t) => t.status === 'resolved').length,
  };

  const createTicket = ({ title, desc, priority, category }) => {
    const newTicket = {
      id: Date.now(),       /* Temporary ID , Real one wil be supplied by backend*/
      title,
      desc,
      status: 'open',
      priority,
      category,
      assignedTo: 'Unassigned',
      creator: 'Employee',  /* TODO: replace with auth context user name */
      comments: 0,
      files: 0,
      hasNotif: false,
    };

    setTickets((prev) => [newTicket, ...prev]);
  };

  return {
    tickets: filteredTickets,
    stats,
    search,
    setSearch,
    statusFilter,
    setStatusFilter,
    createTicket,
    };
    };