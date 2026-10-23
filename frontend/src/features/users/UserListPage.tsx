import { useState } from 'react';
import { errorMessage } from '../../shared/api/http';
import type { Role, User } from '../../shared/api/types';
import { Badge } from '../../shared/components/Badge';
import { Button } from '../../shared/components/Button';
import { ConfirmDialog } from '../../shared/components/ConfirmDialog';
import { EmptyState } from '../../shared/components/EmptyState';
import { ErrorState } from '../../shared/components/ErrorState';
import { PageHeader } from '../../shared/components/PageHeader';
import { Pagination } from '../../shared/components/Pagination';
import { Spinner } from '../../shared/components/Spinner';
import { Table } from '../../shared/components/Table';
import { td, th } from '../../shared/components/styles';
import { useAuth } from '../auth/useAuth';
import { UserFormDialog } from './UserFormDialog';
import { useChangeRole, useSetActive, useUsers } from './hooks';

export function UserListPage() {
  const { user: me } = useAuth();
  const [page, setPage] = useState(0);
  const { data, isPending, isError, refetch } = useUsers(page);
  const [adding, setAdding] = useState(false);
  const [deactivating, setDeactivating] = useState<User | null>(null);
  const changeRole = useChangeRole();
  const setActive = useSetActive();

  return (
    <>
      <PageHeader title="Users" actions={<Button onClick={() => setAdding(true)}>Add user</Button>} />
      {changeRole.isError && (
        <p role="alert" className="mb-4 text-sm text-red-700">
          {errorMessage(changeRole.error)}
        </p>
      )}
      {isPending ? (
        <Spinner label="Loading users" />
      ) : isError ? (
        <ErrorState message="Could not load users." onRetry={refetch} />
      ) : data.content.length === 0 ? (
        <EmptyState title="No users yet" action={<Button onClick={() => setAdding(true)}>Add user</Button>} />
      ) : (
        <div className="space-y-4">
          <Table
            caption="Users"
            head={
              <tr>
                <th className={th}>Name</th>
                <th className={th}>Email</th>
                <th className={th}>Role</th>
                <th className={th}>Status</th>
                <th className={`${th} text-right`}>Actions</th>
              </tr>
            }
          >
            {data.content.map((u) => {
              const isMe = u.id === me?.id;
              return (
                <tr key={u.id}>
                  <td className={`${td} font-medium`}>
                    {u.fullName} {isMe && <span className="text-slate-500">(you)</span>}
                  </td>
                  <td className={td}>{u.email}</td>
                  <td className={td}>
                    <label htmlFor={`role-${u.id}`} className="sr-only">
                      Role for {u.email}
                    </label>
                    <select
                      id={`role-${u.id}`}
                      value={u.role}
                      disabled={isMe || changeRole.isPending}
                      onChange={(e) => changeRole.mutate({ id: u.id, role: e.target.value as Role })}
                      className="rounded-md border border-slate-300 bg-white px-2 py-1 text-sm disabled:bg-slate-100"
                    >
                      <option value="STAFF">Staff</option>
                      <option value="ADMIN">Admin</option>
                    </select>
                  </td>
                  <td className={td}>
                    <Badge tone={u.active ? 'success' : 'neutral'}>{u.active ? 'Active' : 'Deactivated'}</Badge>
                  </td>
                  <td className={`${td} text-right`}>
                    {!isMe &&
                      (u.active ? (
                        <Button
                          variant="ghost"
                          onClick={() => {
                            setActive.reset();
                            setDeactivating(u);
                          }}
                          aria-label={`Deactivate ${u.email}`}
                        >
                          Deactivate
                        </Button>
                      ) : (
                        <Button
                          variant="ghost"
                          disabled={setActive.isPending}
                          onClick={() => setActive.mutate({ id: u.id, active: true })}
                          aria-label={`Activate ${u.email}`}
                        >
                          Activate
                        </Button>
                      ))}
                  </td>
                </tr>
              );
            })}
          </Table>
          <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={setPage} />
        </div>
      )}
      {adding && <UserFormDialog onClose={() => setAdding(false)} />}
      {deactivating && (
        <ConfirmDialog
          title="Deactivate user"
          message={`${deactivating.fullName} will no longer be able to log in. You can reactivate them later.`}
          confirmLabel="Deactivate"
          pending={setActive.isPending}
          error={setActive.isError ? errorMessage(setActive.error) : undefined}
          onConfirm={() =>
            setActive.mutate({ id: deactivating.id, active: false }, { onSuccess: () => setDeactivating(null) })
          }
          onCancel={() => setDeactivating(null)}
        />
      )}
    </>
  );
}
