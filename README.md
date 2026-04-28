![Stock Market Predictor](banner-bmad-method.png)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Stock Market Predictor (example app)

This repo includes a runnable Spring Boot app at `stock-market-predictor/` that:

- **Ingests** daily OHLCV via **Alpha Vantage** → stores in **PostgreSQL**
- Applies a **macro volatility gate** (macro-sieve)
- Runs a **prediction step** (ONNX runtime scaffold; may run in mock mode if no model is bundled)
- Generates **LLM rationales** → stores in **MongoDB**
- Renders a **daily HTML report** and sends it via SMTP → captured locally by **Mailpit**

### Run locally

```bash
cd stock-market-predictor
docker compose up -d --build
```

### Trigger the pipeline

```bash
curl -sS -X POST localhost:8080/api/pipeline/run \
  -H 'Content-Type: application/json' \
  -H 'X-Pipeline-Key: secret' \
  -d '{"symbols":["MSFT"],"recipient":"test@example.com"}'
```

### Where to verify results

- **App health**: `http://localhost:8080/actuator/health`
- **Mailpit (emails UI)**: `http://localhost:8025`
- **Postgres**: `localhost:5432` (db `stock_market`)
- **Mongo**: `localhost:27017` (db `stock_rationale`)
- **Redis**: `localhost:6379`

### Useful log command

```bash
cd stock-market-predictor
docker compose logs -f app
```

---

## About BMAD-METHOD (framework)

[![Version](https://img.shields.io/npm/v/bmad-method?color=blue&label=version)](https://www.npmjs.com/package/bmad-method)
[![Node.js Version](https://img.shields.io/badge/node-%3E%3D20.0.0-brightgreen)](https://nodejs.org)
[![Discord](https://img.shields.io/badge/Discord-Join%20Community-7289da?logo=discord&logoColor=white)](https://discord.gg/gk8jAdXWmj)

**Build More Architect Dreams** — Open source framework for structured, agent-assisted software delivery.

**Quick start** (Node.js v20+):

```bash
npx bmad-method install
```

[Learn more at **docs.bmad-method.org**](https://docs.bmad-method.org)

## Modules

BMad Method extends with official modules for specialized domains. Available during installation or anytime after.

| Module                                                                                                            | Purpose                                           |
| ----------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- |
| **[BMad Method (BMM)](https://github.com/bmad-code-org/BMAD-METHOD)**                                             | Core framework with 34+ workflows                 |
| **[BMad Builder (BMB)](https://github.com/bmad-code-org/bmad-builder)**                                           | Create custom BMad agents and workflows           |
| **[Test Architect (TEA)](https://github.com/bmad-code-org/bmad-method-test-architecture-enterprise)**             | Risk-based test strategy and automation           |
| **[Game Dev Studio (BMGD)](https://github.com/bmad-code-org/bmad-module-game-dev-studio)**                        | Game development workflows (Unity, Unreal, Godot) |
| **[Creative Intelligence Suite (CIS)](https://github.com/bmad-code-org/bmad-module-creative-intelligence-suite)** | Innovation, brainstorming, design thinking        |





---


