INSERT INTO users (name, email)
VALUES ('owner name', 'owner@mail.com'),
       ('booker name', 'booker@mail.com'),
       ('user name', 'user@mail.com');

MERGE INTO users (id, name, email) KEY (id)
VALUES (1, 'owner name', 'owner@mail.com'),
       (2, 'booker name', 'booker@mail.com'),
       (3, 'user name', 'user@mail.com');

INSERT INTO requests (user_id, description, created)
VALUES (2, 'request description 1', current_timestamp - interval '1' day - interval '1' hour),
       (2, 'request description 2', current_timestamp - interval '1' hour);

INSERT INTO items (user_id, request_id, name, description, available)
VALUES (1, 2, 'iTEXTtem 1', 'item description 1', true),
       (1, 2, 'item 2', 'item deTexTscription 2', true),
       (1, 2, 'texTitem 3', 'item description 3Text', true),
       (1, null, 'item 4', 'item description 4', false);

INSERT INTO booking (booker_id, item_id, status, start_booking, end_booking)
VALUES (2, 1, 'WAITING', current_timestamp - interval '1' day - interval '30' minute, current_timestamp - interval '1' day + interval '30' minute),
       (2, 2, 'REJECTED', current_timestamp - interval '1' day - interval '29' minute, current_timestamp - interval '1' day + interval '31' minute),
       (2, 3, 'APPROVED', current_timestamp - interval '1' day - interval '28' minute, current_timestamp - interval '1' day + interval '32' minute),
       (2, 1, 'APPROVED', current_timestamp - interval '1' day + interval '31' minute, current_timestamp - interval '1' day + interval '1' hour + interval '31' minute),
       (2, 3, 'WAITING', current_timestamp - interval '30' minute, current_timestamp + interval '30' minute),
       (2, 2, 'APPROVED', current_timestamp - interval '29' minute, current_timestamp + interval '31' minute),
       (2, 1, 'REJECTED', current_timestamp - interval '28' minute, current_timestamp + interval '32' minute),
       (2, 1, 'WAITING', current_timestamp + interval '1' day - interval '30' minute, current_timestamp + interval '1' day + interval '30' minute),
       (2, 2, 'APPROVED', current_timestamp + interval '1' day - interval '29' minute, current_timestamp + interval '1' day + interval '31' minute),
       (2, 3, 'REJECTED', current_timestamp + interval '1' day - interval '28' minute, current_timestamp + interval '1' day + interval '32' minute),
       (2, 2, 'APPROVED', current_timestamp + interval '2' day - interval '30' minute, current_timestamp + interval '2' day + interval '30' minute);

INSERT INTO comments (author_id, item_id, text, created)
VALUES (2, 2, 'Positive comment', current_timestamp - interval '9' hour),
       (2, 2, 'Negative comment', current_timestamp - interval '8' hour),
       (2, 1, 'Neutral comment', current_timestamp - interval '7' hour);
