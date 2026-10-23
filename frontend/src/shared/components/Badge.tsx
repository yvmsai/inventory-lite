import type { ReactNode } from 'react';

type Tone = 'neutral' | 'warning' | 'success' | 'danger' | 'info';

const tones: Record<Tone, string> = {
  neutral: 'bg-slate-100 text-slate-700',
  warning: 'bg-amber-100 text-amber-900',
  success: 'bg-emerald-100 text-emerald-900',
  danger: 'bg-red-100 text-red-900',
  info: 'bg-indigo-100 text-indigo-900',
};

export function Badge({ tone = 'neutral', children }: { tone?: Tone; children: ReactNode }) {
  return <span className={`inline-flex rounded-full px-2 py-0.5 text-xs font-medium ${tones[tone]}`}>{children}</span>;
}
