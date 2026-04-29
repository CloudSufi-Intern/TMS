import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getTicketById,
  updateTicket,
  getComments,
  addComment as apiAddComment,
} from '../services/TicketService';
import { downloadAttachment } from '../services/AttachmentService';
import { searchUsers } from '../services/UserService';
import AppShell from '../components/AppShell';
import Badge from '../components/Badge';

const MAX_BYTES = 16 * 1024 * 1024;
const STATUS_OPTIONS   = ['OPEN', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED', 'CLOSED'];
const PRIORITY_OPTIONS = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

const inputCls  = 'w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm text-slate-900 outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors bg-white disabled:bg-slate-50 disabled:text-slate-400';
const selectCls = `${inputCls} cursor-pointer`;
const sectionTitle = 'text-xs font-bold text-slate-500 uppercase tracking-widest mb-3';
const cardCls = 'bg-white rounded-xl border border-slate-200 p-5 mb-4';

const TicketDetail = () => {
  const navigate = useNavigate();
  const { id }   = useParams();

  const [ticket, setTicket]         = useState(null);
  const [loading, setLoading]       = useState(true);
  const [fetchError, setFetchError] = useState('');

  const [comment, setComment]           = useState('');
  const [commentFiles, setCommentFiles] = useState([]);
  const [comments, setComments]         = useState([]);
  const [commentSortDir, setCommentSortDir]       = useState('asc');
  const [commentAuthorFilter, setCommentAuthorFilter] = useState('');

  const [status, setStatus]     = useState('');
  const [priority, setPriority] = useState('');
  const [attachments, setAttachments] = useState([]);
  const [isUpdating, setIsUpdating]   = useState(false);
  const [assigneeEmail, setAssigneeEmail] = useState('');

  const [suggestions, setSuggestions]     = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [mentionIndex, setMentionIndex]   = useState(-1);

  const [toast, setToast] = useState({ show: false, message: '', isError: false });

  const userRole  = localStorage.getItem('role');
  const userEmail = localStorage.getItem('email');
  const isIT      = userRole === 'IT';

  const showToast = (message, isError = false) => {
    setToast({ show: true, message, isError });
    setTimeout(() => setToast({ show: false, message: '', isError: false }), 2500);
  };

  const fetchTicket = useCallback(async () => {
    setLoading(true);
    setFetchError('');
    try {
      const data = await getTicketById(id);
      setTicket(data);
      setStatus(data.status || '');
      setPriority(data.priority || '');
      setAttachments(data.attachments || []);
    } catch (err) {
      setFetchError(err.response?.data?.message || err.message || 'Failed to load ticket.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  const fetchComments = useCallback(async () => {
    try {
      const data = await getComments(id, { sortDir: commentSortDir, author: commentAuthorFilter || undefined });
      setComments(data);
    } catch { /* non-fatal */ }
  }, [id, commentSortDir, commentAuthorFilter]);

  useEffect(() => { fetchTicket(); },   [fetchTicket]);
  useEffect(() => { fetchComments(); }, [fetchComments]);

  const handleAssignAgent = async () => {
    if (!assigneeEmail.trim()) return;
    setIsUpdating(true);
    try {
      await updateTicket(id, { assigneeEmail: assigneeEmail.trim() });
      showToast(`Ticket assigned to ${assigneeEmail}`);
      setAssigneeEmail('');
      await fetchTicket();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to assign agent.', true);
    } finally { setIsUpdating(false); }
  };

  const handleSelfAssign = async () => {
    if (!userEmail) { showToast('User email not found. Please log in again.', true); return; }
    setIsUpdating(true);
    try {
      await updateTicket(id, { assigneeEmail: userEmail });
      showToast('Ticket assigned to you.');
      await fetchTicket();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to self-assign.', true);
    } finally { setIsUpdating(false); }
  };

  const handleStatusChange = async (e) => {
    const newStatus = e.target.value;
    if (newStatus === status) return;
    setIsUpdating(true);
    try {
      await updateTicket(id, { status: newStatus });
      setStatus(newStatus);
      showToast(`Status updated to "${newStatus.replace(/_/g, ' ')}"`);
      await fetchTicket();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to update status.', true);
    } finally { setIsUpdating(false); }
  };

  const handlePriorityChange = async (e) => {
    const newPriority = e.target.value;
    if (newPriority === priority) return;
    setIsUpdating(true);
    try {
      await updateTicket(id, { priority: newPriority });
      setPriority(newPriority);
      showToast(`Priority updated to "${newPriority}"`);
      await fetchTicket();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to update priority.', true);
    } finally { setIsUpdating(false); }
  };

  const closeTicketHandler = async () => {
    setIsUpdating(true);
    try {
      await updateTicket(id, { status: 'CLOSED' });
      showToast('Ticket closed.');
      setTimeout(() => navigate('/dashboard'), 1000);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to close ticket.', true);
    } finally { setIsUpdating(false); }
  };

  const handleAddComment = async () => {
    if (!comment.trim() && commentFiles.length === 0) return;
    try {
      const saved = await apiAddComment(id, comment.trim(), commentFiles);
      setComments((prev) => commentSortDir === 'asc' ? [...prev, saved] : [saved, ...prev]);
      setComment('');
      setCommentFiles([]);
      showToast('Comment added.');
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to add comment.', true);
    }
  };

  const handleCommentFileChange = (e) => {
    const selected = Array.from(e.target.files);
    const oversize = selected.find((f) => f.size > MAX_BYTES);
    if (oversize) { showToast(`"${oversize.name}" exceeds 16 MB.`, true); e.target.value = ''; return; }
    setCommentFiles(selected);
  };

  const handleCommentChange = async (e) => {
    const value     = e.target.value;
    const cursorPos = e.target.selectionStart;
    setComment(value);
    const before       = value.substring(0, cursorPos);
    const mentionMatch = before.match(/(^|\s)@(\w*)$/);
    if (mentionMatch && mentionMatch[2].length >= 2) {
      const matchStart = mentionMatch.index + mentionMatch[1].length;
      try {
        const results = await searchUsers(mentionMatch[2]);
        setSuggestions(results);
        setShowSuggestions(results.length > 0);
        setMentionIndex(matchStart);
      } catch { setShowSuggestions(false); }
    } else {
      setShowSuggestions(false);
    }
  };

  const selectUser = (username) => {
    const beforeMention = comment.substring(0, mentionIndex);
    const restOfString  = comment.substring(mentionIndex);
    const nextSpace     = restOfString.indexOf(' ');
    const afterMention  = nextSpace !== -1 ? restOfString.substring(nextSpace) : '';
    setComment(`${beforeMention}@${username}${afterMention || ' '}`);
    setShowSuggestions(false);
  };

  const renderCommentWithMentions = (text) => {
    if (!text) return null;
    return text.split(/(@\w+)/g).map((part, idx) =>
      part.startsWith('@')
        ? <span key={idx} className="text-indigo-600 font-semibold">{part}</span>
        : part
    );
  };

  const handleDownload = async (file) => {
    try { await downloadAttachment(file.id, file.fileName || file.name); }
    catch { showToast('Failed to download file.', true); }
  };

  const formatDate  = (dt) => dt ? new Date(dt).toLocaleString() : 'N/A';
  const formatBytes = (bytes) => {
    if (!bytes) return '0 KB';
    return bytes > 1024 * 1024
      ? `${(bytes / (1024 * 1024)).toFixed(1)} MB`
      : `${Math.round(bytes / 1024)} KB`;
  };

  const distinctAuthors = Array.from(new Set(comments.map((c) => c.createdBy))).filter(Boolean);
  const ticketTitle = ticket ? `Ticket #${String(ticket.id).padStart(6, '0')}` : 'Ticket Details';

  if (loading) {
    return (
      <AppShell title="Loading...">
        <div className="flex items-center justify-center py-32">
          <svg className="w-6 h-6 animate-spin text-indigo-500" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth={4} />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
        </div>
      </AppShell>
    );
  }

  if (fetchError || !ticket) {
    return (
      <AppShell title="Ticket Not Found">
        <div className="flex flex-col items-center justify-center py-32 text-slate-400">
          <svg className="w-12 h-12 mb-4 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p className="text-base font-semibold text-slate-600">{fetchError || 'Ticket not found'}</p>
          <button onClick={() => navigate('/dashboard')} className="mt-4 text-sm text-indigo-600 font-semibold hover:underline">
            Back to Dashboard
          </button>
        </div>
      </AppShell>
    );
  }

  return (
    <AppShell title={ticketTitle}>
      {/* Back navigation + ticket ID bar */}
      <div className="flex items-center gap-4 mb-5">
        <button
          onClick={() => navigate('/dashboard')}
          className="flex items-center gap-1.5 text-sm font-medium text-slate-500 hover:text-slate-900 transition-colors"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
          Dashboard
        </button>
        <span className="text-slate-300">/</span>
        <span className="text-sm font-semibold text-slate-900">{ticketTitle}</span>
      </div>

      <div className="flex gap-5 items-start">
        {/* LEFT COLUMN */}
        <div className="flex-1 min-w-0 space-y-4">

          {/* Ticket summary */}
          <div className={cardCls}>
            <h1 className="text-lg font-bold text-slate-900 mb-3">{ticket.title}</h1>
            <div className="flex flex-wrap gap-2 mb-4">
              <Badge type={ticket.status?.toLowerCase()} />
              <Badge type={ticket.priority?.toLowerCase()} />
            </div>
            <p className="text-sm text-slate-600 leading-relaxed whitespace-pre-line">{ticket.description}</p>
          </div>

          {/* History */}
          <div className={cardCls}>
            <h2 className={sectionTitle}>Activity Log ({ticket.history?.length || 0})</h2>
            {!ticket.history?.length ? (
              <p className="text-sm text-slate-400">No activity recorded yet.</p>
            ) : (
              <div className="space-y-3">
                {ticket.history.map((entry) => (
                  <div key={entry.id} className="flex gap-3">
                    <div className="w-7 h-7 rounded-full bg-slate-100 text-slate-500 flex items-center justify-center flex-shrink-0 text-xs font-bold">
                      {entry.createdBy?.charAt(0)?.toUpperCase() || '?'}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-0.5">
                        <span className="text-xs font-semibold text-slate-700">{entry.createdBy}</span>
                        <span className="text-xs text-slate-400">{formatDate(entry.createdAt)}</span>
                      </div>
                      <p className="text-sm text-slate-600">{entry.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Attachments */}
          <div className={cardCls}>
            <h2 className={sectionTitle}>Attachments ({attachments.length})</h2>
            {attachments.length === 0 ? (
              <p className="text-sm text-slate-400">No attachments on this ticket.</p>
            ) : (
              <div className="space-y-2">
                {attachments.map((file, i) => (
                  <div key={file.id ?? i} className="flex items-center justify-between gap-3 p-3 bg-slate-50 border border-slate-200 rounded-lg">
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-8 h-8 bg-indigo-100 text-indigo-600 rounded-lg flex items-center justify-center flex-shrink-0">
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                        </svg>
                      </div>
                      <div className="min-w-0">
                        <div className="text-sm font-medium text-slate-900 truncate">{file.fileName || `Attachment #${file.id}`}</div>
                        <div className="text-xs text-slate-400">{formatBytes(file.fileSizeInBytes)}</div>
                      </div>
                    </div>
                    <button
                      onClick={() => handleDownload(file)}
                      className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 rounded-lg transition-colors flex-shrink-0"
                    >
                      <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                      </svg>
                      Download
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Comments */}
          <div className={cardCls}>
            <div className="flex items-center justify-between mb-4">
              <h2 className={`${sectionTitle} mb-0`}>Comments ({comments.length})</h2>
              <div className="flex gap-2">
                <select
                  value={commentSortDir} onChange={(e) => setCommentSortDir(e.target.value)}
                  className="px-2 py-1.5 text-xs border border-slate-300 rounded-lg text-slate-600 outline-none bg-white focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="asc">Oldest first</option>
                  <option value="desc">Newest first</option>
                </select>
                <select
                  value={commentAuthorFilter} onChange={(e) => setCommentAuthorFilter(e.target.value)}
                  className="px-2 py-1.5 text-xs border border-slate-300 rounded-lg text-slate-600 outline-none bg-white focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="">All authors</option>
                  {distinctAuthors.map((a) => <option key={a} value={a}>{a}</option>)}
                </select>
              </div>
            </div>

            {comments.length === 0 ? (
              <p className="text-sm text-slate-400 mb-4">No comments yet.</p>
            ) : (
              <div className="space-y-4 mb-4">
                {comments.map((c) => (
                  <div key={c.id} className="flex gap-3">
                    <div className="w-8 h-8 rounded-full bg-indigo-600 text-white text-xs font-bold flex items-center justify-center flex-shrink-0">
                      {c.createdBy?.charAt(0)?.toUpperCase() || '?'}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-xs font-semibold text-slate-800">{c.createdBy}</span>
                        <span className="text-xs text-slate-400">{formatDate(c.createdAt)}</span>
                      </div>
                      <p className="text-sm text-slate-700 leading-relaxed">{renderCommentWithMentions(c.content)}</p>
                      {c.attachments?.length > 0 && (
                        <div className="mt-2 space-y-1">
                          {c.attachments.map((att) => (
                            <button
                              key={att.id} onClick={() => handleDownload(att)}
                              className="flex items-center gap-2 px-3 py-1.5 text-xs text-indigo-600 bg-indigo-50 hover:bg-indigo-100 rounded-lg transition-colors border border-indigo-100"
                            >
                              <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                              </svg>
                              {att.fileName || `Attachment #${att.id}`}
                              <span className="text-slate-400 ml-1">{formatBytes(att.fileSizeInBytes)}</span>
                            </button>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Add comment */}
            <div className="border-t border-slate-100 pt-4">
              <label className="block text-xs font-semibold text-slate-500 uppercase tracking-widest mb-2">Add a Comment</label>
              <div className="relative">
                <textarea
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm text-slate-900 placeholder-slate-400 outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors resize-none h-24"
                  placeholder="Type your comment here... (use @ to mention a team member)"
                  value={comment}
                  onChange={handleCommentChange}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && e.ctrlKey) handleAddComment();
                    if (e.key === 'Escape') setShowSuggestions(false);
                  }}
                />
                {showSuggestions && suggestions.length > 0 && (
                  <ul className="absolute z-10 bg-white border border-slate-200 rounded-xl shadow-lg mt-1 w-56 overflow-hidden">
                    {suggestions.map((u) => (
                      <li
                        key={u.id}
                        onClick={() => selectUser(u.username)}
                        className="flex items-center gap-2.5 px-3 py-2 hover:bg-slate-50 cursor-pointer"
                      >
                        <div className="w-6 h-6 rounded-full bg-indigo-100 text-indigo-700 text-xs font-bold flex items-center justify-center">
                          {u.username[0].toUpperCase()}
                        </div>
                        <span className="text-sm text-slate-700">{u.username}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <div className="flex items-center gap-3 mt-2.5">
                <input type="file" id="commentFileInput" multiple accept=".png,.jpg,.jpeg,.pdf" onChange={handleCommentFileChange} className="hidden" />
                <button
                  type="button"
                  onClick={() => document.getElementById('commentFileInput').click()}
                  className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors border border-slate-200"
                >
                  <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                  </svg>
                  Attach files
                </button>
                {commentFiles.length > 0 && (
                  <span className="text-xs text-slate-500">{commentFiles.length} file(s) attached</span>
                )}
                <div className="flex-1" />
                <button
                  onClick={handleAddComment}
                  disabled={isUpdating || (!comment.trim() && commentFiles.length === 0)}
                  className="flex items-center gap-1.5 px-4 py-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-300 rounded-lg transition-colors shadow-sm"
                >
                  <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                  </svg>
                  Post Comment
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* RIGHT COLUMN */}
        <div className="w-72 flex-shrink-0 space-y-4">

          {/* Status */}
          <div className={cardCls}>
            <h2 className={sectionTitle}>Status</h2>
            <select value={status} onChange={handleStatusChange} disabled={isUpdating} className={selectCls}>
              {STATUS_OPTIONS.map((s) => (
                <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>
              ))}
            </select>
          </div>

          {/* Priority */}
          <div className={cardCls}>
            <h2 className={sectionTitle}>Priority</h2>
            <select value={priority} onChange={handlePriorityChange} disabled={isUpdating} className={selectCls}>
              {PRIORITY_OPTIONS.map((p) => (
                <option key={p} value={p}>{p.charAt(0) + p.slice(1).toLowerCase()}</option>
              ))}
            </select>
          </div>

          {/* Assign */}
          <div className={cardCls}>
            <h2 className={sectionTitle}>
              {ticket.assignedTo && ticket.assignedTo !== 'Unassigned' ? 'Change Agent' : 'Assign Agent'}
            </h2>

            {ticket.assignedTo && ticket.assignedTo !== 'Unassigned' && (
              <div className="flex items-center gap-2 mb-3 p-2.5 bg-slate-50 rounded-lg border border-slate-200">
                <div className="w-7 h-7 rounded-full bg-indigo-100 text-indigo-700 text-xs font-bold flex items-center justify-center flex-shrink-0">
                  {ticket.assignedTo.charAt(0).toUpperCase()}
                </div>
                <div className="min-w-0">
                  <div className="text-xs text-slate-500">Currently assigned to</div>
                  <div className="text-sm font-semibold text-slate-800 truncate">{ticket.assignedTo}</div>
                </div>
              </div>
            )}

            {isIT && (
              <button
                onClick={handleSelfAssign} disabled={isUpdating}
                className="w-full mb-3 py-2 text-xs font-semibold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 border border-indigo-200 rounded-lg transition-colors"
              >
                Assign to Me
              </button>
            )}

            {!isIT && (
              <div className="mb-3 p-3 bg-amber-50 border border-amber-200 rounded-lg">
                <p className="text-xs text-amber-700">Only IT personnel can assign agents.</p>
              </div>
            )}

            <div className="flex gap-2">
              <input
                type="email"
                className={`${inputCls} flex-1`}
                placeholder={isIT ? 'Agent email...' : 'Unauthorized'}
                value={assigneeEmail}
                onChange={(e) => setAssigneeEmail(e.target.value)}
                disabled={!isIT || isUpdating}
              />
              <button
                onClick={handleAssignAgent}
                disabled={!isIT || isUpdating || !assigneeEmail.trim()}
                className="px-3 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300 rounded-lg transition-colors flex-shrink-0"
              >
                {ticket.assignedTo && ticket.assignedTo !== 'Unassigned' ? 'Update' : 'Assign'}
              </button>
            </div>
          </div>

          {/* Ticket info */}
          <div className={cardCls}>
            <h2 className={sectionTitle}>Ticket Information</h2>
            <div className="space-y-3">
              {[
                { label: 'Ticket ID',    value: `#${String(ticket.id).padStart(6, '0')}` },
                { label: 'Created By',   value: ticket.createdBy || 'Unknown' },
                { label: 'Assigned To',  value: ticket.assignedTo || 'Unassigned' },
                { label: 'Created',      value: formatDate(ticket.createdAt) },
                { label: 'Last Updated', value: formatDate(ticket.updatedAt) },
              ].map(({ label, value }) => (
                <div key={label} className="flex justify-between gap-2">
                  <span className="text-xs text-slate-400 flex-shrink-0">{label}</span>
                  <span className="text-xs font-medium text-slate-700 text-right break-all">{value}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Toast */}
      <div
        className={`fixed bottom-6 right-6 z-50 flex items-center gap-3 px-4 py-3 rounded-xl shadow-lg text-sm font-medium transition-all duration-300 ${
          toast.show ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-2 pointer-events-none'
        } ${toast.isError ? 'bg-red-600 text-white' : 'bg-slate-900 text-white'}`}
      >
        {toast.isError ? (
          <svg className="w-4 h-4 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        ) : (
          <svg className="w-4 h-4 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
          </svg>
        )}
        {toast.message}
      </div>
    </AppShell>
  );
};

export default TicketDetail;
