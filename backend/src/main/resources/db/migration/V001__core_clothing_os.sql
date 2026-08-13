CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS platform;
CREATE SCHEMA IF NOT EXISTS catalog;
CREATE SCHEMA IF NOT EXISTS inventory;
CREATE SCHEMA IF NOT EXISTS cart;
CREATE SCHEMA IF NOT EXISTS orders;
CREATE SCHEMA IF NOT EXISTS payments;
CREATE SCHEMA IF NOT EXISTS delivery;
CREATE SCHEMA IF NOT EXISTS support;
CREATE SCHEMA IF NOT EXISTS grab;
CREATE SCHEMA IF NOT EXISTS drops;
CREATE SCHEMA IF NOT EXISTS signals;
CREATE SCHEMA IF NOT EXISTS ops;
CREATE SCHEMA IF NOT EXISTS outbox;

CREATE TABLE platform.users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  phone TEXT UNIQUE,
  email TEXT UNIQUE,
  display_name TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT users_status_check CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE TABLE platform.roles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL UNIQUE
);

CREATE TABLE platform.capabilities (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code TEXT NOT NULL UNIQUE
);

CREATE TABLE platform.user_capabilities (
  user_id UUID NOT NULL REFERENCES platform.users(id),
  capability_id UUID NOT NULL REFERENCES platform.capabilities(id),
  PRIMARY KEY (user_id, capability_id)
);

CREATE TABLE platform.feature_flags (
  key TEXT PRIMARY KEY,
  enabled BOOLEAN NOT NULL DEFAULT false,
  config JSONB NOT NULL DEFAULT '{}'::jsonb,
  updated_by UUID REFERENCES platform.users(id),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE platform.audit_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  actor_id UUID REFERENCES platform.users(id),
  action TEXT NOT NULL,
  object_type TEXT NOT NULL,
  object_id UUID,
  metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_object ON platform.audit_events (object_type, object_id);

CREATE TABLE catalog.categories (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  slug TEXT NOT NULL UNIQUE,
  parent_id UUID REFERENCES catalog.categories(id)
);

CREATE TABLE catalog.item_batches (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  source_type TEXT NOT NULL,
  source_id UUID,
  status TEXT NOT NULL DEFAULT 'DRAFT',
  expected_count INTEGER NOT NULL DEFAULT 0,
  received_count INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE catalog.item_units (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  batch_id UUID REFERENCES catalog.item_batches(id),
  stock_type TEXT NOT NULL,
  unit_code TEXT NOT NULL UNIQUE,
  source_type TEXT NOT NULL,
  economic_owner_type TEXT,
  economic_owner_id UUID,
  status TEXT NOT NULL DEFAULT 'INTAKE',
  size_label TEXT,
  color_label TEXT,
  condition_public TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT item_units_stock_type_check CHECK (stock_type IN ('THRIFT_ONE_OFF', 'STORE_REJECT', 'NEW_CLOTHING')),
  CONSTRAINT item_units_status_check CHECK (status IN ('INTAKE', 'READY', 'LISTED', 'HELD', 'SOLD', 'UNAVAILABLE', 'LOST', 'DAMAGED')),
  CONSTRAINT item_units_condition_check CHECK (condition_public IS NULL OR condition_public IN ('LIKE_NEW', 'VERY_GOOD', 'GOOD', 'FAIR'))
);

CREATE TABLE catalog.product_listings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title TEXT NOT NULL,
  slug TEXT NOT NULL UNIQUE,
  description TEXT NOT NULL DEFAULT '',
  category_id UUID REFERENCES catalog.categories(id),
  stock_type TEXT NOT NULL,
  pile_eligible BOOLEAN NOT NULL DEFAULT false,
  restockable BOOLEAN NOT NULL DEFAULT false,
  return_policy_type TEXT NOT NULL,
  visibility TEXT NOT NULL DEFAULT 'DRAFT',
  public_price NUMERIC(12, 2) NOT NULL,
  currency TEXT NOT NULL DEFAULT 'GHS',
  published_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT product_listings_stock_type_check CHECK (stock_type IN ('THRIFT_ONE_OFF', 'STORE_REJECT', 'NEW_CLOTHING')),
  CONSTRAINT product_listings_visibility_check CHECK (visibility IN ('DRAFT', 'PUBLIC', 'HIDDEN', 'ARCHIVED'))
);

CREATE TABLE catalog.listing_units (
  listing_id UUID NOT NULL REFERENCES catalog.product_listings(id),
  item_unit_id UUID NOT NULL REFERENCES catalog.item_units(id),
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (listing_id, item_unit_id),
  CONSTRAINT listing_units_status_check CHECK (status IN ('ACTIVE', 'REMOVED', 'SOLD'))
);

CREATE UNIQUE INDEX uniq_active_listing_per_item_unit
ON catalog.listing_units (item_unit_id)
WHERE status = 'ACTIVE';

CREATE TABLE catalog.variant_groups (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  listing_id UUID NOT NULL REFERENCES catalog.product_listings(id),
  size_label TEXT,
  color_label TEXT,
  quantity_mode TEXT NOT NULL DEFAULT 'UNIT_POOL'
);

CREATE TABLE catalog.condition_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_unit_id UUID NOT NULL REFERENCES catalog.item_units(id),
  public_label TEXT NOT NULL,
  internal_score INTEGER NOT NULL,
  evidence JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT condition_assessments_public_label_check CHECK (public_label IN ('LIKE_NEW', 'VERY_GOOD', 'GOOD', 'FAIR')),
  CONSTRAINT condition_assessments_score_check CHECK (internal_score BETWEEN 0 AND 100)
);

CREATE TABLE catalog.item_media (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_unit_id UUID NOT NULL REFERENCES catalog.item_units(id),
  media_type TEXT NOT NULL,
  url TEXT NOT NULL,
  purpose TEXT NOT NULL,
  approved BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE catalog.measurements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_unit_id UUID NOT NULL REFERENCES catalog.item_units(id),
  measurement_type TEXT NOT NULL,
  value NUMERIC(10, 2) NOT NULL,
  unit TEXT NOT NULL DEFAULT 'cm'
);

CREATE TABLE cart.carts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES platform.users(id),
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT carts_status_check CHECK (status IN ('ACTIVE', 'CHECKING_OUT', 'ORDERED', 'ABANDONED'))
);

CREATE TABLE inventory.inventory_holds (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_unit_id UUID NOT NULL REFERENCES catalog.item_units(id),
  listing_id UUID NOT NULL REFERENCES catalog.product_listings(id),
  cart_id UUID NOT NULL REFERENCES cart.carts(id),
  user_id UUID REFERENCES platform.users(id),
  hold_source TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT inventory_holds_status_check CHECK (status IN ('ACTIVE', 'EXPIRED', 'RELEASED', 'CONVERTED')),
  CONSTRAINT inventory_holds_source_check CHECK (hold_source IN ('ADD_TO_BAG', 'LUCKY_PULL_CLAIM', 'STAFF'))
);

CREATE UNIQUE INDEX uniq_active_hold_per_item_unit
ON inventory.inventory_holds (item_unit_id)
WHERE status = 'ACTIVE';

CREATE TABLE cart.cart_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  cart_id UUID NOT NULL REFERENCES cart.carts(id),
  listing_id UUID NOT NULL REFERENCES catalog.product_listings(id),
  item_unit_id UUID REFERENCES catalog.item_units(id),
  hold_id UUID REFERENCES inventory.inventory_holds(id),
  quantity INTEGER NOT NULL DEFAULT 1,
  unit_price NUMERIC(12, 2) NOT NULL,
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT cart_items_status_check CHECK (status IN ('ACTIVE', 'REMOVED', 'EXPIRED', 'ORDERED'))
);

CREATE TABLE orders.orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES platform.users(id),
  order_number TEXT NOT NULL UNIQUE,
  status TEXT NOT NULL DEFAULT 'DRAFT',
  subtotal NUMERIC(12, 2) NOT NULL,
  service_fee NUMERIC(12, 2) NOT NULL,
  delivery_fee NUMERIC(12, 2) NOT NULL,
  total NUMERIC(12, 2) NOT NULL,
  payment_status TEXT NOT NULL DEFAULT 'PENDING',
  delivery_status TEXT NOT NULL DEFAULT 'NOT_STARTED',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE orders.order_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES orders.orders(id),
  listing_id UUID NOT NULL REFERENCES catalog.product_listings(id),
  item_unit_id UUID NOT NULL REFERENCES catalog.item_units(id),
  final_price NUMERIC(12, 2) NOT NULL
);

CREATE TABLE payments.payment_attempts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES orders.orders(id),
  provider TEXT NOT NULL,
  provider_reference TEXT,
  amount NUMERIC(12, 2) NOT NULL,
  currency TEXT NOT NULL DEFAULT 'GHS',
  status TEXT NOT NULL DEFAULT 'STARTED',
  actor_id UUID REFERENCES platform.users(id),
  idempotency_key TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT payment_attempts_status_check CHECK (status IN ('STARTED', 'PENDING', 'CONFIRMED', 'FAILED', 'REVIEW_REQUIRED', 'REFUNDED'))
);

CREATE UNIQUE INDEX uniq_payment_idempotency_per_actor
ON payments.payment_attempts (actor_id, idempotency_key);

CREATE UNIQUE INDEX uniq_payment_provider_reference
ON payments.payment_attempts (provider, provider_reference)
WHERE provider_reference IS NOT NULL;

CREATE TABLE payments.provider_callbacks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  provider TEXT NOT NULL,
  provider_reference TEXT,
  payload JSONB NOT NULL,
  received_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE delivery.deliveries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES orders.orders(id),
  method TEXT NOT NULL,
  address JSONB NOT NULL,
  status TEXT NOT NULL DEFAULT 'PENDING',
  proof_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE delivery.delivery_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  delivery_id UUID NOT NULL REFERENCES delivery.deliveries(id),
  status TEXT NOT NULL,
  metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE orders.acceptance_windows (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES orders.orders(id),
  starts_at TIMESTAMPTZ NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  status TEXT NOT NULL DEFAULT 'OPEN',
  CONSTRAINT acceptance_windows_status_check CHECK (status IN ('OPEN', 'ACCEPTED', 'AUTO_ACCEPTED', 'ISSUE_REPORTED', 'EXTENDED'))
);

CREATE UNIQUE INDEX uniq_open_acceptance_window_per_order
ON orders.acceptance_windows (order_id)
WHERE status = 'OPEN';

CREATE TABLE support.support_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID REFERENCES orders.orders(id),
  item_unit_id UUID REFERENCES catalog.item_units(id),
  type TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'OPEN',
  decision TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE support.support_case_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  case_id UUID NOT NULL REFERENCES support.support_cases(id),
  actor_id UUID REFERENCES platform.users(id),
  event_type TEXT NOT NULL,
  metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE outbox.events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  event_type TEXT NOT NULL,
  aggregate_type TEXT NOT NULL,
  aggregate_id UUID NOT NULL,
  payload JSONB NOT NULL DEFAULT '{}'::jsonb,
  status TEXT NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_pending ON outbox.events (status, created_at);
