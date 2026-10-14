# Review Platform — Environment Documentation

## 1. Environment Overview

The Review Platform can be configured for different environments:

* Local Development
* Testing
* Production

Environment-specific configuration should be kept separate from application source code.

---

## 2. Local Development Environment

The local development environment is used for development and debugging.

### Application

```text
Application URL:
http://localhost:8080/review-platform/
```

### Database

The local environment uses Oracle Database.

Example:

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:orcl
spring.datasource.username=YOUR_DATABASE_USERNAME
spring.datasource.password=YOUR_DATABASE_PASSWORD
```

The actual username and password must be configured according to the local Oracle installation.

---

## 3. Application Configuration

Main configuration file:

```text
src/main/resources/application.properties
```

Typical configuration areas include:

* Server port
* Application context path
* Database connection
* JPA/Hibernate
* Mail configuration
* Application base URL
* Security-related configuration
* API keys and external service configuration

---

## 4. Sensitive Configuration

The following values must be treated as sensitive:

* Database password
* Email/App Password
* API keys
* Authentication secrets
* External service credentials

Sensitive values should not be committed to a public Git repository.

For production deployment, these values should be supplied through environment variables or a secure secrets-management system.

---

## 5. Environment Variables

Production configuration should preferably use environment variab
