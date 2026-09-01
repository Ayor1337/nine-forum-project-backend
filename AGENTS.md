# Repository Guidelines

## Project Structure & Module Organization

This Java 17/Spring Boot 3.5.5 project is a Maven reactor. `common/` contains shared utilities; `model/` owns shared entities, DTOs, VOs, enums, and type handlers. `web/web-app/` provides the public API; `web/web-admin/` provides administration APIs. Code uses Maven layout under `src/main/java`, with mirrored tests under `src/test/java` and runtime files under `src/main/resources`. Local infrastructure and seed SQL are in `.docker/` and `.sql/`.

## Build, Test, and Development Commands

Run commands from the repository root using the wrapper:

- `./mvnw clean test` — compile all modules and run every test.
- `./mvnw clean package` — build all JARs after tests pass.
- `./mvnw -pl web/web-app -am test` — test one application and its dependencies; substitute `web/web-admin` as needed.
- `./mvnw -pl web/web-app -am spring-boot:run` — start the public API on port `9966`; admin uses port `9977`.
- `docker compose -f .docker/docker-compose.yaml up -d` — start local dependencies after creating the ignored `.docker/environment/*.env` files described in `README.md`.

## Coding Style & API Conventions

Use four-space indentation, UTF-8, `PascalCase` types, `camelCase` members, and lowercase packages under `com.ayor`. Preserve suffixes such as `DTO`, `VO`, `Controller`, `Service`, `Mapper`, and `*Impl`. Keep controllers thin and business logic in services. Prefer simple, fail-fast implementations. APIs use verb-oriented RPC endpoints, normally `POST` (read-only operations may use `GET`), return `Result<T>`, and reserve HTTP status for transport failures. No formatter or linter is configured; match nearby code and organize imports.

## Testing Guidelines

Tests use JUnit 5, Mockito, Spring Security Test, and MockMvc. Name unit tests `*Test`; use `*IntegrationTest` or `*ContractTest` for external wiring or API/security contracts. Place tests in the corresponding module and package. Add regression tests for behavior changes; run the module suite first, then `./mvnw clean test`. No coverage gate is configured, so cover changed branches and failure paths.

## Commit & Pull Request Guidelines

Follow Conventional Commits: `feat(auth): 中文描述`, `fix(infra): 中文描述`, or `docs(security): 中文描述`. Keep commits focused. Pull requests should state the problem, implementation, affected modules/configuration, verification commands, and linked issue. Include request/response examples for API changes and screenshots for visible output. Update documentation whenever interfaces, configuration, scripts, architecture, or observable behavior changes.

## Security & Configuration

Never commit `application.yml`, `.env` files, tokens, certificates, or real credentials. Start from tracked `application.example.yml` and `.env.example` files. Keep local services bound to loopback; production secrets belong in external secret management. Review authentication, authorization, validation, and sensitive logging whenever changing endpoints or WebSocket flows.
