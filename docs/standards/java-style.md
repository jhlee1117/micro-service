# Java Style

## Reference

Primary reference:

- Google Java Style Guide

This document defines project-specific decisions and exceptions.
When this document does not define a rule, follow Google Java Style.

Formatting rules are enforced by the configured formatter and Checkstyle.

## Naming

- Class/interface/record: UpperCamelCase
- Method/variable: lowerCamelCase
- Constant: UPPER_SNAKE_CASE
- Package names: lowercase

Prefer domain-specific names.

Avoid vague names such as:

- data
- info
- temp
- obj
- helper
- manager

unless the name accurately represents the domain concept.

## Method design

A method should perform one coherent operation.

Prefer:
- guard clauses
- early return
- intention-revealing names

Avoid:
- deep nesting
- boolean arguments controlling unrelated behavior
- excessive side effects

Do not enforce arbitrary method line limits.

## Null handling

- Do not return null collections.
- Prefer empty collections.
- Prefer Optional for meaningful optional return values.
- Do not use Optional as JPA entity fields by default.

## Comments

Comments should explain:
- WHY
- constraints
- non-obvious design decisions

Do not repeat what the code already says.
