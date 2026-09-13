CREATE TABLE users
(
    uuid       UUID         NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(255) NOT NULL,
    enabled    BOOLEAN      NOT NULL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_users PRIMARY KEY (uuid)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);