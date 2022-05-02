# Wallet Implementation 

for Hubpay coding test.

# Run tests

`mvn test`

# Run the server

`mvn spring-boot:run`

Currently the server uses an in-memory H2 database.

# Postman Collection

There is a collection in the postman sub-directory. 

Calling the create endpoint saved a global walletId which is used in the other endpoints.

The transactions endpoint supports paging, with defaults of page=1&size=5.
