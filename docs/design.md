# Inventory Lite: Design Document

## 1. Overview

Inventory Lite helps a small business track products, suppliers and stock levels. Staff
record stock coming in and going out. Admins manage the catalog and see a dashboard of
low-stock items.

## 2. Users and roles

| Role | Description | What they can do |
|---|---|---|
| ADMIN | Owner or manager | Everything: manage suppliers, products, users; record stock movements; view dashboard |
| STAFF | Warehouse staff | View suppliers and products; record stock movements; view dashboard |

## 3. Features

### 3.1 Authentication

- Users log in with email and password. There is no self-registration: an ADMIN creates users.
- Business rules:
  - Login fails with the same message for an unknown email and a wrong password.

### 3.2 User management (ADMIN)

- An ADMIN can create, list and deactivate users, and assign a role.
- Business rules:
  - Email is unique (case-insensitive).
  - A deactivated user cannot log in.
  - An ADMIN cannot deactivate themselves.

### 3.3 Suppliers

- Create, edit, list (searchable by name) and delete suppliers.
- Business rules:
  - Supplier name is unique.
  - A supplier with products cannot be deleted.

### 3.4 Products

- Create, edit, list (search by name or SKU, filter by supplier) and delete products.
- Business rules:
  - SKU is unique, uppercase letters, digits and hyphens, max 64 characters.
  - Unit price is ≥ 0 with 2 decimal places.
  - Each product has a reorder level (integer ≥ 0, default 10).
  - A product with stock movements cannot be deleted.

### 3.5 Stock movements

- Record a movement: product, type (IN or OUT), quantity, optional note.
- The product's current quantity updates when a movement is recorded.
- View the movement history per product, newest first.
- Business rules:
  - Quantity is a positive integer.
  - An OUT movement that would make stock negative is rejected.
  - Movements are immutable: they can't be edited or deleted.
  - Concurrent movements on the same product must not lose updates.

### 3.6 Dashboard

- Totals: number of products, number of suppliers, total stock value (sum of quantity × unit price).
- Low-stock list: products where quantity ≤ reorder level, lowest first.
- The 10 most recent stock movements.

## 4. Data model

### Supplier

| Field | Type | Required | Rules |
|---|---|---|---|
| name | string(150) | yes | unique |
| email | string(200) | no | valid email |
| phone | string(30) | no | |

### Product

| Field | Type | Required | Rules |
|---|---|---|---|
| sku | string(64) | yes | unique, `^[A-Z0-9-]+$` |
| name | string(200) | yes | |
| unitPrice | decimal(19,2) | yes | ≥ 0 |
| quantity | integer | yes | ≥ 0, changed only by stock movements |
| reorderLevel | integer | yes | ≥ 0, default 10 |
| supplier | Supplier | yes | |

### StockMovement

| Field | Type | Required | Rules |
|---|---|---|---|
| product | Product | yes | |
| type | enum IN, OUT | yes | |
| quantity | integer | yes | > 0 |
| note | string(500) | no | |
| createdBy | User | yes | set from the logged-in user |

### User

| Field | Type | Required | Rules |
|---|---|---|---|
| email | string(200) | yes | unique, case-insensitive |
| fullName | string(150) | yes | |
| role | enum ADMIN, STAFF | yes | |
| active | boolean | yes | default true |

## 5. Screens

| Screen | Who | Shows | Actions |
|---|---|---|---|
| Login | public | Email, password | Log in |
| Dashboard | all | Totals, low-stock list, recent movements | Jump to a product |
| Suppliers | all (edit: ADMIN) | Paginated searchable table | Add, edit, delete |
| Products | all (edit: ADMIN) | Paginated table: SKU, name, supplier, qty, price, low-stock badge | Add, edit, delete, open detail |
| Product detail | all | Product info + movement history | Record movement |
| Users | ADMIN | Users table | Add user, change role, deactivate |

## 6. Non-functional requirements

- Desktop-first, usable on a tablet.
- Lists load in under 1 s for 10,000 products.
- Every change records who did it and when.

## 7. Out of scope

- Multiple warehouses or locations
- Purchase orders and invoicing
- Password reset by email
