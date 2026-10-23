import { Button } from './Button';
import { Dialog } from './Dialog';

interface ConfirmDialogProps {
  title: string;
  message: string;
  confirmLabel: string;
  pending?: boolean;
  error?: string;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({ title, message, confirmLabel, pending, error, onConfirm, onCancel }: ConfirmDialogProps) {
  return (
    <Dialog title={title} onClose={onCancel}>
      <p className="text-sm text-slate-700">{message}</p>
      {error && (
        <p role="alert" className="mt-3 text-sm text-red-700">
          {error}
        </p>
      )}
      <div className="mt-6 flex justify-end gap-2">
        <Button variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button variant="danger" onClick={onConfirm} disabled={pending}>
          {confirmLabel}
        </Button>
      </div>
    </Dialog>
  );
}
