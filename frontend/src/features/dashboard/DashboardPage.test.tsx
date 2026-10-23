import { screen, within } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { API, dashboard, problem, staff } from '../../test/fixtures';
import { renderWithProviders } from '../../test/render';
import { server } from '../../test/server';
import { DashboardPage } from './DashboardPage';

describe('DashboardPage', () => {
  it('shows totals, the low-stock list and recent movements', async () => {
    renderWithProviders(<DashboardPage />, { user: staff });

    expect(await screen.findByText('Total stock value')).toBeInTheDocument();
    expect(screen.getByText((1679.08).toLocaleString(undefined, { minimumFractionDigits: 2 }))).toBeInTheDocument();
    const low = screen.getByRole('table', { name: /reorder level/i });
    expect(within(low).getByRole('link', { name: 'Green tea' })).toHaveAttribute('href', '/products/101');
    const recent = screen.getByRole('table', { name: /most recent/i });
    expect(within(recent).getByText('Opening stock')).toBeInTheDocument();
  });

  it('shows empty states', async () => {
    server.use(
      http.get(`${API}/dashboard`, () =>
        HttpResponse.json({ ...dashboard, lowStock: [], lowStockCount: 0, recentMovements: [] }),
      ),
    );
    renderWithProviders(<DashboardPage />, { user: staff });

    expect(await screen.findByText('Nothing is low on stock')).toBeInTheDocument();
    expect(screen.getByText('No stock movements yet')).toBeInTheDocument();
  });

  it('shows an error state', async () => {
    server.use(http.get(`${API}/dashboard`, () => HttpResponse.json(problem(500, 'x'), { status: 500 })));
    renderWithProviders(<DashboardPage />, { user: staff });

    expect(await screen.findByText('Could not load the dashboard.')).toBeInTheDocument();
  });
});
