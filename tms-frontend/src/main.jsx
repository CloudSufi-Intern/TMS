import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import {TicketProvider} from "./context/TicketContext"
import './index.css'
import App from './App.jsx'

/**
 * Entry Point of the React Application
 * This file is responsible for rendering the root component (<App />)
 * into the DOM.
 *
 * enable global state (like TicketContext),
 *  we wrap <App /> with your provider here.
 *
 * This ensures:
 * All components share the same ticket data
 *  Status updates reflect across dashboard and detail pages
 */

createRoot(document.getElementById('root')).render(
  <StrictMode>
      <TicketProvider>
    <App />
    </TicketProvider>
  </StrictMode>,
)
