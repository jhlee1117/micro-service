# ArchUnit Architecture Rules

This project uses ArchUnit to keep module architecture visible and testable.
Agents should read this file before generating new Java packages, services,
controllers, repositories, events, filters, or shared utilities.

## Reference Model

The rules follow ArchUnit's standard use cases for package dependency checks,
layer checks, and cycle checks. The baseline is a conservative Spring layered
architecture:

```text
controller / listener / web adapter
  -> service / application logic
    -> repository / persistence adapter
      -> domain entity
```

DTOs may be used at web and service boundaries. In this codebase, some DTOs also
contain simple factory methods from domain entities, so DTO-to-domain dependency
is currently allowed. Repositories may return DTO projections where the current
module already uses that pattern.

## Global Rules

- Top-level packages inside each module must be free of dependency cycles.
- Web/controller packages must not access repositories directly.
- Services must not depend on controllers.
- Domain entities must not depend on controllers, services, repositories, DTOs,
  OAuth/web adapters, security, configuration, or utility packages.
- Common modules must not depend on application modules under
  `com.microservices..`.

## Module Rules

### api-gateway

- Code must stay under `com.microservices.gateway`.
- Gateway code is grouped into root application, `config`, and `security`.
- Security components must not depend on the gateway `config` package.
- Configuration classes must live in `config` or `security`.

### auth-service

- `controller` and controller-like `oauth` adapters must not access
  repositories directly.
- `service` must not depend on web/controller packages.
- `repository` must not depend on service, web, OAuth adapter, or security
  packages.
- domain entities are persistence/domain state and must stay independent from
  application layers.
- Spring stereotypes should be placed in their matching packages:
  `@RestController` in `controller` or `oauth`, `@Service` in `service` or
  security support packages, and `@Repository` in `repository`.

### board-service

- `controller` must call `service`, not `repository` directly.
- `repository` must not depend on `controller` or `service`.
- `domain` must not depend on `controller`, `service`, `repository`, or `dto`.
- `service` must not depend on `controller`.

### tenant-provisioning-worker

- `listener` receives RabbitMQ messages and delegates to `service`.
- `listener` must not depend on infrastructure `config`.
- `service` must not depend on `listener` or `config`.
- event message classes must not depend on worker layers.
- `@Service` classes belong in `service`; `@Configuration` classes belong in
  `config`.

### common-jwt

- `com.common.jwt` must not depend on application modules.
- `filter` must not depend on `config` factory classes.
- `authentication` core must not depend on `filter` or `config`.

### common-exceptions

- common exception and response types must not depend on Spring or application
  modules.

### common-util

- common utilities must not depend on Spring or application modules.

## Commands

Run architecture tests with the normal test task:

```bash
./gradlew test
```

Run format, style, and architecture checks together:

```bash
./gradlew spotlessCheck checkstyleMain checkstyleTest test
```

## Updating Rules

When a module intentionally changes architecture, update the module's
`*ArchitectureTest` and this document in the same change. Do not weaken a rule
only to make unrelated code pass; either move the code to the correct package or
record the architectural decision in `docs/adr`.
