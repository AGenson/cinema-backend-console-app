DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS movie;
DROP TABLE IF EXISTS "user";

CREATE TABLE movie (
    id          BIGSERIAL       NOT NULL        PRIMARY KEY,
    uuid        UUID            NOT NULL        UNIQUE,
    title       VARCHAR(32)     NOT NULL        UNIQUE
);

CREATE TABLE room (
    id          BIGSERIAL       NOT NULL        PRIMARY KEY,
    uuid        UUID            NOT NULL        UNIQUE,
    number      INT             NOT NULL        UNIQUE,
    nb_rows     INT             NOT NULL,
    nb_cols     INT             NOT NULL,
    movie_id    BIGINT                          REFERENCES movie(id) ON DELETE SET NULL
);

CREATE TABLE "user" (
    id          BIGSERIAL       NOT NULL        PRIMARY KEY,
    uuid        UUID            NOT NULL        UNIQUE,
    username    VARCHAR(16)     NOT NULL        UNIQUE,
    password    VARCHAR(16)     NOT NULL,
    role        SMALLINT        NOT NULL
);
