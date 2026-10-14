# Review Platform — Known Limitations

## 1. Public SEO Rendering

Public website review content is currently loaded dynamically on the frontend using JavaScript.

For maximum SEO performance, public website and review pages can be migrated to server-side rendering or pre-rendering in a future version.

---

## 2. Email Verification

Business email verification depends on a correctly configured SMTP service.

The local development environment may require valid SMTP credentials before email verification can be fully tested.

---

## 3. DNS Verification

DNS verification requires a publicly accessible domain with the required DNS record.

A localhost URL cannot be used for real-world DNS verification.

---

## 4. Production Deployment

The project has been configured and tested primarily in a local development environment.

Production deployment requires additional infrastructure configuration such as:

* Production server
* Domain
* HTTPS
* Oracle production database
* Secure credentials
* Monitoring
* Backup infrastructure

---

## 5. Environment Configuration

Local development configuration may currently contain environment-specific values.

Before production deployment, sensitive values should be moved to environment variables or a secure secrets-management system.

---

## 6. Advanced Monitoring

Production-grade monitoring and alerting require additional infrastructure configuration.

Future deployments should include:

* Application monitoring
* Database monitoring
* Server monitoring
* Error alerting
* Performance monitoring

---

## 7. Backup Infrastructure

The project documents database backup and restore procedures, but the actual production backup infrastructure depends on the deployment environment and Oracle administration setup.

---

## 8. Advanced Search

The current website search functionality can be extended in the future with:

* Advanced filters
* Category filtering
* Rating filtering
* Sorting
* Search relevance improvements

---

## 9. Scalability

The application is currently designed as a standard Spring Boot application.

For high traffic, additional infrastructure may be required, such as:

* Load balancing
* Caching
* Database optimization
* Connection-pool tuning
* Horizontal scaling

---

## 10. API Rate Limiting

Rate limiting exists for relevant application protection, but production deployments may require more advanced distributed rate limiting when multiple application instances are used.

---

## 11. Frontend Architecture

The current frontend uses HTML, CSS, and JavaScript.

A future version may introduce a more advanced frontend architecture if required by project requirements.

---

## 12. Third-Party Service Dependency

Features such as email verification and external verification processes depend on external services or infrastructure.

Availability and configuration of those services can affect the related functionality.

---

## 13. Testing Scope

Automated tests cover important application components, but additional integration, load, performance, and end-to-end tests can be added for a production-scale deployment.

---

## 14. Documentation

Deployment-specific details may change depending on the hosting provider, server architecture, database infrastructure, and production environment.

The documentation should therefore be updated whenever the deployment architecture changes.
