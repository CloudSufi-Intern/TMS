import { useState } from 'react';

const defaultForm = { title: '', desc: '', priority: 'MEDIUM', files: [] };

/**
 * Modal dialog for creating a new ticket
 * @param {boolean} isOpen - Controls modal visibility
 * @param {function} onClose - Close handler
 * @param {function} onSubmit - Submit handler, receives form data
 * @param {function} onError - Error toast trigger
 * @author-Smriti Bajpai
 */

/**
 * Processes the form submission before sending it to the parent component.
 * * [Ticket Update]: Added strict client-side validation for the 'priority' field
 * to ensure it perfectly mirrors backend constraints and prevents unnecessary API errors.
 * * @author Priyanshu Gupta
 */


const CreateTicketModal = ({ isOpen, onClose, onSubmit, onError }) => {
    const [form, setForm] = useState(defaultForm);


  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = () => {
      console.log("REACT THINKS THE DATA IS:", form);
    if (!form.title?.trim() || !form.desc?.trim() || !form.priority) {
      onError('Please fill in all required fields (Title, Description, and Priority)');
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
  setForm((prev) => ({ ...prev, files: Array.from(e.target.files) }));
};

  return (
    <div className={`modal-overlay ${isOpen ? 'open' : ''}`} onClick={handleOverlayClick}>
      <div className="modal">
        {/* Header */}
        <div className="modal-header">
          <h2 className="modal-title">Create New Ticket</h2>
          <button className="btn-close" onClick={onClose}>
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Body */}
        <div className="modal-body">
          <div className="form-group">
            <label>Ticket Title <span>*</span></label>
            <input
              name="title"
              className="form-input"
              placeholder="Brief description of the issue"
              value={form.title}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label>Description <span>*</span></label>
            <textarea
              name="desc"
              className="form-textarea"
              placeholder="Provide detailed information about the issue..."
              value={form.desc}
              onChange={handleChange}
            />
          </div>

         <div className="form-group">
           <label>Priority <span>*</span></label>
           <select name="priority" className="form-select" value={form.priority} onChange={handleChange}>
             <option value="LOW">Low</option>
             <option value="MEDIUM">Medium</option>
             <option value="HIGH">High</option>
           </select>
         </div>



          <div className="form-group">
            <label>Attachments</label>
            <div className="upload-zone" onClick={() => document.getElementById('fileInput').click()}>
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round"
                  d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
              </svg>
              <p><span>Click to upload</span> or drag and drop</p>
              <small>PNG, JPG, PDF up to 10MB</small>
              {form.files.length > 0 && (
                <ul style={{ marginTop: '8px', fontSize: '12px' }}>
                  {form.files.map((f, i) => <li key={i}>📎 {f.name}</li>)}
                </ul>
              )}
             <input type="file" id="fileInput" style={{ display: 'none' }} multiple accept=".png,.jpg,.jpeg,.pdf" onChange={handleFileChange} />
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="modal-footer">
          <button className="btn-cancel" onClick={onClose}>Cancel</button>
          <button className="btn-submit" onClick={handleSubmit}>Create Ticket</button>
        </div>
      </div>
    </div>
  );
};

export default CreateTicketModal;
