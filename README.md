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

## Product Docs

- `docs/PLATFORM_REVIEW.md`: corrected Clothing OS production scope and build order
- `docs/UI_UX_PRECISION_GUIDE.md`: page-level visual, interaction, edge-case, mobile, and QA rules for Clothing OS screens

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
- Realtime: WebSocket signaling plus WebRTC DataChannel for safe telemetry and live feel; trusted business writes stay on Vert.x APIs and PostgreSQL records

See `docs/REALTIME_DATACHANNEL_ARCHITECTURE.md` for the DataChannel safety rules and implementation shape.

Backend checks:

```bash
cd backend
mvn test
```

Run the API-backed storefront locally:

```bash
cd backend
mvn package -DskipTests
java -jar target/yinilow-clothing-os-api-0.1.0-SNAPSHOT.jar
```

In another terminal:

```bash
npm run dev
```

Then open `http://127.0.0.1:5173/`. The Clothing catalog will load from the Vert.x API through the Vite proxy, and Add to Bag will create a backend hold.
