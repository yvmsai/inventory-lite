import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { Button } from '../../shared/components/Button';
import { Dialog } from '../../shared/components/Dialog';
import { FormField } from '../../shared/components/FormField';
import { inputClass } from '../../shared/components/styles';
import { applyServerErrors } from '../../shared/lib/applyServerErrors';
import { useCreateUser } from './hooks';

const schema = z.object({
  email: z.email('Enter a valid email address').max(200, 'At most 200 characters'),
  fullName: z.string().trim().min(1, 'Full name is required').max(150, 'At most 150 characters'),
  password: z.string().min(8, 'At least 8 characters').max(72, 'At most 72 characters'),
  role: z.enum(['ADMIN', 'STAFF']),
});
type FormValues = z.infer<typeof schema>;

export function UserFormDialog({ onClose }: { onClose: () => void }) {
  const create = useCreateUser();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: '', fullName: '', password: '', role: 'STAFF' },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await create.mutateAsync(values);
      onClose();
    } catch (e) {
      applyServerErrors(e, setError, ['email', 'fullName', 'password', 'role']);
    }
  });

  const a11y = (name: keyof FormValues) => ({
    id: `user-${name}`,
    className: inputClass,
    'aria-invalid': !!errors[name],
    'aria-describedby': `user-${name}-error`,
  });

  return (
    <Dialog title="Add user" onClose={onClose}>
      <form onSubmit={onSubmit} noValidate className="space-y-4">
        <FormField id="user-email" label="Email" error={errors.email?.message}>
          <input type="email" autoComplete="off" {...a11y('email')} {...register('email')} />
        </FormField>
        <FormField id="user-fullName" label="Full name" error={errors.fullName?.message}>
          <input {...a11y('fullName')} {...register('fullName')} />
        </FormField>
        <FormField
          id="user-password"
          label="Initial password"
          error={errors.password?.message}
          hint="At least 8 characters. Share it with the user securely."
        >
          <input type="password" autoComplete="new-password" {...a11y('password')} {...register('password')} />
        </FormField>
        <FormField id="user-role" label="Role" error={errors.role?.message}>
          <select {...a11y('role')} {...register('role')}>
            <option value="STAFF">Staff</option>
            <option value="ADMIN">Admin</option>
          </select>
        </FormField>
        {errors.root && (
          <p role="alert" className="text-sm text-red-700">
            {errors.root.message}
          </p>
        )}
        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            Create user
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
