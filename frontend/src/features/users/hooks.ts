import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { Role } from '../../shared/api/types';
import { createUser, listUsers, setUserActive, updateUser } from './api';

export const userKeys = {
  all: ['users'] as const,
  list: (page: number) => [...userKeys.all, 'list', page] as const,
};

export function useUsers(page: number) {
  return useQuery({ queryKey: userKeys.list(page), queryFn: () => listUsers(page), placeholderData: keepPreviousData });
}

function useUserMutation<T>(fn: (args: T) => Promise<unknown>) {
  const qc = useQueryClient();
  return useMutation({ mutationFn: fn, onSuccess: () => qc.invalidateQueries({ queryKey: userKeys.all }) });
}

export const useCreateUser = () => useUserMutation(createUser);
export const useChangeRole = () => useUserMutation(({ id, role }: { id: number; role: Role }) => updateUser(id, { role }));
export const useSetActive = () =>
  useUserMutation(({ id, active }: { id: number; active: boolean }) => setUserActive(id, active));
