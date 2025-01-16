-- Create index on expiration_time for faster retrieval of auctions by expiration_time
CREATE INDEX idx_expiration_time ON auctions (expiration_time);

-- Create index on status and expiration_time for faster retrieval of auctions by status and expiration_time
CREATE INDEX idx_status_expiration_time ON auctions (status, expiration_time);

-- Create index on auction_id for faster retrieval of bids by auction
CREATE INDEX idx_bids_auction_id ON bids (auction_id);

