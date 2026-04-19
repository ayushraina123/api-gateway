# API Gateway (Spring Cloud Gateway)

This project implements an **API Gateway** using **Spring Boot** and **Spring Cloud Gateway**.  
The gateway acts as a **single entry point for client requests** and routes them to appropriate backend microservices.

It integrates with **Service Discovery (Eureka)** to dynamically locate available service instances.

---

## Tech Stack

- Java
- Spring Boot
- Spring Cloud Gateway
- Netflix Eureka Client
- Spring Cloud LoadBalancer
- Maven

---

## Architecture Overview
- Client  
  ↓
- API Gateway  
  ↓
- Service Discovery (Eureka Server)  
  ↓
- Backend Service  
  ↓
- Database


---

## Responsibilities of the API Gateway

- Centralized entry point for all client requests
- Request routing to backend services
- Load balancing across service instances
- Integration with service discovery
- Ability to apply cross-cutting concerns such as:
    - Rate limiting
    - Circuit breaking
    - Authentication and authorization
    - Logging and monitoring

---

## Logging and Request Tracking

The gateway emits **structured JSON logs** and is the main entry point for correlation ID propagation.

- Accepts the `X-Correlation-Id` request header from clients
- Generates a new correlation ID when the header is missing
- Forwards the same `X-Correlation-Id` header to downstream services
- Includes `correlationId` in gateway request, authentication, and fallback logs

Example request:

```http
GET /api/devotees HTTP/1.1
Host: localhost:8080
X-Correlation-Id: 8f94b4c4-2f6d-4f4b-8d89-7f8e2cb85b67
Authorization: Bearer <token>
```

Use the same `correlationId` value to trace a request in the gateway and backend service logs.

---

## Running the Application

### Build the project

```bash
mvn clean install
```
## Run the Application

```bash
mvn spring-boot:run
```

## Example Request Flow

Client sends request to **API Gateway**:
````
http://localhost:8080/api/service-endpoint
````



### API Gateway:

1. Receives the request
2. Validates the **JWT token** for authentication
3. Uses **Eureka Service Discovery** to locate available service instances
4. Applies **Resilience4j Circuit Breaker** to monitor service health
5. Routes the request to the appropriate microservice
6. If the service fails or becomes unavailable, the **fallback endpoint** is triggered

---

## Implemented Features

- Request routing using **Spring Cloud Gateway**
- Service discovery using **Netflix Eureka**
- Load balancing using **Spring Cloud LoadBalancer**
- **JWT-based authentication** for securing API requests
- **Circuit breaker implemented using Resilience4j**
- **Fallback mechanism** to handle service failures gracefully

---

## Future Improvements

- Implement rate limiting
- Containerize using **Docker**
- Deploy using **Kubernetes**
