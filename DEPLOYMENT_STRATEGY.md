# 🚀 Deployment Strategy (Beta & Budget Production)

**Objective**: Deploy the `UPSC AI Platform` for 5-10 beta testers.

## Option A: The "Free Tier" (Zero Cost)
*Best for: Verification, solo testing, patience.*

| Component | Technology | Recommended Provider | Cost | Constraints |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend** | Next.js | **Vercel** | $0/mo | Excellent performance. |
| **Backend** | Spring Boot | **Render.com** | $0/mo | **Sleeps after idle**. First request takes ~50s. |
| **Database** | PostgreSQL | **Neon.tech** | $0/mo | 0.5 GB storage. Shared CPU. |
| **AI** | Gemini API | **Google AI Studio** | $0/mo | ~60 RPM rate limit. |

---

## Option B: The "Budget Production" (Recommend ~ $7-12/mo)
*Best for: 5-10 active users, no lag, professional feel.*

If you spend a small amount, you eliminate the "Cold Start" lag. The backend stays awake 24/7.

| Component | Provider | Tier Details | Est. Cost |
| :--- | :--- | :--- | :--- |
| **Backend** | **Render.com** | "Starter" Instance (512MB RAM, 0.5 CPU) | **$7.00 / mo** |
| **Database** | **Neon.tech** | Free Tier is usually fast enough for <500 users. | **$0.00** |
| **Frontend** | **Vercel** | Hobby Tier (Generous limits) | **$0.00** |
| **AI Cost** | **Gemini** | Pay-as-you-go (approx $0.50 per 1M tokens) | **~$2.00 / mo** |
| **TOTAL** | | | **~$9.00 / mo** |

### Why this upgrade?
1.  **Zero Lag**: The backend never sleeps. The site loads instantly.
2.  **Reliability**: Dedicated RAM prevents crashes during heavy PDF processing.

---

## 📅 Deployment Plan (Step-by-Step)

### Phase 1: Database & Cache (15 Mins)
1.  **Database (Neon)**: Create project `upsc-ai-beta`. Copy the `Pooled Connection String`.
2.  **Cache (Upstash)**: Create Redis database. Get `REDIS_URL`.

### Phase 2: Backend (Render) (20 Mins)
*   **Method**: Connect GitHub Repo.
*   **Env Vars**: `SPRING_DATASOURCE_URL`, `GEMINI_API_KEY`, `REDIS_URL`.
*   **Plan**: Select "Free" or "Starter" ($7) based on your budget.

### Phase 3: Frontend (Vercel) (10 Mins)
*   **Method**: Import `upsc-frontend`.
*   **Env Vars**: `NEXT_PUBLIC_API_URL`.

---

## ⚠️ Notes for Beta Testers
*   **If Free Tier**: "Please wait 60 seconds for the first load."
*   **If Paid Tier**: "Experience should be smooth like a real app."
*   **AI Limits**: Even on paid, Gemini Flash is cheap but not infinite. Ensure simple rate limits (Bucket4j) are active.
