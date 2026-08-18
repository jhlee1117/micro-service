# Spring Conventions

## References

Primary references:

- Spring Framework Reference
- Spring Boot Reference

Project rules in this document take precedence over generic examples.

## Dependency Injection

Prefer constructor injection.

Do not use field injection for production code.

## Controller

Controllers are HTTP adapters.

Allowed:
- request validation
- DTO conversion
- application/service invocation
- response construction

Forbidden:
- repository access
- core business logic
- transaction orchestration

## Service

Services represent application use cases or meaningful business operations.

Do not create a Service/ServiceImpl pair automatically.

Interfaces require a concrete architectural reason.

## Repository

Repositories handle persistence concerns.

Business policy must not be implemented inside repositories.

## Entity and DTO

Do not expose JPA entities through external APIs.

Separate persistence models from external request/response contracts.

## Transaction

Transaction boundaries belong to application/service use cases.

Do not annotate controllers with @Transactional.

Do not assume a DB transaction covers:
- HTTP calls
- RabbitMQ delivery
- another microservice

## Configuration

Prefer @ConfigurationProperties for related configuration.

Never hardcode:
- passwords
- tokens
- secrets
- production endpoints

## Security

Authentication and authorization decisions remain server-side.

Frontend behavior must never be treated as authorization.
