DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS movie;

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
    movie_id    BIGINT                          REFERENCES movie(id)
);
