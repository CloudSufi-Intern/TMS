import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import Signup from '../Signup';

/**
 * Unit tests for the Signup page component.
 * Verifies API integration, frontend validation, error handling, and navigation behaviour.
 *
 * @author Yashas Yadav
 */

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

const renderSignup = () =>
  render(
    <MemoryRouter>
      <Signup />
    </MemoryRouter>
  );

const fillValidForm = () => {
  fireEvent.change(screen.getByPlaceholderText('John Doe'), {
    target: { value: 'yashascs' },
  });
  fireEvent.change(screen.getByPlaceholderText('you@company.com'), {
    target: { value: 'yashas@cs.com' },
  });
  fireEvent.change(screen.getByPlaceholderText('+1 (555) 000-0000'), {
    target: { value: '9876543210' },
  });
  fireEvent.change(screen.getByPlaceholderText('••••••••'), {
    target: { value: 'password123' },
  });
};

describe('Signup Page', () => {

  beforeEach(() => {
    mockNavigate.mockReset();
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  // ── Happy path ──────────────────────────────────────────────────

  it('should call POST /api/auth/signup with correct payload and navigate to /login on success', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ id: 1, username: 'yashascs', email: 'yashas@cs.com' }),
    });

    renderSignup();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/auth/signup',
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: 'yashascs',
            email: 'yashas@cs.com',
            password: 'password123',
            phoneNo: '9876543210',
            role: 'IT',
          }),
        })
      );
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  // ── Frontend validation ─────────────────────────────────────────

  it('should show error and not call API when email format is invalid', async () => {
    renderSignup();

    fireEvent.change(screen.getByPlaceholderText('John Doe'), {
      target: { value: 'yashascs' },
    });
    fireEvent.change(screen.getByPlaceholderText('you@company.com'), {
      target: { value: 'invalid-email' },
    });
    fireEvent.change(screen.getByPlaceholderText('+1 (555) 000-0000'), {
      target: { value: '9876543210' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'password123' },
    });

    // jsdom runs constraint validation on submit which blocks the React handler
    // when type="email" has an invalid value. Stub reportValidity to bypass it.
    const form = screen.getByRole('button', { name: /sign up/i }).closest('form');
    form.reportValidity = () => true;

    fireEvent.submit(form);

    await waitFor(() => {
      expect(screen.getByText('Please enter a valid email address.')).toBeInTheDocument();
    });

    expect(global.fetch).not.toHaveBeenCalled();
  });

  it('should show error and not call API when phone number is too short', async () => {
    renderSignup();

    fireEvent.change(screen.getByPlaceholderText('John Doe'), {
      target: { value: 'yashascs' },
    });
    fireEvent.change(screen.getByPlaceholderText('you@company.com'), {
      target: { value: 'yashas@cs.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('+1 (555) 000-0000'), {
      target: { value: '123' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'password123' },
    });
    fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

    await waitFor(() => {
      expect(screen.getByText('Please enter a valid phone number (10-15 digits).')).toBeInTheDocument();
    });

    expect(global.fetch).not.toHaveBeenCalled();
  });

  // ── API error handling ──────────────────────────────────────────

  it('should show "already exists" error on 409 response', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
      status: 409,
      json: async () => ({ message: 'User with this email already exists.' }),
    });

    renderSignup();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

    await waitFor(() => {
      expect(
        screen.getByText('User with this email already exists.')
      ).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('should show backend message on 400 response', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
      status: 400,
      json: async () => ({ message: 'phoneNo: must not be blank' }),
    });

    renderSignup();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

    await waitFor(() => {
      expect(screen.getByText('phoneNo: must not be blank')).toBeInTheDocument();
    });
  });

  it('should show connection error when fetch throws (server down)', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Network Error'));

    renderSignup();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

    await waitFor(() => {
      expect(
        screen.getByText('Unable to connect to the server. Please check your connection.')
      ).toBeInTheDocument();
    });
  });

  // ── UI behaviour ────────────────────────────────────────────────

  it('should clear error when user starts typing after a failure', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
      status: 409,
      json: async () => ({ message: 'User with this email already exists.' }),
    });

    renderSignup();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

    await waitFor(() => {
      expect(screen.getByText('User with this email already exists.')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByPlaceholderText('you@company.com'), {
      target: { value: 'new@cs.com' },
    });

    expect(
      screen.queryByText('User with this email already exists.')
    ).not.toBeInTheDocument();
  });

  it('should show "Creating Account..." and disable button while loading', async () => {
    let resolveFetch;
    global.fetch.mockReturnValueOnce(
      new Promise((resolve) => { resolveFetch = resolve; })
    );

    renderSignup();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

    await waitFor(() => {
      const btn = screen.getByRole('button', { name: /creating account/i });
      expect(btn).toBeDisabled();
    });

    resolveFetch({ ok: true, json: async () => ({}) });
  });

  it('should map fullName field to username in the request body', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({}),
    });

    renderSignup();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

    await waitFor(() => {
      const body = JSON.parse(global.fetch.mock.calls[0][1].body);
      expect(body.username).toBe('yashascs');
      expect(body.fullName).toBeUndefined();
    });
  });
});
