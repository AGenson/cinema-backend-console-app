DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS movie;
DROP TABLE IF EXISTS "order";
DROP TABLE IF EXISTS "user";

CREATE TABLE movie (
    id          IDENTITY        NOT NULL        PRIMARY KEY,
    uuid        UUID            NOT NULL        UNIQUE,
    title       VARCHAR(32)     NOT NULL        UNIQUE
);

CREATE TABLE room (
    id          IDENTITY        NOT NULL        PRIMARY KEY,
    uuid        UUID            NOT NULL        UNIQUE,
    number      INT             NOT NULL        UNIQUE,
    nb_rows     INT             NOT NULL,
    nb_cols     INT             NOT NULL,
    movie_id    BIGINT                          REFERENCES movie(id) ON DELETE SET NULL
);

CREATE TABLE "user" (
    id          IDENTITY        NOT NULL        PRIMARY KEY,
    uuid        UUID            NOT NULL        UNIQUE,
    username    VARCHAR(16)     NOT NULL        UNIQUE,
    password    CHAR(60)        NOT NULL,
    role        SMALLINT        NOT NULL
);

CREATE TABLE "order" (
    id          IDENTITY        NOT NULL        PRIMARY KEY,
    uuid        UUID            NOT NULL        UNIQUE,
    user_id     BIGINT          NOT NULL        REFERENCES "user"(id)
);
