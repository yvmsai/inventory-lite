import type { ReactNode } from 'react';

/** A horizontally scrollable, accessible table wrapper with a visually hidden caption. */
export function Table({ caption, head, children }: { caption: string; head: ReactNode; children: ReactNode }) {
  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
      <table className="min-w-full divide-y divide-slate-200 text-sm">
        <caption className="sr-only">{caption}</caption>
        <thead className="bg-slate-50 text-left text-xs font-semibold tracking-wide text-slate-600 uppercase">
          {head}
        </thead>
        <tbody className="divide-y divide-slate-100">{children}</tbody>
      </table>
    </div>
  );
}
