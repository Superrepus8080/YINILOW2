# YINILOW2 Handoff

## Project Summary

YINILOW2 is now the workspace for the YINILOW Clothing OS platform build.

The first production release is Clothing OS only. Home, Living, and Electronics are parked for a later phase. Earlier Home/Electronics UI remains as concept work, not launch scope.

The current frontend build is focused on customer-facing discovery and shopping flows. The new backend foundation starts the real production spine: public catalog, public privacy filtering, cart, Add to Bag holds, checkout quote, and PostgreSQL schema constraints.

## Built So Far

- React/Vite frontend scaffold
- Unified header with world switcher, search, Ghana location, saved/account/cart actions
- Clothing homepage
- Home & Electronics homepage
- Category browsing screens
- Product detail screen
- Clothing `Dig the Pile` experience
- Clothing `Stock Drop` live-shopping experience
- Home `Find My Match` quiz/recommendation experience
- Mobile responsive checks for the new signature pages
- Vert.x backend foundation in `backend/`
- PostgreSQL migration skeleton in `backend/src/main/resources/db/migration/`
- Unit tests for one-off hold conflict, Add to Bag idempotency, and public catalog privacy

## Key Files

- `src/App.jsx`: main app state, routes, product data, and screen components
- `src/styles.css`: full UI styling and responsive rules
- `src/assets/`: extracted/copy reference and product imagery
- `backend/`: Vert.x API foundation
- `backend/src/main/resources/db/migration/V001__core_clothing_os.sql`: first PostgreSQL schema migration
- `docs/PLATFORM_REVIEW.md`: review of the Clothing OS handoff pack and corrected build direction
- `docs/UI_UX_PRECISION_GUIDE.md`: page-level UI/UX precision guide for Clothing OS screens
- `README.md`: setup and project overview
- `design-qa.md`: design QA notes from earlier prototype work

## Local Commands

Install dependencies:

```bash
npm install
```

Run development server:

```bash
npm run dev
```

Build:

```bash
npm run build
```

Preview build:

```bash
npm run preview
```

Backend tests:

```bash
cd backend
mvn test
```

## Current Frontend Behavior

- The world switcher changes between Clothing and Home.
- Clothing navigation opens:
  - `Dig the Pile`
  - `Stock Drop`
  - browse/category views
- Home navigation opens:
  - `Find My Match`
  - browse/category views
- Product cards open product detail pages.
- Several controls are visual-only placeholders for now, including filters, compare, alerts, checkout, and quiz progression.

## Recommended Backend Plan

Use Vert.x with PostgreSQL.

Suggested modules:

- Auth/session module
- Product/catalog module
- Seller module
- Cart/grab bag module
- Stock Drop module
- Order/checkout module
- WebSocket gateway

Suggested realtime events:

- `stock_drop.started`
- `stock_drop.countdown_updated`
- `stock_drop.item_reserved`
- `stock_drop.item_released`
- `stock_drop.item_sold`
- `cart.updated`
- `saved_item.updated`
- `inventory.updated`

Suggested PostgreSQL tables:

- `users`
- `products`
- `product_media`
- `categories`
- `sellers`
- `saved_items`
- `carts`
- `cart_items`
- `stock_drops`
- `stock_drop_items`
- `orders`
- `order_items`

Critical first database constraints:

- one active listing per physical item unit
- one active hold per physical item unit
- unique payment idempotency key per actor
- unique provider reference for payment callbacks
- one open acceptance window per order

## Next Product Work

1. Replace hardcoded product arrays with API data.
2. Build checkout and order confirmation.
3. Build saved items / My Pile.
4. Add real quiz state to Find My Match.
5. Add realtime Stock Drop item claiming over WebSockets.
6. Start seller/admin workflows: stock upload, item review, drop creation, fulfillment.
7. Continue visual matching against the supplied reference screens.

Use `docs/UI_UX_PRECISION_GUIDE.md` before implementing or reviewing each screen. It defines the attention targets, subtleties, edge cases, mobile rules, and hard visual QA failures.

## Verification Already Done

- `npm run build` passes.
- Browser click-through checked:
  - homepage
  - Dig the Pile
  - product detail
  - Stock Drop
  - Home & Electronics
  - Find My Match
- Desktop horizontal overflow check passed.
- Mobile overflow was found on Find My Match and fixed.
