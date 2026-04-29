import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AppShell from '../components/AppShell';
import StatsGrid from '../components/StatsGrid';
import Toolbar from '../components/Toolbar';
import TicketTable from '../components/TicketTable';
import CreateTicketModal from '../components/CreateTicketModal';
import Toast from '../components/Toast';
import { useTickets } from '../hooks/useTickets';
import { useToast } from '../hooks/useToast';

const Dashboard = () => {
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);
  const role = localStorage.getItem('role');

  const {
    tickets, stats,
    search, setSearch,
    statusFilter, setStatusFilter,
    sortBy, setSortBy,
    sortDir, setSortDir,
    createTicket, loading, error,
  } = useTickets();

  const { toast, showToast } = useToast();

  const handleCreateTicket = async (formData) => {
    try {
      await createTicket(formData);
      showToast('Ticket created successfully.');
    } catch (err) {
      showToast(err.response?.data?.message || err.message || 'Failed to create ticket.', true);
    }
  };

  return (
    <>
      <AppShell title="Dashboard" onCreateClick={() => setModalOpen(true)}>
        <StatsGrid stats={stats} role={role} onFilter={setStatusFilter} activeFilter={statusFilter} />

        {error && (
          <div className="mb-4 flex items-start gap-2.5 p-3.5 bg-red-50 border border-red-200 rounded-lg">
            <svg className="w-4 h-4 text-red-500 mt-0.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span className="text-sm text-red-600">{error}</span>
          </div>
        )}

        {loading ? (
          <div className="bg-white rounded-xl border border-slate-200 py-20 flex flex-col items-center justify-center text-slate-400">
            <svg className="w-6 h-6 animate-spin mb-3 text-indigo-500" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth={4} />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
            </svg>
            <p className="text-sm">Loading tickets...</p>
          </div>
        ) : (
          <div className="bg-white rounded-xl border border-slate-200">
            <Toolbar
              search={search} onSearch={setSearch}
              statusFilter={statusFilter} onStatusFilter={setStatusFilter}
              sortBy={sortBy} onSortBy={setSortBy}
              sortDir={sortDir} onSortDir={setSortDir}
            />
            <TicketTable tickets={tickets} onTicketClick={(t) => navigate(`/tickets/${t.id}`)} />
          </div>
        )}
      </AppShell>

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
