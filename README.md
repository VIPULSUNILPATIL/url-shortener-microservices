# URL Shortener — Microservices Architecture

A scalable, distributed URL shortener built with Spring Boot and Spring Cloud. This project demonstrates modern microservices patterns — service discovery, distributed configuration, and fault tolerance — designed for cloud-native deployment.

## 🏗️ Architecture

```
                        ┌──────────────────┐
                        │   API Gateway     │  (port 8765)
                        │  single entry pt  │
                        └────────┬──────────┘
                                 │
                 ┌───────────────┴────────────────┐
                 ▼                                 ▼
        ┌─────────────────┐              ┌──────────────────┐
        │ shortener-service│              │  redirect-service │
        │  (write, 8080)   │              │   (read, 8081)    │
        └────────┬─────────┘              └─────────┬─────────┘
                 │                                   │
                 └───────────────┬───────────────────┘
                                  ▼
                     ┌─────────────────────┐
                     │   naming-server      │  (Eureka, 8761)
                     │  service discovery   │
                     └─────────────────────┘
                                  ▲
                     ┌─────────────────────┐
                     │    config-server      │  (8888)
                     │ centralized settings  │
                     └─────────────────────┘
```

## Services & Ports

| Service | Role | Port | Status |
|---|---|---|---|
| config-server | Centralized configuration | 8888 | ✅ Done |
| naming-server | Eureka service discovery | 8761 | ✅ Done |
| api-gateway | Single entry point + logging filter | 8765 | ✅ Done |
| shortener-service | Generates short code, saves URL (write) | 8080 | ✅ Done |
| redirect-service | Resolves short code → 302 redirect (read) | 8081 | ✅ Done |

## System Design Principles

- **Read/write segregation (CQRS-inspired):** `shortener-service` (write) is separated from `redirect-service` (read). URL shorteners typically see a ~100:1 read-to-write ratio, so the read path can be scaled independently to absorb traffic spikes without touching the write path.
- **Centralized configuration:** `config-server` externalizes config so credentials, ports, and environment-specific values aren't hardcoded per service, and can be changed without rebuilding.
- **Service discovery:** `naming-server` (Eureka) lets services register and be found dynamically. The gateway's `lb://` routes and `redirect-service`'s OpenFeign client both resolve live instances through Eureka independently — nothing hardcodes an address.
- **Single entry point:** `api-gateway` is the only service clients talk to directly. It owns routing and cross-cutting concerns (a global logging filter today), keeping internal topology hidden. Internal service-to-service calls (Feign) bypass the gateway entirely.
- **Algorithmic design:** short codes are Base62-encoded from the DB auto-increment id — short, URL-safe, alphanumeric, and collision-free without needing a lookup/retry.
- **Fault tolerance (planned):** Resilience4j retry on the `redirect-service → shortener-service` call was attempted but reverted due to persistent dependency/build issues on this bleeding-edge stack; the design (3 attempts, exponential backoff, excluding legitimate 404s from retry) is documented in the interview prep doc as a next step.

## 🛠️ Tech Stack

- **Language:** Java 26
- **Framework:** Spring Boot 4.1.0, Spring Cloud 2025.1.2
- **Infrastructure:** Eureka (discovery), API Gateway (WebFlux/reactive), Config Server
- **Communication:** OpenFeign REST clients
- **Database:** Spring Data JPA, H2 (dev) / MySQL (prod, planned)
- **Deployment:** Docker, Docker Compose, AWS EC2 (planned)

## 🚀 Roadmap & Progress

### ✅ Day 1 — Workspace & Config Server
Initialized the monorepo workspace. Built `config-server` (Spring Cloud Config, native profile) on port `8888`, with `config-repo` serving centralized YAML.

### ✅ Day 2 — Naming Server
Built `naming-server` (Eureka) on port `8761`, standalone.

### ✅ Day 3 — API Gateway
Built `api-gateway` on port `8765`. Routes to `shortener-service` and `redirect-service` via Eureka-backed `lb://` URIs. Global logging filter (`LoggingGlobalFilter`) logs every request/response pre- and post-proxy.

### ✅ Day 4–5 — Shortener Service
Built `shortener-service` on port `8080`. `POST /api/shorten` saves a URL and Base62-encodes its DB id into a short code. `GET /api/urls/{code}` resolves it back. JPA + H2.

### ✅ Day 6–7 — Redirect Service
Built `redirect-service` on port `8081`. `GET /api/redirect/{code}` calls `shortener-service` via a Feign client (own independent Eureka lookup) and returns an HTTP 302 with the original URL in the `Location` header.

### ⏳ Upcoming
- **Docker** — containerize all five services, docker-compose for local orchestration
- **AWS EC2** — deployment, final docs pass, interview writeup

## 🏃 How to Run Locally

Start in this order — each depends on the ones before it:

1. `naming-server` → confirm dashboard loads at `http://localhost:8761`
2. `config-server` → confirm it's up at `http://localhost:8888`
3. `api-gateway` → confirm `API-GATEWAY` appears on the Eureka dashboard
4. `shortener-service` → confirm `SHORTENER-SERVICE` appears on Eureka
5. `redirect-service` → confirm `REDIRECT-SERVICE` appears on Eureka

## ✅ Testing / Verifying the Setup

All tested via PowerShell (`Invoke-RestMethod`) and a browser, both hitting services directly and through the gateway.

**1. Create a short URL (through the gateway):**
```powershell
Invoke-RestMethod -Uri "http://localhost:8765/api/shorten" -Method Post -ContentType "application/json" -Body '{"originalUrl":"https://example.com"}'
```
Returns `{ shortCode, shortUrl }`.

**2. Resolve a short code back to the original URL:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8765/api/urls/<shortCode>"
```

**3. Trigger the actual redirect** (302 + Location header):
```powershell
curl.exe -v http://localhost:8765/api/redirect/<shortCode>
```
Look for `< HTTP/1.1 302 Found` and `< Location: <originalUrl>` in the output.

**4. Real end-to-end check:** paste `http://localhost:8765/api/redirect/<shortCode>` directly into a browser address bar — it should land on the original URL automatically (browsers auto-follow the 302, unlike `curl -v`).

**5. Service discovery sanity check:** confirm all five services (`API-GATEWAY`, `SHORTENER-SERVICE`, `REDIRECT-SERVICE`, plus config/naming server themselves) show as registered on `http://localhost:8761`.

**6. Failure-path check** (manual, no automated test yet): stop `shortener-service`, then hit `/api/redirect/<code>` — currently fails immediately with a connection error rather than degrading gracefully.

## Repository Structure

```
url-shortener-microservices/
├── config-server/
├── naming-server/
├── api-gateway/
├── shortener-service/
├── redirect-service/
└── README.md
```

## Commit Convention

Using [Conventional Commits](https://www.conventionalcommits.org/) for a clean, interview-ready history:
- `feat:` new functionality
- `fix:` bug fixes
- `docs:` README/documentation changes
- `chore:` config, tooling, dependency bumps, reverts