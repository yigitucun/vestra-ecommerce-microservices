-- Seed mock users for development and demo testing
-- Password for all mock users is: Password123!
INSERT INTO users (id, email, first_name, last_name, password, role, is_active, created_at)
VALUES 
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'admin@vestra.com', 'Admin', 'Vestra', '$2a$10$g2KfnO7SggTBLmM8.Uwu3u4GFK.exrHDtriSLYGILnx.fCWkgRGxu', 'ADMIN', true, now()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'ahmet.yilmaz@vestra.com', 'Ahmet', 'Yılmaz', '$2a$10$g2KfnO7SggTBLmM8.Uwu3u4GFK.exrHDtriSLYGILnx.fCWkgRGxu', 'CUSTOMER', true, now()),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'ayse.kaya@vestra.com', 'Ayşe', 'Kaya', '$2a$10$g2KfnO7SggTBLmM8.Uwu3u4GFK.exrHDtriSLYGILnx.fCWkgRGxu', 'CUSTOMER', true, now()),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'mehmet.demir@vestra.com', 'Mehmet', 'Demir', '$2a$10$g2KfnO7SggTBLmM8.Uwu3u4GFK.exrHDtriSLYGILnx.fCWkgRGxu', 'CUSTOMER', true, now())
ON CONFLICT (id) DO NOTHING;
