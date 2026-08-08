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
| api-gateway | Single entry point + logging filter | 8765 | ⏳ Pending |
| shortener-service | Generates short code, saves URL (write) | 8080 | ⏳ Pending |
| redirect-service | Resolves short code → redirect (read) | 8081 | ⏳ Pending |

## System Design Principles

- **Read/write segregation (CQRS-inspired):** `shortener-service` (write) is separated from `redirect-service` (read). URL shorteners typically see a ~100:1 read-to-write ratio, so the read path can be scaled independently to absorb traffic spikes without touching the write path.
- **Centralized configuration:** `config-server` externalizes config so credentials, ports, and environment-specific values aren't hardcoded per service, and can be changed without rebuilding.
- **Service discovery:** `naming-server` (Eureka) lets services register and be found dynamically, so the gateway and inter-service calls (OpenFeign) never depend on static IPs — this is what makes horizontal scaling possible.
- **Single entry point:** `api-gateway` is the only service clients talk to directly. It owns routing and cross-cutting concerns (logging, filters), keeping internal topology hidden.
- **Fault tolerance:** Resilience4j wraps inter-service calls (starting with retry on redirect failures) so one failing service doesn't cascade into a full outage.
- **Algorithmic design:** short codes are generated via Base62 encoding — keeps codes short, alphanumeric, and URL-safe, and the encoding is reversible without a lookup if needed.

## 🛠️ Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.x, Spring Cloud
- **Infrastructure:** Eureka (discovery), API Gateway, Config Server
- **Communication:** OpenFeign REST clients
- **Resilience:** Resilience4j (Retry, Circuit Breaker)
- **Database:** Spring Data JPA, H2 (dev) / MySQL (prod)
- **Deployment:** Docker, Docker Compose, AWS EC2

## 🚀 Roadmap & Progress

### ✅ Day 1 — Workspace & Config Server
- Initialized the monorepo workspace for all microservices
- Built `config-server` (Spring Cloud Config, native profile) on port `8888`
- Set up `config-repo` to serve centralized `application.yml` files

### ✅ Day 2 — Naming Server
- Built `naming-server` (Eureka) on port `8761`, standalone (not registering with itself)

### ⏳ Upcoming
- **Day 3:** API Gateway with routing + logging filter
- **Day 4–5:** Shortener Service — Base62 encoding, JPA/H2
- **Day 6–7:** Redirect Service — HTTP 302 logic, OpenFeign integration
- **Day 8–10:** Resilience4j integration, Dockerize all services
- **Day 11–14:** AWS EC2 deployment, docs pass, system design writeup

## 🏃 How to Run Locally

Start services **in this order** (each depends on the one before it being up):

1. **config-server** — run `ConfigServerApplication`, confirm it's up at `http://localhost:8888`
2. **naming-server** — run `NamingServerApplication`, confirm the Eureka dashboard loads at `http://localhost:8761`
3. *(coming soon)* api-gateway, shortener-service, redirect-service

## Repository Structure

```
url-shortener-microservices/
├── config-server/
├── naming-server/
├── api-gateway/        (pending)
├── shortener-service/  (pending)
├── redirect-service/   (pending)
└── README.md
```

## Commit Convention

Using [Conventional Commits](https://www.conventionalcommits.org/) for a clean, history:
- `feat:` new functionality (e.g. `feat: add naming-server with Eureka discovery on port 8761`)
- `fix:` bug fixes
- `docs:` README/documentation changes
- `chore:` config, tooling, dependency bumps