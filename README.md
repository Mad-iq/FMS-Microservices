This microservices-based Flight Management System uses :
1. Eureka Service Discovery: For microservice registry
2. OpenFeign and API Gateway: For inter-service communication
3. Resilience4j Circuit Breaker: For fault tolerance
4. Spring Cloud Config Server: For centralized configuration
5. RabbitMQ (using Docker) : as an event-driven messaging layer (message broker)
6. Notification Service (using Spring Mail) for asynchronous email delivery.
The system persists data using Spring Data JPA with MySQL, supports validation using Jakarta Validation, and
includes comprehensive JUnit + Mockito test coverage with JaCoCo.
