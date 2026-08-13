# YINILOW2 Handoff

## Project Summary

YINILOW2 is the storefront prototype for YINILOW, a two-sided marketplace experience with:

- Clothing & Accessories
- Home & Electronics

The current build is focused on customer-facing discovery and shopping flows. It uses the supplied YINILOW image references as visual direction: warm white/off-white surfaces, black typography, yellow accents, compact commerce cards, bold condensed fashion headings, and cleaner home/electronics layouts.

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

## Key Files

- `src/App.jsx`: main app state, routes, product data, and screen components
- `src/styles.css`: full UI styling and responsive rules
- `src/assets/`: extracted/copy reference and product imagery
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

## Next Product Work

1. Replace hardcoded product arrays with API data.
2. Build checkout and order confirmation.
3. Build saved items / My Pile.
4. Add real quiz state to Find My Match.
5. Add realtime Stock Drop item claiming over WebSockets.
6. Start seller/admin workflows: stock upload, item review, drop creation, fulfillment.
7. Continue visual matching against the supplied reference screens.

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
