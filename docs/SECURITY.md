# 🛡️ HyperRoute Security & Access Control Configuration

This document specifies the security controls, authentication protocols, and threat defense measures integrated within the **HyperRoute Fleet Management and Route Optimization System**.

---

## 🔑 JWT Stateless Access Flow & Authorization

HyperRoute enforces strict stateless token-based authorization. Standard session tracking is disabled inside the Spring Security filter context.

```
 [Request] ──► [JwtAuthenticationFilter] ──► [Claims Extractor]
                         │
      ┌──────────────────┴──────────────────┐
      ▼ (Valid Signature)                   ▼ (Expired / Tampered)
 [Load UserDetails into context]       [Invoke AuthenticationEntryPoint]
      │                                     │
      ▼                                     ▼
 [Pass to Route Interceptor]           [Emit HTTP 401 JSON Payload]
```

### Authentication Architecture Details
* **Cryptographic Signatures**: Custom security encoders sign claims using **HMAC-SHA256** backed by a 256-bit strong base64-encoded secret key.
* **Stateless Filters**: Every non-public request undergoes verification within `JwtAuthenticationFilter.java` before mapping to any Controller methods.
* **Graceful Exceptions**: If a JWT signature fails verification or expires, the system bypasses raw container errors and returns a clean, structured JSON payload:
  ```json
  {
    "status": 401,
    "error": "Unauthorized",
    "message": "JWT Token has expired or is invalid.",
    "timestamp": "2026-05-29T11:32:00Z"
  }
  ```

---

## 🚦 Role-Based Access Control (RBAC) Core Matrix

Endpoints are secured using Spring Security method protection rules. Operational responsibilities are strictly restricted across three primary user roles:

| API Controller Group | Endpoint Mappings | ADMIN | DISPATCHER | DRIVER |
|:---|:---|:---:|:---:|:---:|
| **Auth Gateway** | `/api/auth/**` | ✅ | ✅ | ✅ |
| **Fleet Registry** | `/api/vehicles/**`, `/api/drivers/**` | ✅ | ✅ | ❌ |
| **Outbound Stops** | `/api/deliveries/**` | ✅ | ✅ | ❌ |
| **TSP Solver Engine**| `/api/routes/calculate` | ✅ | ✅ | ❌ |
| **Dispatch Controllers**| `/api/routes/dispatch`, `/api/routes/{id}`| ✅ | ✅ | ❌ |
| **Odometer Logging** | `/api/routes/{id}/complete` | ✅ | ✅ | ✅ |

---

## 🔒 Injection Prevention & Input Validations

HyperRoute enforces strict defensive coding practices to prevent input injection attacks:

### 1. SQL Injection Prevention
* **Approach**: Hibernate JPA repositories compile queries into parameter-mapped **Prepared Statements** at database driver level.
* **Query Scopes**: Avoid raw string concatenations in custom SQL queries. Complex dynamic queries (filtering, sorting) are solved using Spring Data's JPA **Specification interface** (`CriteriaBuilder` and `Predicate`), securing parameters automatically.

### 2. Strict Input Data Validations
All incoming payloads are validated at the API Controller gateway using the **Jakarta Validation API** (`@Valid`).

```java
public class VehicleRequest {
    @NotBlank(message = "Plate number is required")
    @Pattern(regexp = "^[A-Z]{2}-\\d{2}-[A-Z]{1,2}-\\d{4}$", message = "Invalid vehicle plate format")
    private String plateNumber;

    @Min(value = 100, message = "Vehicle capacity must be at least 100 kg")
    private double capacityKg;
}
```
* **Invalid Payload Responses**: Validation constraints trigger standard validation errors. These errors are caught by `GlobalExceptionHandler.java` and returned as a clear array of errors, helping developers troubleshoot client-side inputs quickly:
  ```json
  {
    "status": 400,
    "error": "Bad Request",
    "validationErrors": {
      "plateNumber": "Invalid vehicle plate format"
    }
  }
  ```
