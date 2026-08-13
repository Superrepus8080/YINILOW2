# YINILOW Clothing OS

Page-Level UI/UX Precision Guide

Version: 0.1  
Date: 2026-08-13  
Scope: Clothing commerce only. Home, living, and electronics are parked.

## 1. Purpose

Use this guide to make the UI match the screenshots closely.

This file gives page-level subtleties, edge cases, attention rules, and specialized design parameters.

It is for frontend agents, product designers, UI reviewers, and QA agents.

## 2. Global visual rules

### 2.1 The YINILOW feeling

The UI must feel like a high-trust street-market operating system.

It must not feel like a generic Shopify theme.

The product should feel:

- bold;
- local;
- direct;
- scarce;
- playful where discovery happens;
- controlled where money and stock happen.

### 2.2 Attention hierarchy

Every customer page must answer three questions fast.

1. What is this item or action?
2. Why should I trust it?
3. What do I do next?

For product pages, the first eye path should be:

image -> title -> price -> scarcity/quantity -> condition -> Add to Bag.

For GRAB pages, the first eye path should be:

drop status -> stock value -> profit or payout -> risk state -> action.

For staff pages, the first eye path should be:

work item -> status -> blocker -> next required action.

### 2.3 Brand primitives

Use:

- cream page background;
- black or near-black type;
- yellow for primary highlight and active state;
- red for price and urgent state;
- green only for confirmed or safe states;
- cards with soft radius;
- strong thin borders;
- large condensed headings;
- monospaced or utility style for small metadata where appropriate.

Avoid:

- blue primary buttons;
- corporate gradients;
- weak gray text on cream;
- random icon styles;
- heavy shadows that make the UI feel like SaaS template only.

### 2.4 Page rhythm

The screenshots use wide breathing space.

Keep:

- generous page margins;
- clear section blocks;
- strong title areas;
- card grids that do not feel cramped;
- consistent vertical rhythm.

Do not:

- overfill the page with too many small controls;
- make product cards tall and thin;
- hide primary action below noise;
- use dense admin spacing on customer pages.

### 2.5 Photo handling

Product photography carries trust.

Each image slot must:

- preserve clothing shape;
- avoid cropping critical product edges;
- use a clean background;
- show defects where needed;
- avoid stretching;
- use object-fit rules per product type.

For clothing:

- tops need full body of garment visible;
- shoes need side profile and sole where needed;
- bags need strap and inside where needed;
- defect images need close crop and label.

## 3. Homepage and clothing landing

Source examples:

- `yinilow_marketplace_homepage_ui_design.png`
- `modern_e_commerce_fashion_landing_page.png`
- `modern_fashion_marketplace_homepage_design.png`

### 3.1 Attention goal

The homepage must make the buyer feel:

- this is clothing first;
- items are curated;
- YINILOW is trusted;
- Dig the Pile is the special thing.

First attention target:

Hero headline and people/product image.

Second attention target:

Shop Now and Dig the Pile CTAs.

Third attention target:

Featured products and trust strip.

### 3.2 Subtleties

- The hero must be wide and confident.
- The product cards below must feel like a curated rack.
- Trust messages should be short.
- Do not make the trust strip taller than the product area.
- The Dig the Pile card must look different enough to feel like an invitation.
- The cart badge must be visible but not loud.
- The location selector should feel Ghana-specific.

### 3.3 Edge cases

| Case | UI behavior |
| --- | --- |
| no featured products | Show curated empty state and keep Dig the Pile CTA. |
| image missing | Use approved fallback image style, not blank gray box. |
| location unavailable | Keep browsing active and ask location at checkout. |
| user logged out | Allow browse and save prompt where needed. |
| Dig the Pile disabled | Hide CTA or show "Coming soon" based on admin flag. |

### 3.4 QA checks

- The page must not promote Home and Electronics.
- The hero must not crop people or clothing awkwardly.
- Product cards must be broad enough to show garment shape.
- The first scroll must show both trust and products.

## 4. Navigation and search

Source example:

- `clean_ux_design_for_unified_marketplace.png`

### 4.1 Attention goal

The user must understand where they are.

For this phase, Clothing must be active.

Home and Electronics can exist as a parked future concept only when necessary.

### 4.2 Subtleties

- Active tab must use yellow.
- Search placeholder must match the active world.
- Desktop nav can show full categories.
- Mobile nav must use fewer labels and more icons.
- Location must not compete with search.

### 4.3 Edge cases

| Case | UI behavior |
| --- | --- |
| user searches electronics term | Return no clothing result and do not open parked category. |
| small mobile width | Collapse secondary nav into horizontal scroll or menu. |
| cart has expiring hold | Cart icon can show small urgent timer state. |
| account not logged in | Account opens sign-in sheet. |

### 4.4 QA checks

- Search bar aligns with header.
- Icon stroke weights match.
- Active category underline or pill is clear.
- Cart count does not overlap icon.

## 5. Product grid and category pages

### 5.1 Attention goal

The grid must feel fast and shoppable.

The user should compare items quickly.

First attention target:

product image.

Second attention target:

price and scarcity.

Third attention target:

condition and size.

### 5.2 Subtleties

- Product cards must be wide enough to show the item shape.
- Price must be stronger than secondary metadata.
- One-off items need a scarcity marker.
- Store reject and new clothing need quantity or variant cue.
- Save heart must be easy to hit but not dominate.
- Quick View must not clutter every card if the grid feels busy.

### 5.3 Specialized parameters

Recommended card ratios:

- desktop card image area: 4:3 or near square;
- mobile product card: image first, text compact;
- minimum card width on desktop: enough to avoid tall thin cards;
- use masonry only if product photography supports it.

### 5.4 Edge cases

| Case | UI behavior |
| --- | --- |
| one-off item sold while grid is open | Fade card and show Sold. |
| item held by another user | Show "Held" or "Someone has this in bag." |
| item saved then sold | Keep saved state but show Sold. |
| store reject has only one size left | Show size scarcity. |
| filter returns no result | Offer clear filter reset and Lucky Pull. |

### 5.5 QA checks

- No card should look stretched vertically.
- Text must not wrap into ugly three-line product names.
- Product image background must feel consistent.
- Sold and held states must be visually different.

## 6. Product detail

Source example:

- `modern_e_commerce_ui_with_vintage_polo.png`

### 6.1 Attention goal

The product page must close trust.

The buyer must see:

- the garment clearly;
- the price;
- scarcity or quantity;
- condition;
- fit;
- proof;
- Add to Bag.

### 6.2 Subtleties

- The main product image must be dominant.
- Thumbnail rail must not steal attention.
- Price in red should feel intentional.
- "Only 1 available" must sit close to the price.
- Condition and size must be visible before CTA.
- YINILOW Verified card must replace public seller identity.
- Video must feel like proof, not entertainment.
- Add to Bag must be the strongest action.
- Add to Pile can be secondary.

### 6.3 Specialized parameters

For one-off thrift:

- show exact condition;
- show exact measurements;
- show public defects;
- show no quantity stepper.

For store reject:

- show variant selector;
- show remaining size stock where useful;
- show return policy type.

For new clothing:

- show normal size and quantity controls.

### 6.4 Edge cases

| Case | UI behavior |
| --- | --- |
| item is held by another user | Disable Add to Bag and offer Save. |
| hold expires after user opens page | Update CTA state live. |
| video missing for premium item | Hide public listing or show staff blocker before publish. |
| measurement missing | Show "measurement pending" only in staff view, not public listing. |
| defect exists | Show defect image and short defect note. |

### 6.5 QA checks

- Public page must not show reseller or vendor identity.
- Image gallery must not crop the garment.
- Add to Bag must stay visible on mobile.
- Fit and condition must be easy to find.

## 7. Dig the Pile

Source example:

- `dig_the_pile_fashion_marketplace_ui.png`

### 7.1 Attention goal

The pile must create discovery tension.

The user should feel:

- there are hidden gems;
- other people are digging;
- I can find something before someone else does.

First attention target:

large pile canvas.

Second attention target:

selected item and quick-view card.

Third attention target:

live social pressure.

### 7.2 Subtleties

- The canvas should feel physical.
- Item overlap should look intentional, not broken.
- Selected item must pop above the pile.
- Price tags must stay readable.
- Shuffle must feel playful.
- Filter must feel controlled.
- Claim toasts must be visible but not disruptive.
- Live cursors must be subtle.
- The page must still feel premium, not like a game board only.

### 7.3 Specialized design parameters

Pile rendering:

- cap visible pile items per scene;
- keep selected item above all;
- prevent price labels from going off-canvas;
- use random rotation within safe bounds;
- preserve image aspect ratio;
- avoid full overlap that hides all items;
- keep CTA card fixed or easy to reach.

Live layer:

- WebRTC cursor movement should be smooth;
- cursor names should be masked or first-name only;
- presence dots should fade when inactive;
- claim toast should be server-confirmed.

### 7.4 Edge cases

| Case | UI behavior |
| --- | --- |
| WebRTC blocked | Use WebSocket count and claim toast. |
| all live systems fail | Static pile still works. |
| selected item is claimed | Show "Just claimed" and fade. |
| user uses keyboard | Provide list view and focus controls. |
| low-power mobile | Reduce animation and item count. |
| store reject in pile | Show "multiple available" or size choice before hold. |

### 7.5 QA checks

- Drag does not move item outside reachable area.
- Price tags are readable.
- Quick View does not cover the whole pile on desktop.
- Reduced-motion mode works.
- Add to Bag always calls the server.

## 8. Quick View

### 8.1 Attention goal

Quick View should help a user decide without leaving the current flow.

### 8.2 Subtleties

- Keep it compact.
- Show only decision-critical info.
- Use image, title, price, condition, size, and action.
- Do not include long description.
- CTA must be clear.

### 8.3 Edge cases

| Case | UI behavior |
| --- | --- |
| item becomes unavailable | Replace CTA with Save or Find similar. |
| user is in Dig the Pile | Do not lose pile position. |
| mobile viewport | Use bottom sheet. |
| missing image | Use fallback and keep text usable. |

### 8.4 QA checks

- Quick View opens fast.
- Dismiss action is clear.
- Focus returns to source item after close.

## 9. Lucky Pull

Source example:

- `lucky_pull_shopping_experience_interface.png`

### 9.1 Attention goal

Lucky Pull must feel like guided discovery.

It must not feel like gambling.

First attention target:

Pull result.

Second attention target:

why the system chose it.

Third attention target:

Claim, Save, or Pull Again.

### 9.2 Subtleties

- The "You got a hit" moment should feel rewarding.
- The result card must feel like a real product page preview.
- Reason chips must build trust.
- Pull history should make the feature feel personal.
- The pull form should not look like a boring filter form.
- Pull Again must be secondary to Claim.

### 9.3 Copy rules

Use:

- "Pull for me"
- "We found this for you"
- "Why we chose this"
- "Pull again"

Avoid:

- jackpot;
- odds;
- spin;
- bet;
- win cash;
- gamble-like language.

### 9.4 Edge cases

| Case | UI behavior |
| --- | --- |
| no match found | Ask user to loosen size, price, or condition. |
| result sold before claim | Show unavailable and offer Pull Again. |
| user pulls too fast | Show cooldown. |
| user has no recommendation consent | Use current form only. |
| size missing | Require size before Pull. |

### 9.5 QA checks

- Pull does not create hold.
- Claim creates hold.
- Result explanation is visible.
- Mobile result does not push all CTAs below the fold.

## 10. Saved Items / My Pile

Source example:

- `saved_items_and_weekend_pile_organizer.png`

### 10.1 Attention goal

This page must feel like a personal wardrobe board, not only a wishlist.

First attention target:

My Pile canvas.

Second attention target:

saved item grid.

Third attention target:

Start Lucky Pull from taste.

### 10.2 Subtleties

- The personal pile should feel arranged by the user.
- Price tags should stay visible.
- Remove controls must be small.
- Saved grid must still be efficient.
- "Start Lucky Pull" should feel like an upgrade from saved taste.
- Recently viewed should be secondary.

### 10.3 Recommendation parameters

Capture:

- saved item;
- unsaved item;
- pile created;
- item added to pile;
- item removed from pile;
- item moved near another item;
- saved item moved to bag;
- pile used for Lucky Pull.

### 10.4 Edge cases

| Case | UI behavior |
| --- | --- |
| saved item sold | Keep item visible with Sold state. |
| pile item unavailable | Show disabled tag and replacement option. |
| user has no saved items | Prompt Dig the Pile and Lucky Pull. |
| recommendation consent off | Do not say "training." Say "personalization off." |
| mobile | Use saved list first, pile as scrollable canvas or card stack. |

### 10.5 QA checks

- Saved page does not look like a generic wishlist.
- My Pile can be used without drag on mobile.
- Sold saved items do not disappear unexpectedly.

## 11. Bag

### 11.1 Attention goal

The bag must create urgency without panic.

First attention target:

items and hold timers.

Second attention target:

subtotal and checkout CTA.

Third attention target:

recovery action for expired holds.

### 11.2 Subtleties

- Hold timer must be close to the item.
- Expiring item must be visible.
- Remove and save for later must not compete with checkout.
- Multi-unit item can use quantity stepper.
- One-off item must not use quantity stepper.

### 11.3 Edge cases

| Case | UI behavior |
| --- | --- |
| one item expired | Keep other items active. |
| all items expired | Show recovery page. |
| fee changes | Show recalculating state. |
| item sold after hold release | Show Sold and Save option. |
| guest user | Continue checkout but ask for phone at payment or delivery. |

### 11.4 QA checks

- Checkout cannot continue with expired one-off hold.
- Timer uses server time.
- Product image and size are visible.

## 12. Checkout

Source examples:

- `ecommerce_checkout_with_delivery_options.png`
- `yinilow_checkout_page_interface_overview.png`

### 12.1 Attention goal

Checkout must feel safe and controlled.

First attention target:

current step and order summary.

Second attention target:

delivery and payment choice.

Third attention target:

Continue or Place Order CTA.

### 12.2 Subtleties

- Stepper must reduce anxiety.
- Payment method cards must feel trustworthy.
- MoMo should feel locally natural.
- COD must be visible only when eligible.
- The order summary must stay readable.
- Service fees must not feel hidden.
- Hold expiry must remain visible.

### 12.3 Edge cases

| Case | UI behavior |
| --- | --- |
| hold expires during checkout | Block payment and show recovery. |
| MoMo pending | Show pending state, not failure. |
| provider returns unclear state | Show review required. |
| COD unavailable | Show reason and alternative payment. |
| delivery address outside zone | Disable unsupported delivery methods. |

### 12.4 QA checks

- Payment CTA cannot be tapped twice without idempotency.
- Totals are stable after quote.
- User sees clear path when payment is pending.

## 13. Order confirmation

Source example:

- `order_confirmation_page_design_screenshot.png`

### 13.1 Attention goal

Confirmation must close the loop.

The user must know:

- order is secured;
- payment state;
- what happens next;
- how to track.

### 13.2 Subtleties

- Use strong success symbol only when payment is confirmed.
- Payment pending needs a different visual state.
- Item cards should reassure the user.
- Next-step cards should be clear.
- Receipt action should not dominate.

### 13.3 Edge cases

| Case | UI behavior |
| --- | --- |
| payment pending | Show pending, not success. |
| COD order | Show order placed and payment on delivery. |
| partial issue | Show affected item state. |
| stock acquisition flow | Show next handling options where relevant. |

### 13.4 QA checks

- Order reference is easy to copy.
- Total paid matches checkout.
- Track Order CTA is visible.

## 14. Track order

Source example:

- `track_your_order_dashboard.png`

### 14.1 Attention goal

Order tracking must tell the customer what is happening now.

First attention target:

selected order status.

Second attention target:

timeline.

Third attention target:

Inspect and Accept or Report Issue.

### 14.2 Subtleties

- Timeline must use clear completed, current, and future states.
- Product image must remind customer what is coming.
- Rider details must not crowd the product details.
- Inspect and Accept must become strong only after delivery.
- Report Issue must be visible but not alarming.

### 14.3 Edge cases

| Case | UI behavior |
| --- | --- |
| delivery delayed | Show delayed state and support option. |
| delivered | Start 24-hour acceptance timer. |
| acceptance window expired | Show auto-accepted and after-service support. |
| issue reported | Replace accept CTA with case status. |
| WebSocket disconnected | Show last updated time. |

### 14.4 QA checks

- Acceptance timer is visible after delivery.
- User can report issue from order detail.
- Timeline updates without full page reload when socket works.

## 15. Support and disputes

Source examples:

- `support_center_dashboard_interface.png`
- `yinilow_exception_and_dispute_dashboard.png`

### 15.1 Attention goal

Support must feel fair and evidence-based.

The customer must not feel abandoned after delivery.

### 15.2 Subtleties

- Issue form must be calm.
- Do not blame the customer.
- Show disclosed defect evidence when relevant.
- Use clear states for under review, approved, and rejected.
- Staff view can be dense.
- Customer view must be simple.

### 15.3 Edge cases

| Case | UI behavior |
| --- | --- |
| issue after acceptance | Open after-service case, not normal return. |
| missing photo evidence | Ask for photo before submit where needed. |
| dispute freezes payout | Show only to reseller and staff. |
| wrong item | Prioritize quick support action. |

### 15.4 QA checks

- Customer cannot see internal payout freeze unless appropriate.
- Staff can compare complaint to disclosed defects.
- Case status is clear.

## 16. Notifications center

Source example:

- `clean_and_modern_notifications_dashboard.png`

### 16.1 Attention goal

Notifications must separate urgent work from normal updates.

### 16.2 Subtleties

- Action-needed items need stronger badge.
- Sold and payout events need clear status color.
- Settings must not crowd the notification list.
- Quiet hours should feel respectful.
- WhatsApp and SMS should feel Ghana-relevant.

### 16.3 Edge cases

| Case | UI behavior |
| --- | --- |
| reservation expiring | Show urgent action. |
| payout available | Route to payout page. |
| support replied | Route to case. |
| quiet hours active | Show muted state. |

### 16.4 QA checks

- Important notices are not buried.
- Mark all read does not remove action-needed state.

## 17. GRAB signup

Source example:

- `yinilow_signup_verification_flow_design.png`

### 17.1 Attention goal

The page must make GRAB feel like a serious earning program.

First attention target:

Join YINILOW GRAB value proposition.

Second attention target:

verification steps.

Third attention target:

submit for verification.

### 17.2 Subtleties

- The left side should sell the opportunity.
- The right side must feel compliant and secure.
- 18+ requirement must be clear.
- ID and selfie upload must not feel optional.
- Payout details must feel safe.

### 17.3 Edge cases

| Case | UI behavior |
| --- | --- |
| phone code failed | Show retry and cooldown. |
| ID rejected | Show reason and re-upload. |
| selfie failed | Allow retake. |
| under 18 | Block program access. |
| payout method missing | Allow verification but block payout if policy permits. |

### 17.4 QA checks

- User cannot enter Stock Drop before verification.
- Upload errors are clear.
- Terms acceptance is required.

## 18. Reseller dashboard

Source examples:

- `yinilow_reseller_dashboard_overview.png`
- `yinilow_reseller_stock_management_dashboard.png`
- `earnings_and_payouts_dashboard_overview.png`

### 18.1 Attention goal

The reseller must know:

- what stock they control;
- what needs action;
- what has sold;
- what can be paid out.

### 18.2 Subtleties

- Use numbers for quick confidence.
- Separate earned from available.
- Do not hide fee.
- Action-needed states must rise to top.
- Stock cards must show listing progress.

### 18.3 Edge cases

| Case | UI behavior |
| --- | --- |
| stock pending media | Show YINILOW processing. |
| price approval needed | Show clear CTA. |
| payout frozen | Show reason without exposing customer private details. |
| item disputed | Show support status. |
| no stock | Route to Stock Drop. |

### 18.4 QA checks

- Reseller dashboard is role-gated.
- Earnings and payout are not confused.
- Buyer cannot see reseller dashboard data.

## 19. Stock Drop

Source examples:

- `stock_drop_e_commerce_dashboard_design.png`
- `stock_drop_e_commerce_dashboard_layout.png`
- `live_event_control_dashboard_screenshot.png`

### 19.1 Attention goal

Stock Drop must create controlled urgency.

First attention target:

live drop status.

Second attention target:

quantity and price tier.

Third attention target:

secure stock CTA.

### 19.2 Subtleties

- Progress bars must feel alive.
- "X eyeing" can build urgency.
- Profit projection must be clear but not exaggerated.
- Tier pricing must not confuse first-time resellers.
- Secure Stock must be strong.

### 19.3 Edge cases

| Case | UI behavior |
| --- | --- |
| drop not started | Show countdown. |
| drop sold out | Disable secure action. |
| reseller unverified | Show locked state. |
| rate limited | Show wait time. |
| stock secured by another reseller | Update live and show unavailable. |

### 19.4 QA checks

- Secure Stock cannot be triggered twice accidentally.
- Live progress updates without leaking other reseller identity.
- Projected profit is labeled as projection.

## 20. Intake appointment and batch

Source example:

- `saas_appointment_scheduling_dashboard_ui.png`

### 20.1 Attention goal

Staff must see the batch, vendor, count, time, and next action immediately.

### 20.2 Subtleties

- Staff UI can be dense but must stay scannable.
- Status pills must be clear.
- Required documents must be obvious.
- Receiving bay must be visible.
- Calendar and time slots must not dominate the operational summary.

### 20.3 Edge cases

| Case | UI behavior |
| --- | --- |
| vendor late | Show appointment risk state. |
| expected count differs | Create discrepancy. |
| missing document | Block intake completion. |
| wrong receiving bay | Allow reassignment with audit. |

### 20.4 QA checks

- Batch ID is visible.
- Expected count and received count are visible.
- Staff can find next action fast.

## 21. Guided item intake and grading

Source examples:

- `clothing_intake_tool_ui_dashboard.png`
- `item_intake_and_condition_grading_dashboard.png`
- `product_grading_dashboard_interface.png`

### 21.1 Attention goal

Staff must make the right condition decision.

### 21.2 Subtleties

- Public condition and internal score must be visually separate.
- Defect capture should not be buried.
- Evidence upload must sit near the defect checklist.
- Authenticity concern must feel serious.
- Override reason must appear only when needed.

### 21.3 Edge cases

| Case | UI behavior |
| --- | --- |
| public label conflicts with defects | Require override reason. |
| missing evidence | Block approval. |
| authenticity concern | Move item to review. |
| cleanliness failed | Block listing or require cleaning state. |

### 21.4 QA checks

- Staff cannot approve incomplete grade.
- Public label remains simple.
- Internal evidence stays private.

## 22. Custody and handover

Source examples:

- `admin_dashboard_for_item_custody_intake.png`
- `custody_assignment_dashboard_interface.png`
- `custody_transfer_dashboard_overview.png`

### 22.1 Attention goal

Staff must know where the physical item is.

### 22.2 Subtleties

- Location must be more prominent than notes.
- Transfer CTA must require confirmation.
- Handover proof must be visible.
- Missing or damaged state must be visually urgent.

### 22.3 Edge cases

| Case | UI behavior |
| --- | --- |
| item missing | Block fulfillment and listing. |
| item damaged after approval | Send back to grading. |
| custody transfer incomplete | Keep old holder until confirmed. |
| wrong item scanned | Show mismatch state. |

### 22.4 QA checks

- Listing cannot publish without custody.
- Fulfillment cannot ship unknown location.
- Transfer creates audit event.

## 23. Photography and measurement

Source examples:

- `photography_studio_dashboard_interface.png`
- `photography_measurement_workflow_dashboard.png`
- `inspection_video_capture_dashboard_interface.png`
- `measurement_evidence_studio_dashboard_overview.png`

### 23.1 Attention goal

Staff must capture enough evidence to sell and defend the item.

### 23.2 Subtleties

- The required media checklist must be visible.
- The image slots must guide the photographer.
- Measurement fields must show unit.
- Premium video requirement must be obvious.
- Quality alerts must not look like optional tips.

### 23.3 Edge cases

| Case | UI behavior |
| --- | --- |
| color mismatch | Require color check note. |
| defect item has no defect image | Block approval. |
| premium item lacks video | Block listing when flag is on. |
| measurement confidence low | Require remeasure or note. |

### 23.4 QA checks

- Product page cannot show unapproved media.
- Measurement table uses approved values.
- Defect images are connected to defect notes.

## 24. Pricing and listing approval

Source examples:

- `saas_pricing_and_product_management_dashboard.png`
- `admin_dashboard_for_marketplace_review_system.png`

### 24.1 Attention goal

Staff must see if the listing is ready to publish.

### 24.2 Subtleties

- The checklist should drive the page.
- Price suggestion should not look like final price until approved.
- Fee and reseller take must be clear in staff/reseller views.
- Publish button must stay disabled until blockers clear.
- Reject reason must be structured.

### 24.3 Edge cases

| Case | UI behavior |
| --- | --- |
| reseller approval missing | Show blocker. |
| price below minimum | Block publish. |
| media not approved | Show blocker. |
| custody missing | Show blocker. |
| private economics in public preview | Fail QA. |

### 24.4 QA checks

- Customer preview hides internal economics.
- Staff view shows blockers clearly.
- Publish action creates clear success state.

## 25. Admin controls

### 25.1 Attention goal

Admin must be able to stop risk quickly.

### 25.2 Subtleties

- Dangerous controls need confirmation.
- Feature flags need current status.
- Hold time changes must explain scope.
- Payout freeze must require reason.
- Expire holds must allow cohort targeting.

### 25.3 Edge cases

| Case | UI behavior |
| --- | --- |
| live pile bug | Disable live layer only. |
| payment issue | Pause payment method or put review mode. |
| COD abuse | Pause COD by zone. |
| reseller fraud risk | Suspend reseller and freeze payout. |
| inventory bug | Hide affected listings. |

### 25.4 QA checks

- Admin controls are role-gated.
- Dangerous actions create audit events.
- Disabling optional features does not break core checkout.

## 26. Mobile-specific precision

### 26.1 Key rules

- Keep primary CTA reachable.
- Do not allow horizontal overflow.
- Use bottom sheets for filters and quick view.
- Keep hold timer visible in bag and checkout.
- Use list fallback for Dig the Pile.
- Reduce item count in live pile.
- Use sticky checkout CTA where useful.

### 26.2 Mobile pitfalls

| Pitfall | Fix |
| --- | --- |
| hero too tall | Shorten hero and show CTA early. |
| product cards too narrow | Use one or two columns based on width. |
| pile impossible to drag | Add list view and tap-first interaction. |
| checkout summary hidden | Use collapsible sticky summary. |
| support form too long | Use steps or sections. |

## 27. Final visual QA checklist

Before handoff, compare each coded screen against the matching screenshot.

Check:

- heading size;
- spacing;
- card radius;
- border weight;
- button style;
- yellow active state;
- red price style;
- icon style;
- image crop;
- product card width;
- mobile layout;
- empty states;
- error states;
- disabled states;
- loading states;
- motion state;
- reduced-motion state.

Hard failures:

- public UI leaks reseller or vendor identity;
- product cards become thin and ugly;
- Add to Bag does not show hold state;
- Dig the Pile blocks shopping when live layer fails;
- checkout hides payment ambiguity;
- staff can publish incomplete listing;
- mobile has horizontal overflow.

## 28. Agent instruction

When you implement a screen:

1. Open the source screenshot.
2. Identify the first, second, and third attention targets.
3. Build the layout.
4. Add all states.
5. Add responsive behavior.
6. Add privacy checks.
7. Compare against screenshot.
8. Fix visible mismatch before moving to the next screen.
