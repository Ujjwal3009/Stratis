# UPSC AI Platform

An advanced AI-powered platform for UPSC preparation, featuring automated test generation, behavioral analytics, and remedial learning.

## 📚 Documentation

Explore our comprehensive documentation to understand the system and start contributing:

- **[System Architecture](docs/ARCHITECTURE.md)**: High-level overview and tech stack.
- **[Design & Implementation](docs/DESIGN.md)**: Low-level details, patterns, and data models.
- **[User Journey & Features](docs/USER_JOURNEY.md)**: Core prep cycle and intelligent features.
- **[Onboarding & Contributing](docs/CONTRIBUTING.md)**: Setup guide for new developers and beginners.
- **[Technical Deep Dive](docs/TECHNICAL_DEEP_DIVE.md)**: File-by-file logic and core code flow breakdown.
- **[Deployment Guide](docs/DEPLOYMENT.md)**: Step-by-step free-to-play and production instructions.

---

## Repository Structure
- **/upsc-backend**: Spring Boot application (Java 17)
- **/upsc-frontend**: Next.js 14+ application (React 19)
- **/docs**: In-depth technical and product documentation
- **/charts**: Helm charts for production Kubernetes deployment
- **/terraform**: IaC templates (AWS VPC & EKS)

## Production Features

### 🛡️ Security & Resilience
- **Actuator Security**: Hardened endpoints restricted to ADMIN roles.
- **Distributed Rate Limiting**: Redis-backed Bucket4j implementation for multi-replica scaling.
- **AI Fallbacks**: Robust circuit breakers and static fallback mechanisms for high availability.

### ⚙️ Automation & Quality
- **Full CI/CD**: GitHub Actions for automated testing, linting, and security scans.
- **Infrastructure as Code**: Production-ready Helm charts and Terraform templates.
- **Error Tracking**: Full Sentry integration on both frontend and backend.

## Quick Start

For detailed instructions, please refer to the **[Onboarding Guide](docs/CONTRIBUTING.md)**.

### Prerequisites
- JDK 17
- Node.js 18+
- PostgreSQL & Redis (Docker Compose provided)

### API Access
Interactive Swagger UI is available at: `http://localhost:8080/swagger-ui.html`
