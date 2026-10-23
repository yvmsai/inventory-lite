-- Local profile only: demo accounts (passwords are listed in the README) and sample catalog data.
INSERT INTO app_user (email, full_name, password_hash, role) VALUES
    ('admin@inventory.local', 'Alex Admin', '$2a$10$jBOXh/wpfqJUn4Xxhx5GMujnmF1.9EbxxuWZ95ao36VgNecmAEoGK', 'ADMIN'),
    ('staff@inventory.local', 'Sam Staff', '$2a$10$fglBlEnxrtTIfAKdQ1DUjOvaW3avSm01DwbYNGW27NvDcnZ.DJOFC', 'STAFF');
