# 🤝 Contributing to HyperRoute

Thank you for your interest in contributing to the **HyperRoute Fleet Management and Route Optimization Engine**! To maintain a world-class, enterprise-grade codebase, we enforce strict standards for engineering, reviews, and versioning.

This guide outlines our development workflow, coding rules, branching strategies, and commit conventions.

---

## 🗺️ Git Branching Strategy (GitFlow Heuristics)

We follow a modified **GitFlow** branching strategy. All contributions must go through feature branches merged via Pull Requests.

```
  main      ────────────────────────────────────────────────────────► (Production)
                               ▲
                               │ [Release Merges]
  develop   ──────┬────────────┴───────┬────────────────────────────► (Integration)
                  │                    ▲
                  │ [Feature Branch]   │ [PR Review & Approval]
  feature/*       └───► feature/XYZ ───┘
```

### Branch Naming Conventions
* **Features**: `feature/short-description` (e.g., `feature/websocket-telemetry`)
* **Bug Fixes**: `bugfix/short-description` (e.g., `bugfix/jwt-expiration-check`)
* **Hotfixes (Direct to Main)**: `hotfix/critical-patch` (e.g., `hotfix/cors-vulnerability`)
* **Documentation**: `docs/update-description` (e.g., `docs/api-payloads`)
* **Refactoring**: `refactor/component-name` (e.g., `refactor/two-opt-scoring`)

---

## ✍️ Semantic Commit Message Standards

We strictly enforce the **Conventional Commits** specification. This ensures a clean, automated changelog and legible Git history.

### Commit Message Format
```
<type>(<scope>): <short summary>

[Optional detailed body describing the "why" behind the change]

[Optional footer referencing Issue numbers: Closes #12]
```

### Allowed Types (`<type>`)
* **`feat`**: A new feature (e.g., `feat(websocket): add real-time coordinates broadcast`)
* **`fix`**: A bug fix (e.g., `fix(security): resolve JWT signature verification bypass`)
* **`refactor`**: Code changes that neither fix bugs nor add features (e.g., `refactor(optimization): extract Two-Opt strategies to sub-package`)
* **`docs`**: Documentation only updates (e.g., `docs(readme): expand quick start guides`)
* **`test`**: Adding missing tests or correcting existing tests (e.g., `test(service): mock OSRM client timeouts`)
* **`style`**: Changes that do not affect code logic (white-space, formatting, missing semi-colons)
* **`chore`**: Maintenance tasks, build setups, or dependency bumps

---

## 🛠️ Java and Javascript Coding Guidelines

### 1. Backend Standards (Java & Spring Boot)
* **Clean Code**: Follow Uncle Bob's Clean Code principles. Methods must do one thing, be small, and avoid side-effects.
* **REST Constraints**: Use correct HTTP response codes (e.g., `201 Created` for creations, `200 OK` for mutations, `204 No Content` for deletions).
* **Exception Strategy**: Never return raw HTTP 500 stacks. Map all domain-specific errors through the `GlobalExceptionHandler` to produce a structured JSON response.
* **Validations**: Proactively enforce constraints on all incoming requests at the Controller layer using `@Valid` and `@NotNull`/`@Size` annotations.
* **Database Access**: Avoid N+1 queries. Use JPA entity graphs or custom joins where necessary.

### 2. Frontend Standards (React & CSS)
* **Component Granularity**: Keep components single-responsibility. Extract smaller visual components into `components/`.
* **State Management**: Leverage local hooks (`useState`, `useContext`) properly. Avoid global state polluting when local state suffices.
* **Style Discipline**: Follow the HSL color variables system inside `index.css`. Do not inject ad-hoc inline styles.

---

## 🔎 Pull Request & Review Process

1. **Synchronize Base**: Ensure your local `develop` or `main` branch is fully synchronized before starting.
2. **Write Unit Tests**: Every feature or bugfix must be accompanied by unit or integration tests (using JUnit 5 and Mockito). Validate your changes pass locally:
   ```bash
   mvn clean test
   ```
3. **Open a Pull Request**: Submit your PR targeting the `develop` branch.
4. **Pass CI/CD**: Ensure the automated GitHub Actions CI build checks out successfully.
5. **Code Review**: At least one senior reviewer must approve the PR before it is merged. Be prepared to address review feedback.
