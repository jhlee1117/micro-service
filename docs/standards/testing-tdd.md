# Testing and TDD

## TDD cycle

For new business behavior:

RED
- Write a failing test describing desired behavior.
- Confirm it fails for the expected reason.

GREEN
- Implement the smallest coherent production change.
- Make the test pass.

REFACTOR
- Improve code without changing behavior.
- Run affected tests again.

## Bug fixing

When practical:

1. reproduce the bug with a failing regression test
2. verify the failure
3. fix the root cause
4. verify the regression test passes

## Structure

Prefer Given / When / Then.

## Unit Tests

Do not load a Spring context when testing plain business logic.

Instantiate the subject directly.

Mock actual collaborators, not simple values.

## Spring Tests

Use the narrowest useful test scope.

Use full @SpringBootTest only when the complete application context is genuinely required.

## Assertions

Assert observable behavior.

Do not test implementation details unnecessarily.

## Definition of Done

A behavior-changing task is incomplete until:
- required tests exist
- relevant tests pass
- tests were not disabled to make the build green
