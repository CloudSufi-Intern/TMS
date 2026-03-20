/**
 * @file tickets.js
 * @description Mock ticket data for the Ticket Management System (TMS).
 * This file serves as a temporary data source until the backend API is connected.
 * Todo: Replace `initialTickets` usage in `useTickets.js` with an API fetch call
 *
 * @author Smriti Bajpai
 */

export const initialTickets = [
  {
    /* ── Dashboard fields ── */
    id: 1,
    title: 'Laptop not connecting to WiFi',
    desc: 'Unable to connect to office WiFi network since morning',
    status: 'open',
    priority: 'high',
    category: 'Hardware',
    assignedTo: 'Person 1 (IT)',
    creator: 'Smriti Bajpai',
    comments: 3,
    files: 1,
    hasNotif: true,

    /* ── TicketDetail fields ── */
    description: "Unable to connect to office WiFi network since morning. I've tried restarting my laptop multiple times but the issue persists.",
    createdBy:       { name: 'Smriti Bajpai', email: 'smriti.bajpai@company.com', phone: '+91 98765 43210', role: 'employee' },
    assignedToDetail:{ name: 'Person 1',      email: 'person1.it@company.com',    phone: '+91 91234 56789', role: 'it'       },
    approver:        { name: 'Manager A',     email: 'manager.a@company.com' },
    createdAt:   '10/02/2026, 09:30:00',
    updatedAt:   '10/02/2026, 10:15:00',
    assignedAt:  '10/02/2026, 09:35:00',
    sla:         '11/02/2026, 09:30:00',
    isApprovalRequired: false,
    approvalStatus: null,
    attachments: [
      { name: 'error-screenshot.png', size: '245 KB', fileType: 'image', uploadedBy: 'Smriti Bajpai', uploadedAt: '10/02/2026, 09:32:00' },
    ],
    commentsList: [
      { id: 1, author: 'Person 1 (IT)',  text: "I've received your ticket. Can you check if other devices connect to WiFi?", time: '10/02/2026, 09:45:00' },
      { id: 2, author: 'Smriti Bajpai', text: "Yes, my phone connects fine. Only my laptop has problems.",                   time: '10/02/2026, 10:00:00' },
      { id: 3, author: 'Person 1 (IT)',  text: "I'll come to your desk in 15 minutes to check the network adapter.",         time: '10/02/2026, 10:15:00' },
    ],
    history: [
      { id: 1, description: 'Ticket created',               createdAt: '10/02/2026, 09:30:00', createdBy: 'Smriti Bajpai' },
      { id: 2, description: 'Assigned to Person 1 (IT)',    createdAt: '10/02/2026, 09:35:00', createdBy: 'System'        },
      { id: 3, description: 'Status changed to In Progress',createdAt: '10/02/2026, 09:45:00', createdBy: 'Person 1 (IT)' },
    ],
  },

  {
    /* ── Dashboard fields ── */
    id: 2,
    title: 'Request for new software license',
    desc: 'Need Adobe Creative Cloud license for design work',
    status: 'pending_approval',
    priority: 'medium',
    category: 'Software',
    assignedTo: 'Ram Bansal',
    creator: 'Ashika Sharma',
    comments: 5,
    files: 0,
    hasNotif: false,

    /* ── TicketDetail fields ── */
    description: 'Need Adobe Creative Cloud license for design work. Our team is working on multiple design projects and current tools are insufficient.',
    createdBy:       { name: 'Ashika Sharma', email: 'ashika.sharma@company.com', phone: '+91 98001 11222', role: 'employee' },
    assignedToDetail:{ name: 'Ram Bansal',    email: 'ram.bansal@company.com',    phone: '+91 98001 33444', role: 'it'       },
    approver:        { name: 'Manager B',     email: 'manager.b@company.com' },
    createdAt:   '10/02/2026, 10:00:00',
    updatedAt:   '10/02/2026, 11:30:00',
    assignedAt:  '10/02/2026, 10:05:00',
    sla:         '12/02/2026, 10:00:00',
    isApprovalRequired: true,
    approvalStatus: 'pending',
    attachments: [],
    commentsList: [
      { id: 1, author: 'Ram Bansal',     text: 'Forwarded the license request to procurement for approval.',          time: '10/02/2026, 10:30:00' },
      { id: 2, author: 'Ashika Sharma',  text: 'Thank you! Please let me know once approved.',                        time: '10/02/2026, 10:45:00' },
      { id: 3, author: 'Ram Bansal',     text: 'Pending manager approval. Should be done by end of day.',             time: '10/02/2026, 11:00:00' },
      { id: 4, author: 'Ashika Sharma',  text: 'Understood, I will wait.',                                            time: '10/02/2026, 11:15:00' },
      { id: 5, author: 'Ram Bansal',     text: 'Manager approved. License key will be sent by tomorrow morning.',     time: '10/02/2026, 11:30:00' },
    ],
    history: [
      { id: 1, description: 'Ticket created',                    createdAt: '10/02/2026, 10:00:00', createdBy: 'Ashika Sharma' },
      { id: 2, description: 'Sent for approval to Manager B',    createdAt: '10/02/2026, 10:05:00', createdBy: 'Ram Bansal'    },
    ],
  },

  {
    /* ── Dashboard fields ── */
    id: 3,
    title: 'Email account access issue',
    desc: 'Cannot access my email account after password reset',
    status: 'in_progress',
    priority: 'high',
    category: 'Account',
    assignedTo: 'Rahul Jain (IT)',
    creator: 'Shruti Sen',
    comments: 2,
    files: 2,
    hasNotif: false,

    /* ── TicketDetail fields ── */
    description: 'Cannot access my email account after a password reset. I keep getting an authentication error. This is urgent as I have important client emails pending.',
    createdBy:       { name: 'Shruti Sen',  email: 'shruti.sen@company.com',   phone: '+91 99887 76655', role: 'employee' },
    assignedToDetail:{ name: 'Rahul Jain', email: 'rahul.jain@company.com',   phone: '+91 99887 11223', role: 'it'       },
    approver:        null,
    createdAt:   '10/02/2026, 08:00:00',
    updatedAt:   '10/02/2026, 08:45:00',
    assignedAt:  '10/02/2026, 08:10:00',
    sla:         '10/02/2026, 14:00:00',
    isApprovalRequired: false,
    approvalStatus: null,
    attachments: [
      { name: 'auth-error.png',  size: '120 KB', fileType: 'image', uploadedBy: 'Shruti Sen', uploadedAt: '10/02/2026, 08:05:00' },
      { name: 'browser-log.pdf', size: '340 KB', fileType: 'pdf',   uploadedBy: 'Shruti Sen', uploadedAt: '10/02/2026, 08:06:00' },
    ],
    commentsList: [
      { id: 1, author: 'Rahul Jain (IT)', text: 'Account is locked. Resetting credentials now, please wait 10 minutes.', time: '10/02/2026, 08:30:00' },
      { id: 2, author: 'Shruti Sen',      text: 'Still getting the same error after 10 minutes.',                         time: '10/02/2026, 08:45:00' },
    ],
    history: [
      { id: 1, description: 'Ticket created',                createdAt: '10/02/2026, 08:00:00', createdBy: 'Shruti Sen'      },
      { id: 2, description: 'Assigned to Rahul Jain (IT)',   createdAt: '10/02/2026, 08:10:00', createdBy: 'System'          },
      { id: 3, description: 'Status changed to In Progress', createdAt: '10/02/2026, 08:30:00', createdBy: 'Rahul Jain (IT)' },
    ],
  },

  {
    /* ── Dashboard fields ── */
    id: 4,
    title: 'Printer not working',
    desc: '3rd floor printer is showing error message',
    status: 'resolved',
    priority: 'low',
    category: 'Hardware',
    assignedTo: 'Krish Goyal (IT)',
    creator: 'Sarah Khan',
    comments: 4,
    files: 1,
    hasNotif: true,

    /* ── TicketDetail fields ── */
    description: '3rd floor printer showing error and not printing. Error reads "Paper jam - Tray 2" but there is no visible paper jam.',
    createdBy:       { name: 'Sarah Khan',  email: 'sarah.khan@company.com',  phone: '+91 97654 32109', role: 'employee' },
    assignedToDetail:{ name: 'Krish Goyal', email: 'krish.goyal@company.com', phone: '+91 97654 98765', role: 'it'       },
    approver:        null,
    createdAt:   '09/02/2026, 14:00:00',
    updatedAt:   '09/02/2026, 16:30:00',
    assignedAt:  '09/02/2026, 14:10:00',
    sla:         '10/02/2026, 14:00:00',
    isApprovalRequired: false,
    approvalStatus: null,
    attachments: [
      { name: 'printer-error.jpg', size: '198 KB', fileType: 'image', uploadedBy: 'Sarah Khan', uploadedAt: '09/02/2026, 14:05:00' },
    ],
    commentsList: [
      { id: 1, author: 'Krish Goyal (IT)', text: 'Coming to check. Please do not force-clear the jam.',                          time: '09/02/2026, 14:30:00' },
      { id: 2, author: 'Sarah Khan',        text: 'Okay, we will leave it. Please come soon.',                                   time: '09/02/2026, 14:45:00' },
      { id: 3, author: 'Krish Goyal (IT)', text: 'Found torn paper in roller. Cleared it. Printer should work now.',             time: '09/02/2026, 16:00:00' },
      { id: 4, author: 'Sarah Khan',        text: 'Confirmed, printer is working fine now. Thank you!',                          time: '09/02/2026, 16:30:00' },
    ],
    history: [
      { id: 1, description: 'Ticket created',              createdAt: '09/02/2026, 14:00:00', createdBy: 'Sarah Khan'       },
      { id: 2, description: 'Assigned to Krish Goyal (IT)',createdAt: '09/02/2026, 14:10:00', createdBy: 'System'           },
      { id: 3, description: 'Status changed to Resolved',  createdAt: '09/02/2026, 16:30:00', createdBy: 'Krish Goyal (IT)' },
    ],
  },

  {
    /* ── Dashboard fields ── */
    id: 5,
    title: 'VPN connection issues',
    desc: 'Cannot connect to VPN from home',
    status: 'open',
    priority: 'medium',
    category: 'Network',
    assignedTo: 'Unassigned',
    creator: 'Dhruv Jain',
    comments: 0,
    files: 0,
    hasNotif: false,

    /* ── TicketDetail fields ── */
    description: 'Cannot connect to company VPN from home since yesterday evening. Tried reinstalling VPN client and restarting router but connection keeps timing out.',
    createdBy:       { name: 'Dhruv Jain', email: 'dhruv.jain@company.com', phone: '+91 96543 21098', role: 'employee' },
    assignedToDetail:{ name: 'Unassigned', email: '—',                      phone: '—',               role: '—'        },
    approver:        null,
    createdAt:   '10/02/2026, 07:00:00',
    updatedAt:   '10/02/2026, 07:00:00',
    assignedAt:  null,
    sla:         '11/02/2026, 07:00:00',
    isApprovalRequired: false,
    approvalStatus: null,
    attachments: [],
    commentsList: [],
    history: [
      { id: 1, description: 'Ticket created', createdAt: '10/02/2026, 07:00:00', createdBy: 'Dhruv Jain' },
    ],
  },
];