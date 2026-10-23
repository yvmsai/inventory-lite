import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import type { Supplier } from '../../shared/api/types';
import { Button } from '../../shared/components/Button';
import { Dialog } from '../../shared/components/Dialog';
import { FormField } from '../../shared/components/FormField';
import { inputClass } from '../../shared/components/styles';
import { applyServerErrors } from '../../shared/lib/applyServerErrors';
import { useSaveSupplier } from './hooks';
import { supplierSchema, type SupplierFormValues } from './schemas';

interface SupplierFormDialogProps {
  supplier?: Supplier;
  onClose: () => void;
}

export function SupplierFormDialog({ supplier, onClose }: SupplierFormDialogProps) {
  const save = useSaveSupplier();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<SupplierFormValues>({
    resolver: zodResolver(supplierSchema),
    defaultValues: { name: supplier?.name ?? '', email: supplier?.email ?? '', phone: supplier?.phone ?? '' },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await save.mutateAsync({ id: supplier?.id, body: { ...values, version: supplier?.version } });
      onClose();
    } catch (e) {
      applyServerErrors(e, setError, ['name', 'email', 'phone']);
    }
  });

  const field = (name: keyof SupplierFormValues) => ({
    id: `supplier-${name}`,
    className: inputClass,
    'aria-invalid': !!errors[name],
    'aria-describedby': `supplier-${name}-error`,
    ...register(name),
  });

  return (
    <Dialog title={supplier ? 'Edit supplier' : 'Add supplier'} onClose={onClose}>
      <form onSubmit={onSubmit} noValidate className="space-y-4">
        <FormField id="supplier-name" label="Name" error={errors.name?.message}>
          <input {...field('name')} />
        </FormField>
        <FormField id="supplier-email" label="Email (optional)" error={errors.email?.message}>
          <input type="email" {...field('email')} />
        </FormField>
        <FormField id="supplier-phone" label="Phone (optional)" error={errors.phone?.message}>
          <input type="tel" {...field('phone')} />
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
            Save
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
