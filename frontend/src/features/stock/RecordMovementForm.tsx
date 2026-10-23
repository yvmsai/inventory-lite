import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { Button } from '../../shared/components/Button';
import { FormField } from '../../shared/components/FormField';
import { inputClass } from '../../shared/components/styles';
import { applyServerErrors } from '../../shared/lib/applyServerErrors';
import { formatNumber } from '../../shared/lib/format';
import { useRecordMovement } from './hooks';

const schema = z.object({
  type: z.enum(['IN', 'OUT']),
  quantity: z.string().trim().regex(/^[1-9]\d*$/, 'Enter a whole number greater than 0'),
  note: z.string().trim().max(500, 'At most 500 characters'),
});
type FormValues = z.infer<typeof schema>;

interface RecordMovementFormProps {
  productId: number;
  available: number;
}

export function RecordMovementForm({ productId, available }: RecordMovementFormProps) {
  const record = useRecordMovement(productId);
  const [saved, setSaved] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { type: 'IN', quantity: '', note: '' } });

  const onSubmit = handleSubmit(async (values) => {
    setSaved(null);
    try {
      const movement = await record.mutateAsync({
        type: values.type,
        quantity: Number(values.quantity),
        note: values.note || undefined,
      });
      reset({ type: values.type, quantity: '', note: '' });
      setSaved(`Recorded. New quantity: ${formatNumber(movement.quantityAfter)}`);
    } catch (e) {
      applyServerErrors(e, setError, ['type', 'quantity', 'note']);
    }
  });

  return (
    <form onSubmit={onSubmit} noValidate className="space-y-3">
      <fieldset>
        <legend className="text-sm font-medium text-slate-700">Type</legend>
        <div className="mt-1 flex gap-4 text-sm">
          <label className="flex items-center gap-2">
            <input type="radio" value="IN" {...register('type')} /> Stock in
          </label>
          <label className="flex items-center gap-2">
            <input type="radio" value="OUT" {...register('type')} /> Stock out
          </label>
        </div>
      </fieldset>
      <FormField id="movement-quantity" label="Quantity" error={errors.quantity?.message} hint={`${formatNumber(available)} in stock`}>
        <input
          id="movement-quantity"
          inputMode="numeric"
          className={inputClass}
          aria-invalid={!!errors.quantity}
          aria-describedby="movement-quantity-error"
          {...register('quantity')}
        />
      </FormField>
      <FormField id="movement-note" label="Note (optional)" error={errors.note?.message}>
        <textarea
          id="movement-note"
          rows={2}
          className={inputClass}
          aria-invalid={!!errors.note}
          aria-describedby="movement-note-error"
          {...register('note')}
        />
      </FormField>
      {errors.root && (
        <p role="alert" className="text-sm text-red-700">
          {errors.root.message}
        </p>
      )}
      {saved && (
        <p role="status" className="text-sm text-emerald-800">
          {saved}
        </p>
      )}
      <Button type="submit" className="w-full" disabled={isSubmitting}>
        Record movement
      </Button>
    </form>
  );
}
