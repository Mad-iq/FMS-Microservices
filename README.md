# Flight Booking Microservices

_(I have added the logs folder containing the docker logs for each individual service)_

### A scalable Flight Management System built using Spring Boot Microservices, featuring flight search, booking, notifications, seat reservation, and distributed communication using Feign, RabbitMQ, Docker, Circuit Breaker, and MySQL.


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




Ports in use:


| Thing         | Internal Port | External Port (Postman) | Notes                                   |
| ------------- | ------------- | ----------------------- | --------------------------------------- |
| API Gateway   | 8765          | 8765                    | You call localhost:8765                 |
| Flight        | 8081          | 8081                    | Gateway uses internal:8081              |
| Booking       | 8082          | 8082                    | Gateway → internal, Postman → external  |
| Notification  | 8083          | 8083                    | Same logic                              |
| Config Server | 8888          | 8888                    | Services use internal 8888              |
| Eureka        | 8761          | 8761                    | Services use internal 8761              |
| MySQL         | 3306          | 3306                    | Java services use internal `mysql:3306` 




API Endpoints:

1. Authentication Service (via API Gateway)

| Method | Endpoint             | Description               |
| ------ | -------------------- | ------------------------- |
| POST   | `/api/auth/register` | Register a new user       |
| POST   | `/api/auth/login`    | User login (generate JWT) |



3. Flight Service

| Method | Endpoint                             | Description        |
| ------ | ------------------------------------ | ------------------ |
| POST   | `/api/flight`                        | Add a flight       |
| POST   | `/api/flight/search`                 | Search flights     |
| POST   | `/api/flight/inventory/reserve/{id}` | Reserve seats      |
| POST   | `/api/flight/inventory/release/{id}` | Release seats      |
| GET    | `/api/flight/inventory/{id}`         | Get flight details |

3. Booking Service

| Method | Endpoint                              | Description        |
| ------ | ------------------------------------- | ------------------ |
| POST   | `/api/flight/booking/{flightId}`      | Book ticket        |
| GET    | `/api/flight/ticket/{pnr}`            | Get ticket details |
| GET    | `/api/flight/booking/history/{email}` | Booking history    |
| DELETE | `/api/flight/booking/cancel/{pnr}`    | Cancel booking     |



Database Design:


<img width="780" height="728" alt="image" src="https://github.com/user-attachments/assets/07e3c25e-44ea-49de-a9d2-281b7e693491" />



Docker COnfigurations:


1. Building the docker containers for each microservice

<img width="1919" height="944" alt="Screenshot 2025-12-08 191911" src="https://github.com/user-attachments/assets/ef853e68-3ac2-4a93-af52-ef160b190128" />




2. Running the dockerized containers

<img width="1901" height="958" alt="Screenshot 2025-12-08 192944" src="https://github.com/user-attachments/assets/08327b84-d735-4771-8fa4-cdced039786e" />




3. Creating the databases inside the mysql container

<img width="1919" height="963" alt="Screenshot 2025-12-08 194325" src="https://github.com/user-attachments/assets/ca1523cb-5878-4b93-aff9-1fb5facb1bfe" />




4. Docker desktop showing all the services for the Flight Management System

<img width="1919" height="968" alt="Screenshot 2025-12-08 223215" src="https://github.com/user-attachments/assets/0050f854-d6e5-43a1-9c42-5da6e75b2e6f" />




5. All services registered on eureka

<img width="1919" height="949" alt="Screenshot 2025-12-08 192915" src="https://github.com/user-attachments/assets/a89906fd-4791-4aba-8860-c5ac029ac81e" />




API Endpoint testing using Postman

1. Registering the User or Admin (depending on role)

<img width="1722" height="932" alt="Screenshot 2025-12-09 000954" src="https://github.com/user-attachments/assets/78193d9e-a90d-4d44-8e03-b4a02229625d" />

<img width="1716" height="944" alt="Screenshot 2025-12-09 001032" src="https://github.com/user-attachments/assets/77a2fd74-04ef-49d0-9604-e53d9bf6461e" />





2. Signin

<img width="1745" height="934" alt="Screenshot 2025-12-09 001411" src="https://github.com/user-attachments/assets/670662ef-6e83-44a9-aa42-b629da4aaaeb" />




3. Unauthorised access (without jwt token)

<img width="1767" height="938" alt="Screenshot 2025-12-09 001805" src="https://github.com/user-attachments/assets/f3339365-88f7-4e4f-befb-fc03645c5bb1" />




4. Unauthorized access (using USER token, when ADMIN token is required)

<img width="1728" height="937" alt="image" src="https://github.com/user-attachments/assets/269a9509-9eed-48cb-b721-db91e5a08db7" />




5. Adding inventory (for ADMIN role)

<img width="1731" height="900" alt="Screenshot 2025-12-09 002206" src="https://github.com/user-attachments/assets/1c376bf6-1ba6-4347-82eb-62a725644999" />




6. Searching for a flight (open for any role)

<img width="1726" height="880" alt="Screenshot 2025-12-09 002725" src="https://github.com/user-attachments/assets/5ea589f4-78df-4d90-b3e6-535b489c28cd" />




7. Booking a flight (for USER role only)

<img width="1740" height="936" alt="Screenshot 2025-12-09 003133" src="https://github.com/user-attachments/assets/0a06890e-11b8-4710-a3ac-b57819d96b21" />




8. Searching with PNR number (for USER role only)

<img width="1734" height="931" alt="Screenshot 2025-12-09 003248" src="https://github.com/user-attachments/assets/9db35f34-eb54-4e5f-87e8-85f92c8dddd3" />




9. Searching with email (for USER role only)

<img width="1750" height="932" alt="Screenshot 2025-12-09 003345" src="https://github.com/user-attachments/assets/30899409-37d1-4792-84e1-86d58ea69ac3" />




10. Delete a booking (for USER role only)

<img width="1739" height="930" alt="Screenshot 2025-12-09 003549" src="https://github.com/user-attachments/assets/3e750412-9288-47f6-91d8-65c22358cf92" />











   












