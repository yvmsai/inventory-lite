import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import type { Product } from '../../shared/api/types';
import { Button } from '../../shared/components/Button';
import { Dialog } from '../../shared/components/Dialog';
import { FormField } from '../../shared/components/FormField';
import { inputClass } from '../../shared/components/styles';
import { applyServerErrors } from '../../shared/lib/applyServerErrors';
import { useSaveProduct, useSupplierOptions } from './hooks';
import { productSchema, type ProductFormValues } from './schemas';

interface ProductFormDialogProps {
  product?: Product;
  onClose: () => void;
}

const FIELDS = ['sku', 'name', 'unitPrice', 'reorderLevel', 'supplierId'];

export function ProductFormDialog({ product, onClose }: ProductFormDialogProps) {
  const save = useSaveProduct();
  const suppliers = useSupplierOptions();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ProductFormValues>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      sku: product?.sku ?? '',
      name: product?.name ?? '',
      unitPrice: product?.unitPrice ?? '',
      reorderLevel: String(product?.reorderLevel ?? 10),
      supplierId: product ? String(product.supplierId) : '',
    },
  });

  const onSubmit = handleSubmit(async (values) => {
    const body = {
      sku: values.sku,
      name: values.name,
      unitPrice: values.unitPrice,
      reorderLevel: Number(values.reorderLevel),
      supplierId: Number(values.supplierId),
      version: product?.version,
    };
    try {
      await save.mutateAsync({ id: product?.id, body });
      onClose();
    } catch (e) {
      applyServerErrors(e, setError, FIELDS);
    }
  });

  const a11y = (name: keyof ProductFormValues) => ({
    id: `product-${name}`,
    'aria-invalid': !!errors[name],
    'aria-describedby': `product-${name}-error`,
  });

  return (
    <Dialog title={product ? 'Edit product' : 'Add product'} onClose={onClose}>
      <form onSubmit={onSubmit} noValidate className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <FormField id="product-sku" label="SKU" error={errors.sku?.message} hint="Uppercase letters, digits, hyphens">
          <input
            {...a11y('sku')}
            className={`${inputClass} uppercase`}
            {...register('sku', { setValueAs: (v: string) => v.toUpperCase() })}
          />
        </FormField>
        <FormField id="product-name" label="Name" error={errors.name?.message}>
          <input {...a11y('name')} className={inputClass} {...register('name')} />
        </FormField>
        <FormField id="product-unitPrice" label="Unit price" error={errors.unitPrice?.message}>
          <input {...a11y('unitPrice')} inputMode="decimal" className={inputClass} {...register('unitPrice')} />
        </FormField>
        <FormField id="product-reorderLevel" label="Reorder level" error={errors.reorderLevel?.message}>
          <input {...a11y('reorderLevel')} inputMode="numeric" className={inputClass} {...register('reorderLevel')} />
        </FormField>
        <div className="sm:col-span-2">
          <FormField id="product-supplierId" label="Supplier" error={errors.supplierId?.message}>
            <select {...a11y('supplierId')} className={inputClass} {...register('supplierId')}>
              <option value="">{suppliers.isPending ? 'Loading suppliers…' : 'Choose a supplier'}</option>
              {suppliers.data?.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </FormField>
        </div>
        {errors.root && (
          <p role="alert" className="text-sm text-red-700 sm:col-span-2">
            {errors.root.message}
          </p>
        )}
        <div className="flex justify-end gap-2 sm:col-span-2">
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
