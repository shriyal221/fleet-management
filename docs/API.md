# 🔌 HyperRoute REST API Specification & Endpoint Documentation

All endpoints are hosted by default on base path `http://localhost:8082` (or mapped through the frontend Dev proxy on `/api/*`).

---

## 🔑 Authentication Service & JWT Workflow

All protected endpoints require a valid stateless JWT provided in the HTTP authorization headers:
```http
Authorization: Bearer <your-jwt-token>
```

### 1. User Registration
Creates a new administrative or dispatcher account.
* **HTTP Method**: `POST`
* **Path**: `/api/auth/register`
* **Request Payload**:
```json
{
  "username": "dispatcher_hq",
  "password": "strongPassword123",
  "role": "DISPATCHER"
}
```
* **Response Payload (`201 Created`)**:
```json
{
  "message": "User registered successfully!",
  "username": "dispatcher_hq",
  "role": "DISPATCHER"
}
```

### 2. User Login
Generates a signed JSON Web Token valid for 8 hours.
* **HTTP Method**: `POST`
* **Path**: `/api/auth/login`
* **Request Payload**:
```json
{
  "username": "dispatcher_hq",
  "password": "strongPassword123"
}
```
* **Response Payload (`200 OK`)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaXNwYXRjaGVyX2hxIiwicm9sZSI6WyJESVNQQVRDSEVSIl0sImlhdCI6MTY4NTI2MDgwMCwiZXhwIjoxNjg1Mjg5NjAwfQ.xyz-signature...",
  "type": "Bearer",
  "username": "dispatcher_hq",
  "role": "DISPATCHER"
}
```

---

## 🚚 Fleet Asset Management APIs

### 1. Register a New Vehicle
* **HTTP Method**: `POST`
* **Path**: `/api/vehicles`
* **Required Roles**: `ADMIN` or `DISPATCHER`
* **Request Payload**:
```json
{
  "plateNumber": "KA-01-AB-9999",
  "model": "Tata Ultra T.7",
  "capacityKg": 3500.0,
  "fuelType": "DIESEL"
}
```
* **Response Payload (`201 Created`)**:
```json
{
  "id": 6,
  "plateNumber": "KA-01-AB-9999",
  "model": "Tata Ultra T.7",
  "capacityKg": 3500.0,
  "fuelType": "DIESEL",
  "status": "AVAILABLE"
}
```

### 2. Search & List Paginated Vehicles
Exposes dynamic search filters using database specifications.
* **HTTP Method**: `GET`
* **Path**: `/api/vehicles?page=0&size=10&sort=plateNumber,asc&status=AVAILABLE&search=Tata`
* **Response Payload (`200 OK`)**:
```json
{
  "content": [
    {
      "id": 6,
      "plateNumber": "KA-01-AB-9999",
      "model": "Tata Ultra T.7",
      "capacityKg": 3500.0,
      "fuelType": "DIESEL",
      "status": "AVAILABLE"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 🚶 Driver Lifecycle & Shift Registry

### 1. Register a Driver
* **HTTP Method**: `POST`
* **Path**: `/api/drivers`
* **Request Payload**:
```json
{
  "name": "Arjun Singh",
  "licenseNumber": "DL-01-202300456",
  "shiftStart": "08:00",
  "shiftEnd": "18:00"
}
```
* **Response Payload (`201 Created`)**:
```json
{
  "id": 5,
  "name": "Arjun Singh",
  "licenseNumber": "DL-01-202300456",
  "shiftStart": "08:00:00",
  "shiftEnd": "18:00:00",
  "status": "AVAILABLE",
  "assignedVehicleId": null
}
```

---

## 📍 Outbound Stop Orders (Delivery Tasks)

### 1. Create a Outbound Stop
Registers a delivery order with exact geographic coordinates.
* **HTTP Method**: `POST`
* **Path**: `/api/deliveries`
* **Request Payload**:
```json
{
  "recipientName": "Acme Megastore",
  "address": "Indiranagar 100ft Rd, Bengaluru",
  "latitude": 12.97189,
  "longitude": 77.64115,
  "weightKg": 450.0
}
```
* **Response Payload (`201 Created`)**:
```json
{
  "id": 9,
  "recipientName": "Acme Megastore",
  "address": "Indiranagar 100ft Rd, Bengaluru",
  "latitude": 12.97189,
  "longitude": 77.64115,
  "weightKg": 450.0,
  "status": "UNASSIGNED"
}
```

---

## 🗺️ Heuristic Routing & Dispatch Console

### 1. Calculate Optimized TSP Route (Dry Run)
Calculates optimal waypoint sequencing without writing to persistent storage.
* **HTTP Method**: `POST`
* **Path**: `/api/routes/calculate`
* **Request Payload**:
```json
{
  "vehicleId": 1,
  "driverId": 1,
  "taskIds": [1, 3, 5]
}
```
* **Response Payload (`200 OK`)**:
```json
{
  "totalDistanceKm": 24.3,
  "estimatedDurationMins": 48.6,
  "fuelEstimateLiters": 3.04,
  "waypointSequence": [
    { "id": 3, "address": "Fashion Hub MG Road", "sequenceIndex": 0 },
    { "id": 1, "address": "Priya Electronics Indiranagar", "sequenceIndex": 1 },
    { "id": 5, "address": "Global Logistics Whitefield", "sequenceIndex": 2 }
  ],
  "efficiencyScore": 92.5
}
```

### 2. Confirm & Dispatch Route
Saves and starts the physical telemetry simulation thread.
* **HTTP Method**: `POST`
* **Path**: `/api/routes`
* **Request Payload**:
```json
{
  "vehicleId": 1,
  "driverId": 1,
  "taskIds": [1, 3, 5]
}
```
* **Response Payload (`201 Created`)**:
```json
{
  "routeId": 12,
  "vehiclePlate": "KA-01-AB-1234",
  "driverName": "Ramesh Kumar",
  "totalStops": 3,
  "totalDistanceKm": 24.3,
  "status": "ACTIVE"
}
```
