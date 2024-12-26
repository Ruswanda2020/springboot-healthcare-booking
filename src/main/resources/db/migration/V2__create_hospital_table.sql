CREATE TABLE hospital (
    id BIGSERIAL PRIMARY KEY ,
    name VARCHAR(255) NOT NULL ,
    address varchar(255) NOT NULL ,
    phone VARCHAR(255) NOT NULL ,
    email VARCHAR(255) NOT NULL ,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_hospital_name ON hospital(name);