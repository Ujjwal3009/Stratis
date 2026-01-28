# UPSC AI Backend

AI-powered UPSC Test Platform - Backend API built with Spring Boot

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Running the Application](#running-the-application)
- [Environment Configuration](#environment-configuration)
- [Database Migrations](#database-migrations)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Deployment](#deployment)
- [Project Structure](#project-structure)
- [Additional Resources](#additional-resources)

---

## 🔧 Prerequisites

Before running the application, ensure you have the following installed:

- **Java 17 or higher** ([Download](https://adoptium.net/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))
- **Docker** (optional, for PostgreSQL) ([Download](https://www.docker.com/))
- **Git** ([Download](https://git-scm.com/))

### Verify Installation

```bash
java -version    # Should show Java 17+
mvn -version     # Should show Maven 3.6+
docker --version # Optional
```

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd upsc-backend
```

### 2. Run the Application (Development Mode)

```bash
# Run with Maven (recommended for development)
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

### 3. Verify It's Running

```bash
# Health check
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2"
      }
    }
  }
}
```

---

## 🏃 Running the Application

### Method 1: Maven (Development)

```bash
# Default profile (dev)
mvn spring-boot:run

# Explicit dev profile
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

**Features:**
- H2 in-memory database
- H2 Console at http://localhost:8080/h2-console
- All test endpoints enabled
- Verbose logging (DEBUG level)
- Auto-reload on code changes

---

### Method 2: JAR File (Production-like)

```bash
# Build the JAR
mvn clean package -DskipTests

# Run the JAR
java -jar target/upsc-backend-0.0.1-SNAPSHOT.jar
```

---

### Method 3: Docker (Staging/Production)

```bash
# Development with Docker
docker-compose up

# Staging with PostgreSQL
docker-compose -f docker-compose.staging.yml up -d

# View logs
docker-compose -f docker-compose.staging.yml logs -f app

# Stop
docker-compose -f docker-compose.staging.yml down
```

---

## ⚙️ Environment Configuration

The application supports three environments:

### Development (Default)

```bash
# No configuration needed - uses H2 in-memory database
mvn spring-boot:run
```

**Access H2 Console:**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:upsc_ai_dev`
- Username: `sa`
- Password: (leave empty)

---

### Staging

```bash
# 1. Create .env file
cp .env.example .env

# 2. Edit .env with your staging credentials
nano .env

# 3. Run with staging profile
SPRING_PROFILES_ACTIVE=staging mvn spring-boot:run

# Or with Docker
docker-compose -f docker-compose.staging.yml up -d
```

---

### Production

```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-rds-endpoint.amazonaws.com
export DB_USERNAME=your-db-user
export DB_PASSWORD=your-secure-password
export JWT_SECRET=your-production-jwt-secret

# Run application
java -jar target/upsc-backend-0.0.1-SNAPSHOT.jar
```

---

## 🗄️ Database Migrations

The application uses **Flyway** for database version control.

### Automatic Migrations

Migrations run automatically on application startup:

```bash
mvn spring-boot:run
```

**Console Output:**
```
INFO  o.f.core.internal.command.DbMigrate - Migrating schema "PUBLIC" to version "1 - Initial Schema"
INFO  o.f.core.internal.command.DbMigrate - Migrating schema "PUBLIC" to version "2 - Add Indexes And Constraints"
INFO  o.f.core.internal.command.DbMigrate - Migrating schema "PUBLIC" to version "3 - Seed Data"
INFO  o.f.core.internal.command.DbMigrate - Successfully applied 3 migrations
```

### Default Users (Seed Data)

After migrations, these users are available:

| Email | Password | Role |
|-------|----------|------|
| admin@upsc-ai.com | admin123 | ADMIN |
| test@upsc-ai.com | test123 | USER |

> ⚠️ **Change these passwords in production!**

### Creating New Migrations

```bash
# Create new migration file
touch src/main/resources/db/migration/V4__Your_Description.sql

# Add your SQL
cat > src/main/resources/db/migration/V4__Add_Feature.sql << 'EOF'
CREATE TABLE your_table (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
EOF

# Run application - migration applies automatically
mvn spring-boot:run
```

📖 **See [MIGRATIONS.md](MIGRATIONS.md) for complete migration guide**

---

## 📚 API Documentation

### Health & Monitoring Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info

# Metrics
curl http://localhost:8080/actuator/metrics

# Flyway migration status
curl http://localhost:8080/actuator/flyway

# All actuator endpoints (dev only)
curl http://localhost:8080/actuator
```

### Test Endpoints (Development Only)

```bash
# Test endpoint
curl http://localhost:8080/api/test/hello

# Health test
curl http://localhost:8080/api/health/test
```

> 🔒 Test endpoints are **disabled** in staging and production

---

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test

```bash
mvn test -Dtest=YourTestClass
```

### Run with Coverage

```bash
mvn clean test jacoco:report
```

---

## 🚢 Deployment

### Build for Production

```bash
# Build JAR (skip tests for faster build)
mvn clean package -DskipTests

# JAR location
ls -lh target/upsc-backend-0.0.1-SNAPSHOT.jar
```

### Docker Build

```bash
# Build Docker image
docker build -t upsc-backend:latest .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-db-host \
  -e DB_USERNAME=your-user \
  -e DB_PASSWORD=your-password \
  upsc-backend:latest
```

### Environment Variables

Required for staging/production:

```bash
# Application
SPRING_PROFILES_ACTIVE=staging|prod

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=upsc_ai
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Security
JWT_SECRET=your-secret-key

# OAuth2 (optional)
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret

# External Services
OPENAI_API_KEY=your-api-key

# Frontend
FRONTEND_URL=https://your-domain.com
```

📖 **See [ENVIRONMENTS.md](ENVIRONMENTS.md) for detailed environment configuration**

---

## 📁 Project Structure

```
upsc-backend/
├── src/main/
│   ├── java/com/upsc/ai/
│   │   ├── config/              # Configuration classes
│   │   ├── controller/          # REST controllers
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── exception/           # Exception handlers
│   │   ├── filter/              # Request/Response filters
│   │   ├── health/              # Custom health indicators
│   │   └── test/                # Test controllers (dev only)
│   └── resources/
│       ├── application.yml      # Base configuration
│       ├── application-dev.yml  # Development config
│       ├── application-staging.yml
│       ├── application-prod.yml
│       ├── logback-spring.xml   # Logging configuration
│       └── db/migration/        # Flyway migrations
│           ├── V1__Initial_Schema.sql
│           ├── V2__Add_Indexes_And_Constraints.sql
│           └── V3__Seed_Data.sql
├── pom.xml                      # Maven dependencies
├── Dockerfile                   # Docker build
├── docker-compose.yml           # Development Docker setup
├── docker-compose.staging.yml   # Staging Docker setup
├── .env.example                 # Environment variables template
├── README.md                    # This file
├── ENVIRONMENTS.md              # Environment setup guide
└── MIGRATIONS.md                # Database migration guide
```

---

## 🔍 Troubleshooting

### Port 8080 Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different port
export SERVER_PORT=8081
mvn spring-boot:run
```

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker ps

# Test connection
psql -h localhost -U postgres -d upsc_ai

# Check logs
tail -f logs/application.log | grep -i "database\|connection"
```

### Migration Failures

```bash
# Check migration status
curl http://localhost:8080/actuator/flyway | jq

# Repair migration history (development only)
mvn flyway:repair

# Clean and restart (development only - deletes all data!)
mvn flyway:clean
mvn spring-boot:run
```

---

## 📖 Additional Resources

- **[ENVIRONMENTS.md](ENVIRONMENTS.md)** - Multi-environment configuration guide
- **[MIGRATIONS.md](MIGRATIONS.md)** - Database migration guide
- **[ENV_QUICK_START.md](ENV_QUICK_START.md)** - Quick environment setup
- **[LOGGING_MONITORING_GUIDE.md](LOGGING_MONITORING_GUIDE.md)** - Logging and monitoring

---

## 🛠️ Development Tools

### Recommended IDE

- **IntelliJ IDEA** (recommended)
- **VS Code** with Java extensions
- **Eclipse**

### Useful Commands

```bash
# Clean build
mvn clean install

# Skip tests
mvn clean package -DskipTests

# Update dependencies
mvn clean install -U

# View dependency tree
mvn dependency:tree

# Format code (if configured)
mvn spotless:apply
```

---

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Make your changes
3. Run tests: `mvn test`
4. Commit: `git commit -m "feat: your feature"`
5. Push: `git push origin feature/your-feature`
6. Create a Pull Request

---

## 📝 License

[Add your license here]

---

## 📧 Support

For issues or questions:
- Check the troubleshooting section above
- Review documentation in `ENVIRONMENTS.md` and `MIGRATIONS.md`
- Check application logs: `logs/application.log`
- Contact the development team

---

## 🎯 Quick Reference

```bash
# Start development server
mvn spring-boot:run

# Access H2 Console
open http://localhost:8080/h2-console

# Check health
curl http://localhost:8080/actuator/health

# View logs
tail -f logs/application.log

# Build for production
mvn clean package -DskipTests

# Run with Docker
docker-compose up
```

---

**Built with ❤️ using Spring Boot 3.2.2**
