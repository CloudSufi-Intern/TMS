import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import Badge from '../Badge';

/**
 * Unit tests for the Badge component.
 * Verifies correct label rendering and CSS class application for all status and priority values.
 *
 * @author Yashas Yadav
 */
describe('Badge', () => {

  // Status badges

  it('renders "Open" label for type="open"', () => {
    render(<Badge type="open" />);
    expect(screen.getByText('Open')).toBeInTheDocument();
  });

  it('renders "In Progress" label for type="in_progress"', () => {
    render(<Badge type="in_progress" />);
    expect(screen.getByText('In Progress')).toBeInTheDocument();
  });

  it('renders "On Hold" label for type="on_hold"', () => {
    render(<Badge type="on_hold" />);
    expect(screen.getByText('On Hold')).toBeInTheDocument();
  });

  it('renders "Resolved" label for type="resolved"', () => {
    render(<Badge type="resolved" />);
    expect(screen.getByText('Resolved')).toBeInTheDocument();
  });

  it('renders "Closed" label for type="closed"', () => {
    render(<Badge type="closed" />);
    expect(screen.getByText('Closed')).toBeInTheDocument();
  });

  // Priority badges

  it('renders "Low" label for type="low"', () => {
    render(<Badge type="low" />);
    expect(screen.getByText('Low')).toBeInTheDocument();
  });

  it('renders "Medium" label for type="medium"', () => {
    render(<Badge type="medium" />);
    expect(screen.getByText('Medium')).toBeInTheDocument();
  });

  it('renders "High" label for type="high"', () => {
    render(<Badge type="high" />);
    expect(screen.getByText('High')).toBeInTheDocument();
  });

  it('renders "Urgent" label for type="urgent"', () => {
    render(<Badge type="urgent" />);
    expect(screen.getByText('Urgent')).toBeInTheDocument();
  });

  // Case insensitivity

  it('normalises uppercase type to lowercase for matching', () => {
    render(<Badge type="OPEN" />);
    expect(screen.getByText('Open')).toBeInTheDocument();
  });

  it('normalises mixed-case type correctly', () => {
    render(<Badge type="IN_PROGRESS" />);
    expect(screen.getByText('In Progress')).toBeInTheDocument();
  });

  // Custom label override

  it('renders custom label when label prop is provided', () => {
    render(<Badge type="open" label="Custom Label" />);
    expect(screen.getByText('Custom Label')).toBeInTheDocument();
    expect(screen.queryByText('Open')).not.toBeInTheDocument();
  });

  // Unknown type fallback

  it('renders the raw type string when type is unknown', () => {
    render(<Badge type="unknown_status" />);
    expect(screen.getByText('unknown_status')).toBeInTheDocument();
  });

  it('renders empty string when type is undefined', () => {
    const { container } = render(<Badge />);
    const badge = container.querySelector('span');
    expect(badge).toBeInTheDocument();
    expect(badge.textContent).toBe('');
  });

  // CSS classes applied correctly

  it('applies blue classes for "open" status', () => {
    const { container } = render(<Badge type="open" />);
    const badge = container.querySelector('span');
    expect(badge.className).toContain('bg-blue-50');
    expect(badge.className).toContain('text-blue-700');
  });

  it('applies amber classes for "in_progress" status', () => {
    const { container } = render(<Badge type="in_progress" />);
    const badge = container.querySelector('span');
    expect(badge.className).toContain('bg-amber-50');
    expect(badge.className).toContain('text-amber-700');
  });

  it('applies red classes for "urgent" priority', () => {
    const { container } = render(<Badge type="urgent" />);
    const badge = container.querySelector('span');
    expect(badge.className).toContain('bg-red-50');
    expect(badge.className).toContain('text-red-700');
  });

  it('applies slate classes for "closed" status', () => {
    const { container } = render(<Badge type="closed" />);
    const badge = container.querySelector('span');
    expect(badge.className).toContain('bg-slate-100');
    expect(badge.className).toContain('text-slate-600');
  });

  it('always renders as a span element', () => {
    const { container } = render(<Badge type="resolved" />);
    expect(container.querySelector('span')).toBeInTheDocument();
  });
});
