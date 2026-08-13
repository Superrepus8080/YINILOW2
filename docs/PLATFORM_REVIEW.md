# YINILOW Platform Review

Reviewed source pack:

- `YINILOW_Clothing_OS_Agent_Handoff_Pack.zip`
- Extracted to `C:\Users\comeph & associates\Documents\YINIILOW\clothing-os-handoff-pack`

## Decision

The proper production build is **YINILOW Clothing OS first**.

Home, Living, and Electronics are parked for this phase. The existing prototype can keep those screens as concept work, but production engineering should not let Home/Electronics define the first release scope.

## Product Truth

YINILOW is not a normal online shop. It is a clothing commerce operating system for Ghana, covering:

- one-off thrift and vintage items;
- store rejects with limited unit pools;
- new clothing;
- reseller stock through YINILOW GRAB;
- vendor-supplied stock;
- YINILOW-managed intake, grading, custody, media, pricing, listing, checkout, support, and payout.

The buyer sees one trusted YINILOW marketplace. Buyer-facing APIs and UI must not expose vendor identity, reseller identity, economic owner, payout, margin, custody holder, or internal grade notes.

## Core Invariant

A customer can find an available clothing item, add it to a valid bag, pay once, receive an order, inspect the item, accept it, or report a valid issue.

This must work when optional features are disabled:

- Dig the Pile;
- Lucky Pull;
- Saved Pile training;
- YINILOW GRAB;
- Stock Drop;
- WebRTC;
- Redis cache.

## Build Order

1. Platform users, capabilities, feature flags, audit events.
2. Catalog, item units, public listings, public product DTOs.
3. Inventory holds with PostgreSQL unique active-hold protection.
4. Cart and Add to Bag hold command.
5. Checkout quote and order draft.
6. Payment attempt and idempotent callback.
7. Order confirmation, delivery state, acceptance window.
8. Support case.
9. WebSocket gateway and outbox.
10. Optional experiences: Dig the Pile, Lucky Pull, Saved Items, GRAB, Stock Drop.
11. Internal operations: intake, grading, custody, studio, pricing, listing approval.

## Stop Gates

Do not release if:

- a one-off item can double-sell;
- an expired hold can checkout;
- duplicate payment callback creates a duplicate order;
- public product API leaks reseller or vendor identity;
- payout can release before the sale is safe;
- core checkout fails when Redis is unavailable;
- Dig the Pile failure blocks normal product detail or checkout;
- WebSocket auth allows private subscription;
- staff can publish without custody, condition, media, price, and approval.

## First Engineering Slice

This repo now starts the real platform with a Vert.x backend foundation:

- public catalog endpoints;
- public DTO shape that hides private seller/owner fields;
- cart Add to Bag command;
- in-memory hold service for first tests;
- PostgreSQL migration skeleton with the critical active-hold constraint;
- unit tests for one-off hold conflict and idempotent Add to Bag behavior.

The in-memory service is a stepping stone. The production implementation must move hold truth into PostgreSQL transactions using the migration in `backend/src/main/resources/db/migration/V001__core_clothing_os.sql`.
