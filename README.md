# Fleet Management and Route Optimization System (Project 2)

This is a standalone Fleet Management project aligned with the Project 2 document. It uses a Java 17 Spring Boot backend, MySQL database, OSRM-assisted route optimization, JWT security, and a React/Vite frontend.

The app is separate from the Warehouse Management System project:

- Backend API: `http://localhost:8082`
- Frontend app: `http://localhost:5173` or the port shown by Vite
- Swagger API docs: `http://localhost:8082/swagger-ui/index.html`
- Database: MySQL database named `fleet_db`

## Core Modules

- Vehicle registry with payload capacity, volume capacity, fuel type, odometer, GPS location, and maintenance status.
- Driver registry with license expiry, shift timing, current status, and assigned vehicle.
- Delivery task registry with GPS coordinates, package weight/volume, delivery status, and delivery time windows.
- Route planner with OSRM distance matrix, nearest-neighbor ordering, 2-opt improvement, and Haversine fallback.
- JWT-secured APIs with role-based access for admin and dispatcher users.

## Professional Validation Rules

The route planner now validates the main business constraints before creating a route:

- Vehicle must be `OPERATIONAL`.
- Driver must be `AVAILABLE`.
- Driver license must not be expired.
- Driver assigned vehicle must match the selected vehicle when an assignment exists.
- Selected delivery total weight must not exceed vehicle payload capacity.
- Selected delivery total volume must not exceed vehicle volume capacity.
- Delivery tasks must be unassigned and not already attached to another route.
- Duplicate delivery stop selection is rejected.
- Planned route must respect delivery time-window closing times.
- Estimated route completion must fit inside the driver's shift.
- OSRM waypoint count is limited by `fleet.osrm.max-waypoints`.

## Run Locally

### Backend

Create the database in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS fleet_db;
```

Start the backend:

```powershell
cd "C:\Users\shriyal\OneDrive\Documents\New project\fleet-management\backend"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
mvn spring-boot:run
```

Backend health check:

```text
http://localhost:8082/actuator/health
```

### Frontend

```powershell
cd "C:\Users\shriyal\OneDrive\Documents\New project\fleet-management\frontend"
npm install
npm run dev
```

Open the URL shown by Vite, usually:

```text
http://localhost:5173/
```

If port `5173` is busy, Vite may use `5174`.

## Seed Users

When the backend starts, it creates sample users if they do not already exist:

- Admin: `admin` / `admin123`
- Dispatcher: `dispatcher` / `dispatcher123`

It also seeds sample vehicles, drivers, and Bengaluru delivery stops for demonstration.

## Testing

Backend unit tests cover route capacity validation, time-window validation, delivery state transitions, and driver license checks:

```powershell
cd "C:\Users\shriyal\OneDrive\Documents\New project\fleet-management\backend"
mvn test
```

Frontend build check:

```powershell
cd "C:\Users\shriyal\OneDrive\Documents\New project\fleet-management\frontend"
npm run build
```

## CI

GitHub Actions workflow is included at `.github/workflows/ci.yml`. It runs:

- Backend Maven tests
- Frontend npm install and production build
