import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { API, page, problem, staff } from '../../test/fixtures';
import { renderWithProviders } from '../../test/render';
import { server } from '../../test/server';
import { ProductDetailPage } from './ProductDetailPage';

const renderDetail = () =>
  renderWithProviders(<ProductDetailPage />, { route: '/products/100', path: '/products/:id', user: staff });

describe('ProductDetailPage', () => {
  it('shows product info and movement history', async () => {
    renderDetail();

    expect(await screen.findByRole('heading', { name: 'Coffee beans 1 kg' })).toBeInTheDocument();
    const history = await screen.findByRole('table', { name: /movement history/i });
    expect(within(history).getByText('Opening stock')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /edit product/i })).not.toBeInTheDocument();
  });

  it('records a movement and reports the new quantity', async () => {
    let body: unknown;
    server.use(
      http.post(`${API}/products/:id/movements`, async ({ request }) => {
        body = await request.json();
        return HttpResponse.json(
          { ...(await import('../../test/fixtures')).movement, type: 'OUT', quantity: 2, quantityAfter: 40 },
          { status: 201 },
        );
      }),
    );
    renderDetail();
    await screen.findByRole('heading', { name: 'Coffee beans 1 kg' });

    await userEvent.click(screen.getByLabelText('Stock out'));
    await userEvent.type(screen.getByLabelText('Quantity'), '2');
    await userEvent.click(screen.getByRole('button', { name: /record movement/i }));

    expect(await screen.findByText('Recorded. New quantity: 40')).toBeInTheDocument();
    expect(body).toEqual({ type: 'OUT', quantity: 2 });
  });

  it('rejects a non-positive quantity before calling the API', async () => {
    renderDetail();
    await screen.findByRole('heading', { name: 'Coffee beans 1 kg' });

    await userEvent.type(screen.getByLabelText('Quantity'), '0');
    await userEvent.click(screen.getByRole('button', { name: /record movement/i }));

    expect(await screen.findByText('Enter a whole number greater than 0')).toBeInTheDocument();
  });

  it('shows the insufficient-stock rule from the server', async () => {
    server.use(
      http.post(`${API}/products/:id/movements`, () =>
        HttpResponse.json(problem(422, 'Insufficient stock for NW-COF-1KG: 42 available, 50 requested'), {
          status: 422,
        }),
      ),
    );
    renderDetail();
    await screen.findByRole('heading', { name: 'Coffee beans 1 kg' });

    await userEvent.click(screen.getByLabelText('Stock out'));
    await userEvent.type(screen.getByLabelText('Quantity'), '50');
    await userEvent.click(screen.getByRole('button', { name: /record movement/i }));

    expect(await screen.findByText(/Insufficient stock/)).toBeInTheDocument();
  });

  it('shows not found for an unknown product', async () => {
    server.use(http.get(`${API}/products/:id`, () => HttpResponse.json(problem(404, 'nope'), { status: 404 })));
    renderDetail();

    expect(await screen.findByText('Product not found')).toBeInTheDocument();
  });

  it('shows an empty history', async () => {
    server.use(http.get(`${API}/products/:id/movements`, () => HttpResponse.json(page([]))));
    renderDetail();

    expect(await screen.findByText('No movements yet')).toBeInTheDocument();
  });
});
