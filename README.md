# YINILOW2

YINILOW2 is the YINILOW Clothing OS platform workspace.

## Production Scope

The current production scope is **Clothing OS only**.

Home, Living, and Electronics are parked for a later phase. Some concept UI from earlier exploration still exists in the React prototype, but the production build should follow the clothing handoff pack and the platform review in `docs/PLATFORM_REVIEW.md`.

## Current Prototype

- Unified YINILOW navigation and world switcher
- Clothing homepage based on the supplied reference image
- Home & Electronics concept homepage from earlier exploration
- Browse/category pages
- Product detail pages
- Dig the Pile experience
- Stock Drop experience
- Find My Match concept screen from earlier exploration
- Responsive layout checks for desktop and mobile

## Backend Foundation

The `backend/` folder starts the real Vert.x platform spine:

- public catalog endpoints
- public product DTOs that hide private ownership fields
- Add to Bag hold command
- cart quote endpoint
- PostgreSQL migration skeleton
- tests for one-off hold conflict and public API privacy

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

Backend checks:

```bash
cd backend
mvn test
```
