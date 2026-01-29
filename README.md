# UPSC AI Platform

An advanced AI-powered platform for UPSC preparation, featuring automated test generation, behavioral analytics, and remedial learning.

## Repository Structure
- **/upsc-backend**: Spring Boot application (Java 17)
- **/upsc-frontend**: Next.js 15 application (React 19)
- **/docs**: Product and business documentation
- **/scripts**: Helper scripts for environment management

## Standard Startup Features

### 1. Automation & CI/CD
- **Backend CI**: Runs Maven build, Checkstyle, SpotBugs, and JaCoCo on every push.
- **Frontend CI**: Runs Next.js build, Lint, Prettier check, and Vitest unit tests.

### 2. Observability & Error Tracking
- Integrated with **Sentry** for real-time error tracking and session replays.
- Actuator endpoints exposed for Prometheus monitoring.

### 3. Code Quality
- **Static Analysis**: Google Java Style enforced via Checkstyle.
- **Pre-commit Hooks**: Husky and lint-staged prevent broken code from being committed.
- **API Documentation**: Interactive Swagger UI available at `/swagger-ui.html`.

## Getting Started

### Prerequisites
- JDK 17
- Node.js 20+
- PostgreSQL

### Local Setup
1. Clone BOTH repositories.
2. In `upsc-backend`, copy `.env.example` to `.env` and fill the values. Run `./mvnw spring-boot:run`.
3. In `upsc-frontend`, run `npm install` and `npm run dev`.

## API Discovery
Access the OpenAPI documentation at: `http://localhost:8080/swagger-ui.html`
