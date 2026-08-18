# Clean Code Principles

## Priority

When trade-offs exist:

1. Correctness
2. Security
3. Readability
4. Maintainability
5. Testability
6. Performance when relevant
7. Cleverness

## KISS

Prefer the simplest implementation satisfying the current requirement.

Do not introduce abstractions for hypothetical future requirements.

## YAGNI

Do not build extension points without a real use case.

## DRY

Remove duplicated knowledge, not every duplicated line.

Two similar blocks do not automatically justify a common abstraction.

## SOLID

SOLID is guidance, not a requirement to maximize interfaces/classes.

Do not create:
- interfaces
- factories
- strategies
- abstract base classes

without a concrete design reason.

## Refactoring

Avoid unrelated refactoring during feature development.

Large structural refactoring requires a separate Plan.
