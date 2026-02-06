# Onboarding & Contributing Guide

Welcome! We are thrilled to have you contribute to the UPSC AI Platform. This guide will help you get set up and understand how to become a part of the project.

## Quick Start (Local Setup)

### Prerequisites
- **JDK 17 + Maven**
- **Node.js 18+**
- **Docker & Docker Compose**
- **OpenAI or Gemini API Key**

### 1. Clone & Init
```bash
git clone --recursive [repository-url]
cd upsc-ai-platform
```

### 2. Infrastructure
```bash
docker-compose up -d redis postgres
```

### 3. Backend Setup
```bash
cd upsc-backend
cp src/main/resources/application-dev-sample.yml src/main/resources/application-dev.yml
# Edit variables in application-dev.yml
mvn spring-boot:run
```

### 4. Frontend Setup
```bash
cd ../upsc-frontend
npm install
npm run dev
```

## Contributing Workflow

1.  **Find an Issue**: Check the GitHub Issues board for beginner-friendly labels.
2.  **Branch**: Create a feature branch (`feature/your-id-task`).
3.  **Code**: Follow the existing patterns and linting rules.
4.  **Test**: Ensure Maven (backend) and Prettier/ESLint (frontend) checks pass.
5.  **PR**: Submit a Pull Request with a clear description of changes.

## Beginner Tips

- **Backend Layers**: Controllers -> Services -> Repositories. Keep logic in Services.
- **Frontend Components**: Use the `Button` and `Card` components from `@/components/ui` for consistency.
- **AI Prompts**: Prompts are stored in `application.yml` and `GeminiAiService.java`. Test them thoroughly before committing.

## Communication
If you get stuck, tag the maintainers in your PR or open a Discussion item on GitHub. Happy coding!
