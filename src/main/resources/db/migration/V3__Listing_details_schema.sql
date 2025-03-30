CREATE TABLE listing (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    selling_company_id BIGINT,
    make_id BIGINT,
    model_id BIGINT,
    created_at DATE NOT NULL,
    expires_at DATE,
    revoked BOOLEAN NOT NULL,
    price DECIMAL(10, 2) CHECK (price >= 0),
    production_year INT CHECK (production_year >= 1900),
    mileage INT CHECK (mileage >= 0),
    fuel VARCHAR(255),
    car_operational_status VARCHAR(255) NOT NULL,
    car_usage VARCHAR(255) NOT NULL,
    car_type VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    FOREIGN KEY (selling_company_id) REFERENCES company(id),
    FOREIGN KEY (make_id) REFERENCES make(id),
    FOREIGN KEY (model_id) REFERENCES model(id)
);

CREATE TABLE listing_feature (
     listing_id BIGINT,
     feature_id BIGINT,
     PRIMARY KEY (listing_id, feature_id),
     FOREIGN KEY (listing_id) REFERENCES listing(id) ON DELETE CASCADE,
     FOREIGN KEY (feature_id) REFERENCES feature(id) ON DELETE CASCADE
);