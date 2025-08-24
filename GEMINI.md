# Healthcare MVP Project Overview

This is a Spring Boot application built with Maven and Java 21, designed as a Minimum Viable Product (MVP) for a healthcare management system. It provides secure login, authorization, and super admin functionalities, including the registration of tech advisors and hospital administrators with associated hospital details.

## Key Technologies and Architecture

*   **Backend Framework:** Spring Boot
*   **Language:** Java 21
*   **Build Tool:** Maven
*   **Database:** PostgreSQL (with Spring Data JPA for persistence)
*   **Security:** Spring Security, JWT (JSON Web Tokens) for authentication and authorization, including refresh token rotation and a JWT blocklist for secure logout (implemented using Redis).
*   **Authentication:** Integrates with AWS Cognito Identity Provider.
*   **Caching:** Redis (for JWT blocklist).
*   **API Documentation:** SpringDoc OpenAPI (Swagger UI).
*   **Logging:** SLF4J with Logback and Logstash encoder.
*   **Monitoring:** Spring Boot Actuator (health, info, metrics, Prometheus).
*   **Email:** Spring Boot Starter Mail.
*   **PDF Generation:** iText PDF.
*   **Configuration:** `application.yml` for environment-specific settings.

## Core Features Implemented

*   **Secure User Authentication:**
    *   User login with email and password.
    *   Password hashing using BCrypt.
    *   Account lockout mechanism to prevent brute-force attacks.
    *   Refresh token rotation for enhanced security.
    *   JWT blocklist for secure logout.
*   **Role-Based Access Control (RBAC):**
    *   `SUPER_ADMIN`, `TECH_ADVISOR`, `HOSPITAL_ADMIN` roles are defined.
    *   Endpoints are secured based on user roles using `@PreAuthorize`.
*   **Super Admin Functionality:**
    *   Super admin landing page.
    *   Ability to register tech advisors and hospital administrators with hospital details.
*   **Password Management:**
    *   Secure password change functionality.
    *   Password reset request and confirmation with token expiration.
*   **Auditing:** Detailed audit logging for authentication and security events.

## Building and Running

This project uses Maven. Ensure you have Maven and Java 21 installed.

### Prerequisites

*   Java Development Kit (JDK) 21
*   Apache Maven
*   PostgreSQL database instance
*   Redis instance (for JWT blocklist)

### Build the Project

To build the project, navigate to the root directory and run:

```bash
mvn clean install
```

### Run the Application

To run the Spring Boot application:

```bash
mvn spring-boot:run
```

Alternatively, you can build a JAR and run it:

```bash
mvn clean package
java -jar target/healthcare-mvp-1.0.0-SNAPSHOT.jar
```

### Docker Compose

The project includes a `docker-compose.yml` file for setting up the application and its dependencies (PostgreSQL, Redis, Nginx).

To start the services using Docker Compose:

```bash
docker-compose up --build
```

### Testing

To run unit and integration tests:

```bash
mvn test
```

## Development Conventions

*   **Code Style:** Follows standard Java and Spring Boot conventions.
*   **Logging:** Uses SLF4J with Logback. Log messages should be informative and include relevant context.
*   **Error Handling:** Custom `AuthenticationException` and `BaseResponse` DTOs are used for consistent API error responses.
*   **Security:** Emphasis on secure coding practices, including input validation, password hashing, and robust authentication/authorization mechanisms.
*   **API Documentation:** Uses Swagger/OpenAPI for API documentation.
*   **Database Migrations:** Uses Flyway (inferred from `db/migration` directory, though not explicitly in `pom.xml` as a direct dependency, it's a common pattern with `V001__create_base_tables.sql`).

## Future Enhancements (TODOs)

*   Implement comprehensive input validation across all DTOs and API endpoints.
*   Add security headers (e.g., Content-Security-Policy, X-XSS-Protection, X-Frame-Options) to `SecurityConfig`.
*   Explore more advanced monitoring and alerting solutions.
*   Implement email service for password reset and other notifications.
*   Consider a more robust token invalidation strategy for refresh tokens (e.g., storing refresh tokens in a secure, HTTP-only cookie).
