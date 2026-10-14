# Review Platform — Setup Documentation

## 1. Prerequisites

Before running the project, install the following:

* Java JDK
* Eclipse IDE or another Java IDE
* Maven
* Oracle Database
* Git
* Postman (for API testing)

---

## 2. Java Version

The project is developed using Java.

Make sure Java is installed and available in the system PATH.

Check the installed Java version:

```bash
java -version
```

---

## 3. Oracle Database Setup

Install and start Oracle Database.

Create/configure the database user used by the application.

The application uses Oracle JDBC connectivity.

Database configuration is maintained in:

```text
src/main/resources/application.properties
```

Configure the following according to the local environment:

* Database URL
* Database username
* Database password
* Hibernate/JPA settings

---

## 4. Open the Project

Open Eclipse and import the project as an existing Maven project.

The project should contain:

```text
pom.xml
src/
README.md
SETUP.md
```

Allow Maven to download all required dependencies.

---

## 5. Configure Application

Open:

```text
src/main/resources/application.properties
```

Configure the local database and application settings.

Example database configuration format:

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:orcl
spring.datasource.username=YOUR_DATABASE_USERNAME
spring.datasource.password=YOUR_DATABASE_PASSWORD
```

Do not commit real passwords, API keys, email passwords, or other secrets to a public repository.

---

## 6. Run the Application

In Eclipse:

1. Open the Spring Boot main application class.
2. Right-click the class.
3. Select:

```text
Run As → Spring Boot App
```

Wait until the Spring Boot application starts successfully.

---

## 7. Open the Application

After the server starts, open:

```text
http://localhost:8080/review-platform/
```

---

## 8. Swagger UI

Swagger UI can be opened at:

```text
http://localhost:8080/review-platform/swagger-ui/index.html
```

OpenAPI documentation:

```text
http://localhost:8080/review-platform/v3/api-docs
```

---

## 9. Postman Setup

Install Postman and create/open the:

```text
Review Platform API
```

collection.

Use the local application URL:

```text
http://localhost:8080/review-platform
```

for API testing.

---

## 10. Common Startup Checks

If the application does not start:

1. Check that Oracle Database is running.
2. Check the database URL.
3. Check the database username and password.
4. Check that the required Maven dependencies are downloaded.
5. Check the Eclipse Console for the exact error.
6. Make sure the configured server
