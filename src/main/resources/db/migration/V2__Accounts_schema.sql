CREATE TABLE company (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255)
);

CREATE TABLE company_user (
    company_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (company_id, user_id),
    FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE
)