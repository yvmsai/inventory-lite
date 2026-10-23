import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../../features/auth/useAuth';
import { Button } from '../../shared/components/Button';

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-50 text-indigo-800' : 'text-slate-700 hover:bg-slate-100'}`;

export function AppShell() {
  const { user, logout } = useAuth();
  return (
    <div className="min-h-screen">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-3 px-4 py-3">
          <div className="flex flex-wrap items-center gap-4">
            <span className="font-semibold text-indigo-700">Inventory Lite</span>
            <nav aria-label="Main" className="flex flex-wrap gap-1">
              <NavLink to="/" end className={linkClass}>
                Dashboard
              </NavLink>
              <NavLink to="/products" className={linkClass}>
                Products
              </NavLink>
              <NavLink to="/suppliers" className={linkClass}>
                Suppliers
              </NavLink>
              {user?.role === 'ADMIN' && (
                <NavLink to="/users" className={linkClass}>
                  Users
                </NavLink>
              )}
            </nav>
          </div>
          <div className="flex items-center gap-3 text-sm">
            <span className="text-slate-600">
              {user?.fullName} <span className="text-slate-400">({user?.role})</span>
            </span>
            <Button variant="secondary" onClick={logout}>
              Log out
            </Button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
