# Complex Spring Boot Application

A comprehensive Spring Boot application demonstrating enterprise-level features and best practices.

## 🚀 Features

### Core Technologies
- **Spring Boot 3.2.0** - Latest Spring Boot version with Java 17
- **Spring Security** - JWT-based authentication and authorization
- **Spring Data JPA** - Database persistence with Hibernate
- **Spring Data Redis** - Caching and session management
- **Spring Data Elasticsearch** - Full-text search capabilities
- **Spring Kafka** - Event-driven messaging
- **Spring Cloud** - Microservices support with OpenFeign and Circuit Breaker

### Security & Authentication
- JWT token-based authentication
- Role-based access control (RBAC)
- Password encryption with BCrypt
- Account lockout protection
- Email verification system
- Password reset functionality

### Database & Caching
- **H2 Database** - In-memory database for development
- **PostgreSQL** - Production-ready database support
- **Redis** - Distributed caching with TTL configuration
- **Connection pooling** with HikariCP
- **Database migrations** with Hibernate

### Monitoring & Management
- **Spring Boot Admin** - Application monitoring
- **Actuator** - Health checks and metrics
- **Prometheus** - Metrics collection
- **Micrometer** - Application metrics
- **Logging** - Structured logging with SLF4J

### API Documentation
- **OpenAPI 3.0** - API documentation with Swagger UI
- **RESTful APIs** - Comprehensive REST endpoints
- **Validation** - Request/response validation
- **Error handling** - Global exception handling

### Additional Features
- **WebSocket** - Real-time communication
- **Email service** - SMTP email integration
- **Batch processing** - Spring Batch integration
- **Scheduling** - Quartz scheduler integration
- **File upload** - Multipart file handling
- **CORS** - Cross-origin resource sharing
- **Compression** - Response compression

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker (optional, for external services)

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd complex-springboot-app
```

### 2. Build the Application
```bash
mvn clean install
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access the Application
- **Main Application**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **Actuator**: http://localhost:8080/actuator

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/example/complexapp/
│   │   ├── config/           # Configuration classes
│   │   ├── controller/       # REST controllers
│   │   ├── domain/          # JPA entities
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Custom exceptions
│   │   ├── mapper/          # MapStruct mappers
│   │   ├── repository/      # Data repositories
│   │   ├── security/        # Security components
│   │   ├── service/         # Business logic
│   │   ├── util/            # Utility classes
│   │   ├── client/          # External API clients
│   │   ├── search/          # Elasticsearch components
│   │   ├── messaging/       # Kafka components
│   │   ├── batch/           # Batch processing
│   │   └── scheduler/       # Scheduled tasks
│   └── resources/
│       ├── application.yml  # Application configuration
│       └── templates/       # Thymeleaf templates
└── test/                    # Test classes
```

## 🔧 Configuration

### Application Properties
The application uses YAML configuration with multiple profiles:

- **dev** - Development environment (default)
- **prod** - Production environment
- **test** - Testing environment

### Key Configuration Sections
- Database configuration (H2/PostgreSQL)
- Redis caching configuration
- Elasticsearch configuration
- Kafka messaging configuration
- Email configuration
- Security configuration
- Monitoring configuration

## 🔐 Security

### Authentication Flow
1. User registers with username/email and password
2. Password is encrypted using BCrypt
3. JWT token is generated upon successful login
4. Token is validated on each request
5. Role-based access control is enforced

### Available Roles
- **USER** - Basic user permissions
- **ADMIN** - Full administrative access
- **MODERATOR** - Content moderation permissions
- **PREMIUM_USER** - Enhanced user features

## 📊 Monitoring

### Health Checks
- Database connectivity
- Redis connectivity
- Elasticsearch connectivity
- Kafka connectivity

### Metrics
- Application metrics
- Business metrics
- Custom metrics
- Prometheus integration

## 🧪 Testing

### Test Types
- Unit tests
- Integration tests
- End-to-end tests
- Performance tests

### Test Containers
- PostgreSQL container
- Redis container
- Elasticsearch container
- Kafka container

## 🚀 Deployment

### Docker Deployment
```bash
# Build Docker image
docker build -t complex-springboot-app .

# Run container
docker run -p 8080:8080 complex-springboot-app
```

### Production Considerations
- Use PostgreSQL instead of H2
- Configure external Redis instance
- Set up Elasticsearch cluster
- Configure Kafka cluster
- Enable HTTPS
- Set up monitoring and alerting
- Configure backup strategies

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - User logout

### User Management Endpoints
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Product Endpoints
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation

## 🔄 Version History

- **v1.0.0** - Initial release with core features
- **v1.1.0** - Added monitoring and metrics
- **v1.2.0** - Enhanced security features
- **v1.3.0** - Added batch processing and scheduling
