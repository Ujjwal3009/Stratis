# Technical Deep Dive: How the System Works

This document provides a granular, file-by-file explanation of the UPSC AI Platform's logic and the step-by-step code flows that power its core features.

---

## 📂 Backend Logic Breakdown (`upsc-backend`)

### 🛡️ Configuration & Security (`com.upsc.ai.config`)
- **`SecurityConfig.java`**: The "Gatekeeper." It defines which URLs are public (auth, health) and which require `ADMIN` or `USER` roles. It also integrates the JWT filter and handles CORS for production domains.
- **`RateLimitingConfig.java`**: The "Governor." It connects to Redis to manage request buckets. It ensures that even if we scale to 10 servers, a single user can't exceed 5 AI requests per minute.
- **`RedisConfig.java`**: Simple plumbing that provides the `RedisClient` bean used by the Governor.
- **`JpaConfig.java`**: Configures the database connection and auditing fields (like `createdAt`).

### ⚙️ Services (The "Brains" - `com.upsc.ai.service`)
- **`GeminiAiService.java`**: Orchestrates AI. It takes a prompt, calls Gemini, parses the JSON response, and logs token costs. If Gemini is down, it knows how to return "Safe" static questions.
- **`QuestionService.java`**: Handles the logic of picking questions for a test, whether from the AI generator or the pre-stored database.
- **`BehaviourAnalyticsService.java`**: The "Profiler." It looks at *how* a user answered (time taken, patterns) to calculate if they were guessing or showing signs of fatigue.
- **`PdfDocumentService.java`**: Handles file uploads, text extraction via PDFBox, and preparing the "Context" for the AI.

### 🔌 Controllers (The "Interface" - `com.upsc.ai.controller`)
- **`TestController.java`**: Handles the lifecycle of a test (Start, Submit, Get Result).
- **`AuthController.java`**: Manages Login, Registration, and "Me" (current user profile) requests.

---

## 🎨 Frontend Logic Breakdown (`upsc-frontend`)

### 🧩 Contexts & State (`src/contexts`, `src/store`)
- **`AuthContext.tsx`**: Wraps the entire app. It checks for a local JWT on startup, fetches the user profile, and provides the `user` object to all components.
- **`useTestStore.ts` (Zustand)**: A lightweight global store that tracks the user's progress during a test (current question, selected answers, timer state).

### 🚀 Pages & Components (`src/app`, `src/components`)
- **`test/[testId]/page.tsx`**: The main test-taking engine. It handles keyboard navigation, the countdown timer, and the final submission modal.
- **`PerformanceRadar.tsx`**: Converts raw backend JSON metrics into a beautiful Recharts spider chart.

---

## 🌊 Core Code Flows (Step-by-Step)

### 1. The Life of a Test Generation
1.  **User UI**: User selects "History" and "10 Questions" then clicks "Generate."
2.  **Backend Controller**: `TestController` receives the request and calls `QuestionService`.
3.  **AI Service**: `GeminiAiService` constructs a prompt including the subject and difficulty.
4.  **AI Execution**: Gemini generates MCQs. The service parses the raw text into Java objects (`QuestionDTO`).
5.  **Persistence**: The session is saved to Postgres, and the questions are returned to the user.

### 2. The Life of a Test Submission
1.  **Stop Timer**: Frontend stops the timer and sends all `selectedOptions` to the backend.
2.  **Scoring**: Backend iterates through answers, comparing with the `Question` table.
3.  **Profiling**: `BehaviourAnalyticsService` is triggered to analyze time-per-question to detect "anomalies."
4.  **Finalize**: Score is saved, and a "Result Page" URL is returned to the user.

---

## 💡 Pro-Tips for Beginners
- **Adding a new API?** Start with the `Entity` -> `Repository` -> `Service` -> `Controller` flow.
- **Changing the Look?** Most UI components are in `src/components/ui`. They use Tailwind classes, so you can change styles without touching CSS files.
- **Testing AI?** You don't need to call the real API. Use the `application-dev.yml` to set `app.ai.mock=true` (if implemented) or check the `GeminiAiService` fallbacks.
