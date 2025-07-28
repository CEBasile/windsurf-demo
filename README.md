# Ticket Submission Application

A full-stack ticket management system built with Spring Boot (backend) and Angular (frontend), featuring OpenID Connect authentication and role-based access control.

## 🏗️ Architecture

- **Backend**: Spring Boot 3.3.5 with Java 21
- **Frontend**: Angular 17+ with standalone components
- **Database**: H2 (development), configurable for production
- **Authentication**: OpenID Connect (OIDC) with JWT tokens
- **Security**: Role-based access control (ADMIN, SUPPORT, USER)

## 📋 Features

- **Ticket Management**: Create, view, update, and delete tickets
- **Role-Based Access Control**:
  - **ADMIN**: Full access including delete operations
  - **SUPPORT**: Can view all tickets and update any ticket
  - **USER**: Can only view/update own tickets
- **Security Toggle**: Enable/disable authentication for development
- **Responsive UI**: Modern Angular frontend with mobile support
- **RESTful API**: Comprehensive REST endpoints with proper HTTP status codes

## 🚀 Quick Start

### Prerequisites

- **Java 21** or higher
- **Node.js 18+** and npm
- **Maven 3.6+**
- Git

### 1. Clone the Repository

```bash
git clone <repository-url>
cd windsurf-project
```

### 2. Backend Setup

```bash
# Build the backend
mvn clean install

# Run the backend (will start on http://localhost:8080)
mvn spring-boot:run

# Run the backend without security for local development (Powershell)
mvn spring-boot:run "-Dspring-boot.run.arguments=--security.enabled=false"
```

### 3. Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start the development server (will start on http://localhost:4200)
ng serve
```

### 4. Access the Application

- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080/api
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: `password`

## 🔧 Configuration

### Security Configuration

The application supports toggling authentication on/off via configuration:

#### Enable Security (Production)
```yaml
spring:
  security:
    enabled: true
```

#### Disable Security (Development)
```yaml
spring:
  security:
    enabled: false
```

#### Environment Variable Override
```bash
# Enable security
export SECURITY_ENABLED=true

# Disable security  
export SECURITY_ENABLED=false
```

For detailed security configuration, see [SECURITY_TOGGLE.md](SECURITY_TOGGLE.md).

### OpenID Connect Setup

For production with OIDC authentication, configure your identity provider:

```yaml
spring:
  security:
    enabled: true
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-oidc-provider.com
          jwk-set-uri: https://your-oidc-provider.com/.well-known/jwks.json
```

## 🧪 Testing

### Backend Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=SecurityToggleTest

# Run integration tests
mvn test -Dtest=*IntegrationTest
```

### Security Testing

The project includes comprehensive security test scripts:

```bash
# Windows
.\test-security.ps1

# Linux/Mac
./test-security.sh
```

These scripts test:
- Authentication requirements
- Role-based access control
- Ticket ownership validation
- JWT token processing

### Frontend Tests

```bash
cd frontend

# Run unit tests
ng test

# Run e2e tests
ng e2e

# Run tests with coverage
ng test --code-coverage
```

## 📚 API Documentation

### Authentication

All API endpoints (except actuator) require authentication when security is enabled.

**Headers Required:**
```
Authorization: Bearer <JWT_TOKEN>
```

### Endpoints

| Method | Endpoint | Description | Required Roles |
|--------|----------|-------------|----------------|
| POST | `/api/tickets` | Create a new ticket | Any authenticated user |
| GET | `/api/tickets` | Get all tickets | ADMIN, SUPPORT |
| GET | `/api/tickets/my` | Get current user's tickets | Any authenticated user |
| GET | `/api/tickets/{id}` | Get specific ticket | Owner, ADMIN, SUPPORT |
| PUT | `/api/tickets/{id}` | Update ticket | Owner, ADMIN, SUPPORT |
| DELETE | `/api/tickets/{id}` | Delete ticket | ADMIN only |

### Request/Response Examples

#### Create Ticket
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "title": "Login Issue",
    "description": "Cannot access my account",
    "priority": "HIGH"
  }'
```

#### Response
```json
{
  "id": 1,
  "title": "Login Issue",
  "description": "Cannot access my account",
  "status": "OPEN",
  "priority": "HIGH",
  "createdAt": "2024-01-15T10:30:00Z",
  "createdBy": "user123"
}
```

## 🏗️ Building for Production

### Backend

```bash
# Create production JAR
mvn clean package -DskipTests

# Run production build
java -jar target/ticket-app-1.0.0.jar
```

### Frontend

```bash
cd frontend

# Build for production
ng build --configuration production

# Files will be in dist/ directory
```

### Docker (Optional)

```dockerfile
# Backend Dockerfile example
FROM openjdk:17-jre-slim
COPY target/ticket-app-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔍 Development

### Project Structure

```
windsurf-project/
├── src/main/java/com/ticketapp/
│   ├── controller/          # REST controllers
│   ├── entity/             # JPA entities
│   ├── repository/         # Data repositories
│   ├── service/            # Business logic
│   └── config/             # Configuration classes
├── src/main/resources/
│   ├── application.yml     # Main configuration
│   └── application-test.yml # Test configuration
├── src/test/               # Test classes
├── frontend/
│   ├── src/app/           # Angular components
│   ├── src/assets/        # Static assets
│   └── package.json       # Frontend dependencies
└── pom.xml                # Maven configuration
```

### Code Quality

The project includes:
- Comprehensive unit and integration tests
- Security testing with mock JWT tokens
- Proper error handling and validation
- Clean architecture with separation of concerns

## 🐛 Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Find process using port 8080
   netstat -ano | findstr :8080
   # Kill the process
   taskkill /PID <process_id> /F
   ```

2. **Frontend Build Errors**
   ```bash
   # Clear npm cache
   npm cache clean --force
   # Delete node_modules and reinstall
   rm -rf node_modules package-lock.json
   npm install
   ```

3. **Database Connection Issues**
   - Check H2 console at http://localhost:8080/h2-console
   - Verify JDBC URL: `jdbc:h2:mem:testdb`
   - Default credentials: sa/password

4. **Authentication Issues**
   - Verify JWT token format and claims
   - Check OIDC provider configuration
   - Use security toggle for development: `SECURITY_ENABLED=false`

## 📝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Support

For questions or issues:
1. Check the troubleshooting section above
2. Review the [SECURITY_TOGGLE.md](SECURITY_TOGGLE.md) for authentication setup
3. Run the security test scripts to validate your setup
4. Open an issue in the repository

---

**Happy coding! 🎉**
