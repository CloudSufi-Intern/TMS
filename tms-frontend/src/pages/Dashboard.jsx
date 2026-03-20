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

/**
 * Dashboard page — main ticket management view
 * Composes all sub-components and wires up state
 * @author-Smriti Bajpai
 */
const Dashboard = () => {
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);
  const { tickets, stats, search, setSearch, statusFilter, setStatusFilter, createTicket } = useTickets();
  const { toast, showToast } = useToast();

  const handleLogout = () => {
    showToast('Logged out successfully');
    setTimeout(() => navigate('/login'), 1000);
  };

  const handleCreateTicket = (formData) => {
    createTicket(formData);
    showToast('Ticket created successfully!');
  };

  const handleTicketClick = (ticket) => {
    navigate(`/tickets/${ticket.id}`);
  };

  return (
    <>
      <Header onLogout={handleLogout} />

      <main className="main">
        <StatsGrid stats={stats} />

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
