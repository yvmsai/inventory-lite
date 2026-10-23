import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../../test/render';
import { LoginPage } from './LoginPage';

const renderLogin = () => renderWithProviders(<LoginPage />, { route: '/login', path: '/login' });

describe('LoginPage', () => {
  it('validates the form before calling the API', async () => {
    renderLogin();

    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Enter a valid email address')).toBeInTheDocument();
    expect(screen.getByText('Password is required')).toBeInTheDocument();
  });

  it('shows a generic error for bad credentials', async () => {
    renderLogin();

    await userEvent.type(screen.getByLabelText('Email'), 'admin@inventory.local');
    await userEvent.type(screen.getByLabelText('Password'), 'wrong');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Invalid email or password')).toBeInTheDocument();
  });

  it('signs in and goes home', async () => {
    renderLogin();

    await userEvent.type(screen.getByLabelText('Email'), 'admin@inventory.local');
    await userEvent.type(screen.getByLabelText('Password'), 'Admin123!');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Home screen')).toBeInTheDocument();
  });
});
