import type { FieldValues, Path, UseFormSetError } from 'react-hook-form';
import { ApiError, errorMessage } from '../api/http';

/** Maps a 400 problem-detail's errors[] onto form fields; anything else becomes a form-level error. */
export function applyServerErrors<T extends FieldValues>(error: unknown, setError: UseFormSetError<T>, fields: string[]) {
  if (error instanceof ApiError && error.fieldErrors.length > 0) {
    const unknown: string[] = [];
    error.fieldErrors.forEach((f) => {
      if (fields.includes(f.field)) setError(f.field as Path<T>, { message: f.message });
      else unknown.push(`${f.field} ${f.message}`);
    });
    if (unknown.length > 0) setError('root', { message: unknown.join('; ') });
    return;
  }
  setError('root', { message: errorMessage(error) });
}
