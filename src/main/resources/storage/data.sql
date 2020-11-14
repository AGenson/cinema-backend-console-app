INSERT INTO movie (id, uuid, title) VALUES
    (1, 'efa6e994-11cd-439f-9106-e6ba1033d107', 'E.T. THE EXTRA-TERRESTRIAL'),
    (2, 'a63f583d-c6fc-4ed3-8620-8235a04bc2c6', 'READY PLAYER ONE'),
    (3, '8b302fcb-9dfc-4035-b43e-83843e9020d4', 'JURASSIC PARK');

INSERT INTO room (id, uuid, number, nb_rows, nb_cols, movie_id) VALUES
    (1, '48b61c7e-cffa-4961-ad7c-f0b567e7ee47', 1, 10, 15, 1),
    (2, 'ded941e7-695f-47a4-a088-fffafe29b6ef', 2, 9, 14, 2),
    (3, 'c66820cd-546f-40fb-bbf8-4e464eae9981', 3, 8, 13, 3);

INSERT INTO "user" (id, uuid, username, password, role) VALUES
    (1, 'f99b946d-8d93-42fd-93f0-4f6fa25781be', 'staff', '$2a$10$y5FAw5f3NRxF2SV4Wvwc7OZOV2zwXc86b.tCaHj7umczGQkhGFTvS', 1),
    (2, '1b7fce81-b2a2-4cfa-9b85-f76dd0d48f8c', 'costumer', '$2a$10$cV.BVUULK5XXIoxrcvvnQ.9esx.TscTIaTyChaNogk3h96YreEJEm', 0);

INSERT INTO "order" (id, uuid, user_id) VALUES
    (1, '50cb36de-5c03-4097-a281-1d8d99fd793c', 2),
    (2, 'f5bca9f2-e582-48b1-ba8d-c85f0d89844d', 2);

INSERT INTO ticket (id, uuid, seat, room_id, order_id) VALUES
    (1, '4165fa3a-d546-48ca-8915-15aaf4b47cce', 'A01', 1, 1),
    (2, '218fd0d2-cadc-46f9-a09e-f9f37b9b4114', 'A02', 1, 1),
    (3, '5864afe0-72db-4183-9b52-e3fd698bfffd', 'D12', 2, 2);
