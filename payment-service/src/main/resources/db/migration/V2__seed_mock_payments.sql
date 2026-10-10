-- Seed mock payments corresponding to orders
INSERT INTO payments (id, order_id, order_number, user_id, amount, status, payment_method, transaction_id, card_last_four, version, created_at, updated_at) VALUES
    ('60000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 'ORD-2026-001', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 2948.99, 'SUCCESS', 'CREDIT_CARD', 'TXN-2026-981240', '5432', 0, now() - INTERVAL '3 days', now() - INTERVAL '3 days'),
    ('60000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', 'ORD-2026-002', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 1498.99, 'SUCCESS', 'CREDIT_CARD', 'TXN-2026-551982', '4012', 0, now() - INTERVAL '1 day', now() - INTERVAL '1 day'),
    ('60000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', 'ORD-2026-003', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 3999.00, 'PENDING', 'CREDIT_CARD', NULL, NULL, 0, now() - INTERVAL '2 hours', now() - INTERVAL '2 hours')
ON CONFLICT (id) DO NOTHING;
