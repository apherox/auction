INSERT INTO users (user_id, username, password, email, full_name, roles, created_at)
VALUES
    (1, 'john_doe', '$2a$12$qcxK/pZQ/./y8N76oF5r5e9e8Hg0ahP2E7HbbVho3EsE1nenE35yy', 'john.doe@example.com', 'John Doe', 'ROLE_admin', CURRENT_TIMESTAMP),
    (2, 'jane_smith', '$2a$12$qcxK/pZQ/./y8N76oF5r5e9e8Hg0ahP2E7HbbVho3EsE1nenE35yy', 'jane.smith@example.com', 'Jane Smith', 'ROLE_admin', CURRENT_TIMESTAMP),
    (3, 'alice_brown', '$2a$12$qcxK/pZQ/./y8N76oF5r5e9e8Hg0ahP2E7HbbVho3EsE1nenE35yy', 'alice.brown@example.com', 'Alice Brown', 'ROLE_user', CURRENT_TIMESTAMP);


INSERT INTO auctions (auction_id, title, description, starting_price, expiration_time, status, highest_bid, highest_bid_user_id, created_at, updated_at, version)
VALUES
    (1, 'Vintage Watch', 'A rare vintage wristwatch from the 1950s', 100.00, DATEADD(DAY, 3, CURRENT_TIMESTAMP), 'OPEN', 150.00, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'Antique Painting', 'A painting by a renowned artist from the 1800s', 500.00, DATEADD(DAY, 5, CURRENT_TIMESTAMP), 'OPEN', 250, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'Collectible Stamp', 'A rare collectible stamp from the early 20th century', 20.00, DATEADD(DAY, 7, CURRENT_TIMESTAMP), 'OPEN', 30.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


INSERT INTO bids (bid_id, auction_id, user_id, amount, bid_time)
VALUES
    (1, 1, 2, 150.00, CURRENT_TIMESTAMP),
    (2, 1, 1, 200.00, CURRENT_TIMESTAMP),
    (3, 2, 3, 550.00, CURRENT_TIMESTAMP),
    (4, 3, 1, 35.00, CURRENT_TIMESTAMP),
    (5, 3, 2, 40.00, CURRENT_TIMESTAMP);