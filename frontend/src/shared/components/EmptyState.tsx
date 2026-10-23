import type { ReactNode } from 'react';

interface EmptyStateProps {
  title: string;
  description?: string;
  action?: ReactNode;
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center gap-2 rounded-md border border-dashed border-slate-300 bg-white p-10 text-center">
      <p className="font-medium text-slate-800">{title}</p>
      {description && <p className="text-sm text-slate-600">{description}</p>}
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
}
