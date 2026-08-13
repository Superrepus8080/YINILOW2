# YINILOW Clothing OS API

Vert.x backend foundation for the YINILOW Clothing OS production build.

## Current Slice

- Health endpoint
- Feature flag endpoint
- Public clothing catalog endpoints
- Public DTO filtering that hides private ownership fields
- Cart Add to Bag command
- In-memory hold service for first behavior tests
- PostgreSQL migration skeleton for the production source of truth

## Run

```bash
mvn test
mvn package
java -jar target/yinilow-clothing-os-api-0.1.0-SNAPSHOT.jar
```

Default port: `8080`

## Production Direction

The in-memory catalog and hold service are temporary. The production version must move catalog, holds, cart, checkout, orders, payments, support, GRAB, and staff operations into PostgreSQL-backed services with Redis used only for cache, live presence, timers, rate limits, and background coordination.
