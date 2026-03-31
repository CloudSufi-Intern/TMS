/**
 * @description Custom React hook for managing all ticket-related state and logic.
 * Handles ticket listing, real-time search filtering, status filtering,
 * and ticket creation.
 *
 * @author Smriti Bajpai
 */


 /**
  * Executes the API call to create a new ticket in the backend database.
  * * [Ticket Update]: Replaced frontend mock data with a real asynchronous fetch request.
  * Implemented FormData construction to correctly handle text fields and file attachments.
  * Mapped frontend state variables to exactly match backend DTO requirements.
  * * @param {Object} formData - The ticket details from the UI modal
  * @author Priyanshu Gupta
  */
import { useTicketContext } from "../context/TicketContext";
import { useState,useEffect  } from 'react';
import { initialTickets } from '../data/tickets';

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
  const { tickets ,setTickets} = useTicketContext();
  const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    /**
     * Fetches all tickets for the authenticated user from the backend on component mount.
     * Replaces the previous mock data initialization with a real API call.
     * Calls GET /api/tickets/my — returns tickets where user is creator or assignee.
     * [Ticket Update]: Integrated real backend API to replace initialTickets mock data.
     * @author Priyanshu Gupta
     */

  const [refreshTrigger, setRefreshTrigger] = useState(false);

    useEffect(() => {
     // Set loading state before API call
      const fetchTickets = async () => {
        setLoading(true);
        try {
          const token = localStorage.getItem('token');
          const response = await fetch('http://localhost:8080/api/tickets/my', {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to fetch tickets');
          }
          const data = await response.json();
          setTickets(data);
        } catch (err) {
          console.error('Error fetching tickets:', err);
          setError(err.message);
        } finally {
          setLoading(false);
        }
      };

      fetchTickets();
    }, [refreshTrigger]);

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
         ticket.title?.toLowerCase().includes(query) ||
         ticket.description?.toLowerCase().includes(query) ||
         ticket.category?.toLowerCase().includes(query);

    /*
     * Check if the ticket matches the selected status filter.
     * If no filter is selected (empty string), all tickets pass.
     */

    const matchesStatus = !statusFilter ||
      ticket.status?.toLowerCase() === statusFilter.toLowerCase();

    return matchesSearch && matchesStatus;
  });


const stats = {
    open:             tickets.filter((t) => t.status?.toUpperCase() === 'OPEN').length,
    in_progress:      tickets.filter((t) => t.status?.toUpperCase() === 'IN_PROGRESS').length,
    pending_approval: tickets.filter((t) => t.status?.toUpperCase() === 'PENDING_APPROVAL').length,
    resolved:         tickets.filter((t) => t.status?.toUpperCase() === 'RESOLVED').length,
  };

  const createTicket = async ({ title, desc, priority, category, files }) => {
      try {
          const formData = new FormData();
          formData.append('title', title);
          formData.append('description', desc);
          formData.append('priority', priority || 'MEDIUM');

          if (files && files.length > 0) {
              for (let i = 0; i < files.length; i++) {
                  formData.append('attachments', files[i]);
              }
          }
          const token = localStorage.getItem('token');
          const response = await fetch('http://localhost:8080/api/tickets', {
              method: 'POST',
              headers: {
                  'Authorization': `Bearer ${token}`

              },
              body: formData
          });
          if (!response.ok) {
              const errorData = await response.json();
              throw new Error(errorData.message || 'Validation failed or server error');
          }
          const savedTicket = await response.json();
          setRefreshTrigger((prev) => !prev);

      } catch (error) {
          console.error("Error creating ticket:", error);
          throw error;
      }
  };
  return {
     tickets: filteredTickets,
     stats,
     search,
     setSearch,
     statusFilter,
     setStatusFilter,
     createTicket,
     loading,
     error,
    }
  };
