# ByteBites Microservices Project

## Overview

ByteBites is a microservices-based system designed to manage restaurant operations, including authentication, restaurant management, order processing, and API gateway routing. The project leverages Spring Boot, Spring Cloud, Eureka for service discovery, and RabbitMQ for messaging.

## Architecture

The system consists of the following services:

- **Eureka Server**: Service registry for microservice discovery.
- **API Gateway**: Central entry point for all client requests, routing to appropriate services.
- **Auth Service**: Handles user authentication and authorization.
- **Restaurant Service**: Manages restaurant data and operations.
- **Order Service**: Handles order creation, management, and processing.

Each service is a standalone Spring Boot application with its own configuration and dependencies.

---

## Services Breakdown

### 1. Eureka Server

- **Path**: `eureka_server`
- **Purpose**: Service registry for all microservices.
- **Tech Stack**: Spring Boot, Spring Cloud Netflix Eureka
- **Main Class**: `EurekaServerApplication.java`
- **Config**: `src/main/resources/application.yml`
  - Runs on port `2000`
  - Self-registration disabled

### 2. API Gateway

- **Path**: `api-gateway`
- **Purpose**: Routes external requests to internal services, handles JWT security.
- **Tech Stack**: Spring Boot, Spring Cloud Gateway, Eureka Client
- **Main Class**: `ApiGatewayApplication.java`
- **Config**: `src/main/resources/application.yml`
  - Runs on port `5000`
  - Routes:
    - `/api/v1/auth/**` → Auth Service
    - `/api/v1/restaurants/**` → Restaurant Service
    - `/api/v1/orders/**` → Order Service
  - JWT security settings

### 3. Auth Service

- **Path**: `auth-service`
- **Purpose**: User authentication, JWT issuance, and user management.
- **Tech Stack**: Spring Boot, Spring Security, JPA, PostgreSQL, Eureka Client
- **Main Class**: `AuthServiceApplication.java`
- **Config**: `src/main/resources/application.yml`
  - Default port: `3000`
  - Uses environment variables for DB and JWT config
  - Registers with Eureka

### 4. Restaurant Service

- **Path**: `restaurant_service`
- **Purpose**: CRUD operations for restaurants, menu management.
- **Tech Stack**: Spring Boot, Spring Security, JPA, PostgreSQL, Eureka Client
- **Main Class**: `RestaurantServiceApplication.java`
- **Config**: `src/main/resources/application.yml`
  - Default port: `4000`
  - Uses environment variables for DB and JWT config
  - Registers with Eureka

### 5. Order Service

- **Path**: `order_service`
- **Purpose**: Order creation, status tracking, and messaging via RabbitMQ.
- **Tech Stack**: Spring Boot, Spring Security, JPA, PostgreSQL, RabbitMQ, Eureka Client
- **Main Class**: `OrderServiceApplication.java`
- **Config**: `src/main/resources/application.yml`
  - Uses environment variables for DB, RabbitMQ, and service config
  - Registers with Eureka

---

## Common Environment Variables

Each service uses environment variables for configuration. Example variables:

- `SERVER_PORT`: Service port
- `SPRING_APPLICATION_NAME`: Service name
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER_CLASS_NAME`: Database config
- `JWT_SECRET`, `JWT_EXPIRATION_MS`: JWT settings (where applicable)
- `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`: RabbitMQ config (Order Service)

---

## Running the System

1. **Start Eureka Server**:
   - Navigate to `eureka_server` and run: `mvn spring-boot:run`
2. **Start API Gateway**:
   - Navigate to `api-gateway` and run: `mvn spring-boot:run`
3. **Start Auth, Restaurant, and Order Services**:
   - For each, navigate to the directory and run: `mvn spring-boot:run`
   - Ensure required environment variables are set (see above)

---

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.3**
- **Spring Cloud 2025.0.0**
- **Spring Security** with JWT authentication
- **Spring Data JPA** for database operations
- **PostgreSQL** as primary database
- **RabbitMQ** for message queuing (Order Service)
- **Eureka Server** for service discovery
- **Spring Cloud Gateway** for API routing
- **Maven** for dependency management
- **Lombok** for reducing boilerplate code

---

## Prerequisites

Before running the system, ensure you have the following installed:

- **Java 21** or higher
- **Maven 3.6+**
- **PostgreSQL** database
- **RabbitMQ** (for Order Service messaging)
- **Git**

## Quick Start

1. **Clone the repository**:

   ```bash
   git clone <repository-url>
   cd ByteBites
   ```

2. **Set up environment variables**:
   Create `.env` files in each service directory or set environment variables:

   ```bash
   # Database Configuration
   DB_URL=jdbc:postgresql://localhost:5432/bytebites
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   DB_DRIVER_CLASS_NAME=org.postgresql.Driver

   # JWT Configuration
   JWT_SECRET=your_jwt_secret_key
   JWT_EXPIRATION_MS=86400000

   # RabbitMQ Configuration (for Order Service)
   RABBITMQ_HOST=localhost
   RABBITMQ_PORT=5672
   RABBITMQ_USERNAME=guest
   RABBITMQ_PASSWORD=guest
   ```

3. **Start services in order**:

   ```bash
   # Terminal 1: Start Eureka Server
   cd eureka_server
   mvn spring-boot:run

   # Terminal 2: Start API Gateway
   cd api-gateway
   mvn spring-boot:run

   # Terminal 3: Start Auth Service
   cd auth-service
   mvn spring-boot:run

   # Terminal 4: Start Restaurant Service
   cd restaurant_service
   mvn spring-boot:run

   # Terminal 5: Start Order Service
   cd order_service
   mvn spring-boot:run
   ```

4. **Verify services are running**:
   - Eureka Dashboard: http://localhost:2000
   - API Gateway: http://localhost:5000

## API Endpoints

### Auth Service (Port 3000)

- `POST /api/v1/auth/signin` - User login/authentication
- `POST /api/v1/auth/register` - User registration (if implemented)
- `POST /api/v1/auth/refresh` - Refresh JWT token (if implemented)

**Request Body for Login:**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**

```json
{
  "token": "jwt_token_here",
  "userId": 1,
  "email": "user@example.com",
  "role": "CUSTOMER",
  "fullName": "John Doe"
}
```

### Restaurant Service (Port 4000)

- `GET /api/v1/restaurants` - Get all restaurants (public access)
- `POST /api/v1/restaurants` - Create restaurant (requires RESTAURANT_OWNER role)
- `GET /api/v1/restaurants/{id}` - Get restaurant by ID (public access)
- `PUT /api/v1/restaurants/{id}` - Update restaurant (requires ownership)
- `DELETE /api/v1/restaurants/{id}` - Delete restaurant (requires ownership)
- `GET /api/v1/restaurants/my-restaurants` - Get current user's restaurants
- `GET /api/v1/restaurants/{id}/exists` - Check if restaurant exists
- `GET /api/v1/restaurants/count` - Get total count of restaurants

**Authentication:** Requires JWT token with appropriate role (RESTAURANT_OWNER for write operations)

### Order Service (Port varies)

- `POST /api/v1/orders` - Create order (requires CUSTOMER role)
- `GET /api/v1/orders/{id}` - Get order by ID
- `GET /api/v1/orders/customer/{customerId}` - Get orders by customer ID
- `GET /api/v1/orders/restaurant/{restaurantId}` - Get orders by restaurant ID

**Authentication:** Requires JWT token with CUSTOMER role for order creation
**Messaging:** Uses RabbitMQ for order processing and notifications

## Development

### Project Structure

```
ByteBites/
├── eureka_server/          # Service discovery
├── api-gateway/           # API Gateway
├── auth-service/          # Authentication service
├── restaurant_service/    # Restaurant management
├── order_service/         # Order processing
└── README.md
```

### Building Individual Services

```bash
# Build a specific service
cd <service-directory>
mvn clean install

# Run tests
mvn test

# Package as JAR
mvn package
```

## Troubleshooting

### Common Issues

1. **Service not registering with Eureka**:

   - Check if Eureka Server is running on port 2000
   - Verify service configuration in `application.yml`
   - Check network connectivity

2. **Database connection issues**:

   - Ensure PostgreSQL is running
   - Verify database credentials in environment variables
   - Check database URL format

3. **JWT authentication failures**:

   - Verify JWT_SECRET is set consistently across services
   - Check token expiration settings

4. **RabbitMQ connection issues**:
   - Ensure RabbitMQ server is running
   - Verify connection credentials
   - Check firewall settings

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Security & Authentication

### JWT Token Structure

The system uses JWT tokens for authentication across all services. Tokens contain:

- `userId`: User's unique identifier
- `email`: User's email address
- `role`: User's role (CUSTOMER, RESTAURANT_OWNER)
- `fullName`: User's full name

### Role-Based Access Control

- **CUSTOMER**: Can create orders and view their own orders
- **RESTAURANT_OWNER**: Can manage restaurants (CRUD operations)

### API Gateway Security

- Validates JWT tokens and extracts user information
- Adds user headers (`X-User-Id`, `X-User-Role`, `X-User-Email`, `X-User-FullName`) to requests
- Routes requests to appropriate microservices

## Notes

- All services register with Eureka for discovery
- API Gateway handles routing and JWT-based security
- Use `.env` files or environment variables for sensitive configuration
- For more details, see each service's `HELP.md` and `application.yml`
- Services use Spring Boot DevTools for hot reloading during development
- Each service has its own security configuration and exception handling
- Global exception handlers provide consistent error responses across services
