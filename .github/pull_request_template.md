# 🚀 Pull Request Template

## 📋 Description
Provide a concise description of the changes introduced by this PR. Detail the business problem, architectural decisions, and why these modifications were necessary.

## 🔗 Related Issues
List any related GitHub issues resolved by this pull request (e.g., `Closes #12`, `Fixes #34`).

## 🛠️ Changes Type
What type of changes does your code introduce? (Mark with an `x`)
- [ ] 🐛 Bug fix (non-breaking change which fixes an issue)
- [ ] ✨ New feature (non-breaking change which adds functionality)
- [ ] 💥 Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] ⚡ Performance tuning
- [ ] ⚙️ Chore/Refactoring

---

## 🎨 Visuals (If applicable)
*Attach screenshots or GIFs illustrating frontend changes, database ER alterations, or system workflow outputs.*

---

## 🧪 Testing Verification Checklist

Please verify that all tests pass before seeking review:

### Automated Testing
- [ ] Backend JUnit tests compiled and passed (`mvn test`)
- [ ] Coverage requirements met (no regression in line coverage)
- [ ] Tested API request/response contracts using Swagger/Postman

### Manual Testing Run
- [ ] Verified database migrations applied successfully without resource locks
- [ ] Hand-tested login, dispatch registration, and websocket coordination paths on localhost

---

## 🔒 Security & Code Quality Gate

- [ ] All inputs validated on Controller layer (no raw queries or unescaped strings)
- [ ] Proper authorization roles applied via `@PreAuthorize` or SecurityConfig
- [ ] No hardcoded secrets or credentials (secrets injected via environment vars)
- [ ] Code is formatted in compliance with the project's standard rules
