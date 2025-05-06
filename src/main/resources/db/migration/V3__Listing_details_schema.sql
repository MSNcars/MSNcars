CREATE TABLE listing (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    owner_type VARCHAR(50) NOT NULL,
    model_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ,
    revoked BOOLEAN NOT NULL,
    price DECIMAL(10, 2) CHECK (price >= 0),
    production_year INT CHECK (production_year >= 1900),
    mileage INT CHECK (mileage >= 0),
    fuel VARCHAR(255),
    car_operational_status VARCHAR(255) NOT NULL,
    car_usage VARCHAR(255) NOT NULL,
    car_type VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    FOREIGN KEY (model_id) REFERENCES model(id)
);

CREATE TABLE listing_feature (
     listing_id BIGINT,
     feature_id BIGINT,
     PRIMARY KEY (listing_id, feature_id),
     FOREIGN KEY (listing_id) REFERENCES listing(id) ON DELETE CASCADE,
     FOREIGN KEY (feature_id) REFERENCES feature(id) ON DELETE CASCADE
);