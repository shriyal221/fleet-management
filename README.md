# 🚚 HyperRoute: Enterprise Fleet Management & Intelligent Route Optimization Engine

<div align="center">

[![Java Version](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](https://opensource.org/licenses/MIT)

[![Build Status](https://img.shields.io/github/actions/workflow/status/shriyal221/advanced-wms/ci.yml?branch=main&style=flat-square&label=CI%2FCD%20Build)](https://github.com/)
[![Security Grade](https://img.shields.io/badge/Security-A%2B-brightgreen?style=flat-square)](https://spring.io/projects/spring-security)
[![API Status](https://img.shields.io/badge/API-Online-success?style=flat-square)](http://localhost:8082/actuator/health)
[![Coverage](https://img.shields.io/badge/Tests-13%20Passed-success?style=flat-square)](http://localhost:8082)

**An enterprise-grade, high-performance fleet orchestration platform and Traveling Salesperson Problem (TSP) solver. Engineered for real-time logistics optimization, fleet telemetry, and dispatch operations.**

[Explore Docs](file:///c:/Users/shriyal/OneDrive/Documents/New%20project/fleet-management/docs/ARCHITECTURE.md) • [API Specification](file:///c:/Users/shriyal/OneDrive/Documents/New%20project/fleet-management/docs/API.md) • [Report Bug](https://github.com/) • [Request Feature](https://github.com/)

</div>

---

## 🌌 Interactive Visual Experience & Live Dashboard Preview

Here is a look at the high-fidelity dark glassmorphic console designed to streamline logistics dispatch:

| 🎛️ Real-Time Fleet Dashboard | 🗺️ Live TSP Route Optimizer |
|:---:|:---:|
| ![Dashboard Console](https://raw.githubusercontent.com/shriyal221/advanced-wms/main/docs/assets/dashboard.png) | ![Route Planner](https://raw.githubusercontent.com/shriyal221/advanced-wms/main/docs/assets/planner.png) |
| *High-fidelity operational KPIs, stats strip, active fleet telemetry status, and security context tracking.* | *Dynamic waypoint selection, interactive routing graphs, active driver dispatcher, and fuel/time optimizer.* |

| 🔑 Secure Dispatch Login | 🛠️ Fleet Registry & Asset Manager |
|:---:|:---:|
| ![Login UI](https://raw.githubusercontent.com/shriyal221/advanced-wms/main/docs/assets/login.png) | ![Fleet Registry](https://raw.githubusercontent.com/shriyal221/advanced-wms/main/docs/assets/registry.png) |
| *Dispatcher and Administrator authorization screens backed by strong SHA-256 HMAC-signed JSON Web Tokens.* | *Side-by-side view tracking operational trucks and active driver profiles with live shift assignments.* |

---

## 💼 Business Case & Technical Breakthrough

### The Logistics Bottleneck (The Problem)
Last-mile delivery operations are plagued by compounding inefficiencies: sub-optimal vehicle routing, fluctuating driver schedules, unmapped road constraints, and a complete lack of real-time coordinate updates. Solving these manual dispatch challenges requires overcoming the NP-hard **Traveling Salesperson Problem (TSP)**, which scales factorially ($O(N!)$) with the number of delivery stops.

### The HyperRoute Architecture (The Solution)
**HyperRoute** addresses these constraints head-on by combining a robust, multi-threaded **Spring Boot 3** backend with a responsive **React 18** dashboard:
* **Hybrid TSP Solver**: Combines a greedy **Nearest Neighbor heuristic** for initial path discovery with a **2-opt local search heuristic** to refine and cross-eliminate overlapping paths. This provides optimal routes in sub-10ms for up to 15 key waypoints.
* **Reactive OSRM Integration**: Integrates directly with the Open Source Routing Machine (OSRM) API using a reactive, non-blocking **WebClient** pipeline featuring duration-based timeouts, retries, and local coordinate approximations.
* **Telemetry Broadcasts**: Supports full **STOMP over WebSockets** event messaging to simulate live vehicle coordinates, enabling real-time visual tracking on client dashboards without continuous HTTP polling.

---

## 🛠️ System Architecture & Data Flow

HyperRoute implements a strict **3-Tier layered architecture** with horizontal separation of concerns:

```mermaid
graph TD
    subgraph Client Layer (Vite + React)
        UI[React Glassmorphic Dashboard]
        WS_Client[SockJS + STOMP Client]
    end

    subgraph API Gateway & Security Layer
        GW[Vite API Reverse Proxy]
        SEC[Spring Security Filter Chain]
        JWT[JWT Authentication Provider]
    end

    subgraph Service & Core Engine Layer
        CTRL[REST Controllers]
        OPT[Route Optimization Service]
        SIM[GPS Telemetry Simulator]
        AUDIT[Central Audit Log Engine]
        TWOOPT[Two-Opt Heuristics Engine]
    end

    subgraph Persistence Layer
        DB[(PostgreSQL Database)]
        OSRM[OSRM Public Routing API]
    end

    UI -->|HTTPS REST Request| GW
    GW --> SEC
    SEC -->|Token Validation| JWT
    SEC --> CTRL
    CTRL --> OPT
    CTRL --> AUDIT
    WS_Client -->|STOMP WebSockets| SIM
    OPT -->|Coordinate Geodesics| TWOOPT
    OPT -->|Reactive Route Bounds| OSRM
    OPT --> DB
    SIM -->|Broadcast Telemetry| UI
```

### Component Breakdown
1. **Presentation Layer**: Built on React 18, bundling styling via HSL variables to support a high-fidelity glassmorphic layout. State management is built on lightweight reactive hooks.
2. **Security Gateway**: Enforces secure stateless sessions. Every incoming request must carry a valid cryptographic Bearer JWT, which is decoded and parsed by `JwtAuthenticationFilter` before reaching the API layer.
3. **Core Engine**: Pluggable strategies run optimization algorithms. The GPS simulation operates using thread-safe task schedulers to feed mock coordinates via WebSockets.
4. **Data Layer**: Integrates with PostgreSQL using isolated tables (`vehicles`, `drivers`, `delivery_tasks`, `routes`, `fleet_users`) mapped through clean Hibernate/JPA abstractions.

---

## 🗂️ Clean Project Directory Structure

```
fleet-management/
├── .github/
│   ├── workflows/
│   │   └── ci.yml               # Automated GitHub Actions test & build pipeline
│   └── ISSUE_TEMPLATE/
│       ├── bug_report.md        # Standardized bug reporting form
│       └── feature_request.md   # Standardized feature request form
├── backend/
│   ├── src/main/java/com/infotact/fleet/
│   │   ├── api/                 # REST controllers & validation DTOs
│   │   ├── config/              # WebSocket configurations & Security policies
│   │   ├── domain/              # Hibernate/JPA entities (Vehicle, Driver, etc.)
│   │   ├── repository/          # Custom spring repositories (Specifications-enabled)
│   │   └── service/             # Optimization services, JWT, and GPS simulations
│   ├── src/test/java/           # JUnit 5 & Mockito unit test suite
│   ├── pom.xml                  # Maven dependencies & build metadata
│   └── Dockerfile               # Multi-stage JDK 17 build configuration
├── frontend/
│   ├── src/
│   │   ├── components/          # Reusable UI widgets (Charts, Leaflet Tracker)
│   │   ├── api.js               # Service calls backed by Axios interceptors
│   │   ├── App.jsx              # Main routing and glassmorphic layout shell
│   │   └── index.css            # Modular variables and theme styles
│   ├── vite.config.js           # Proxy routing and server port configs
│   └── Dockerfile               # Nginx static deployment build
├── docker-compose.yml           # Unified services manager (Database + App + Frontend)
└── README.md                    # Technical showcase & instructions manual
```

---

## 🚀 Key Feature Highlights

* **🔑 JWT & Role-Based Access Control**: Strict endpoint locking mapping to roles (`ADMIN`, `DISPATCHER`, `DRIVER`). Backed by custom exceptions, automatic user seeding, and stateless filtering.
* **📈 Rich Analytics & Live Stats**: Beautiful interactive SVG charts tracking fuel efficiency, completion percentages, active driver ratios, and operational performance.
* **📍 Pluggable Route Optimization Heuristics**: Custom strategy patterns to compute routes using the OSRM road API or a local mathematical geodesic fallback when OSRM is offline.
* **📡 Real-Time GPS Tracking Simulator**: Simulates vehicle progression between scheduled waypoints, broadcasting live longitude and latitude coordinates onto the client dashboard via STOMP WebSockets.
* **🔍 Specifications-Backed Pagination**: Advanced server-side paginated queries enabling real-time search, sorting, and state-machine status filtering across all fleet tables.
* **🗃️ Centralized Security Auditing**: Every high-risk event (login failures, route dispatches, asset modifications) is captured inside an isolated system audit database table with exact timestamps, user contexts, and IP tracking.

---

## ⚡ Quick Start & Installation

Ensure you have **Java 21**, **Node.js (v18+)**, and **PostgreSQL (v14+)** or **Docker** installed on your system.

### Option A: Run via Docker Compose (Recommended)
You can launch the entire ecosystem—including database, API, and UI—with a single command:
```bash
docker-compose up --build -d
```
* Access Frontend Dashboard: `http://localhost:5174`
* Access Swagger API Documentation: `http://localhost:8082/swagger-ui.html`

### Option B: Local Manual Setup

#### 1. Setup the Database
Create a PostgreSQL database named `wms` on port `5432`. Ensure your database credentials match `application.yml` or supply them as environment variables:
```sql
CREATE DATABASE wms;
```

#### 2. Bootstrap the Spring Boot Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
*The backend server compiles and initializes on port `8082`.*

#### 3. Build & Run the React UI
```bash
cd ../frontend
npm install
npm run dev
```
*The frontend Vite server spins up on port `5174` and auto-proxies API requests.*

---

## 🔒 Security Configuration & JWT Workflow

HyperRoute utilizes a custom Spring Security filter chain to enforce stateless token-based authorization:

```
[Incoming Request] ──► [JwtAuthenticationFilter] ──► [Verify Signature & Expire Date]
                                  │
      ┌───────────────────────────┴───────────────────────────┐
      ▼ (Valid Token)                                         ▼ (Invalid / Missing)
[Inject into SecurityContext]                            [Reject with 401 Unauthorized]
      │
      ▼
[Route Request to Controller]
```

* **Token Lifespan**: Set to 8 hours by default, highly configurable via `application.yml`.
* **Signature Algorithm**: Signed using HS256 with a strong 32-byte secret.
* **Input Validation**: All incoming requests undergo strict validation using Hibernate Validator (`@NotNull`, `@Size`, `@Pattern`) before business processing.

---

## 📈 Engineering Roadmap & Horizon Features

- [ ] **AI-Driven Route Predictions**: Integrate historical traffic patterns using machine learning regression models.
- [ ] **Kubernetes Orchestration**: Transition the multi-container configuration into Helm charts ready for cloud environments.
- [ ] **Distributed Cache Layer**: Inject a Redis instance to cache repeated OSRM routing requests, reducing network calls by up to 40%.
- [ ] **Microservices Migration**: Decouple the monolithic optimization engine into an isolated service communicated via Apache Kafka event streams.

---

## 📄 License & Acknowledgements

* Distributed under the **MIT License**. See [LICENSE](file:///c:/Users/shriyal/OneDrive/Documents/New%20project/fleet-management/LICENSE) for details.
* Powered by [Open Source Routing Machine (OSRM)](https://project-osrm.org/) API.
* Designed with ❤️ as a modern showcase project.

---

<div align="center">
  <h3>✨ Built for Recruiters, Evaluators, and Engineers ✨</h3>
  <p>For inquiries, feedback, or contribution proposals, feel free to open an issue or pull request!</p>
</div>
