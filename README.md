# YINILOW2

YINILOW2 is a React storefront prototype for the YINILOW marketplace concept.

The product has two customer-facing shopping worlds:

- Clothing & Accessories
- Home & Electronics

## Current Prototype

- Unified YINILOW navigation and world switcher
- Clothing homepage based on the supplied reference image
- Home & Electronics homepage based on the supplied reference image
- Browse/category pages
- Product detail pages
- Dig the Pile experience
- Stock Drop experience
- Find My Match experience for home recommendations
- Responsive layout checks for desktop and mobile

## Run Locally

```bash
npm install
npm run dev
```

Preview a production build:

```bash
npm run build
npm run preview
```

## Stack Direction

- Frontend: React + Vite
- Backend: Vert.x
- Database: PostgreSQL
- Realtime: WebSockets for live drops, cart/grab updates, saved items, and inventory availability

