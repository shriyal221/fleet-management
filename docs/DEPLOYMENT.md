# 🐳 HyperRoute Deployment & Container Orchestration Guide

This document describes the configurations and environment steps required to deploy the **HyperRoute Fleet Management and Route Optimization Engine** in local development, testing, and production container environments.

---

## 🛠️ Multi-Container Docker Architecture

We bundle a production-ready **Docker Compose** layout to spin up the entire ecosystem as isolated networking containers.

```
       [ Client Browser ]
               │ (Port 5174)
               ▼
┌──────────────────────────────┐
│  frontend (Nginx container)   │
└──────────────┬───────────────┘
               │ (Reverse Proxy on /api/*)
               ▼
┌──────────────────────────────┐
│   backend (Java 17 App)      │
└──────────────┬───────────────┘
               │ (Port 5432)
               ▼
┌──────────────────────────────┐
│  fleet-db (PostgreSQL Image) │
└──────────────────────────────┘
```

### Container Setup Profiles
* **`fleet-db`**: Anchored PostgreSQL container hosting isolated relational schemas. Uses host volumes to persist telemetry information across restarts.
* **`backend`**: Built using a multi-stage `Dockerfile` that packages the application with a minimal JDK runtime image, exposing port `8082`.
* **`frontend`**: Bundles static assets compiled via Vite, served through high-performance **Nginx** configurations mapped to proxy all `/api` calls.

---

## ⚙️ Environment Variables Reference Matrix

Configure these variables inside your shell or a `.env` file at the root of the project to customize the application runtime behavior:

| Variable Name | Default Value | Description |
|:---|:---|:---|
| `SERVER_PORT` | `8082` | HTTP port for the Spring Boot backend service. |
| `DB_URL` | `jdbc:postgresql://localhost:5432/wms` | JDBC datasource url linking the PostgreSQL instance. |
| `DB_USERNAME` | `wms` | Username for database access. |
| `DB_PASSWORD` | `wms` | Password for database access. |
| `FLEET_JWT_SECRET` | `standalone-fleet-only-change-me-to-a-strong-32-byte-secret` | Cryptographic secret for signing JWT claims. |
| `JWT_EXPIRES_MINUTES` | `480` | Lifespan of generated JWT tokens (8 hours). |
| `OSRM_BASE_URL` | `https://router.project-osrm.org` | Road network API endpoint mapping TSP coordinate distances. |
| `CORS_ALLOWED_ORIGINS`| `http://localhost:5174` | Allowed origins for secure web browser queries. |

---

## 🚀 Step-by-Step Deployment Guides

### Option A: Local Dev Standalone Startup
To run HyperRoute locally on your physical machine for development iterations:

#### 1. Database Initialization
Ensure PostgreSQL is active. Execute the following in your database terminal to create the isolated schema context:
```sql
CREATE DATABASE wms;
```

#### 2. Run the Backend API
Navigate to the backend folder and boot the Spring Boot context. Tables will auto-migrate using Hibernate's `update` routine:
```bash
cd backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
```

#### 3. Run the React UI Dashboard
Open a parallel shell to build and execute Vite:
```bash
cd frontend
npm install
npm run dev
```
*Vite launches on `http://localhost:5174/`, routing traffic to the API proxy configuration.*

---

### Option B: High-Performance Multi-Stage Container Startup
For automated staging or deployment, build the images using the multi-stage configurations:

#### 1. Compile and Launch Multi-Containers
Run the docker command from the root folder containing the `docker-compose.yml` file:
```bash
docker-compose up --build -d
```

#### 2. Verify Container Health
Verify that all services are online and active:
```bash
docker ps
```
To review active streaming telemetry or troubleshooting logs:
```bash
docker-compose logs -f backend
```
To shut down and clear active container resources:
```bash
docker-compose down -v
```
