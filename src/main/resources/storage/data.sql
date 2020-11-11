INSERT INTO movie (id, uuid, title) VALUES
    (1, 'efa6e994-11cd-439f-9106-e6ba1033d107', 'E.T. THE EXTRA-TERRESTRIAL'),
    (2, 'a63f583d-c6fc-4ed3-8620-8235a04bc2c6', 'READY PLAYER ONE'),
    (3, '8b302fcb-9dfc-4035-b43e-83843e9020d4', 'JURASSIC PARK');

INSERT INTO room (id, uuid, number, nb_rows, nb_cols, movie_id) VALUES
    (1, '48b61c7e-cffa-4961-ad7c-f0b567e7ee47', 1, 10, 15, 1),
    (2, 'ded941e7-695f-47a4-a088-fffafe29b6ef', 2, 9, 14, 2),
    (3, 'c66820cd-546f-40fb-bbf8-4e464eae9981', 3, 8, 13, 3);

INSERT INTO "user" (id, uuid, username, password, role) VALUES
    (1, 'f99b946d-8d93-42fd-93f0-4f6fa25781be', 'staff', 'password', 1),
    (2, '1b7fce81-b2a2-4cfa-9b85-f76dd0d48f8c', 'costumer', 'password', 0);
