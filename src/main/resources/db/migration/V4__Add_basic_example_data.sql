INSERT INTO make (id, name) VALUES
(1, 'Toyota'),
(2, 'Ford'),
(3, 'BMW');

INSERT INTO model (id, name, make_id) VALUES
(1, 'Corolla', 1),
(2, 'Mustang', 2),
(3, 'X5', 3);

INSERT INTO feature (id, name) VALUES
(1, 'Air Conditioning'),
(2, 'Sunroof'),
(3, 'Bluetooth'),
(4, 'Leather Seats'),
(5, 'Backup Camera');

INSERT INTO company (id, owner_id, name, address, phone, email) VALUES
(1, 'owner001', 'AutoWorld', '123 Auto St.', '555-1234', 'contact@autoworld.com'),
(2, 'owner002', 'Cars4You', '456 Car Blvd.', '555-5678', 'info@cars4you.com');

INSERT INTO company_user (company_id, user_id) VALUES
(1, 'user001'),
(1, 'user002'),
(2, 'user003');

INSERT INTO listing (
    id,
    owner_id,
    selling_company_id,
    make_id,
    model_id,
    created_at,
    expires_at,
    revoked,
    price,
    production_year,
    mileage,
    fuel,
    car_operational_status,
    car_usage,
    car_type,
    description
) VALUES
(
    1,
    'owner123',  -- owner_id
    1,           -- selling_company_id (AutoWorld)
    1,           -- make_id (Toyota)
    1,           -- model_id (Corolla)
    '2025-04-03', -- created_at
    '2025-06-03', -- expires_at
    FALSE,       -- revoked
    20000.00,    -- price
    2020,        -- production_year
    15000,       -- mileage
    'PETROL',    -- fuel
    'WORKING', -- car_operational_status
    'USED',  -- car_usage
    'SEDAN',     -- car_type
    'A well-maintained Toyota Corolla with low mileage.' -- description
),
(
    2,
    'owner124',  -- owner_id
    2,           -- selling_company_id (Cars4You)
    2,           -- make_id (Ford)
    2,           -- model_id (Mustang)
    '2025-04-03', -- created_at
    '2025-06-03', -- expires_at
    FALSE,       -- revoked
    30000.00,    -- price
    2019,        -- production_year
    25000,       -- mileage
    'PETROL',    -- fuel
    'WORKING', -- car_operational_status
    'USED',  -- car_usage
    'COUPE',     -- car_type
    'A sporty Ford Mustang with great performance and design.' -- description
);

INSERT INTO listing_feature (listing_id, feature_id) VALUES
(1, 1),  -- listing_id = 1 (Toyota Corolla) ma funkcję 'Air Conditioning'
(1, 2),  -- listing_id = 1 (Toyota Corolla) ma funkcję 'Sunroof'
(2, 3),  -- listing_id = 2 (Ford Mustang) ma funkcję 'Bluetooth'
(2, 4);  -- listing_id = 2 (Ford Mustang) ma funkcję 'Leather Seats'
