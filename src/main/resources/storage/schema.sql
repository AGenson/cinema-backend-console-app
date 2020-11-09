DROP TABLE IF EXISTS movie;

CREATE TABLE movie (
    id      BIGSERIAL       NOT NULL        PRIMARY KEY,
    uuid    UUID            NOT NULL        UNIQUE,
    title   VARCHAR(32)     NOT NULL        UNIQUE
);
