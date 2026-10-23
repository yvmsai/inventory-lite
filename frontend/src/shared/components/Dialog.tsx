import { useEffect, useId, useRef, type ReactNode } from 'react';

interface DialogProps {
  title: string;
  onClose: () => void;
  children: ReactNode;
}

/** Modal dialog: focus moves inside on open, Escape closes, focus returns to the opener on close. */
export function Dialog({ title, onClose, children }: DialogProps) {
  const titleId = useId();
  const panel = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const opener = document.activeElement as HTMLElement | null;
    const first = panel.current?.querySelector<HTMLElement>('input, select, textarea, button');
    first?.focus();
    return () => opener?.focus();
  }, []);

  return (
    <div className="fixed inset-0 z-20 flex items-center justify-center bg-slate-900/40 p-4">
      <div
        ref={panel}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onKeyDown={(e) => e.key === 'Escape' && onClose()}
        className="w-full max-w-lg rounded-lg bg-white p-6 shadow-xl"
      >
        <h2 id={titleId} className="mb-4 text-lg font-semibold">
          {title}
        </h2>
        {children}
      </div>
    </div>
  );
}
