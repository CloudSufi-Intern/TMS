import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import Login from '../Login';

/**
 * Unit tests for the Login page component.
 * Verifies API integration, error handling, and navigation behaviour.
 *
 * @author Yashas Yadav
 */

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

const renderLogin = () =>
  render(
    <MemoryRouter>
      <Login />
    </MemoryRouter>
  );

describe('Login Page', () => {

  beforeEach(() => {
    mockNavigate.mockReset();
    localStorage.clear();
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  // ── Happy path ──────────────────────────────────────────────────

  it('should store JWT in localStorage and navigate to /dashboard on valid credentials', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ token: 'mock.jwt.token', tokenType: 'Bearer' }),
    });

    renderLogin();

    fireEvent.change(screen.getByPlaceholderText('you@company.com'), {
      target: { value: 'yashas@cs.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'password123' },
    });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(localStorage.getItem('token')).toBe('mock.jwt.token');
      expect(localStorage.getItem('tokenType')).toBe('Bearer');
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('should call POST /api/auth/login with correct payload', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ token: 'tok', tokenType: 'Bearer' }),
    });

    renderLogin();

    fireEvent.change(screen.getByPlaceholderText('you@company.com'), {
      target: { value: 'yashas@cs.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'password123' },
    });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/auth/login',
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: 'yashas@cs.com', password: 'password123' }),
        })
      );
    });
  });

  // ── Error handling ──────────────────────────────────────────────

  it('should show "Invalid email or password" on 401 response', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
      status: 401,
      json: async () => ({ message: 'Invalid email or password.' }),
    });

    renderLogin();

    fireEvent.change(screen.getByPlaceholderText('you@company.com'), {
      target: { value: 'yashas@cs.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'wrongpassword' },
    });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText('Invalid email or password. Please try again.')).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('should show backend message on 400 response', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
      status: 400,
      json: async () => ({ message: 'password: must not be blank' }),
    });

    renderLogin();

    fireEvent.change(screen.getByPlaceholderText('you@company.com'), {
      target: { value: 'yashas@cs.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: ' ' },
    });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText('password: must not be blank')).toBeInTheDocument();
    });
  });

  it('should show connection error when fetch throws (server down)', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Network Error'));

    renderLogin();

    fireEvent.change(screen.getByPlaceholderText('you@company.com'), {
      target: { value: 'yashas@cs.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'password123' },
    });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(
        screen.getByText('Unable to connect to the server. Please check your connection.')
      ).toBeInTheDocument();
    });
  });

  // ── UI behaviour ────────────────────────────────────────────────

  it('should clear error message when user starts typing after a failure', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
      status: 401,
      json: async () => ({ message: 'Invalid email or password.' }),
    });

    renderLogin();

    fireEvent.change(screen.getByPlaceholderText('you@company.com'), {
      target: { value: 'yashas@cs.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'wrong' },
    });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText('Invalid email or password. Please try again.')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'newattempt' },
    });

    expect(screen.queryByText('Invalid email or password. Please try again.')).not.toBeInTheDocument();
  });

  it('should show "Signing In..." and disable button while loading', async () => {
    let resolveFetch;
    global.fetch.mockReturnValueOnce(
      new Promise((resolve) => { resolveFetch = resolve; })
    );

    renderLogin();

    fireEvent.change(screen.getByPlaceholderText('you@company.com'), {
      target: { value: 'yashas@cs.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'password123' },
    });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      const btn = screen.getByRole('button', { name: /signing in/i });
      expect(btn).toBeDisabled();
    });

    resolveFetch({ ok: true, json: async () => ({ token: 't', tokenType: 'Bearer' }) });
  });
});
