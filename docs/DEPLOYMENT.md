# Deployment Guide (Free-to-Play & Production)

This guide provides step-by-step instructions to get the UPSC AI Platform running for free on your local machine to "play" with the features, followed by the path to production.

---

## 🎮 Free-to-Play: Local Setup (Docker)

The fastest and completely free way to test the entire system (Backend, Frontend, DB, Redis) is using Docker Compose.

### 1. Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed.
- An API Key for Gemini (Get one for free at [Google AI Studio](https://aistudio.google.com/)).

### 2. Configuration
1.  Navigate to the root directory.
2.  Open `docker-compose.yml` (if it exists) or create one with the services: `postgres`, `redis`, `upsc-backend`, and `upsc-frontend`.
3.  Ensure your Gemini API Key is set in the environment variables.

### 3. Launch
```bash
docker-compose up -d
```
Access the app at `http://localhost:3000`.

---

## 🏗️ The Production Path (Kubernetes)

For the "Canary Release" or full production, we use the provided Helm charts.

### 1. Setup a Local Cluster (Free)
If you want to test the production Helm charts locally for free:
1.  Install **Minikube**: `brew install minikube` (Mac).
2.  Start it: `minikube start`.

### 2. Deploy infrastructure
Install Redis and Postgres using standard Helm charts:
```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install my-release bitnami/postgresql
helm install my-cache bitnami/redis
```

### 3. Deploy UPSC Platform
Use our custom chart located in `/charts/upsc-app`:
```bash
cd charts/upsc-app
helm install upsc-ai . -f values.yaml
```

---

## ☁️ Low-Cost Cloud Hosting ($10-12/mo)

If you want to host this live for a small audience:
1.  **Server**: Use a **DigitalOcean Droplet** or **Hetzner Cloud** instance (2GB RAM).
2.  **Database**: Run Postgres inside Docker on the same instance to save costs (instead of using a managed DB).
3.  **Frontend**: Deploy to **Vercel** or **Netlify** (Free tier) to save server resources.
4.  **Monitoring**: Use the free tier of **Sentry**.

---

## 📋 Post-Deployment Checklist
- [ ] Verify `/api/v1/health` returns `UP`.
- [ ] Login/Register a new user.
- [ ] Upload a sample PDF and verify text extraction.
- [ ] Generate a 5-question test and submit.
- [ ] Check Sentry for any "hidden" production bugs.
