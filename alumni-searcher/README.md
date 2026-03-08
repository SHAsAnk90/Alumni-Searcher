# Alumni Searcher Application

A Spring Boot RESTful API application that allows users to search for LinkedIn profiles of alumni from specific educational institutions. It integrates with the PhantomBuster API to scrape rich alumni data directly from LinkedIn based on search criteria and stores this data locally in a PostgreSQL database.

## Features

- **RESTful API Architecture:** Robust headless backend constructed using Spring Boot MVC.
- **PhantomBuster API Integration:** Directly interacts with PhantomBuster Agents to launch searches and retrieve structured JSON results.
- **Data Persistence:** Utilizes Spring Data JPA to store and retrieve historical searches efficiently from a PostgreSQL database.
- **Design Patterns:** Proper application layer separation (Controllers, Services, Repositories, Client, DTOs).
- **Unit Testing:** Comprehensive test suite written in JUnit 5 with Mockito for TDD adherence.
- **Global Error Handling:** Unified API response format using `RestControllerAdvice`.

## Technology Stack

- **Java 17+**
- **Spring Boot 3.x**
  - Spring Web
  - Spring Data JPA
- **PostgreSQL** (Database)
- **PhantomBuster API** (3rd party integration)
- **JUnit 5 & Mockito** (Testing)
- **Maven** (Build Tool)

## Prerequisites

Before running the application, ensure you have the following installed:

1. **Java JDK 17** or higher
2. **Maven 3.6+**
3. **PostgreSQL** database running locally on default port `5432`
4. A valid **PhantomBuster Account** with an active Agent ID and API Key.
5. Your active LinkedIn `sessionCookie` for PhantomBuster.

## Setup Instructions

### 1. Database Configuration

Create a local PostgreSQL database named `alumni_db`:

```sql
CREATE DATABASE alumni_db;
```

Update your `src/main/resources/application.properties` with your database credentials and PhantomBuster keys:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/alumni_db
spring.datasource.username=postgres
spring.datasource.password=your_db_password

# PhantomBuster Properties
phantombuster.api.key=YOUR_PHANTOMBUSTER_API_KEY
phantombuster.agent.id=YOUR_AGENT_ID
phantombuster.session.cookie=YOUR_LINKEDIN_SESSION_COOKIE
```
> **Note:** Do not expose your `sessionCookie` or `api.key` in public repositories. Pass them as environment variables in production.

### 2. Build and Run

You can build and run the application using Maven from the root directory (`alumni-searcher`):

```bash
mvn clean install
mvn spring-boot:run
```
The application will start on `http://localhost:8080`.

## API Endpoints

### 1. Search Alumni Profiles

Initiates a PhantomBuster agent search based on criteria, scrapes LinkedIn, and saves the data to the PostgreSQL database.

**Request:**
- **URL:** `POST /api/alumni/search`
- **Content-Type:** `application/json`

**Body:**
```json
{
  "university": "Stanford University",
  "designation": "Software Engineer",
  "passoutYear": 2020
}
```

**Response:**
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "name": "Jane Doe",
      "currentRole": "Software Engineer",
      "university": "Stanford University",
      "location": "San Francisco, CA",
      "linkedinHeadline": "Software Engineer at Google",
      "passoutYear": 2020,
      "scrapedAt": "2026-03-08T10:15:30"
    }
  ]
}
```

### 2. Get All Saved Alumni Profiles

Retrieves all historically scraped profiles saved in the database.

**Request:**
- **URL:** `GET /api/alumni/all`

**Response:**
```json
{
  "status": "success",
  "data": [
    {
       // ... array of alumni objects
    }
  ]
}
```

### Additional Filter API Endpoints

- `GET /api/alumni/university/{university}` - Filter saved alumni by University name.
- `GET /api/alumni/role/{role}` - Filter saved alumni by their current job designation.
- `GET /api/alumni/location/{location}` - Filter saved alumni by location.

## Running Tests

To run the unit tests, execute the following command:

```bash
mvn test
```

## Architecture

The project follows standard Spring Boot MVC principles:
- **`controller/AlumniController`**: Exposes REST API endpoints.
- **`service/AlumniService`**: Contains business logic, coordinates DB saves and Client calls.
- **`client/PhantomBusterClient`**: Manages HTTP RestTemplate outgoing connections to PhantomBuster handling asynchronous agent polling and JSON traversal.
- **`repository/AlumniRepository`**: Standard Spring Data JPA `JpaRepository` interface for PostgreSQL.
- **`model/Alumni` & `dto/*`**: Entities mappings and Data Transfer Objects mapping to the JSON standard requested.
