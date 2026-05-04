import { renderHook, act, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TicketProvider } from '../../context/TicketContext';
import { useTickets } from '../useTickets';

/**
 * Unit tests for the useTickets hook.
 * API calls are mocked; the hook's filtering, stats, and state management are tested directly.
 *
 * @author Yashas Yadav
 */

vi.mock('../../services/TicketService', () => ({
  getMyTickets: vi.fn(),
  raiseTicket: vi.fn(),
}));

import { getMyTickets, raiseTicket } from '../../services/TicketService';

const wrapper = ({ children }) => <TicketProvider>{children}</TicketProvider>;

const mockTickets = [
  {
    id: 1,
    title: 'Fix login bug',
    description: 'Login breaks on mobile',
    status: 'OPEN',
    priority: 'HIGH',
    createdBy: 'yashascs',
    assignedTo: 'anshcs',
  },
  {
    id: 2,
    title: 'Deploy v2.0',
    description: 'Production deployment',
    status: 'IN_PROGRESS',
    priority: 'URGENT',
    createdBy: 'yashascs',
    assignedTo: null,
  },
  {
    id: 3,
    title: 'Security audit',
    description: 'Quarterly review',
    status: 'RESOLVED',
    priority: 'MEDIUM',
    createdBy: 'anshcs',
    assignedTo: 'yashascs',
  },
  {
    id: 4,
    title: 'Update docs',
    description: 'Refresh API documentation',
    status: 'CLOSED',
    priority: 'LOW',
    createdBy: 'anshcs',
    assignedTo: null,
  },
  {
    id: 5,
    title: 'DB migration',
    description: 'Schema changes for v3',
    status: 'ON_HOLD',
    priority: 'HIGH',
    createdBy: 'yashascs',
    assignedTo: null,
  },
];

describe('useTickets', () => {

  beforeEach(() => {
    getMyTickets.mockReset();
    raiseTicket.mockReset();
    localStorage.clear();
    localStorage.setItem('userName', 'anshcs');
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  // Initial fetch

  it('fetches tickets on mount and populates the list', async () => {
    getMyTickets.mockResolvedValueOnce(mockTickets);

    const { result } = renderHook(() => useTickets(), { wrapper });

    expect(result.current.loading).toBe(true);

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.tickets).toHaveLength(5);
    expect(getMyTickets).toHaveBeenCalledTimes(1);
  });

  it('sets error when fetch fails', async () => {
    getMyTickets.mockRejectedValueOnce({ message: 'Network error' });

    const { result } = renderHook(() => useTickets(), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.error).toBe('Network error');
    expect(result.current.tickets).toHaveLength(0);
  });

  // Stats

  it('computes correct stats from ticket list', async () => {
    getMyTickets.mockResolvedValueOnce(mockTickets);

    const { result } = renderHook(() => useTickets(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    const { stats } = result.current;
    expect(stats.open).toBe(1);
    expect(stats.in_progress).toBe(1);
    expect(stats.resolved).toBe(1);
    expect(stats.closed).toBe(1);
    expect(stats.pending_approval).toBe(1);
    // userName is 'anshcs'; assigned tickets: id=3 (assignedTo='yashascs' - not anshcs), id=1 (assignedTo='anshcs' - yes)
    expect(stats.assigned).toBe(1);
  });

  // Search filtering

  it('filters tickets by search query on title', async () => {
    getMyTickets.mockResolvedValueOnce(mockTickets);

    const { result } = renderHook(() => useTickets(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    act(() => {
      result.current.setSearch('login');
    });

    expect(result.current.tickets).toHaveLength(1);
    expect(result.current.tickets[0].title).toBe('Fix login bug');
  });

  it('filters tickets by search query on description', async () => {
    getMyTickets.mockResolvedValueOnce(mockTickets);

    const { result } = renderHook(() => useTickets(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    act(() => {
      result.current.setSearch('quarterly');
    });

    expect(result.current.tickets).toHaveLength(1);
    expect(result.current.tickets[0].id).toBe(3);
  });

  it('search is case-insensitive', async () => {
    getMyTickets.mockResolvedValueOnce(mockTickets);

    const { result } = renderHook(() => useTickets(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    act(() => {
      result.current.setSearch('DEPLOY');
    });

    expect(result.current.tickets).toHaveLength(1);
    expect(result.current.tickets[0].title).toBe('Deploy v2.0');
  });

  it('returns all tickets when search is cleared', async () => {
    getMyTickets.mockResolvedValueOnce(mockTickets);

    const { result } = renderHook(() => useTickets(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    act(() => { result.current.setSearch('login'); });
    expect(result.current.tickets).toHaveLength(1);

    act(() => { result.current.setSearch(''); });
    expect(result.current.tickets).toHaveLength(5);
  });

  // Status filtering

  it('filters by status', async () => {
    getMyTickets.mockResolvedValueOnce(mockTickets);

    const { result } = renderHook(() => useTickets(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    act(() => { result.current.setStatusFilter('open'); });

    expect(result.current.tickets).toHaveLength(1);
    expect(result.current.tickets[0].status).toBe('OPEN');
  });

  it('filters by "assigned" shows only tickets assigned to current user', async () => {
    getMyTickets.mockResolvedValueOnce(mockTickets);

    const { result } = renderHook(() => useTickets(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    act(() => { result.current.setStatusFilter('assigned'); });

    // userName is 'anshcs'; only ticket id=1 is assigned to 'anshcs'
    expect(result.current.tickets).toHaveLength(1);
    expect(result.current.tickets[0].id).toBe(1);
  });

  it('returns all tickets when status filter is cleared', async () => {
    getMyTickets.mockResolvedValueOnce(mockTickets);

    const { result } = renderHook(() => useTickets(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    act(() => { result.current.setStatusFilter('resolved'); });
    expect(result.current.tickets).toHaveLength(1);

    act(() => { result.current.setStatusFilter(''); });
    expect(result.current.tickets).toHaveLength(5);
  });

  // Refresh

  it('re-fetches tickets when refresh() is called', async () => {
    getMyTickets.mockResolvedValue(mockTickets);

    const { result } = renderHook(() => useTickets(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(getMyTickets).toHaveBeenCalledTimes(1);

    act(() => { result.current.refresh(); });

    await waitFor(() => expect(getMyTickets).toHaveBeenCalledTimes(2));
  });

  // createTicket

  it('calls raiseTicket API and then re-fetches', async () => {
    getMyTickets.mockResolvedValue(mockTickets);
    raiseTicket.mockResolvedValueOnce({ id: 6 });

    const { result } = renderHook(() => useTickets(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    const initialCallCount = getMyTickets.mock.calls.length;

    await act(async () => {
      await result.current.createTicket({
        title: 'New ticket',
        desc: 'Description',
        priority: 'LOW',
        files: [],
      });
    });

    expect(raiseTicket).toHaveBeenCalledWith({
      title: 'New ticket',
      description: 'Description',
      priority: 'LOW',
      files: [],
    });
    expect(getMyTickets.mock.calls.length).toBeGreaterThan(initialCallCount);
  });
});
