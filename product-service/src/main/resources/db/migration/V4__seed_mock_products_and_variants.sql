-- ============ ADDITIONAL CATEGORIES ============
INSERT INTO categories (id, parent_id, name, slug, description) VALUES
    ('33333333-3333-3333-3333-333333333333', NULL, 'Aksesuar', 'aksesuar', 'Deri aksesuarlar, çantalar ve cüzdanlar'),
    ('44444444-4444-4444-4444-444444444444', NULL, 'Ev & Yaşam', 'ev-yasam', 'Ev, ofis ve günlük yaşam ürünleri')
ON CONFLICT (id) DO NOTHING;

-- ============ PRODUCTS ============
INSERT INTO products (id, name, slug, description, image_url, category_id) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Oversize Ağır Pamuklu T-Shirt', 'oversize-agir-pamuklu-tshirt', '%100 premium pamuk, 240 gsm kalın kumaş, rahat ve modern kalıp.', 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800', '11111111-1111-1111-1111-111111111111'),
    ('10000000-0000-0000-0000-000000000002', 'Slim Fit Taşlanmış Denim Jean', 'slim-fit-taslanmis-denim-jean', 'Esnek pamuklu kumaş, modern slim fit kesim ve dayanıklı dikişler.', 'https://images.unsplash.com/photo-1542272604-780c96856592?w=800', '11111111-1111-1111-1111-111111111111'),
    ('20000000-0000-0000-0000-000000000001', 'Vestra Studio ANC Kablosuz Kulaklık', 'vestra-studio-anc-kablosuz-kulaklik', '40dB hibrit aktif gürültü engelleme, 60 saat pil ömrü ve stüdyo kalitesinde Hi-Res ses.', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800', '22222222-2222-2222-2222-222222222222'),
    ('20000000-0000-0000-0000-000000000002', 'Apex Akıllı Saat OLED GPS', 'apex-akilli-saat-oled-gps', '1.4 inç AMOLED retina ekran, titanyum çerçeve, 14 gün şarj ve 50 metreye kadar su geçirmezlik.', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800', '22222222-2222-2222-2222-222222222222'),
    ('30000000-0000-0000-0000-000000000001', 'Hakiki Deri RFID Korumalı Cüzdan', 'hakiki-deri-rfid-korumali-cuzdan', '%100 dana derisinden el yapımı cüzdan, temassız kart kopyalanmasını önleyen RFID koruma.', 'https://images.unsplash.com/photo-1627123424574-724758594e93?w=800', '33333333-3333-3333-3333-333333333333'),
    ('40000000-0000-0000-0000-000000000001', 'Çift Cidarlı Paslanmaz Termos 750ml', 'cift-cidarli-paslanmaz-termos-750ml', 'Vakumlu çift katman paslanmaz çelik, 24 saat soğuk ve 12 saat sıcak tutma kapasitesi.', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800', '44444444-4444-4444-4444-444444444444')
ON CONFLICT (id) DO NOTHING;

-- ============ VARIANTS ============
INSERT INTO variants (id, product_id, price, sku) VALUES
    -- T-Shirt variants
    ('11000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 449.99, 'TSHIRT-BLK-S'),
    ('11000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 449.99, 'TSHIRT-BLK-M'),
    ('11000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', 449.99, 'TSHIRT-BLK-L'),
    ('11000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', 449.99, 'TSHIRT-WHT-M'),
    -- Jeans variants
    ('12000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 899.99, 'JEAN-BLU-30'),
    ('12000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 899.99, 'JEAN-BLU-32'),
    ('12000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 899.99, 'JEAN-BLU-34'),
    -- Headphone variants
    ('21000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 2499.00, 'HP-ANC-BLK'),
    ('21000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 2499.00, 'HP-ANC-SLV'),
    -- Smartwatch variants
    ('22000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', 3999.00, 'WATCH-APX-BLK'),
    ('22000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 4299.00, 'WATCH-APX-LTH'),
    -- Wallet variants
    ('31000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 599.00, 'WLT-LTH-BRN'),
    ('31000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 599.00, 'WLT-LTH-BLK'),
    -- Thermos variant
    ('41000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 499.00, 'BTL-SS-MTE')
ON CONFLICT (id) DO NOTHING;

-- ============ VARIANT ATTRIBUTES ============
INSERT INTO variant_attributes (variant_id, attribute_name, attribute_value) VALUES
    ('11000000-0000-0000-0000-000000000001', 'Renk', 'Siyah'),
    ('11000000-0000-0000-0000-000000000001', 'Beden', 'S'),
    ('11000000-0000-0000-0000-000000000002', 'Renk', 'Siyah'),
    ('11000000-0000-0000-0000-000000000002', 'Beden', 'M'),
    ('11000000-0000-0000-0000-000000000003', 'Renk', 'Siyah'),
    ('11000000-0000-0000-0000-000000000003', 'Beden', 'L'),
    ('11000000-0000-0000-0000-000000000004', 'Renk', 'Beyaz'),
    ('11000000-0000-0000-0000-000000000004', 'Beden', 'M'),

    ('12000000-0000-0000-0000-000000000001', 'Renk', 'Koyu Mavi'),
    ('12000000-0000-0000-0000-000000000001', 'Beden', '30'),
    ('12000000-0000-0000-0000-000000000002', 'Renk', 'Koyu Mavi'),
    ('12000000-0000-0000-0000-000000000002', 'Beden', '32'),
    ('12000000-0000-0000-0000-000000000003', 'Renk', 'Koyu Mavi'),
    ('12000000-0000-0000-0000-000000000003', 'Beden', '34'),

    ('21000000-0000-0000-0000-000000000001', 'Renk', 'Gece Siyahı'),
    ('21000000-0000-0000-0000-000000000002', 'Renk', 'Mat Gümüş'),

    ('22000000-0000-0000-0000-000000000001', 'Renk', 'Titanyum Siyahı'),
    ('22000000-0000-0000-0000-000000000001', 'Kordon', 'Florokauçuk Silikon'),
    ('22000000-0000-0000-0000-000000000002', 'Renk', 'Gümüş Titanyum'),
    ('22000000-0000-0000-0000-000000000002', 'Kordon', 'Kahverengi Hakiki Deri'),

    ('31000000-0000-0000-0000-000000000001', 'Renk', 'Taba'),
    ('31000000-0000-0000-0000-000000000002', 'Renk', 'Siyah'),

    ('41000000-0000-0000-0000-000000000001', 'Renk', 'Mat Siyah')
ON CONFLICT (variant_id, attribute_name) DO NOTHING;

-- ============ REVIEWS ============
INSERT INTO reviews (product_id, user_id, rating, comment) VALUES
    ('20000000-0000-0000-0000-000000000001', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 5, 'ANC performansı uçakta ve ofiste mükemmel çalışıyor. Bataryası gerçekten günlerce yetiyor.'),
    ('10000000-0000-0000-0000-000000000001', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 5, 'Kumaş kalınlığı ve kalıbı harika. Yıkandıktan sonra çekme veya solma yapmadı.'),
    ('20000000-0000-0000-0000-000000000002', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 4, 'Ekran kalitesi ve malzeme hissi premium. Spor takibi gayet tutarlı.')
ON CONFLICT DO NOTHING;
