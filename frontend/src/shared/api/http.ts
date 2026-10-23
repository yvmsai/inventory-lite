const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export interface FieldError {
  field: string;
  message: string;
}

export class ApiError extends Error {
  readonly status: number;
  readonly title: string;
  readonly detail: string;
  readonly fieldErrors: FieldError[];

  constructor(status: number, title: string, detail: string, fieldErrors: FieldError[] = []) {
    super(detail || title);
    this.status = status;
    this.title = title;
    this.detail = detail;
    this.fieldErrors = fieldErrors;
  }

  static async from(res: Response): Promise<ApiError> {
    try {
      const body = (await res.json()) as { title?: string; detail?: string; errors?: FieldError[] };
      return new ApiError(res.status, body.title ?? res.statusText, body.detail ?? '', body.errors ?? []);
    } catch {
      return new ApiError(res.status, res.statusText, '');
    }
  }
}

let accessToken: string | null = null;
let onUnauthorized: () => void = () => {};

export const setAccessToken = (token: string | null) => {
  accessToken = token;
};
export const setUnauthorizedHandler = (handler: () => void) => {
  onUnauthorized = handler;
};

export async function http<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body !== undefined) headers.set('Content-Type', 'application/json');
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`);

  const res = await fetch(`${BASE_URL}${path}`, { ...init, headers });

  if (res.status === 401 && accessToken) onUnauthorized();
  if (!res.ok) throw await ApiError.from(res);
  return (res.status === 204 ? undefined : await res.json()) as T;
}

/** Builds a query string, skipping empty values. */
export function query(params: Record<string, string | number | undefined | null>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, String(value));
  });
  const text = search.toString();
  return text ? `?${text}` : '';
}

/** A readable message for any error thrown by a query or mutation. */
export function errorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  if (error instanceof ApiError) return error.detail || error.title || fallback;
  return fallback;
}
