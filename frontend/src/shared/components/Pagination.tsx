import { Button } from './Button';

interface PaginationProps {
  page: number;
  totalPages: number;
  totalElements: number;
  onChange: (page: number) => void;
}

export function Pagination({ page, totalPages, totalElements, onChange }: PaginationProps) {
  if (totalPages <= 1) return <p className="text-sm text-slate-600">{totalElements} total</p>;
  return (
    <nav aria-label="Pagination" className="flex items-center justify-between gap-4">
      <p className="text-sm text-slate-600">
        Page {page + 1} of {totalPages} · {totalElements} total
      </p>
      <div className="flex gap-2">
        <Button variant="secondary" disabled={page === 0} onClick={() => onChange(page - 1)}>
          Previous
        </Button>
        <Button variant="secondary" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>
          Next
        </Button>
      </div>
    </nav>
  );
}
