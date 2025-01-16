# Auction REST API

This is an Auction REST API built with **Spring Boot** using **Gradle** as a build tool.
The application provides endpoints for managing auctions, user registration, placing bids, and more.
It uses an **H2 in-memory database** for data storage and includes various features
such as Swagger for API documentation and built-in security for user authentication by username and password.

## Features

- **User Management**:
    - User registration
    - User login/logout
    - Predefined users with encrypted passwords (default password: `password123`)

- **Auction Management**:
    - Create a new auction
    - Update auction details
    - Find an auction by ID
    - List all auctions

- **Bid Management**:
    - Place a bid on an auction

## Technology Stack
- **Spring Boot**: Backend framework.
- **Gradle**: Build tool.
- **H2 Database**: In-memory database.
- **Flyway**: Database Migration Tool.
- **OpenApi**: API documentation.
- **Swagger**: Swagger Rest endpoints UI.
- **Mapstruct**: Mapping Request objects to domain objects and vice versa.
- **JUnit & ArchUnit**: Testing for controllers, services, and architecture rules.

## Build & Run

### Build the Application

You can build the application using gradle by running:

```bash
./gradlew clean build
```

### Run the Application

The application runs on `localhost` with port `8088`. You can start the application by running:

```bash
./gradlew bootRun
```
### Available Resources

Once the application is running, you can access the following resources:

- **Swagger UI:** [http://localhost:8088/openapi.html](http://localhost:8088/openapi.html)
- **OpenAPI Documentation:** [http://localhost:8088/openapi](http://localhost:8088/openapi)
- **H2 Console:** [http://localhost:8088/h2-console](http://localhost:8088/h2-console)

## Testing Strategy

The application includes a comprehensive set of tests to ensure the quality and reliability of the system. The following types of tests have been implemented:

### 1. **Integration Tests**

- **Purpose**: Verify the correct functioning of the entire application flow, including interactions with the database, external services, and APIs.
- **Examples**:
  - Validating REST API endpoints.
  - Ensuring proper data flow between service and repository layers.
  - Testing user authentication and authorization.

Integration tests are executed using real database interactions (H2 in-memory database) to simulate production-like environments.

### 2. **Architectural Tests**

- **Purpose**: Ensure the correct organization of components, such as the separation of concerns, proper layering, and dependency management.
- **Examples**:
  - Checking package structure adherence.
  - Verifying layer dependencies using tools like [ArchUnit](https://github.com/TNG/ArchUnit).
  - Ensuring that specific components (e.g., services, repositories, and controllers) don’t have unintended dependencies or violate architectural constraints.

Architectural tests help maintain code quality and prevent unwanted coupling between layers as the application grows.

### Troubleshooting

If you encounter an issue where the application fails to start due to a **"Port already in use"** error on Linux (even after closing the application), it is likely that the port is still occupied by the previous instance of the application. You can follow these steps to identify and terminate the process using the port:

**Terminate the  application occupying the port:**

   Run the following command to terminate the process using port `8088`:

   ```bash
   fuser -k 8088/tcp
   ```

