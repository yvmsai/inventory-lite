-- Local profile only: demo accounts (passwords are listed in the README) and sample catalog data.
INSERT INTO app_user (email, full_name, password_hash, role) VALUES
    ('admin@inventory.local', 'Alex Admin', '$2a$10$jBOXh/wpfqJUn4Xxhx5GMujnmF1.9EbxxuWZ95ao36VgNecmAEoGK', 'ADMIN'),
    ('staff@inventory.local', 'Sam Staff', '$2a$10$fglBlEnxrtTIfAKdQ1DUjOvaW3avSm01DwbYNGW27NvDcnZ.DJOFC', 'STAFF');

INSERT INTO supplier (name, email, phone) VALUES
    ('Northwind Traders', 'orders@northwind.example', '+1 555 0100'),
    ('Contoso Hardware', 'sales@contoso.example', '+1 555 0101'),
    ('Fabrikam Office', NULL, NULL);

INSERT INTO product (sku, name, unit_price, reorder_level, supplier_id)
SELECT v.sku, v.name, v.unit_price, v.reorder_level, s.id
FROM (VALUES
    ('NW-COF-1KG', 'Coffee beans 1 kg', 18.50, 10, 'Northwind Traders'),
    ('NW-TEA-100', 'Green tea, 100 bags', 6.25, 15, 'Northwind Traders'),
    ('CH-HAM-16', 'Claw hammer 16 oz', 14.99, 5, 'Contoso Hardware'),
    ('CH-SCR-M4', 'M4 screws, box of 200', 7.40, 20, 'Contoso Hardware'),
    ('FO-PAP-A4', 'A4 copy paper, 500 sheets', 5.10, 30, 'Fabrikam Office'),
    ('FO-PEN-BLU', 'Blue ballpoint pens, 12-pack', 3.80, 10, 'Fabrikam Office')
) AS v(sku, name, unit_price, reorder_level, supplier_name)
JOIN supplier s ON s.name = v.supplier_name;

-- Opening stock, recorded as movements so quantities and history agree.
INSERT INTO stock_movement (product_id, type, quantity, quantity_after, note, created_by)
SELECT p.id, 'IN', v.qty, v.qty, 'Opening stock', u.id
FROM (VALUES ('NW-COF-1KG', 42), ('NW-TEA-100', 8), ('CH-HAM-16', 12),
             ('CH-SCR-M4', 3), ('FO-PAP-A4', 120), ('FO-PEN-BLU', 10)) AS v(sku, qty)
JOIN product p ON p.sku = v.sku
JOIN app_user u ON u.email = 'admin@inventory.local';

UPDATE product p SET quantity = m.quantity_after
FROM stock_movement m WHERE m.product_id = p.id;
