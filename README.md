# Review Platform

## 1. Project Overview

Review Platform is a web-based website review system built using Spring Boot and Oracle Database.

The platform allows users to discover websites, read reviews, submit reviews, rate websites, vote on reviews, and report inappropriate reviews.

It also provides business and admin functionality for managing businesses, reviews, reports, users, and verification processes.

---

## 2. Main Features

### User Features

* User registration and login
* Website search
* View website details
* View website reviews
* Submit website reviews
* Update own reviews
* Delete own reviews
* Helpful / Not Helpful review voting
* Report inappropriate reviews

### Business Features

* Create business
* View businesses
* Update business information
* Business verification
* Business email verification
* Meta tag verification
* DNS verification
* Business claims
* Business response functionality

### Admin Features

* User management
* Website management
* Review management
* Review moderation
* Report management
* Business verification
* Admin management
* SEO management

---

## 3. Technology Stack

### Backend

* Java
* Spring Boot
* Spring MVC
* Spring Data JPA
* Spring Security
* Jakarta Validation
* Hibernate

### Database

* Oracle Database
* Oracle JDBC Driver

### Frontend

* HTML
* CSS
* JavaScript

### API Documentation

* OpenAPI
* Swagger UI
* Postman

### Testing

* JUnit 5
* Mockito
* Spring Boot Test

---

## 4. Project Structure

The project is organized into separate modules/packages for different application features.

Main modules include:

* `user` — user management and authentication
* `Website` — website management
* `review` — review management
* `business` — business management and verification
* `admin` — administrator functionality
* `Report` — review reporting
* `security` — application security
* `dto` — Data Transfer Objects
* `config` — application configuration

The frontend files are located under:

```text
src/main/webapp/
```

---

## 5. API

The application provides REST APIs for:

* Users
* Websites
* Reviews
* Review Votes
* Reports
* Admins
* Businesses
* Business Claims
* Business Responses

Versioned APIs are available under:

```text
/api/v1/
```

---

## 6. Authentication and Security

The application uses Spring Security for authentication and authorization.

Security features include:

* Password hashing using BCrypt
* Role-based authorization
* Admin authorization
* Method-level security
* Owner-based access control
* Review ownership validation
* Business access validation
* CSRF configuration
* Security headers
* Content Security Policy
* Protected admin endpoints

---

## 7. Review System

Users can submit reviews with:

* Overall rating
* Review comment
* Delivery rating
* Support rating
* Refund rating
* Product rating
* Pricing rating

Reviews support moderation and verification statuses.

The system also includes checks for:

* Duplicate reviews
* Similar reviews
* Spam
* Advertising content
* Malicious links
* Harassment
* Personal information
* Review velocity
* Coordinated review patterns

---

## 8. SEO

The public website pages include SEO functionality such as:

* Unique page titles
* Meta descriptions
* Canonical URLs
* Open Graph metadata
* Breadcrumbs
* Structured data
* Review structured data
* XML sitemap
* Website sitemap

Internal and restricted pages use appropriate `noindex` directives where required.

---

## 9. Swagger / OpenAPI

API documentation is available through Swagger UI.

When the application is running locally, open:

```text
http://localhost:8080/review-platform/swagger-ui/index.html
```

OpenAPI documentation is available through:

```text
http://localhost:8080/review-platform/v3/api-docs
```

---

## 10. Postman API Testing

The REST APIs have been tested using Postman.

The following API modules have been tested:

* User Management
* Website APIs
* Review APIs
* Review Vote APIs
* Report APIs
* Admin APIs
* Business APIs

The Postman collection used for API testing is:

```text
Review Platform API
```

---

## 11. Database

The application uses Oracle Database for persistent data storage.

Main database entities include:

* Users
* Websites
* Reviews
* Review Votes
* Reports
* Businesses
* Business Claims
* Business Responses
* Admins

---

## 12. Testing

The project includes automated tests using JUnit and Mockito.

Tested areas include:

* Service layer
* Website functionality
* Review functionality
* DTO validation
* Rate limiting
* Application context

API functionality is also manually tested using Postman.

---

## 13. Local Application

The application can be run locally using Spring Boot.

Default local application URL:

```text
http://localhost:8080/review-platform/
```

---

## 14. Project Status

The main backend modules and API functionality have been implemented and tested.

Remaining documentation and deployment-related work is maintained separately in the project documentation.

---

## 15. Future Improvements

Possible future improvements include:

* Server-side rendering for public SEO pages
* Advanced search and filtering
* Improved business verification workflows
* Production deployment
* Monitoring and logging improvements
* Automated API test collection
* Performance optimization
* Additional automated integration tests
* Additional SEO improvements

---

## 16. License

This project is currently developed as an academic/project implementation.
