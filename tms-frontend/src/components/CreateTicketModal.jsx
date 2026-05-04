import { useState } from 'react';

const MAX_BYTES = 16 * 1024 * 1024;
const defaultForm = { title: '', desc: '', priority: 'MEDIUM', files: [] };

const inputCls  = 'w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm text-slate-900 placeholder-slate-400 outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors bg-white';
const labelCls  = 'block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide';
const selectCls = `${inputCls}`;

const CreateTicketModal = ({ isOpen, onClose, onSubmit, onError }) => {
  const [form, setForm] = useState(defaultForm);

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = () => {
    if (!form.title?.trim() || !form.desc?.trim() || !form.priority) {
      onError('Please fill in all required fields — Title, Description, and Priority.');
      return;
    }
    onSubmit(form);
    setForm(defaultForm);
    onClose();
  };

  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) onClose();
  };

  const handleFileChange = (e) => {
    const selected = Array.from(e.target.files);
    const oversize = selected.find((f) => f.size > MAX_BYTES);
    if (oversize) {
      onError(`"${oversize.name}" exceeds the 16 MB limit.`);
      e.target.value = '';
      return;
    }
    setForm((prev) => ({ ...prev, files: selected }));
  };

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-sm p-4"
      onClick={handleOverlayClick}
    >
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <div>
            <h2 className="text-base font-bold text-slate-900">Create New Ticket</h2>
            <p className="text-xs text-slate-500 mt-0.5">Provide details about the issue you want to report.</p>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Body */}
        <div className="p-6 space-y-4 overflow-y-auto flex-1">
          <div>
            <label className={labelCls}>Ticket Title <span className="text-red-500 normal-case">*</span></label>
            <input name="title" className={inputCls} placeholder="Brief description of the issue" value={form.title} onChange={handleChange} />
          </div>

          <div>
            <label className={labelCls}>Description <span className="text-red-500 normal-case">*</span></label>
            <textarea
              name="desc" value={form.desc} onChange={handleChange}
              placeholder="Provide detailed information about the issue, steps to reproduce, and any relevant context..."
              className={`${inputCls} resize-none h-28`}
            />
          </div>

          <div>
            <label className={labelCls}>Priority <span className="text-red-500 normal-case">*</span></label>
            <select name="priority" className={selectCls} value={form.priority} onChange={handleChange}>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>

          <div>
            <label className={labelCls}>Attachments <span className="text-slate-400 normal-case font-normal">(optional)</span></label>
            <div
              onClick={() => document.getElementById('ticketFileInput').click()}
              className="border-2 border-dashed border-slate-300 hover:border-indigo-400 rounded-xl p-6 text-center cursor-pointer transition-colors hover:bg-indigo-50/30"
            >
              <svg className="w-7 h-7 text-slate-400 mx-auto mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
              </svg>
              <p className="text-sm text-slate-600 font-medium">Click to upload files</p>
              <p className="text-xs text-slate-400 mt-1">PNG, JPG, PDF — max 16 MB each</p>
              {form.files.length > 0 && (
                <div className="mt-3 space-y-1">
                  {form.files.map((f, i) => (
                    <div key={i} className="text-xs text-indigo-600 font-medium">
                      {f.name} ({(f.size / 1024 / 1024).toFixed(1)} MB)
                    </div>
                  ))}
                </div>
              )}
              <input type="file" id="ticketFileInput" className="hidden" multiple accept=".png,.jpg,.jpeg,.pdf" onChange={handleFileChange} />
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-200 bg-slate-50 rounded-b-2xl">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm font-semibold text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            className="px-4 py-2 text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg transition-colors shadow-sm"
          >
            Create Ticket
          </button>
        </div>
      </div>
    </div>
  );
};

export default CreateTicketModal;
