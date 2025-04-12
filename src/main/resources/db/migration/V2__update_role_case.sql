-- Update all role values from uppercase to lowercase
UPDATE users SET role = 'admin' WHERE role = 'ADMIN';
UPDATE users SET role = 'manager' WHERE role = 'MANAGER';
UPDATE users SET role = 'representative' WHERE role = 'REPRESENTATIVE';
