import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { admin, API, page, problem, staff } from '../../test/fixtures';
import { renderWithProviders } from '../../test/render';
import { server } from '../../test/server';
import { SupplierListPage } from './SupplierListPage';

describe('SupplierListPage', () => {
  it('lists suppliers', async () => {
    renderWithProviders(<SupplierListPage />, { user: staff });

    expect(screen.getByRole('status')).toHaveTextContent(/loading suppliers/i);
    const table = await screen.findByRole('table', { name: 'Suppliers' });
    expect(within(table).getByText('Northwind Traders')).toBeInTheDocument();
  });

  it('hides edit actions from staff', async () => {
    renderWithProviders(<SupplierListPage />, { user: staff });

    await screen.findByText('Northwind Traders');
    expect(screen.queryByRole('button', { name: /add supplier/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /delete/i })).not.toBeInTheDocument();
  });

  it('sends the search term to the API', async () => {
    const seen: string[] = [];
    server.use(
      http.get(`${API}/suppliers`, ({ request }) => {
        seen.push(new URL(request.url).searchParams.get('q') ?? '');
        return HttpResponse.json(page([]));
      }),
    );
    renderWithProviders(<SupplierListPage />, { user: staff });

    await userEvent.type(screen.getByRole('searchbox', { name: /search suppliers/i }), 'acme');

    expect(await screen.findByText('No suppliers match your search')).toBeInTheDocument();
    await waitFor(() => expect(seen).toContain('acme'));
  });

  it('shows an error with retry', async () => {
    server.use(http.get(`${API}/suppliers`, () => HttpResponse.json(problem(500, 'boom'), { status: 500 })));
    renderWithProviders(<SupplierListPage />, { user: staff });

    expect(await screen.findByText('Could not load suppliers.')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /try again/i })).toBeInTheDocument();
  });

  it('shows the empty state with an add action for admins', async () => {
    server.use(http.get(`${API}/suppliers`, () => HttpResponse.json(page([]))));
    renderWithProviders(<SupplierListPage />, { user: admin });

    expect(await screen.findByText('No suppliers yet')).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: /add supplier/i })).toHaveLength(2);
  });

  it('shows a duplicate-name conflict from the server', async () => {
    server.use(
      http.post(`${API}/suppliers`, () =>
        HttpResponse.json(problem(409, "A supplier named 'Acme' already exists"), { status: 409 }),
      ),
    );
    renderWithProviders(<SupplierListPage />, { user: admin });
    await screen.findByText('Northwind Traders');

    await userEvent.click(screen.getByRole('button', { name: /add supplier/i }));
    const dialog = screen.getByRole('dialog', { name: /add supplier/i });
    await userEvent.type(within(dialog).getByLabelText('Name'), 'Acme');
    await userEvent.click(within(dialog).getByRole('button', { name: /save/i }));

    expect(await within(dialog).findByText("A supplier named 'Acme' already exists")).toBeInTheDocument();
  });

  it('validates the email client-side', async () => {
    renderWithProviders(<SupplierListPage />, { user: admin });
    await screen.findByText('Northwind Traders');

    await userEvent.click(screen.getByRole('button', { name: /add supplier/i }));
    const dialog = screen.getByRole('dialog');
    await userEvent.type(within(dialog).getByLabelText('Name'), 'Acme');
    await userEvent.type(within(dialog).getByLabelText(/email/i), 'not-an-email');
    await userEvent.click(within(dialog).getByRole('button', { name: /save/i }));

    expect(await within(dialog).findByText('Enter a valid email address')).toBeInTheDocument();
  });

  it('explains why a supplier with products cannot be deleted', async () => {
    server.use(
      http.delete(`${API}/suppliers/:id`, () =>
        HttpResponse.json(problem(409, "Supplier 'Northwind Traders' has products and cannot be deleted"), {
          status: 409,
        }),
      ),
    );
    renderWithProviders(<SupplierListPage />, { user: admin });

    await userEvent.click(await screen.findByRole('button', { name: 'Delete Northwind Traders' }));
    const dialog = screen.getByRole('dialog', { name: /delete supplier/i });
    await userEvent.click(within(dialog).getByRole('button', { name: 'Delete' }));

    expect(await within(dialog).findByText(/has products and cannot be deleted/)).toBeInTheDocument();
  });
});
