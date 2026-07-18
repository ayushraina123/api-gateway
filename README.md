# SLP Management System - API Gateway

[![Java](https://img.shields.io/badge/Java-21-blue)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-ready-green)](https://spring.io/projects/spring-boot)
[![Spring Cloud Gateway](https://img.shields.io/badge/Spring_Cloud-Gateway-green)](https://spring.io/projects/spring-cloud-gateway)
[![Docker](https://img.shields.io/badge/Docker-ready-blue)](https://www.docker.com/)

The API Gateway is the public backend entry point for the **SLP Management System**. It receives client requests, forwards them to registered backend services through Eureka, applies gateway-level resilience, and preserves correlation IDs for request tracing.

---

## Table of Contents

- [Responsibilities](#responsibilities)
- [System Context](#system-context)
- [Technology Stack](#technology-stack)
- [Configuration](#configuration)
- [Routing](#routing)
- [Circuit Breaker](#circuit-breaker)
- [Logging](#logging)
- [Local Development](#local-development)
- [Docker](#docker)
- [Future Scope](#future-scope)

---

## Responsibilities

- Provide a single HTTP entry point for backend APIs
- Discover service instances through Eureka
- Route requests to the Devotee Service using Spring Cloud Gateway
- Load balance requests with Spring Cloud LoadBalancer
- Apply Resilience4j circuit breaker behavior
- Forward fallback responses when downstream services are unavailable
- Propagate `X-Correlation-Id` to downstream services

---

## System Context

```text
Browser / Client
  |
  v
Frontend
  |
  v
API Gateway
  |
  v
Eureka Service Discovery
  |
  v
Devotee Service
```

The gateway is expected to run on port `8080` locally and in Docker Compose.

---

## Technology Stack

| Area | Technology |
|------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot |
| Gateway | Spring Cloud Gateway |
| Discovery | Netflix Eureka Client |
| Load Balancing | Spring Cloud LoadBalancer |
| Resilience | Resilience4j Circuit Breaker |
| Observability | Spring Boot Actuator, JSON logs |
| Build | Maven |
| Containerization | Docker |

---

## Configuration

The gateway is configured through `src/main/resources/application.properties`. Deployment environments should override values with environment variables.

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `SERVER_PORT` | `8080` | HTTP port for the gateway |
| `EUREKA_CLIENT_SERVICE_URL` | `http://localhost:8761/eureka` | Eureka registration URL |
| `EUREKA_INSTANCE_PREFER_IP_ADDRESS` | `true` | Register service by IP address |

For Docker Compose, use the Eureka service name:

```text
EUREKA_CLIENT_SERVICE_URL=http://eureka-server:8761/eureka
```

---

## Routing

The gateway currently routes all incoming paths to the Devotee Service:

```properties
spring.cloud.gateway.routes[0].id=devotee-service
spring.cloud.gateway.routes[0].uri=lb://DEVOTEE-SERVICE
spring.cloud.gateway.routes[0].predicates[0]=Path=/**
```

Example gateway URLs:

| Purpose | URL |
|---------|-----|
| Devotees API | `http://localhost:8080/api/devotees` |
| Login | `http://localhost:8080/login` |
| Refresh token | `http://localhost:8080/refresh-token` |
| Dashboard summary | `http://localhost:8080/api/dashboard/summary` |

The service name `DEVOTEE-SERVICE` is resolved through Eureka.

---

## Circuit Breaker

The gateway applies a Resilience4j circuit breaker named `devoteeCircuitBreaker` to the Devotee Service route.

Current behavior:

- Sliding window type: count based
- Sliding window size: `30`
- Minimum calls: `20`
- Failure rate threshold: `80%`
- Open-state wait duration: `5s`
- Half-open permitted calls: `3`
- Fallback URI: `forward:/fallback`

---

## Logging

The gateway emits structured JSON logs and is the main entry point for correlation ID propagation.

- Accepts `X-Correlation-Id` from clients
- Generates a new correlation ID when the header is missing
- Forwards the same header to downstream services
- Adds the value to logging MDC as `correlationId`

Example request:

```http
GET /api/devotees HTTP/1.1
Host: localhost:8080
X-Correlation-Id: 8f94b4c4-2f6d-4f4b-8d89-7f8e2cb85b67
Authorization: Bearer <token>
```

---

## Local Development

### Prerequisites

- Java 21
- Maven
- Eureka Server running on port `8761`
- Devotee Service registered in Eureka

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

The gateway starts on:

```text
http://localhost:8080
```

---

## Docker

### Build Image

```bash
docker build -t slp-api-gateway:local .
```

### Run Container

This example assumes the gateway and Eureka Server share a Docker network.

```bash
docker run --rm \
  --name slp-api-gateway \
  -p 8080:8080 \
  -e SERVER_PORT=8080 \
  -e EUREKA_CLIENT_SERVICE_URL=http://eureka-server:8761/eureka \
  -e EUREKA_INSTANCE_PREFER_IP_ADDRESS=true \
  slp-api-gateway:local
```

For the complete stack, use the Docker Compose setup in the **SLP Management System Deployment** folder.

---

## Future Scope

- Add gateway rate limiting
- Add stricter production security headers and CORS configuration
- Add gateway-specific integration tests
- Deploy through Kubernetes