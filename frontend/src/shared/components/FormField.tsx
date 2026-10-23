import type { ReactNode } from 'react';

interface FormFieldProps {
  id: string;
  label: string;
  error?: string;
  hint?: string;
  children: ReactNode;
}

/** Label + control + inline error. The control must use the same `id` and `aria-describedby={`${id}-error`}`. */
export function FormField({ id, label, error, hint, children }: FormFieldProps) {
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={id} className="text-sm font-medium text-slate-700">
        {label}
      </label>
      {children}
      {hint && !error && <p className="text-xs text-slate-500">{hint}</p>}
      {error && (
        <p id={`${id}-error`} role="alert" className="text-xs text-red-700">
          {error}
        </p>
      )}
    </div>
  );
}
