CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,

    username VARCHAR(50) NOT NULL UNIQUE,

    license_number VARCHAR(30) NOT NULL UNIQUE,

    license_category VARCHAR(20) NOT NULL,

    license_issue_date DATE NOT NULL,

    license_expiry_date DATE NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_driver_user
        FOREIGN KEY (username)
        REFERENCES users(username)
);