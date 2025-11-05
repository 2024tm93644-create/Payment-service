Author -
Amitosh Gautam
Payment Service - Event Ticketing System
Developed as part of the Software Architecture and Microservices Assignment.

** For more details check the Amitosh_PaymentService.pdf file attached in the repository **

--------------------------------------------------------------------------------------------------------------------------------------------------

# Payment Service - Event Ticketing System

## Overview
The Payment Service is a microservice in the Event Ticketing System that manages customer payments, refunds, and transaction tracking. It provides REST APIs to process charges, issue refunds, and maintain idempotency to prevent duplicate payments. The service is built using Spring Boot and PostgreSQL, and can be deployed using Docker and Minikube (Kubernetes).

## Features
- Process payment requests with idempotency support
- Record and track payment status (PENDING, SUCCESS, FAILED, REFUNDED)
- Process refunds for successful payments
- Store transactions in PostgreSQL
- REST APIs with Swagger documentation
- Docker and Kubernetes deployment support

## Technologies Used
| Component | Technology |
|------------|-------------|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Build Tool | Maven |
| Database | PostgreSQL |
| API Documentation | Swagger / OpenAPI |
| Deployment | Docker, Minikube |
| Monitoring | Spring Boot Actuator, Prometheus |

## Project Structure
payment-service/
│
├── src/
│ ├── main/java/com/ticketing/payment/
│ │ ├── controller/
│ │ ├── dto/
│ │ ├── model/
│ │ ├── repository/
│ │ ├── service/
│ │ └── config/
│ └── resources/
│ ├── application.yml
│ └── data.sql (optional)
│
├── Dockerfile
├── docker-compose.yml
├── k8s/
│ ├── deployment.yaml
│ ├── service.yaml
│ └── configmap.yaml
├── swagger.yaml
├── README.md
└── pom.xml


## Setup Instructions

### 1. Database Setup
Create an empty PostgreSQL database named `paymentsdb` and update the credentials in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/paymentsdb
    username: postgres
    password: admin
  jpa:
    hibernate:
      ddl-auto: update

2. Build and Run
mvn clean package -DskipTests
java -jar target/payment-service-1.0.0.jar


The service will be available at http://localhost:8080.

3. Access Swagger UI
Open the following URL in your browser:
http://localhost:8080/swagger-ui/index.html

4. Import to Postman
Open Postman, select Import, and upload the swagger.yaml file.
All endpoints will be automatically created in a collection.

API Endpoints -
Endpoint                     |        	Method	           |       Description
/v1/payments/charge	         |     POST	                   |Create a new payment (requires Idempotency-Key header)
/v1/payments/{id}            |	GET	                       |Retrieve payment details
/v1/payments/refund          |	POST                       |	Process a refund for a successful payment

Idempotency-
Each payment request must include an Idempotency-Key header.
If the same key is reused with identical data, the service returns the previous response.
If the payload differs, it throws a conflict error.

Example header:
Idempotency-Key: TXN-12345

Payment Status Flow -
Payment starts in PENDING state.
If amount is even, it succeeds and changes to SUCCESS.
If amount is odd, it fails and changes to FAILED.
Refund changes status to REFUNDED.

Docker Setup
To build and run using Docker:
docker-compose up --build


This will start the Payment Service and PostgreSQL together.
Access the service at http://localhost:8080.




