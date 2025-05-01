CREATE TABLE Invitation (
    id uuid NOT NULL PRIMARY KEY,
    recipient_user_id VARCHAR(255),
    sender_company_id BIGINT,
    creation_date_time TIMESTAMP WITH TIME ZONE,
    invitation_state VARCHAR(50),
    FOREIGN KEY (sender_company_id) REFERENCES company(id) ON DELETE CASCADE
)