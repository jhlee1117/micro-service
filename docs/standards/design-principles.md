# Design Principles

## General

Patterns solve problems.
Patterns are not implementation goals.

Use the simplest design that satisfies current requirements.

## Strategy

Consider Strategy when multiple genuine interchangeable behaviors exist.

Do not use Strategy for one implementation.

## Factory

Use a Factory when object creation contains meaningful selection
or complex construction logic.

Do not create factories for trivial constructors.

## Adapter

Prefer Adapter when integrating external systems whose data model
should not leak into the application/domain model.

## Builder

Use Builder for complex construction with many optional values.

Avoid Builder for trivial DTOs.

## Events

Use events when temporal or ownership decoupling provides real value.

Do not introduce RabbitMQ simply to remove an ordinary method/API call.

## Microservices

A new service needs a meaningful reason such as:

- business ownership boundary
- deployment independence
- scaling requirement
- failure isolation
- clear bounded context

Do not create microservices merely to separate packages.
