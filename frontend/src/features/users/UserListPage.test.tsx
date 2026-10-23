import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { admin, API, problem, staff } from '../../test/fixtures';
import { renderWithProviders } from '../../test/render';
import { server } from '../../test/server';
import { UserListPage } from './UserListPage';

describe('UserListPage', () => {
  it('prevents admins from deactivating or demoting themselves', async () => {
    renderWithProviders(<UserListPage />, { user: admin });

    const table = await screen.findByRole('table', { name: 'Users' });
    expect(within(table).getByLabelText('Role for admin@inventory.local')).toBeDisabled();
    expect(within(table).queryByRole('button', { name: 'Deactivate admin@inventory.local' })).not.toBeInTheDocument();
    expect(within(table).getByLabelText('Role for staff@inventory.local')).toBeEnabled();
  });

  it('deactivates another user after confirmation', async () => {
    let called = false;
    server.use(
      http.post(`${API}/users/:id/deactivate`, () => {
        called = true;
        return HttpResponse.json({ ...staff, active: false });
      }),
    );
    renderWithProviders(<UserListPage />, { user: admin });

    await userEvent.click(await screen.findByRole('button', { name: 'Deactivate staff@inventory.local' }));
    await userEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Deactivate' }));

    await screen.findByRole('table', { name: 'Users' });
    expect(called).toBe(true);
  });

  it('creates a user and shows a duplicate-email conflict', async () => {
    server.use(
      http.post(`${API}/users`, () =>
        HttpResponse.json(problem(409, "A user with email 'sam@x.com' already exists"), { status: 409 }),
      ),
    );
    renderWithProviders(<UserListPage />, { user: admin });
    await userEvent.click(await screen.findByRole('button', { name: /add user/i }));
    const dialog = screen.getByRole('dialog', { name: /add user/i });

    await userEvent.type(within(dialog).getByLabelText('Email'), 'sam@x.com');
    await userEvent.type(within(dialog).getByLabelText('Full name'), 'Sam');
    await userEvent.type(within(dialog).getByLabelText('Initial password'), 'short');
    await userEvent.click(within(dialog).getByRole('button', { name: /create user/i }));
    expect(await within(dialog).findByText('At least 8 characters')).toBeInTheDocument();

    await userEvent.type(within(dialog).getByLabelText('Initial password'), '-long-enough');
    await userEvent.click(within(dialog).getByRole('button', { name: /create user/i }));
    expect(await within(dialog).findByText(/already exists/)).toBeInTheDocument();
  });
});
