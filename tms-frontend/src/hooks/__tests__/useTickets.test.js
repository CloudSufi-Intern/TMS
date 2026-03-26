import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useTickets } from '../useTickets'

// Mock the initial tickets data
vi.mock('../../data/tickets', () => ({
  initialTickets: [
    { id: 1, title: 'Keyboard broken', desc: 'Keys not working', category: 'HARDWARE', status: 'open', priority: 'HIGH' },
    { id: 2, title: 'VPN issue', desc: 'Cannot connect', category: 'NETWORK', status: 'in_progress', priority: 'MEDIUM' },
    { id: 3, title: 'App crash', desc: 'Software crashes', category: 'SOFTWARE', status: 'resolved', priority: 'LOW' },
  ]
}))

// Mock fetch globally
const mockFetch = vi.fn()
global.fetch = mockFetch

// Mock localStorage
const mockToken = 'mock-jwt-token'
vi.spyOn(Storage.prototype, 'getItem').mockReturnValue(mockToken)

beforeEach(() => {
  vi.clearAllMocks()
})

// createTicket — API call tests
describe('createTicket - API call', () => {

  it('should call POST http://localhost:8080/api/tickets', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ id: 99, title: 'New Ticket', status: 'open' })
    })

    const { result } = renderHook(() => useTickets())

    await act(async () => {
      await result.current.createTicket({
        title: 'New Ticket',
        desc: 'Some issue',
        priority: 'HIGH',
        category: 'HARDWARE'
      })
    })

    expect(mockFetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/tickets',
      expect.objectContaining({ method: 'POST' })
    )
  })

  it('should send Authorization header with Bearer token from localStorage', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ id: 99, title: 'New Ticket' })
    })

    const { result } = renderHook(() => useTickets())

    await act(async () => {
      await result.current.createTicket({ title: 'Test', desc: 'Desc', priority: 'LOW' })
    })

    const callHeaders = mockFetch.mock.calls[0][1].headers
    expect(callHeaders['Authorization']).toBe(`Bearer ${mockToken}`)
  })

  it('should send FormData with correct fields — title, description, priority', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ id: 100, title: 'Mouse issue' })
    })

    const { result } = renderHook(() => useTickets())

    await act(async () => {
      await result.current.createTicket({
        title: 'Mouse issue',
        desc: 'Mouse not working',
        priority: 'MEDIUM',
      })
    })

    const sentBody = mockFetch.mock.calls[0][1].body
    expect(sentBody).toBeInstanceOf(FormData)
    expect(sentBody.get('title')).toBe('Mouse issue')
    expect(sentBody.get('description')).toBe('Mouse not working') // mapped from desc → description
    expect(sentBody.get('priority')).toBe('MEDIUM')
  })

  it('should default priority to MEDIUM if not provided', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ id: 101, title: 'No priority ticket' })
    })

    const { result } = renderHook(() => useTickets())

    await act(async () => {
      await result.current.createTicket({ title: 'No priority ticket', desc: 'Some desc' })
    })

    const sentBody = mockFetch.mock.calls[0][1].body
    expect(sentBody.get('priority')).toBe('MEDIUM')
  })

  it('should append file attachments to FormData when files are provided', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ id: 102, title: 'Ticket with file' })
    })

    const { result } = renderHook(() => useTickets())
    const mockFile = new File(['content'], 'screenshot.png', { type: 'image/png' })

    await act(async () => {
      await result.current.createTicket({
        title: 'Ticket with file',
        desc: 'Has attachment',
        priority: 'HIGH',
        files: [mockFile]
      })
    })

    const sentBody = mockFetch.mock.calls[0][1].body
    expect(sentBody.get('attachments')).toBeTruthy()
  })

  it('should NOT append attachments field when no files provided', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ id: 103, title: 'No file ticket' })
    })

    const { result } = renderHook(() => useTickets())

    await act(async () => {
      await result.current.createTicket({ title: 'No file ticket', desc: 'No attachment' })
    })

    const sentBody = mockFetch.mock.calls[0][1].body
    expect(sentBody.get('attachments')).toBeNull()
  })
})


// createTicket — DB / state tests


describe('createTicket - ticket saved and shown in list', () => {

  it('should add the returned ticket from backend into the tickets list', async () => {
    const newTicket = { id: 99, title: 'New Ticket', desc: 'Desc', status: 'open', priority: 'HIGH', category: 'HARDWARE' }

    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => newTicket
    })

    const { result } = renderHook(() => useTickets())
    const countBefore = result.current.tickets.length

    await act(async () => {
      await result.current.createTicket({ title: 'New Ticket', desc: 'Desc', priority: 'HIGH' })
    })

    expect(result.current.tickets.length).toBe(countBefore + 1)
  })

  it('should show the new ticket with the correct title in the list', async () => {
    const newTicket = { id: 99, title: 'Printer broken', desc: 'Not printing', status: 'open', priority: 'HIGH', category: 'HARDWARE' }

    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => newTicket
    })

    const { result } = renderHook(() => useTickets())

    await act(async () => {
      await result.current.createTicket({ title: 'Printer broken', desc: 'Not printing', priority: 'HIGH' })
    })

    const found = result.current.tickets.find(t => t.title === 'Printer broken')
    expect(found).toBeDefined()
  })

  it('should use the id returned from the backend (not a local id)', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ id: 999, title: 'Backend ticket', status: 'open' })
    })

    const { result } = renderHook(() => useTickets())

    await act(async () => {
      await result.current.createTicket({ title: 'Backend ticket', desc: 'Desc', priority: 'LOW' })
    })

    const added = result.current.tickets.find(t => t.id === 999)
    expect(added).toBeDefined()
  })
})


// createTicket — error handling

describe('createTicket - error handling', () => {

  it('should throw an error if response is not ok (e.g. 400 Bad Request)', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      json: async () => ({ message: 'Validation failed' })
    })

    const { result } = renderHook(() => useTickets())

    await expect(
      act(async () => {
        await result.current.createTicket({ title: 'Bad ticket', desc: 'Missing fields', priority: 'HIGH' })
      })
    ).rejects.toThrow('Validation failed')
  })

  it('should throw an error if fetch itself fails (network error)', async () => {
    mockFetch.mockRejectedValueOnce(new Error('Network Error'))

    const { result } = renderHook(() => useTickets())

    await expect(
      act(async () => {
        await result.current.createTicket({ title: 'Network fail', desc: 'Desc', priority: 'LOW' })
      })
    ).rejects.toThrow('Network Error')
  })

  it('should NOT add ticket to list if API call fails', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      json: async () => ({ message: 'Server error' })
    })

    const { result } = renderHook(() => useTickets())
    const countBefore = result.current.tickets.length

    await act(async () => {
      try {
        await result.current.createTicket({ title: 'Failed ticket', desc: 'Desc', priority: 'HIGH' })
      } catch (_) {}
    })

    expect(result.current.tickets.length).toBe(countBefore)
  })
})
