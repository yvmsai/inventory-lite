import { useState } from 'react';
import { ErrorState } from '../../shared/components/ErrorState';
import { EmptyState } from '../../shared/components/EmptyState';
import { Pagination } from '../../shared/components/Pagination';
import { Spinner } from '../../shared/components/Spinner';
import { useMovementHistory } from './hooks';
import { MovementTable } from './MovementTable';

export function MovementHistory({ productId }: { productId: number }) {
  const [page, setPage] = useState(0);
  const { data, isPending, isError, refetch } = useMovementHistory(productId, page);

  if (isPending) return <Spinner label="Loading movements" />;
  if (isError) return <ErrorState message="Could not load the movement history." onRetry={refetch} />;
  if (data.content.length === 0)
    return <EmptyState title="No movements yet" description="Record stock coming in or going out above." />;
  return (
    <div className="space-y-4">
      <MovementTable movements={data.content} caption="Movement history, newest first" />
      <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={setPage} />
    </div>
  );
}
