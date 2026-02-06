# System Design & Implementation

This document details the low-level design, data models, and key component implementations of the UPSC AI Platform.

## Core Data Models

### User & Authentication
- **User**: Stores profile, role (USER, ADMIN), and password hash.
- **UserTokenUsage**: Tracks prompt and completion tokens per user for cost auditing.

### Content & Tests
- **PdfDocument**: Metadata for uploaded source materials (History, Polity, etc.).
- **PdfChunk**: Granular text segments for AI context injection.
- **Question**: UPSC-style MCQs with text, options, difficulty, and professional explanation.
- **TestSession**: Represents a single test attempt, tracking score, timing, and behavioral metadata.

## Backend Service Breakdown

### `GeminiAiService.java`
- **Responsibility**: Orchestrates AI interactions.
- **Patterns**:
    - **Fallback**: Uses locally cached JSON questions if the AI API fails.
    - **Circuit Breaker**: Integrated with Resilience4j to prevent cascading failures.
    - **Cost Control**: Automatic logging to `UserTokenUsage` after every call.

### `BehaviourAnalyticsService.java`
- **Responsibility**: Processes raw test interaction data.
- **Logic**: Calculates "fatigue curves" and identifies behavioral patterns (e.g., Silly Mistakes vs. Knowledge Gaps).

### `RateLimitingConfig.java`
- **Responsibility**: Implements distributed rate limiting.
- **Mechanism**: Lettuce (Redis client) provides the storage backend for Bucket4j, ensuring a unified 5-req/min limit across multiple replica pods.

## Frontend Component Design

### State Management (Zustand)
- **`useTestStore`**: Manages the multi-step test flow (Configuration -> Active Test -> Result Visualization).

### Key Components
- **`QuestionCard.tsx`**: Renders dynamic question types (Statements + Options).
- **`PerformanceRadar.tsx`**: Uses Recharts to visualize performance across GS subjects.
- **`ErrorBoundary.tsx`**: Global React error boundary for UI resilience.

## Design Patterns Used

- **Strategy Pattern**: Used in AI services to switch between API implementations.
- **Observer Pattern**: Followed by React Context/Zustand for reactive state updates.
- **Intercepting Filter**: Spring Security and Rate Limiting Interceptors filter requests before reaching controllers.
