# Prototype Instructions

Run the local server yourself and open the preview in the browser available to this environment. Do not give the user server-start instructions when you can run it.

Before making substantial visual changes, use the Product Design plugin's `get-context` skill when the visual source is unclear or no longer matches the current goal. When the user gives durable prototype-specific design feedback, preferences, or decisions, record them in `AGENTS.md`.

When implementing from a selected generated mock, treat that image as the source of truth for layout, component anatomy, density, spacing, color, typography, visible content, and hierarchy.

Build app UI in `src/`. Keep `.openai/hosting.json`, `worker/index.js`, `scripts/prepare-sites-build.mjs`, and `tests/sites-worker.test.mjs` intact so the same local prototype can be handed to Sites. Before a Sites handoff, run `npm run build` and `npm run test:sites`; the build must leave `dist/client/index.html`, `dist/server/index.js`, and `dist/.openai/hosting.json`.

## Standing UI invariants

These are stop gates from `docs/PLATFORM_REVIEW.md` and `docs/UI_UX_PRECISION_GUIDE.md`. Do not regress them.

- **No vendor or reseller identity on public surfaces.** Render seller attribution through `publicTrustLabel()` in `src/App.jsx`, which allowlists YINILOW trust labels and falls back to "YINILOW Verified". Never render a raw `seller`/`sellerTrustLabel` value, even one supplied by the API.
- **Category nav must not advertise an empty rack.** Clothing nav comes from `clothingNavItems(catalog)`, which lists only categories that actually hold stock. Add new categories to `CLOTHING_CATEGORY_ORDER` for ordering, not to a hardcoded nav array.
- **No horizontal overflow at any width.** Header columns must stay shrinkable; check 1280/1440/1600 plus mobile after header or nav changes.
- **Product cards must not nest controls inside a clickable card.** The card title is the accessible open control (`.card-open-btn`) with a stretched overlay; action buttons sit above it via `z-index`.
