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

## OWASP Top 10 2021 Coverage

Here's how the current implementation addresses the OWASP Top 10 2021 vulnerabilities:

### A01: Broken Access Control

This vulnerability is mitigated by Spring Security's robust authorization mechanisms, ensuring that users can only access resources and perform actions for which they have explicit permission.

*   **`SecurityConfig.java`**: The `filterChain` method defines URL-based authorization rules using `requestMatchers().hasAnyRole(...)` and `anyRequest().authenticated()`, restricting access to specific API paths based on roles.
*   **`@PreAuthorize` annotations**: Used in various controllers (e.g., `AuthController`, `SuperAdminController`) to enforce fine-grained, method-level authorization checks based on the authenticated user's roles.
*   **`BusinessUser.java`**: Defines the `BusinessRole` enum, which is central to the role-based access control system.

### A02: Cryptographic Failures

Protection against cryptographic failures is achieved through secure handling of sensitive data, particularly passwords and JSON Web Tokens (JWTs).

*   **`SecurityConfig.java`**: The `passwordEncoder()` bean is configured with `BCryptPasswordEncoder(12)`, providing a strong, industry-standard hashing algorithm for storing user passwords.
*   **`AuthenticationService.java`**:
    *   `validatePassword()`: Securely verifies provided passwords against stored hashes using `passwordEncoder.matches()`, preventing direct comparison of plain-text passwords.
    *   `changePassword()` and `confirmPasswordReset()`: Ensure that new passwords are always hashed using `passwordEncoder.encode()` before being stored.
*   **`JwtUtil.java`**: Utilizes `Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))` and `Jwts.SIG.HS512` for strong cryptographic signing of JWTs. It also includes validation to ensure the JWT secret key is of sufficient length (minimum 64 bytes) for the HS512 algorithm.
*   **`application.yml`**: Centralizes the configuration of the `app.security.jwt.secret`, which is crucial for the integrity and confidentiality of JWTs.

### A03: Injection

Injection vulnerabilities, such as SQL Injection, are primarily prevented through comprehensive input validation and the use of parameterized queries (prepared statements).

*   **DTOs (`LoginRequest.java`, `ChangePasswordRequest.java`, `ConfirmResetRequest.java`, `CreateBusinessUserRequest.java`)**: Extensive use of Jakarta Bean Validation annotations like `@NotBlank`, `@Email`, `@Size`, `@Pattern`, `@NotNull`, `@DecimalMin`, `@DecimalMax`, `@Min`, and `@Max` ensures that all incoming user data is rigorously validated before processing. This prevents malicious input from being used in unexpected ways.
*   **Spring Data JPA**: By leveraging Spring Data JPA for database interactions, queries are automatically constructed using prepared statements. This inherently neutralizes SQL injection risks by separating SQL code from user-supplied data.

### A04: Insecure Design

This broad category is addressed by implementing specific security features that improve the overall design and resilience of the authentication system.

*   **`AuthenticationService.java`**:
    *   **Refresh Token Rotation**: The updated `refreshToken()` method now issues a *new* refresh token each time a valid refresh token is used to obtain a new access token. This prevents an attacker from indefinitely using a stolen refresh token, as the old one is immediately invalidated.
    *   **JWT Blocklist**: The `logout()` method, in conjunction with the `JwtBlocklistService`, provides a server-side mechanism to immediately invalidate (blocklist) an access token upon user logout. This prevents the reuse of tokens that are no longer authorized.
*   **`JwtBlocklistService.java`**: Manages the storage and checking of blocklisted JWTs in Redis, ensuring that invalidated tokens cannot be used.
*   **`JwtAuthenticationFilter.java`**: Integrates with the `JwtBlocklistService` to check every incoming JWT against the blocklist, rejecting any requests made with invalidated tokens.

### A05: Security Misconfiguration

Security misconfigurations are mitigated by carefully configuring Spring Security components and implementing standard security headers.

*   **`SecurityConfig.java`**:
    *   Disables CSRF protection for stateless APIs, which is appropriate for JWT-based authentication.
    *   Configures CORS using an injected `CorsConfigurationSource` to define allowed origins, methods, and headers, preventing cross-origin attacks.
    *   Sets `SessionCreationPolicy.STATELESS` to ensure that no session state is maintained on the server, which is essential for JWT security.
    *   **Security Headers**: The `headers()` configuration adds:
        *   `X-Content-Type-Options: nosniff`: Prevents browsers from MIME-sniffing a response away from the declared content-type.
        *   `X-Frame-Options: DENY`: Prevents clickjacking attacks by disallowing the page from being rendered in an iframe.
        *   `X-XSS-Protection: 1; mode=block`: Enables the browser's XSS filter and instructs it to block detected attacks.
        *   `Content-Security-Policy`: A basic policy is set (`default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self';`) to mitigate XSS and data injection attacks. (Note: This policy may require further refinement based on frontend assets and third-party scripts).
    *   (Note: `Strict-Transport-Security` is commented out but noted for production, as it requires HTTPS enforcement).
*   **`application.yml`**: Provides a centralized and externalized way to manage security-related configurations like JWT secrets and expiration times, and Redis connection details, reducing the risk of hardcoded sensitive values.

### A06: Vulnerable and Outdated Components

While not directly addressed by code changes, this vulnerability is mitigated through diligent dependency management practices.

*   **`pom.xml`**: Explicitly defines versions for all project dependencies. Regular updates of these dependencies and the use of tools like OWASP Dependency-Check Maven Plugin are crucial to identify and remediate known vulnerabilities in third-party libraries.

### A07: Identification and Authentication Failures

This category is extensively covered by the robust authentication service and JWT handling.

*   **`AuthenticationService.java`**:
    *   `login()`: Manages the entire user authentication process, including password validation and updating login-related user data.
    *   `findAndValidateUser()`: Ensures that only registered users can attempt authentication.
    *   `validateAccountStatus()`: Checks if a user's account is active and not locked, preventing access to compromised or disabled accounts.
    *   `validatePassword()`: Performs secure password comparison using hashing.
    *   `handleFailedLogin()`: Implements an account lockout mechanism after a configurable number of failed login attempts, protecting against brute-force attacks.
    *   `requestPasswordReset()` and `confirmPasswordReset()`: Provide a secure password reset flow, including token generation, expiration, and invalidation after use.
    *   `changePassword()`: Allows authenticated users to securely update their passwords.
*   **`JwtUtil.java`**: Responsible for validating the integrity and authenticity of JWT tokens (`validateToken()`), ensuring that only valid and untampered tokens are accepted.
*   **`JwtAuthenticationFilter.java`**: Intercepts incoming requests, extracts and validates JWTs, and sets the security context, ensuring that only authenticated requests proceed.

### A08: Software and Data Integrity Failures

Data integrity is maintained through rigorous input validation, secure storage of sensitive data, and transactional operations.

*   **Input Validation (as detailed in A03)**: Prevents malformed or malicious data from corrupting the application's state or database.
*   **`AuthenticationService.java`**: Uses `@Transactional` annotations to ensure that database operations are atomic, consistent, isolated, and durable (ACID), preventing partial updates and maintaining data integrity.
*   **`BusinessUser.java`**: Stores password hashes instead of plain-text passwords, protecting sensitive user credentials even if the database is compromised.

### A09: Security Logging and Monitoring Failures

This vulnerability is addressed by comprehensive security logging and the use of a dedicated audit logger.

*   **`AuditLogger.java` (inferred)**: A dedicated component (as seen by its usage) for logging critical security events, including authentication attempts (success/failure), password changes, and account lockouts. This provides an immutable record for security monitoring and incident response.
*   **`AuthenticationService.java`**: Actively uses `auditLogger.logAuthenticationEvent()` and `auditLogger.logSecurityEvent()` at key points in the authentication and security flows to record important events.
*   **`application.yml`**: Configures detailed logging levels for various application packages, including `com.healthcare.mvp.auth` and `org.springframework.security`, enabling effective monitoring of security-related activities.

### A10: Server-Side Request Forgery (SSRF)

This vulnerability typically arises when an application fetches a remote resource without properly validating a user-supplied URL. The current scope of implemented changes (authentication, authorization, and super admin features) does not directly introduce or mitigate SSRF, as there are no new functionalities that involve fetching external resources based on user input.

*   **Not Directly Applicable**: The current code does not expose any endpoints or functionalities that take a URL from user input and then fetch a resource from that URL. Therefore, SSRF is not a direct concern within the scope of the recent security enhancements.
*   **Future Consideration**: If future features involve fetching external resources (e e.g., fetching profile pictures from a user-provided URL, integrating with external APIs based on user input), specific validation, URL whitelisting, and network segmentation would be required to prevent SSRF attacks.

## Future Enhancements (TODOs)

*   Implement email service for password reset and other notifications.
*   Consider a more robust token invalidation strategy for refresh tokens (e.g., storing refresh tokens in a secure, HTTP-only cookie).