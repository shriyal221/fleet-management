# Standalone Fleet Management & Route Optimization System (Project 2)

This is a **completely standalone, enterprise-grade Fleet Management and Route Optimization System** built from scratch in compliance with the **Project 2 brief**. It features a Java 17 / Spring Boot backend, a MySQL database, an external OSRM public API integration for travel matrix calculations, and a high-fidelity glassmorphic React (Vite) frontend.

This standalone application operates on separate local ports (**`8082` for Backend, `5174` for Frontend**) to prevent any conflicts with the existing Warehouse Management System (Project 1) running in the root workspace.

---

## 🚚 Key Features

### 📋 Registry Registry (Vehicles & Drivers)
* **Vehicle registry**: Supports payload capacities (weight/volume), fuel types (`DIESEL`, `PETROL`, `CNG`, `ELECTRIC`), live odometer logging, and active operational states (`OPERATIONAL`, `IN_MAINTENANCE`, `OUT_OF_SERVICE`).
* **Driver profiles**: Shifts availability window configuration, license validity credentials tracking, and vehicle assignments.

### 📍 TSP Route Optimization Engine (OSRM)
* **Traveling Salesperson Problem (TSP) Solver**:
  1. Resolves cost matrices by querying the free **OSRM Table API** (`/table/v1/driving/`).
  2. Applies a greedy **Nearest Neighbor** algorithm starting from the depot origin coordinates.
  3. Applies an iterative **2-opt local search** algorithm to resolve segment overlaps and further minimize distance.
  4. Queries the **OSRM Route API** (`/route/v1/driving/`) with the optimized sequence to get exact travel distances, total driving times, and precise waypoints.
* **Math Fallback Heuristic**: Features an automatic **Haversine formula math fallback**. If OSRM is rate-limited or offline, the system will seamlessly fall back to local geometry calculations, keeping the app 100% operational.

### 🔄 Outbound State Machine
Delivery stops (`DeliveryTask`) follow a strictly validated state machine to protect transactional logging:
```
UNASSIGNED ──> DISPATCHED ──> IN_TRANSIT ──> DELIVERED
                                         └──> FAILED
```
Completing a route automatically increments vehicle odometers and resets driver statuses to available.

---

## 🐳 Run with Docker (Recommended)

From the `fleet-management` directory, run:

```bash
docker compose up --build
```

Then open:
* **Frontend Portal**: `http://localhost:3001`
* **Backend API**: `http://localhost:8082`
* **Swagger OpenAPI Documentation**: `http://localhost:8082/swagger-ui/index.html`

---

## 💻 Run Locally

### 1. Pre-requisites
* **Java**: OpenJDK 17 or higher
* **Database**: MySQL running locally on port `3306` with database `fleet_db` (or matching environment parameters)
* **Node.js**: Version 18+ and `npm`

### 2. Standalone Backend Startup

Configure your database url, username, and password in `backend/src/main/resources/application.yml` or export them as environment variables:

```bash
cd backend
mvn spring-boot:run
```

The Spring Boot backend will start on **`http://localhost:8082`** and automatically seed:
* **Admin user**: `admin` / `admin123`
* **Dispatcher user**: `dispatcher` / `dispatcher123`
* **5 Vehicles** (mix of Diesel, CNG, Electric)
* **4 Drivers** (shift configured, license credentials)
* **8 Delivery Stops** in Bengaluru (real GPS coordinates)

### 3. Standalone Frontend Startup

```bash
cd frontend
npm install
npm run dev
```

The frontend server starts on **`http://localhost:5174`** and proxies API calls to the backend on port `8082`. Log in using the `dispatcher` / `dispatcher123` credentials to test the optimizer!
