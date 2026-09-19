CREATE TABLE resources
(
    uuid        UUID         NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_resources PRIMARY KEY (uuid)
);

CREATE TABLE files
(
    uuid         UUID         NOT NULL,
    name         VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size         BIGINT       NOT NULL,
    resource     UUID         NOT NULL,
    CONSTRAINT pk_files PRIMARY KEY (uuid),
    CONSTRAINT fk_files_resource FOREIGN KEY (resource) REFERENCES resources (uuid)
);
