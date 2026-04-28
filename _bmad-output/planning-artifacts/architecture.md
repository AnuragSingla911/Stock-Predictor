---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7]
inputDocuments: ['prd.md', 'product-brief.md', 'ux-design-specification.md', 'mocks/email-report.html']
workflowType: 'architecture'
project_name: 'stock-market-predictor'
user_name: 'Anuragsingla'
date: '2026-04-26'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**
The system is a scheduled, macro-aware data pipeline that integrates market data ingestion, ML-driven analysis, and automated report delivery. 
- **High-Signal Selection:** Dynamic stock selection based on a confidence threshold (e.g., >70%) rather than a fixed count.
- **Rationale Synthesis Engine:** Grounded LLM-driven generation that interprets ML features into human-readable narratives.
- **Macro-Sieve:** Initial analysis layer to halt the pipeline or issue "Low Confidence" warnings during extreme market volatility.
- **Ticker Universe Management:** Systematic rotation or focus on a pre-defined high-liquidity universe to respect API limits.
- **Sentiment Feedback Loop:** Lightweight mechanism to capture user satisfaction with rationale quality for continuous model tuning.

**Non-Functional Requirements:**
- **Early Morning Readiness:** Reports must be delivered by 6:00 AM local time.
- **Operational Reliability:** 99.9% uptime requirement for the scheduled pipeline.
- **Explainability:** The "Rationale" is a first-class citizen; predictions without deep, grounded context are considered low-value.
- **Trust Integrity:** "Low Confidence" days are framed as protective capital-preservation features.

**Scale & Complexity:**
- Primary domain: API / Backend Pipeline / AI Synthesis
- Complexity level: Medium-High
- Estimated architectural components: 9 (Ingestor, Macro Sieve, ML Engine, Rationale Engine, Fact-Checker, Universe Manager, Feedback Collector, Report Generator, Monitor)

### Technical Constraints & Dependencies
- **Alpha Vantage API:** Free tier rate limits (5 requests/minute). Focus on S&P 100 or Nasdaq 100 universe for feasibility.
- **Inference Strategy:** Embedded ONNX/TensorFlow Lite models within the pipeline.
- **LLM Grounding:** Strict "Fact-Checking" layer to ensure LLM narratives align with raw market data.
- **Performance:** Utilization of asynchronous ingestion patterns to maximize API throughput.
- **Email Delivery:** Mandatory domain authentication (SPF/DKIM/DMARC) for high deliverability.

### Cross-Cutting Concerns Identified
- **Grounding Architecture:** Mathematically consistent mapping between ML features and LLM narratives.
- **Model Health & Drift:** Automated "Safe Mode" if accuracy falls below a set threshold.
- **Delivery SLA Management:** Proactive alerts if pipeline delays threaten the 6:00 AM window.
- **User Satisfaction Signal:** Correlation between AI rationale quality and user engagement.

## Starter Template Evaluation

### Primary Technology Domain

**API / Backend Service (Java Spring Boot)** based on requirements for high reliability, polyglot persistence, and complex orchestration.

### Starter Options Considered

1.  **Python / GitHub Actions:** Rejected as primary service layer due to requirement for Java Spring Boot ecosystem, though likely used for ML sidecars.
2.  **Spring Boot + PostgreSQL (Single DB):** Considered but rejected in favor of polyglot approach to handle unstructured rationale data (MongoDB) and low-latency caching (Redis).

### Selected Starter: Spring Boot 3.4+ (Java 21)

**Rationale for Selection:**
Spring Boot provides the industrial-grade foundation needed for polyglot persistence and strictly grounded AI synthesis. Java 21's Virtual Threads are ideal for the concurrent nature of data ingestion and inference pipelines.

**Initialization Command:**

```bash
# Generated via start.spring.io
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,data-mongodb,data-redis,postgresql,validation,actuator,docker-compose,testcontainers \
  -d javaVersion=21 \
  -d type=maven-project \
  -d name=stock-market-predictor \
  -o stock-market-predictor.zip
```

**Architectural Decisions Provided by Starter:**

**Language & Runtime:**
Java 21 with Maven. Utilizing Virtual Threads for non-blocking I/O during high-volume data fetching.

**Persistence Configuration:**
- **PostgreSQL:** Transactional source of truth for historical data.
- **MongoDB:** Flexible storage for Rationale metadata and news extracts.
- **Redis:** Distributed caching and rate-limiting for API ingestion.

**Contract Testing:**
Integration of **Spring Cloud Contract** to maintain API boundaries and service contracts.

**Build Tooling & DX:**
Maven-based lifecycle with **Testcontainers** for isolated integration testing of all database layers.

**Stories & Component Mapping:**

| Requirement (Story) | Architectural Component | Tech Responsibility |
| :--- | :--- | :--- |
| **Daily Ingestion** | `DataIngestionService` | Java/Spring JPA + Alpha Vantage API |
| **Macro Analysis** | `MacroSieveEngine` | Redis (Cache) + Logic Layer |
| **ML Inference** | `PredictionEngine` | ONNX Runtime (Java) |
| **Rationale Synthesis** | `SynthesisProvider` | LLM API Integration (JSONB in Postgres/Mongo) |
| **Report Generation** | `ReportDispatcher` | Thymeleaf Templating + Mail Sender |
| **Performance Tracking** | `BackCalcService` | PostgreSQL Aggregations |

**Note:** Project initialization using this command should be the first implementation story.

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**
- **Polyglot Strategy:** ID-Mapping between PostgreSQL and MongoDB to keep transactional data and rationale metadata decoupled but linked.
- **Orchestration:** GitHub Actions for daily cron-scheduling, triggering a Java Spring Boot service.
- **Inference Location:** Embedded ONNX Runtime in the Java service to minimize external dependencies.

**Important Decisions (Shape Architecture):**
- **External API Client:** Spring `HttpExchange` interface clients for Alpha Vantage to enforce a "Contract-first" integration.
- **Hosting:** Railway for persistent service hosting and managed SQL/NoSQL databases.

**Deferred Decisions (Post-MVP):**
- **WebSocket Integration:** Real-time updates for a dashboard (Deferred until web-portal phase).
- **Brokerage API:** Live trading execution (Vision phase).

### Data Architecture

- **PostgreSQL (v16+):** Relational source of truth for Tickers, Historical OHLCV, and Prediction results.
- **MongoDB (v7.0+):** Document store for high-volume rationale metadata, LLM prompt logs, and unstructured news extracts.
- **Redis (v7.2+):** Caching layer for pre-market report availability and Alpha Vantage API rate-limit management.
- **Coordination:** **ID-Mapping Strategy.** Shared Primary Keys (UUIDs) across stores to avoid complex distributed transactions.

### Authentication & Security

- **Pipeline Secrets:** **GitHub Actions Secrets** for Alpha Vantage API keys, LLM API keys, and Database credentials.
- **Database Access:** Encrypted connection strings with strictly limited IP whitelisting in Railway.

### API & Communication Patterns

- **Integration Client:** **Spring Interface Clients (@HttpExchange)** for Alpha Vantage integration.
- **Contracts:** Manual definition of DTOs based on Alpha Vantage JSON responses, validated via **Spring Cloud Contract** verifiers.
- **Internal API:** RESTful patterns for any internal communication between the ingestion and report generation modules.

### Infrastructure & Deployment

- **Orchestration:** **Docker Compose** is the primary orchestration tool for both local development and production. The entire system (App, PostgreSQL, MongoDB, Redis) MUST be defined in `docker-compose.yml`.
- **Platform:** **Railway** (supporting Docker/Docker Compose) for persistent backend hosting.
- **CI/CD:** **GitHub Actions** for the daily 5:00 AM Cron trigger, executing against the containerized service.
- **Monitoring:** Spring Boot Actuator integrated with OpenTelemetry for pipeline tracing.

### Decision Impact Analysis

**Implementation Sequence:**
1. Setup Railway Environment (Postgres, Mongo, Redis).
2. Initialize Spring Boot 3.4 Project with Maven.
3. Implement Alpha Vantage @HttpExchange client with contract tests.
4. Implement Embedded ONNX inference module.

**Cross-Component Dependencies:**
- The **Report Generation** module depends on the successful completion of both the **ML Engine** (Postgres) and the **Rationale Synthesis** (Mongo).

## Implementation Patterns & Consistency Rules

### Naming Patterns

**Database Naming:**
- **PostgreSQL:** `snake_case` for all tables and columns (e.g., `stock_predictions`, `ticker_symbol`).
- **MongoDB:** `camelCase` for all fields and collections (e.g., `rationaleMetadata`, `promptLog`).
- **Redis Keys:** `kebab-case` with colon separators (e.g., `rate-limit:alpha-vantage`, `report-cache:2026-10-24`).

**Code Naming:**
- **Classes:** `PascalCase` (e.g., `PredictionService`).
- **Methods/Variables:** `camelCase` (e.g., `calculateConfidence()`).
- **Packages:** `lowercase` following functional grouping (e.g., `com.bmad.stock.ingestion`).

### Structure Patterns

**Project Organization (Package by Feature):**
- `com.bmad.stock.ingestion`: Alpha Vantage clients and ingestion logic.
- `com.bmad.stock.analysis`: Macro-sieve and ML inference orchestration.
- `com.bmad.stock.synthesis`: LLM rationale generation and grounding.
- `com.bmad.stock.reporting`: Email generation and dispatch logic.
- `com.bmad.stock.shared`: Common DTOs, constants, and utilities.

**Resource Organization:**
- **Prompts:** All LLM prompts stored as external files in `src/main/resources/prompts/*.prompt`.
- **Contracts:** Spring Cloud Contract DSLs stored in `src/test/resources/contracts`.

### Format Patterns

**API Response Structure:**
All internal and potential external API responses must follow the **Envelope Pattern**:
```json
{
  "status": "success | error | warning",
  "data": { ... },
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable message"
  },
  "timestamp": "ISO-8601"
}
```

**Data Exchange:**
- **Dates:** Always use ISO-8601 strings in UTC.
- **Currency:** Decimals for prices, integers for volume.

### Process Patterns

**Error Handling:**
- Use a global `@ControllerAdvice` for consistent error mapping.
- Financial data failures must trigger a `CRITICAL` log level and immediate Slack/Discord alerting.

**LLM Grounding Process:**
- **Input:** Raw JSON features from ML engine.
- **Process:** Prompt includes "Strict Grounding" instructions + Data Sandbox.
- **Verification:** Post-processing regex/validator ensures no hallucinated numbers exist in the final rationale.

### Enforcement Guidelines

**All AI Agents MUST:**
- Follow the "Package by Feature" structure strictly.
- Register all new external API calls as `@HttpExchange` interfaces.
- Ensure every `Prediction` record in Postgres has a corresponding `Rationale` link in Mongo.

## Project Structure & Boundaries

### Complete Project Directory Structure

```
stock-market-predictor/
├── pom.xml
├── README.md
├── .gitignore
├── .github/
│   └── workflows/
│       └── daily-pipeline.yml
├── src/
│   ├── main/
│   │   ├── java/com/bmad/stock/
│   │   │   ├── StockPredictorApplication.java
│   │   │   ├── ingestion/             # Alpha Vantage clients
│   │   │   ├── analysis/              # ONNX Inference + Macro Sieve
│   │   │   ├── synthesis/             # LLM Grounding logic
│   │   │   ├── reporting/             # Thymeleaf Email logic
│   │   │   └── shared/                # DTOs, Exceptions, Config
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── prompts/               # Rationale Engine prompts
│   │       ├── templates/             # Email HTML templates
│   │       └── models/                # Embedded ONNX models
│   └── test/
│       ├── java/com/bmad/stock/
│       └── resources/
│           └── contracts/             # Spring Cloud Contract DSLs
├── docker-compose.yml                 # Local Postgres, Mongo, Redis
└── railway.toml                       # Production deployment config
```

### Architectural Boundaries

**API Boundaries:**
- **External:** Outbound calls to Alpha Vantage REST endpoints via `@HttpExchange` (Ingestion Module).
- **Internal:** Service-to-service communication via strictly defined Java Interfaces.

**Data Boundaries:**
- **PostgreSQL:** Owned by the `analysis` and `ingestion` modules.
- **MongoDB:** Owned by the `synthesis` module for rationale storage.
- **Redis:** Shared infrastructure for caching and rate-limiting.

**Component Communication:**
- **Sequential Pipeline:** `Ingestion` → `Analysis` → `Synthesis` → `Reporting`.
- **Event-Aware:** The `Reporting` module only triggers once `Synthesis` confirms all rationales are persisted in MongoDB.

### Requirements to Structure Mapping

**Feature/Epic Mapping:**
- **Market Data Ingestion:** `com.bmad.stock.ingestion`
- **Macro & ML Analysis:** `com.bmad.stock.analysis`
- **Rationale Synthesis:** `com.bmad.stock.synthesis`
- **Pre-Market Reporting:** `com.bmad.stock.reporting`

**Cross-Cutting Concerns:**
- **Observability:** `com.bmad.stock.shared.monitoring`
- **Security:** `com.bmad.stock.shared.security`
- **Error Handling:** `com.bmad.stock.shared.exception`

### Integration Points

**Internal Communication:**
Service interfaces define the contracts between modules. DTOs in `com.bmad.stock.shared.dto` act as the data exchange format.

**External Integrations:**
- **Alpha Vantage:** via `StockDataClient` interface.
- **LLM Provider:** via `RationaleClient` interface.

**Data Flow:**
1. Ingestor pulls OHLCV → Saves to Postgres.
2. Analysis Engine pulls EOD → ONNX Inference → Saves Prediction to Postgres.
3. Synthesis Engine pulls Prediction → LLM Generation → Saves Rationale to Mongo (mapped by Prediction UUID).
4. Reporting Engine pulls Prediction (Postgres) + Rationale (Mongo) → Generates HTML → Dispatches Email.

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility:**
Java 21, Spring Boot 3.4, and the polyglot persistence stack (PostgreSQL, MongoDB, Redis) are highly compatible within the modern Spring ecosystem. Using ONNX Runtime for Java ensures that ML inference remains embedded and performant without external dependencies.

**Pattern Consistency:**
The dual naming convention (snake_case for SQL, camelCase for NoSQL/Java) aligns with industry standards for each technology while maintaining internal consistency.

**Structure Alignment:**
The "Package by Feature" structure directly supports the architectural boundaries, ensuring that components like `ingestion` and `synthesis` remain decoupled.

### Requirements Coverage Validation ✅

**Functional Requirements Coverage:**
All core features, including Alpha Vantage ingestion, Macro-Sieve logic, and LLM Rationale Synthesis, are mapped to specific Java services and modules.

**Non-Functional Requirements Coverage:**
- **Performance:** Addressed via Java 21 Virtual Threads and Redis caching.
- **Reliability:** Supported by Spring Actuator, Testcontainers, and Railway hosting.
- **Explainability:** Enforced via the Rationale Synthesis grounding patterns.

### Implementation Readiness Validation ✅

**Decision Completeness:** All critical tech stack, security, and data decisions are documented.
**Structure Completeness:** A full Maven project tree has been provided.
**Pattern Completeness:** Explicit rules for naming, error handling, and LLM grounding are established.

### Architecture Completeness Checklist

- [x] Project context thoroughly analyzed
- [x] Scale and complexity assessed
- [x] Technical constraints identified
- [x] Cross-cutting concerns mapped
- [x] Technology stack fully specified
- [x] Integration patterns defined
- [x] Performance considerations addressed
- [x] Naming conventions established
- [x] Structure patterns defined
- [x] Communication patterns specified
- [x] Process patterns documented
- [x] Complete directory structure defined
- [x] Component boundaries established
- [x] Integration points mapped
- [x] Requirements to structure mapping complete

### Architecture Readiness Assessment

**Overall Status:** READY FOR IMPLEMENTATION

**Confidence Level:** HIGH

**Key Strengths:**
- **Robust Polyglot Strategy:** Uses the right tool for the right data (SQL for trades, NoSQL for AI insights).
- **Embedded ML:** Minimizes latency and external costs by running models in-process.
- **Contract-First:** Ensures stability despite relying on a 3rd party API (Alpha Vantage) without a formal spec.

### Implementation Handoff

**AI Agent Guidelines:**
- **Container-First:** The entire project MUST be runnable via `docker-compose up`. No manual local database or Redis setup should be required.
- **Package by Feature:** Follow the feature-based structure strictly.
- **Contract-Strict:** Register all external API calls as `@HttpExchange` interfaces.
- **Grounded AI:** Prioritize the Fact-Checker layer to prevent hallucinations in rationales.

**First Implementation Priority:**
Initialize the Spring Boot 3.4 project using the provided `curl` command to `start.spring.io`.
