CREATE TABLE listing (
     id BIGSERIAL PRIMARY KEY,
     owner_id VARCHAR(255) NOT NULL,
     company_id BIGINT,
     make_id BIGINT,
     model_id BIGINT,
     car_type BIGINT,
     created_at DATE NOT NULL,
     expires_at DATE,
     listing_status VARCHAR(255) NOT NULL,
     price DECIMAL(10, 2) CHECK (price >= 0),
     production_year INT CHECK (production_year >= 1900),
     mileage INT CHECK (mileage >= 0),
     fuel VARCHAR(255),
     car_condition VARCHAR(255),
     description VARCHAR(500),
     FOREIGN KEY (company_id) REFERENCES company(id),
     FOREIGN KEY (make_id) REFERENCES make(id),
     FOREIGN KEY (model_id) REFERENCES model(id),
     FOREIGN KEY (car_type) REFERENCES car_type(id)
);

CREATE TABLE listing_feature (
     listing_id BIGINT,
     feature_id BIGINT,
     PRIMARY KEY (listing_id, feature_id),
     FOREIGN KEY (listing_id) REFERENCES listing(id) ON DELETE CASCADE,
     FOREIGN KEY (feature_id) REFERENCES feature(id) ON DELETE CASCADE
);