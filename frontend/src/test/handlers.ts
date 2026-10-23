import { http, HttpResponse } from 'msw';
import { admin, API, dashboard, lowProduct, movement, page, problem, product, staff, supplier } from './fixtures';

/** Default happy-path handlers mirroring the API contract; tests override per case with server.use(). */
export const handlers = [
  http.post(`${API}/auth/login`, async ({ request }) => {
    const { password } = (await request.json()) as { email: string; password: string };
    return password === 'Admin123!'
      ? HttpResponse.json({ accessToken: 'token', expiresIn: 1800 })
      : HttpResponse.json(problem(401, 'Invalid email or password'), { status: 401 });
  }),
  http.get(`${API}/auth/me`, () => HttpResponse.json(admin)),
  http.get(`${API}/dashboard`, () => HttpResponse.json(dashboard)),
  http.get(`${API}/suppliers`, () => HttpResponse.json(page([supplier]))),
  http.post(`${API}/suppliers`, () => HttpResponse.json(supplier, { status: 201 })),
  http.get(`${API}/products`, () => HttpResponse.json(page([product, lowProduct]))),
  http.get(`${API}/products/:id`, () => HttpResponse.json(product)),
  http.post(`${API}/products`, () => HttpResponse.json(product, { status: 201 })),
  http.get(`${API}/products/:id/movements`, () => HttpResponse.json(page([movement]))),
  http.post(`${API}/products/:id/movements`, () =>
    HttpResponse.json({ ...movement, id: 1001, type: 'OUT', quantity: 2, quantityAfter: 40 }, { status: 201 }),
  ),
  http.get(`${API}/users`, () => HttpResponse.json(page([admin, staff]))),
  http.post(`${API}/users`, () => HttpResponse.json(staff, { status: 201 })),
];
