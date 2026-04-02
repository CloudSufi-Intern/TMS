import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../dashboard.css';
import Header from '../components/Header';
import StatsGrid from '../components/StatsGrid';
import Toolbar from '../components/Toolbar';
import TicketTable from '../components/TicketTable';
import CreateTicketModal from '../components/CreateTicketModal';
import Toast from '../components/Toast';
import { useTickets } from '../hooks/useTickets';
import { useToast } from '../hooks/useToast';
import { removeToken } from '../utils/auth';

/**
 * Dashboard page — main ticket management view
 * Composes all sub-components and wires up state
 * @author-Smriti Bajpai
 */

/**
 * Handles the ticket creation workflow from the UI.
 * * [Ticket Update]: Converted to an async function to await the backend API response.
 * This ensures the success Toast is only shown if the database successfully
 * creates the ticket, and properly catches backend validation errors.
 * const handleCreateTicket = async (formData) => {
         try{
             await createTicket(formData);
             showToast('Ticket created successfully!');
             }catch(error){
                 showToast(error.message || 'Failed to create ticket',true);}
     };
 * * @param {Object} formData - The data from the CreateTicketModal
 * @author Priyanshu Gupta
 */

const Dashboard = () => {
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);
  const role = localStorage.getItem('role');
  const { tickets, stats, search, setSearch, statusFilter, setStatusFilter, createTicket } = useTickets();
  const { toast, showToast } = useToast();

  const handleLogout = () => {
      /*
         * Remove token first before navigating.
         * This ensures ProtectedRoute sees no token
         * if user presses back button after logout.
         */
        removeToken();
        showToast('Logged out successfully');
        setTimeout(() => navigate('/login'), 1000);
  };

  const handleCreateTicket = async (formData) => {
      try{
          await createTicket(formData);
          showToast('Ticket created successfully!');
          }catch(error){
              showToast(error.message || 'Failed to create ticket',true);
              }
  };

  const handleTicketClick = (ticket) => {
    navigate(`/tickets/${ticket.id}`);
  };

  return (
    <>
      <Header onLogout={handleLogout} />

      <main className="main">
       <StatsGrid stats={stats} role={role} />

        <Toolbar
          search={search}
           onSearch={setSearch}
           statusFilter={statusFilter}
           onStatusFilter={setStatusFilter}
           onCreateClick={() => setModalOpen(true)}
        />

        <TicketTable
          tickets={tickets}
          onTicketClick={handleTicketClick}
        />
      </main>

      <CreateTicketModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        onSubmit={handleCreateTicket}
        onError={(msg) => showToast(msg, true)}
      />

      <Toast visible={toast.visible} message={toast.message} isError={toast.isError} />
    </>
  );
};

export default Dashboard;

