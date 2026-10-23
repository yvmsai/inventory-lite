import { screen } from '@testing-library/react';
import { admin, staff } from '../../test/fixtures';
import { renderWithProviders } from '../../test/render';
import { RequireAuth } from './RequireAuth';

const guarded = (
  <RequireAuth role="ADMIN">
    <p>Admin area</p>
  </RequireAuth>
);

describe('RequireAuth', () => {
  it('redirects to login when signed out', () => {
    renderWithProviders(guarded, { route: '/users', path: '/users' });
    expect(screen.getByText('Login screen')).toBeInTheDocument();
  });

  it('redirects home when the role is missing', () => {
    renderWithProviders(guarded, { route: '/users', path: '/users', user: staff });
    expect(screen.getByText('Home screen')).toBeInTheDocument();
  });

  it('renders the page for the right role', () => {
    renderWithProviders(guarded, { route: '/users', path: '/users', user: admin });
    expect(screen.getByText('Admin area')).toBeInTheDocument();
  });
});
