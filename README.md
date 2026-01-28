# Stratis 🚀 (UPSC AI Backend)

![CI](https://github.com/Ujjwal3009/Stratis/workflows/CI/badge.svg)
![Code Quality](https://github.com/Ujjwal3009/Stratis/workflows/Code%20Quality/badge.svg)
![Latest Commit](https://img.shields.io/github/last-commit/Ujjwal3009/Stratis)

**Stratis** is a high-performance, AI-driven backend engine designed to revolutionize UPSC preparation. By merging Retrieval-Augmented Generation (RAG) with advanced psychometric analysis, Stratis goes beyond simple testing—it builds a "Brain Model" of the aspirant.

## 🌟 Core Innovations

- **AI-Powered "UPSC-Level" Question Engine:** Generates complex, multi-statement questions using Google Gemini, specifically tuned for the UPSC Prelims difficulty curve.
- **Deep RAG Pipeline:** Ingests standard texts (Laxmikanth, PYQs, NCERTs) and chunks them for contextualized test generation.
- **Tukka Analysis Logic:** A custom algorithm that differentiates between "Conceptual Gaps," "Educated Guesses," and "Pure Tukka" based on user confidence markers.
- **Automated Revision Pointers:** Generates hyper-specific revision cards for questions answered incorrectly.

---

## 📅 Latest Project Updates

**Last Update:** January 28, 2026
**Latest Major Commit:** `a356bb5` - Add comprehensive OpenAPI/Swagger documentation

### Recent Milestones:
- ✅ **Swagger Integration:** Full API documentation for frontend consumption.
- ✅ **PDF Chunking Engine:** Implemented recursive character splitting for high-fidelity RAG.
- ✅ **Gemini AI Service:** Integrated direct LLM support for question generation.
- ✅ **Stability Fixes:** Resolved H2 migration conflicts and 500 error edge cases.

---

## 🛠️ Technical Architecture

- **Framework:** Spring Boot 3.2.2 (Java 17)
- **Database:** PostgreSQL (Production/Staging) | H2 (Dev)
- **Migration:** Flyway for automated schema versioning.
- **AI Engine:** Google Gemini API / OpenAI API.
- **Security:** JWT-based Auth + OAuth2 ready.
- **Documentation:** SpringDoc OpenAPI (Swagger UI).

---

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Environment Configuration](#environment-configuration)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)

---

## 🔧 Prerequisites

- **Java 17+**
- **Maven 3.6+**
- **Docker** (Optional)
- **OpenAI/Gemini API Key**

---

## 🚀 Quick Start

### 1. Clone & Setup
```bash
git clone https://github.com/Ujjwal3009/Stratis.git
cd Stratis/upsc-backend
cp .env.example .env # Add your API Keys here
```

### 2. Launch
```bash
mvn spring-boot:run
```
The engine fires up at **http://localhost:8080**

### 3. Explore APIs
Access the live documentation at:
**http://localhost:8080/swagger-ui.html**

---

## 📁 Project Overview

```
upsc-backend/
├── src/main/java/com/upsc/ai/
│   ├── analysis/      # Tukka & Performance logic
│   ├── content/       # RAG Engine & PDF Chunking
│   ├── controller/    # REST Endpoints
│   ├── dto/           # Data Transfer Layer
│   ├── entity/        # JPA Models
│   ├── security/      # JWT & Auth logic
│   └── service/       # Business Intelligence
└── resources/
    └── db/migration/  # Flyway Schema History
```

---

## 🤝 The Vision

Stratis is the foundational layer for **Aspirant.ai** (or LBSNAA.ai), aiming to be the world's most precise personal strategist for civil service aspirants.

**Built with ❤️ for the UPSC Community.**
