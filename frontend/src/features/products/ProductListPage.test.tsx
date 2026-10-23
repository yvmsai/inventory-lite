import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { admin, API, page, problem, staff } from '../../test/fixtures';
import { renderWithProviders } from '../../test/render';
import { server } from '../../test/server';
import { ProductListPage } from './ProductListPage';

describe('ProductListPage', () => {
  it('shows products with a low-stock badge only on low items', async () => {
    renderWithProviders(<ProductListPage />, { user: staff });

    const table = await screen.findByRole('table', { name: 'Products' });
    const teaRow = within(table).getByText('Green tea').closest('tr')!;
    const coffeeRow = within(table).getByText('Coffee beans 1 kg').closest('tr')!;
    expect(within(teaRow).getByText('Low stock')).toBeInTheDocument();
    expect(within(coffeeRow).queryByText('Low stock')).not.toBeInTheDocument();
    expect(within(coffeeRow).getByRole('link', { name: 'Coffee beans 1 kg' })).toHaveAttribute('href', '/products/100');
  });

  it('filters by supplier', async () => {
    const seen: (string | null)[] = [];
    server.use(
      http.get(`${API}/products`, ({ request }) => {
        seen.push(new URL(request.url).searchParams.get('supplierId'));
        return HttpResponse.json(page([]));
      }),
    );
    renderWithProviders(<ProductListPage />, { user: staff });
    const filter = screen.getByRole('combobox', { name: /filter by supplier/i });
    await screen.findByRole('option', { name: 'Northwind Traders' });

    await userEvent.selectOptions(filter, 'Northwind Traders');

    await waitFor(() => expect(seen).toContain('10'));
    expect(await screen.findByText('No products match your filters')).toBeInTheDocument();
  });

  it('validates SKU format and price before submitting', async () => {
    renderWithProviders(<ProductListPage />, { user: admin });
    await userEvent.click(await screen.findByRole('button', { name: /add product/i }));
    const dialog = screen.getByRole('dialog', { name: /add product/i });

    await userEvent.type(within(dialog).getByLabelText('SKU'), 'ab c');
    await userEvent.type(within(dialog).getByLabelText('Name'), 'Thing');
    await userEvent.type(within(dialog).getByLabelText('Unit price'), '1.234');
    await userEvent.click(within(dialog).getByRole('button', { name: /save/i }));

    expect(await within(dialog).findByText('Use only uppercase letters, digits and hyphens')).toBeInTheDocument();
    expect(within(dialog).getByText('Enter a price of 0 or more, like 19.99')).toBeInTheDocument();
    expect(within(dialog).getByText('Choose a supplier', { selector: 'p' })).toBeInTheDocument();
  });

  it('maps server field errors onto the form', async () => {
    let body: Record<string, unknown> | undefined;
    server.use(
      http.post(`${API}/products`, async ({ request }) => {
        body = (await request.json()) as Record<string, unknown>;
        return HttpResponse.json(problem(400, 'One or more fields are invalid', [{ field: 'sku', message: 'is taken' }]), {
          status: 400,
        });
      }),
    );
    renderWithProviders(<ProductListPage />, { user: admin });
    await userEvent.click(await screen.findByRole('button', { name: /add product/i }));
    const dialog = screen.getByRole('dialog');
    await within(dialog).findByRole('option', { name: 'Northwind Traders' });

    await userEvent.type(within(dialog).getByLabelText('SKU'), 'abc-1');
    await userEvent.type(within(dialog).getByLabelText('Name'), 'Thing');
    await userEvent.type(within(dialog).getByLabelText('Unit price'), '2.50');
    await userEvent.selectOptions(within(dialog).getByLabelText('Supplier'), 'Northwind Traders');
    await userEvent.click(within(dialog).getByRole('button', { name: /save/i }));

    expect(await within(dialog).findByText('is taken')).toBeInTheDocument();
    expect(body).toMatchObject({ sku: 'ABC-1', unitPrice: '2.50', reorderLevel: 10, supplierId: 10 });
  });

  it('hides admin actions from staff', async () => {
    renderWithProviders(<ProductListPage />, { user: staff });
    await screen.findByRole('table', { name: 'Products' });
    expect(screen.queryByRole('button', { name: /add product/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /^edit/i })).not.toBeInTheDocument();
  });
});
