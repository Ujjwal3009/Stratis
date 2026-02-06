# UPSC AI Platform (Backend)

An advanced AI-powered platform for UPSC preparation, featuring automated test generation, behavioral analytics, and remedial learning.

## 📚 Production Documentation

Explore the detailed technical documentation for the entire platform:

- **[System Architecture](docs/ARCHITECTURE.md)**: High-level overview and tech stack.
- **[Design & Implementation](docs/DESIGN.md)**: Low-level details, patterns, and data models.
- **[User Journey & Features](docs/USER_JOURNEY.md)**: Core prep cycle and intelligent features.
- **[Technical Deep Dive](docs/TECHNICAL_DEEP_DIVE.md)**: File-by-file logic and code flow breakdown.
- **[Onboarding & Contributing](docs/CONTRIBUTING.md)**: Setup guide for new developers and beginners.
- **[Deployment Guide](docs/DEPLOYMENT.md)**: Step-by-step free-to-play and production instructions.

---

## 🛡️ Production Readiness Features

### Security & Resilience
- **Actuator Security**: Hardened endpoints restricted to `ADMIN` roles.
- **Distributed Rate Limiting**: Redis-backed Bucket4j implementation for multi-replica scaling.
- **CORS Management**: Dynamically injected allowed origins for secure cross-domain communication.
- **AI Fallbacks**: Robust circuit breakers and static fallback mechanisms.

### Automation & Quality
- **CI/CD Pipeline**: GitHub Actions for automated building, linting, and security scans.
- **Error Tracking**: Full Sentry integration for real-time error monitoring.
- **API Documentation**: Interactive Swagger UI at `/swagger-ui.html`.

## Getting Started

Refer to the **[Onboarding Guide](docs/CONTRIBUTING.md)** for a complete local setup.

### Prerequisites
- JDK 17
- PostgreSQL & Redis (Docker Compose provided)

### Launch
```bash
mvn spring-boot:run
```

## API Discovery
Access the OpenAPI documentation at: `http://localhost:8080/swagger-ui.html`
