# 🧪 HyperRoute Testing Strategy & Verification Manual

This document details the automated testing patterns, Mockito mocking strategies, validation steps, and coverage commands integrated within the **HyperRoute Route Optimization Engine**.

---

## 🏗️ Backend Testing Architecture

HyperRoute features 100% automated surefire verification coverage for critical business logic services, optimization engines, and state machine transitions.

```
       [ Maven Test Runner ]
                 │
  ┌──────────────┴──────────────┐
  ▼                             ▼
[JUnit 5 Engine]         [Mockito Core]
  │                             │
  ├─► VehicleServiceTest        ├─► Mock repositories
  ├─► DriverServiceTest         ├─► Mock OSRM WebClient
  └─► RouteOptimizationTest     └─► Simulated geodetic bounds
```

### Core Technologies
* **JUnit 5 (Jupiter)**: The standard test runner engine for lifecycle and assertion management.
* **Mockito**: Isolates components during testing. Used to mock databases and HTTP clients, keeping tests fast and focused on single-responsibility modules.
* **Spring Boot Starter Test**: Leverages isolated mock profiles to verify Spring contexts without spinning up network ports.

---

## 🛠️ Mocking Heuristics & Mockito Usage

To ensure tests execute in milliseconds without database operations or external API calls, all network operations and repository queries are systematically mocked.

### Example Mock Configuration (`RouteOptimizationServiceTest.java`)
```java
@ExtendWith(MockitoExtension.class)
class RouteOptimizationServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private OsrmClient osrmClient;

    @InjectMocks
    private RouteOptimizationService routeOptimizationService;

    @Test
    void shouldOptimizeRouteAndCalculateScoresSuccessfully() {
        // Arrange
        Vehicle testVehicle = new Vehicle("KA-01-AB-1234", "Tata Ace", 750.0, FuelType.DIESEL);
        when(vehicleRepository.findById(1Long)).thenReturn(Optional.of(testVehicle));
        
        // Act & Assert statements...
    }
}
```

---

## 🏃 Commands to Execute Test Suites

Verify backend stability locally by running the Maven surefire plugin from the backend directory:

### 1. Run Complete Test Suite
Executes all unit tests and outputs compilation reports:
```bash
mvn test
```

### 2. Run Isolated Test Target
To run a specific test class during development iterations:
```bash
mvn test -Dtest=RouteOptimizationServiceTest
```

---

## 🔌 Postman Verification Workflow

To manual-test the API contracts or execute continuous validation runs:

### 1. Retrieve Access Bearer JWT
* Dispatch a `POST` request targeting `http://localhost:8082/api/auth/login` containing valid credentials.
* Extract the value of the `token` parameter from the JSON response body.

### 2. Configure Header Inheritance
* Under the root collection inside Postman, navigate to the **Authorization** tab.
* Set the type to **Bearer Token**.
* Paste the extracted token into the **Token** field.
* This inherits token credentials across all downstream requests, preventing manual headers edits.

### 3. Verify Database Seeding
Dispatch a `GET` request targeting `http://localhost:8082/api/vehicles` to verify that pre-seeded database assets are retrieved successfully.
