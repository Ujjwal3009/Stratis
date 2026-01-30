# 🚀 UPSC AI Platform - Remaining Roadmap

## Phase 2: Intelligence & Pedagogy (Current)
- [ ] **AI Diagnostic Engine**
    - [ ] Implement result analysis service (patterns in mistakes)
    - [ ] Create "Strengths vs Weakness" calculation logic
    - [ ] Frontend: Add "Performance Radar" chart to Dashboard
- [ ] **Advanced PDF Processing**
    - [ ] Text-chunking strategy for large UPSC books
    - [ ] Source-to-Question linking (metadata storage)
    - [ ] Option to "Chat with PDF" for conceptual clarity


## Phase 2.5: Professional Workflow & Hardening (New)
- [ ] **DevOps Maturity**
    - [ ] **Git Flow**: Establish `main` (Prod), `staging` (Pre-Prod), and `dev` branches.
    - [ ] **CI/CD** Pipelines: Auto-deploy `dev` to Render Test env, `main` to Prod.
- [ ] **Admin Dashboard (Internal Tool)**
    - [ ] **Token Monitor**: Visualization of Gemini API usage (Cost Control).
    - [ ] **User Management**: View active users, ban spam accounts.
    - [ ] **Content CMS**: Interface to upload/edit Questions (fix "Polity" vs "Indian Polity" mismatch).
    - [ ] **System Settings**: Hot-swap API Keys, toggle "Mock Mode" vs "Live AI" without code deploys.

## Phase 3: Engagement & Growth
- [ ] **Gamification Layer**
    - [ ] Daily test streaks
    - [ ] "All India Mock Test" scheduled events
- [ ] **Social & Community**
    - [ ] "Share result" feature (Social media cards)
    - [ ] Profile personalization (UPSC Attempt Year, Optional Subjects)
- [ ] **Authentication Finalization**
    - [ ] Full Google OAuth flow completion (backend integration)

## Phase 4: Production & Monetization
- [ ] **Subscription System**
    - [ ] Stripe/Razorpay integration
    - [ ] Credit-based usage limits for PDF generation
- [ ] **Performance Polish**
    - [ ] Redis caching for Question Bank
    - [ ] Frontend performance audit (Lighthouse scores)
- [ ] **Monitoring Setup**
    - [ ] Prometheus/Grafana dashboard configuration
