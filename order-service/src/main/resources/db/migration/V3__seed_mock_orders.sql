-- Seed mock orders and order items
INSERT INTO orders (id, order_number, user_id, status, total_amount, shipping_address, customer_email, version, created_at, updated_at) VALUES
    ('50000000-0000-0000-0000-000000000001', 'ORD-2026-001', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'COMPLETED', 2948.99, 'Atatürk Mah. Karanfil Sok. No:14 D:5 Kadıköy / İstanbul', 'ahmet.yilmaz@vestra.com', 0, now() - INTERVAL '3 days', now() - INTERVAL '2 days'),
    ('50000000-0000-0000-0000-000000000002', 'ORD-2026-002', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'SHIPPED', 1498.99, 'Çankaya Mah. Tunalı Hilmi Cad. No:82/3 Çankaya / Ankara', 'ayse.kaya@vestra.com', 0, now() - INTERVAL '1 day', now() - INTERVAL '12 hours'),
    ('50000000-0000-0000-0000-000000000003', 'ORD-2026-003', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'PENDING', 3999.00, 'Atatürk Mah. Karanfil Sok. No:14 D:5 Kadıköy / İstanbul', 'ahmet.yilmaz@vestra.com', 0, now() - INTERVAL '2 hours', now() - INTERVAL '2 hours')
ON CONFLICT (id) DO NOTHING;

INSERT INTO order_items (id, order_id, variant_id, product_name, sku, quantity, unit_price, subtotal) VALUES
    -- Order 1 items (Ahmet)
    ('51000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000001', 'Vestra Studio ANC Kablosuz Kulaklık', 'HP-ANC-BLK', 1, 2499.00, 2499.00),
    ('51000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001', '11000000-0000-0000-0000-000000000002', 'Oversize Ağır Pamuklu T-Shirt', 'TSHIRT-BLK-M', 1, 449.99, 449.99),

    -- Order 2 items (Ayşe)
    ('52000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000002', 'Slim Fit Taşlanmış Denim Jean', 'JEAN-BLU-32', 1, 899.99, 899.99),
    ('52000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', '31000000-0000-0000-0000-000000000001', 'Hakiki Deri RFID Korumalı Cüzdan', 'WLT-LTH-BRN', 1, 599.00, 599.00),

    -- Order 3 items (Ahmet - Pending)
    ('53000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000003', '22000000-0000-0000-0000-000000000001', 'Apex Akıllı Saat OLED GPS', 'WATCH-APX-BLK', 1, 3999.00, 3999.00)
ON CONFLICT (id) DO NOTHING;
