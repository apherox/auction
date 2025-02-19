#!/bin/bash

# Base URL for the API
BASE_URL="http://localhost:8088/v1/api"

# Authorization header value
AUTH_HEADER="Basic am9obl9kb2U6cGFzc3dvcmQxMjM="

# Number of virtual users (concurrent bidders)
NUM_USERS=10

# Number of requests each user will make
REQUESTS_PER_USER=50

# Range of bid values (for example, between $10 and $500)
MIN_BID=150
MAX_BID=150000

# Create a new user (Assumed POST endpoint for creating users)
create_user() {
  TIMESTAMP=$(date +%s%3N)  # Get the current timestamp with milliseconds

  curl -X POST "$BASE_URL/users" \
    -H "Content-Type: application/json" \
    -H "Authorization: $AUTH_HEADER" \
    -d '{
      "username": "jane_doe_'$TIMESTAMP'",
      "password": "password123",
      "email": "jane_doe_'$TIMESTAMP'@auction.com",
      "fullName": "Jane Doe",
      "roles": ["admin"]
    }'
}


# Create an auction (Assumed POST endpoint for creating auction)
create_auction() {
  # Get the current date and time plus 1 month in the required format: yyyy-MM-dd'T'HH:mm:ss
  EXPIRATION_TIME=$(date -d "+1 month" '+%Y-%m-%dT%H:%M:%S')

  # Create auction and capture the response
  RESPONSE=$(curl -s -X POST "$BASE_URL/auctions" \
    -H "Content-Type: application/json" \
    -H "Authorization: $AUTH_HEADER" \
    -d '{
      "title": "Hair dryer",
      "description": "Hair dryer",
      "startingPrice": 150.00,
      "expirationTime": "'"$EXPIRATION_TIME"'"
    }')

  # Extract auctionId from the response
  AUCTION_ID=$(echo $RESPONSE | jq -r '.auctionId')

  echo "Created Auction with ID: $AUCTION_ID, Expiration Time: $EXPIRATION_TIME"
}


# Place a bid
place_bid() {
  local BID_AMOUNT=$1  # Accept the current bid amount as a parameter

  # Make the POST request to place the bid
  curl -X POST "$BASE_URL/auctions/$AUCTION_ID/bids" \
    -H "Content-Type: application/json" \
    -H "Authorization: $AUTH_HEADER" \
    -d '{"amount":'"$BID_AMOUNT"'}'

  echo "Placed bid of amount: $BID_AMOUNT"
}

# Register users
echo "Creating users..."
for i in $(seq 1 $NUM_USERS); do
  create_user &
  sleep 0.2
done

# Wait for users to be created
wait

# Create auction
echo "Creating auction..."
create_auction

# Load test with bids
echo "Starting load test with bids..."

LAST_BID_AMOUNT=150

for i in $(seq 1 $REQUESTS_PER_USER); do
  BID_AMOUNT=$((LAST_BID_AMOUNT + 10))  # Increment the bid amount for each call
  place_bid $BID_AMOUNT &
  LAST_BID_AMOUNT=$BID_AMOUNT
done

# Wait for all bids to be placed
wait

echo "Load test completed."
