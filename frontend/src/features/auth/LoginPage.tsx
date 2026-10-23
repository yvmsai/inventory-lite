import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { ApiError } from '../../shared/api/http';
import { Button } from '../../shared/components/Button';
import { FormField } from '../../shared/components/FormField';
import { inputClass } from '../../shared/components/styles';
import { useAuth } from './useAuth';

const schema = z.object({
  email: z.email('Enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
});
type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/';
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  if (user) return <Navigate to={from} replace />;

  const onSubmit = handleSubmit(async ({ email, password }) => {
    try {
      await login(email, password);
      navigate(from, { replace: true });
    } catch (e) {
      const message =
        e instanceof ApiError && e.status === 401 ? 'Invalid email or password' : 'Could not sign in. Please try again.';
      setError('root', { message });
    }
  });

  return (
    <main className="flex min-h-screen items-center justify-center p-4">
      <form onSubmit={onSubmit} noValidate className="w-full max-w-sm space-y-4 rounded-lg bg-white p-8 shadow">
        <div>
          <h1 className="text-2xl font-semibold">Inventory Lite</h1>
          <p className="text-sm text-slate-600">Sign in to continue</p>
        </div>
        <FormField id="email" label="Email" error={errors.email?.message}>
          <input
            id="email"
            type="email"
            autoComplete="username"
            className={inputClass}
            aria-invalid={!!errors.email}
            aria-describedby="email-error"
            {...register('email')}
          />
        </FormField>
        <FormField id="password" label="Password" error={errors.password?.message}>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            className={inputClass}
            aria-invalid={!!errors.password}
            aria-describedby="password-error"
            {...register('password')}
          />
        </FormField>
        {errors.root && (
          <p role="alert" className="text-sm text-red-700">
            {errors.root.message}
          </p>
        )}
        <Button type="submit" className="w-full" disabled={isSubmitting}>
          {isSubmitting ? 'Signing in…' : 'Sign in'}
        </Button>
      </form>
    </main>
  );
}
