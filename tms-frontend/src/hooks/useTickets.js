import { useEffect, useState } from 'react';
import { useTicketContext } from '../context/TicketContext';
import { getMyTickets, raiseTicket as apiRaiseTicket } from '../services/TicketService';

/**
 * Custom hook for ticket list state.
 *
 * - Initial fetch on mount.
 * - Re-fetches when sort options change.
 * - Re-fetches after a ticket is created.
 * - Search and status filter run client-side over the fetched list.
 */
export const useTickets = () => {
  const { tickets, setTickets } = useTicketContext();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  useEffect(() => {
    let cancelled = false;
    const fetchTickets = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await getMyTickets({ sortBy, sortDir });
        if (!cancelled) setTickets(data);
      } catch (err) {
        if (!cancelled) setError(err.response?.data?.message || err.message || 'Failed to fetch tickets');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    fetchTickets();
    return () => { cancelled = true; };
  }, [refreshTrigger, sortBy, sortDir, setTickets]);

  const currentUserName = localStorage.getItem('userName');

  const filteredTickets = tickets.filter((ticket) => {
    const query = search.toLowerCase();
    const matchesSearch =
      !query ||
      ticket.title?.toLowerCase().includes(query) ||
      ticket.description?.toLowerCase().includes(query);
    const matchesStatus = (() => {
      if (!statusFilter) return true;
      if (statusFilter === 'assigned') return ticket.assignedTo === currentUserName;
      return ticket.status?.toLowerCase() === statusFilter.toLowerCase();
    })();
    return matchesSearch && matchesStatus;
  });
  const stats = {
    open: tickets.filter((t) => t.status?.toUpperCase() === 'OPEN').length,
    in_progress: tickets.filter((t) => t.status?.toUpperCase() === 'IN_PROGRESS').length,
    pending_approval: tickets.filter((t) => t.status?.toUpperCase() === 'ON_HOLD').length,
    resolved: tickets.filter((t) => t.status?.toUpperCase() === 'RESOLVED').length,
    closed: tickets.filter((t) => t.status?.toUpperCase() === 'CLOSED').length,
    assigned: tickets.filter((t) => t.assignedTo === currentUserName).length,
  };

  const createTicket = async ({ title, desc, priority, files }) => {
    await apiRaiseTicket({ title, description: desc, priority, files });
    setRefreshTrigger((n) => n + 1);
  };

  return {
    tickets: filteredTickets,
    stats,
    search,
    setSearch,
    statusFilter,
    setStatusFilter,
    sortBy,
    setSortBy,
    sortDir,
    setSortDir,
    createTicket,
    loading,
    error,
    refresh: () => setRefreshTrigger((n) => n + 1),
  };
};
