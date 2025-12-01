**FMS – Flight Management System (Microservices Architecture)**

A scalable Flight Management System built using Spring Boot Microservices, featuring flight search, booking, notifications, seat reservation, and distributed communication using Feign, RabbitMQ, Docker, Circuit Breaker, and MySQL.

This microservices-based Flight Management System uses :
  1. Eureka Service Discovery: For microservice registry
  2. OpenFeign and API Gateway: For inter-service communication
  3. Resilience4j Circuit Breaker: For fault tolerance
  4. Spring Cloud Config Server: For centralized configuration
  5. RabbitMQ (using Docker) : as an event-driven messaging layer (message broker)
  6. Notification Service (using Spring Mail) for asynchronous email delivery.
     
The system persists data using Spring Data JPA with MySQL, supports validation using Jakarta Validation, and
includes comprehensive JUnit + Mockito test coverage with JaCoCo.

System Architecture:

<img width="670" height="476" alt="image" src="https://github.com/user-attachments/assets/b590c8af-24f6-4b0f-b1d5-168faddc59a4" />

Microservices in This Repository:

| Service Name                | Description                                           |
| --------------------------- | ----------------------------------------------------- |
| `flight-microservice`       | Manages flights, search, seat inventory               |
| `booking-microservice`      | Handles bookings, cancellations, history              |
| `notification-microservice` | Sends email notifications triggered by booking events |
| `service-registry`          | Eureka service registry                               |
| `api-gateway`               | Routes requests to respective services                |
| `config-server`             | Centralized configuration server                      |


API Endpoints:

1. Flight Service

| Method | Endpoint                             | Description        |
| ------ | ------------------------------------ | ------------------ |
| POST   | `/api/flight`                        | Add a flight       |
| POST   | `/api/flight/search`                 | Search flights     |
| POST   | `/api/flight/inventory/reserve/{id}` | Reserve seats      |
| POST   | `/api/flight/inventory/release/{id}` | Release seats      |
| GET    | `/api/flight/inventory/{id}`         | Get flight details |

2. Booking Service

| Method | Endpoint                              | Description        |
| ------ | ------------------------------------- | ------------------ |
| POST   | `/api/flight/booking/{flightId}`      | Book ticket        |
| GET    | `/api/flight/ticket/{pnr}`            | Get ticket details |
| GET    | `/api/flight/booking/history/{email}` | Booking history    |
| DELETE | `/api/flight/booking/cancel/{pnr}`    | Cancel booking     |

Database Design:

1. Flight Database:

<img width="780" height="728" alt="image" src="https://github.com/user-attachments/assets/07e3c25e-44ea-49de-a9d2-281b7e693491" />


