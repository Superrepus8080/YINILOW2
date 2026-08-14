import { useEffect, useMemo, useState } from "react";
import {
  ArrowRight,
  Bell,
  ChevronDown as CaretDown,
  Clock3,
  Circle,
  CookingPot,
  Fan,
  Gem as Diamond,
  Grid3X3 as GridNine,
  HandHeart,
  Headphones,
  Heart,
  House,
  Leaf,
  Lock,
  MapPin,
  Monitor as Desktop,
  Package,
  RotateCcw,
  Recycle,
  Search as MagnifyingGlass,
  ShieldCheck,
  ShoppingCart,
  Shuffle,
  SlidersHorizontal,
  Sparkles as Sparkle,
  Truck,
  User,
  WashingMachine,
  Zap as Lightning,
} from "lucide-react";

import clothingHero from "./assets/clothing-hero-right-full.jpg";
import clothingNewdrop from "./assets/clothing-newdrop.jpg";
import clothingBag from "./assets/clothing-bag.jpg";
import clothingJacketTile from "./assets/clothing-jacket.jpg";
import clothingLooks from "./assets/clothing-looks.jpg";
import clothingDigPile from "./assets/clothing-dig-pile.jpg";
import digPileReference from "./assets/dig-pile-reference.png";
import prodLeather from "./assets/prod-leather.jpg";
import prodCargo from "./assets/prod-cargo.jpg";
import prodJordan from "./assets/prod-jordan.jpg";
import prodBag from "./assets/prod-bag.jpg";
import prodWindbreaker from "./assets/prod-windbreaker.jpg";
import prodRugby from "./assets/prod-rugby.jpg";
import prodTee from "./assets/prod-tee.jpg";
import prodSunglasses from "./assets/prod-sunglasses.jpg";

import homeHero from "./assets/home-hero-right-full.jpg";
import homeSolarStation from "./assets/home-solar-station.jpg";
import homeSolarPanel from "./assets/home-solar-panel.jpg";
import homeLedBulbs from "./assets/home-led-bulbs.jpg";
import homeInverter from "./assets/home-inverter.jpg";
import homeMicrowave from "./assets/home-microwave.jpg";
import homeAirfryer from "./assets/home-airfryer.jpg";
import homeAc from "./assets/home-ac.jpg";
import homeEarbuds from "./assets/home-earbuds.jpg";
import findMatchReference from "./assets/find-match-reference.png";
import { addToBag, checkoutQuote, confirmSandboxPayment, createAdminListing, createOrder, getCart, getListings, initializePayment } from "./api";

const clothingProducts = [
  { id: "leather-jacket", name: "Vintage Leather Jacket", price: "GHC150", image: prodLeather, category: "New Drop", condition: "Very good", seller: "Kwame Thrift", location: "Accra", note: "One-of-one leather layer with clean lining and light wear." },
  { id: "camo-cargo", name: "Camo Cargo Pants", price: "GHC90", image: prodCargo, category: "Men", condition: "Good", seller: "Pile House", location: "Kumasi", note: "Utility cargo fit with checked seams and secure pockets." },
  { id: "air-jordan", name: "Air Jordan 1 Chicago (Used)", price: "GHC250", image: prodJordan, category: "Shoes", condition: "Used", seller: "Sneaker Loop", location: "Accra", note: "Authenticated pair with visible wear and photo evidence." },
  { id: "y2k-bag", name: "Y2K Shoulder Bag", price: "GHC75", image: prodBag, category: "Bags & Accessories", condition: "Very good", seller: "Afi Selects", location: "Tema", note: "Compact black shoulder bag with inspected zipper and strap." },
  { id: "windbreaker", name: "Adidas Windbreaker", price: "GHC55", image: prodWindbreaker, category: "Men", condition: "Good", seller: "North Ridge Finds", location: "Accra", note: "Blue windbreaker, lightweight, checked for stains and tears." },
  { id: "rugby-shirt", name: "Vintage Rugby Shirt", price: "GHC60", image: prodRugby, category: "Women", condition: "Very good", seller: "Weekend Pile", location: "Cape Coast", note: "Striped rugby shirt with strong color and verified measurements." },
  { id: "graphic-tee", name: "Graphic Print Tee", price: "GHC25", image: prodTee, category: "New Drop", condition: "Good", seller: "Kwame Thrift", location: "Accra", note: "Soft graphic tee, washed, inspected, and ready to ship." },
  { id: "retro-sunglasses", name: "Retro Sunglasses", price: "GHC25", image: prodSunglasses, category: "Bags & Accessories", condition: "Excellent", seller: "Afi Selects", location: "Tema", note: "Lightweight retro frame with clean lenses and case-ready packaging." },
];

const listingImageById = {
  lst_vintage_polo: prodRugby,
  lst_leather_jacket: prodLeather,
  lst_camo_cargo: prodCargo,
};

const assetImageByUrl = {
  "/assets/prod-rugby.jpg": prodRugby,
  "/assets/prod-leather.jpg": prodLeather,
  "/assets/prod-cargo.jpg": prodCargo,
};

function formatCondition(value) {
  if (!value) return "Verified";
  return value.toLowerCase().replaceAll("_", " ").replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function normalizeApiProduct(product) {
  return {
    id: product.listingId,
    listingId: product.listingId,
    name: product.title,
    price: `${product.currency ?? "GHS"} ${Number(product.price ?? 0).toFixed(2)}`,
    image: listingImageById[product.listingId] ?? assetImageByUrl[product.imageUrl] ?? product.imageUrl ?? prodTee,
    category: product.category ?? "New Drop",
    condition: formatCondition(product.conditionPublic),
    seller: product.sellerTrustLabel ?? "YINILOW Verified",
    location: "Ghana",
    note: "Inspected clothing item fulfilled and supported by YINILOW.",
    stockLabel: product.stockLabel,
    sizeLabel: product.sizeLabel,
    availabilityState: product.availabilityState,
  };
}

function formatMoney(value, currency = "GHS") {
  const amount = Number(value ?? 0);
  return `${currency} ${amount.toFixed(2)}`;
}

function secondsUntil(value) {
  if (!value) return 0;
  return Math.max(0, Math.floor((new Date(value).getTime() - Date.now()) / 1000));
}

function formatCountdown(value) {
  const seconds = secondsUntil(value);
  const minutes = Math.floor(seconds / 60).toString().padStart(2, "0");
  const remainder = (seconds % 60).toString().padStart(2, "0");
  return `${minutes}:${remainder}`;
}

const dropProducts = [
  { ...clothingProducts[4], grabPrice: "GHC38", retail: "GHC65", net: "GHC53", left: "1 left" },
  { ...clothingProducts[5], name: "Vintage striped polo", grabPrice: "GHC38", retail: "GHC65", net: "GHC53", left: "2 left" },
  { ...clothingProducts[6], name: "Vintage graphic tee", grabPrice: "GHC28", retail: "GHC55", net: "GHC44", left: "1 left" },
  { ...clothingProducts[1], name: "90s street bundle (5 pcs)", grabPrice: "GHC120", retail: "GHC210", net: "GHC168", left: "3 left" },
  { ...clothingProducts[2], grabPrice: "GHC290", retail: "GHC480", net: "GHC382", left: "1 left" },
  { ...clothingProducts[3], name: "Leather shoulder bag", grabPrice: "GHC75", retail: "GHC150", net: "GHC119", left: "1 left" },
  { ...clothingProducts[0], name: "Leather jacket", grabPrice: "GHC85", retail: "GHC150", net: "GHC119", left: "1 left" },
  { ...clothingProducts[7], name: "Retro sunglasses set", grabPrice: "GHC25", retail: "GHC50", net: "GHC39", left: "2 left" },
];

const homeEnergy = [
  { id: "solar-station", name: "Solar Power Station 600W", price: "GHS 5,490.00", image: homeSolarStation, rating: "4.8 (132)", category: "Energy Smart", condition: "New", seller: "Energy Hub", location: "Accra", note: "Portable backup power for lights, laptops, routers, and small appliances." },
  { id: "solar-panel", name: "Solar Panel 200W", price: "GHS 2,199.00", image: homeSolarPanel, rating: "4.6 (96)", category: "Power & Energy", condition: "New", seller: "Energy Hub", location: "Accra", note: "Efficient panel for charging stations and small home backup systems." },
  { id: "led-bulbs", name: "LED Bulb 12W (Pack of 4)", price: "GHS 120.00", image: homeLedBulbs, rating: "4.7 (556)", category: "Energy Smart", condition: "New", seller: "YINILOW Home", location: "Kumasi", note: "Energy-saving bulb set for daily home use." },
  { id: "inverter", name: "Inverter 2000VA", price: "GHS 2,990.00", image: homeInverter, rating: "4.6 (381)", category: "Power & Energy", condition: "New", seller: "SmartGrid Ghana", location: "Tema", note: "Stable home inverter for backup power and essential devices." },
];

const homeTop = [
  { id: "microwave", name: "Samsung Microwave 20L", price: "GHS 1,199.00", image: homeMicrowave, rating: "4.7 (213)", category: "Kitchen", condition: "New", seller: "YINILOW Home", location: "Accra", note: "Compact microwave for everyday heating and small kitchens." },
  { id: "airfryer", name: "Philips Air Fryer 4.1L", price: "GHS 1,399.00", image: homeAirfryer, rating: "4.8 (1.2k)", category: "Small Appliances", condition: "New", seller: "Appliance Mart", location: "Accra", note: "Family-size air fryer with warranty support." },
  { id: "portable-ac", name: "Portable AC 9000BTU", price: "GHS 2,699.00", image: homeAc, rating: "4.5 (97)", category: "Cooling & Fans", condition: "New", seller: "CoolZone", location: "Tema", note: "Portable cooling for rooms and small offices." },
  { id: "earbuds", name: "Wireless Earbuds Pro", price: "GHS 999.00", image: homeEarbuds, rating: "4.6 (1.7k)", category: "Entertainment", condition: "New", seller: "Tech Yard", location: "Accra", note: "Wireless earbuds with compact charging case." },
];

const allHomeProducts = [...homeEnergy, ...homeTop];

function Logo() {
  return (
    <div className="logo">
      YINILOW <Sparkle size={21} />
    </div>
  );
}

function Header({ active, setActive, cartCount = 0, onCart, onSeller }) {
  const isHome = active === "home";
  return (
    <header className="topbar">
      <Logo />
      <div className="world-switch" aria-label="Store section">
        <button
          className={active === "clothing" ? "selected" : ""}
          onClick={() => setActive("clothing")}
        >
          Clothing & Accessories
        </button>
        <button
          className={active === "home" ? "selected" : ""}
          onClick={() => setActive("home")}
        >
          Home & Electronics
        </button>
      </div>
      <label className="search">
        <MagnifyingGlass size={22} />
        <input
          placeholder={
            isHome
              ? "Search appliances, decor & electronics..."
              : "Search clothing, shoes and accessories..."
          }
        />
      </label>
      <nav className="utility-nav">
        <button>
          <MapPin size={22} /> Ghana <CaretDown size={12} />
        </button>
        <button>
          <Heart size={22} /> Saved
        </button>
        <button>
          <User size={22} /> Account
        </button>
        <button onClick={onSeller}>
          <Package size={22} /> Seller
        </button>
        <button className="cart" onClick={onCart}>
          <ShoppingCart size={23} />
          <span>{isHome ? 0 : cartCount}</span>
          Cart
        </button>
      </nav>
    </header>
  );
}

function ClothingPage({ onBrowse, onOpenProduct, products }) {
  return (
    <>
      <CategoryNav
        items={[
          "New Drop",
          "Women",
          "Men",
          "Children",
          "Shoes",
          "Bags & Accessories",
          "Dig the Pile",
          "Stock Drop",
        ]}
        active="Stock Drop"
        note="ONE MARKETPLACE. TWO SHOPPING WORLDS."
        onSelect={onBrowse}
      />
      <section className="hero fashion-hero">
        <div className="hero-copy">
          <h1>DRIP FROM<br />OUR ROOTS</h1>
          <p>Curated thrift. Fresh drops.<br />Real style. Only on YINILOW.</p>
          <div className="hero-actions">
            <button className="dark-btn" onClick={() => onBrowse("New Drop")}>Shop now <ArrowRight size={19} /></button>
            <button className="ghost-btn" onClick={() => onBrowse("Dig the Pile")}>Dig the pile <ArrowRight size={19} /></button>
          </div>
        </div>
        <img className="fashion-people" src={clothingHero} alt="YINILOW fashion models" />
        <aside className="hero-stickers">
          <div className="seal"><Diamond size={31} /> CURATED<br />WITH CARE</div>
          <span>Support local sellers</span>
          <p>ONE ACCOUNT<br />ONE CART<br />ONE CHECKOUT</p>
        </aside>
      </section>
      <TrustBar
        items={[
          [Diamond, "One-of-ones", "No replicas. Just real finds."],
          [ShieldCheck, "Trusted sellers", "Verified sellers. Transparent deals."],
          [Recycle, "Sustainable choice", "Wear more. Waste less. Support local."],
          [MapPin, "Local love", "From Accra to Kumasi. Made for Ghana."],
          [Lock, "Secure & easy", "Safe payments. Fast checkout."],
        ]}
      />
      <PromoTiles />
      <SectionTitle title="Featured products" onAction={() => onBrowse("All Categories")} />
      <div className="product-grid clothing-grid">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} fashion onOpen={onOpenProduct} />
        ))}
      </div>
      <TrustBar
        compact
        items={[
          [ShieldCheck, "18+ only", "This market is for adults."],
          [ShieldCheck, "Secure & fair", "Payments are safe. No hidden tricks."],
          [Truck, "Free shipping", "From certain amounts. T&Cs apply."],
          [Package, "Easy returns", "Not your vibe? We make returns simple."],
          [MapPin, "We deliver in Ghana", "Fast & reliable delivery nationwide."],
          [User, "Become a seller", "Join our trusted seller community."],
        ]}
      />
    </>
  );
}

function PromoTiles() {
  const tiles = [
    ["New Drop", "Fresh pieces. Every week.", "Shop now", clothingNewdrop],
    ["Trending Pieces", "See what's hot. Shop trending.", "Shop trending", clothingBag],
    ["Shop by Category", "Find your perfect fit.", "Browse all", clothingJacketTile],
    ["Curated Looks", "Styled by us. Worn by you.", "Get inspired", clothingLooks],
    ["Dig the Pile", "Hidden gems. Big energy.", "Dig in", clothingDigPile],
  ];
  return (
    <div className="promo-grid">
      {tiles.map(([title, body, cta, image], index) => (
        <article className={index === 4 ? "promo-card source-tile accent" : "promo-card source-tile"} key={title}>
          <img src={image} alt={`${title}: ${body} ${cta}`} />
        </article>
      ))}
    </div>
  );
}

function HomePage({ onBrowse, onOpenProduct }) {
  return (
    <>
      <CategoryNav
        home
        items={["Home", "Categories", "Energy Smart", "Stock Drops", "Find My Match"]}
        active="Home"
        onSelect={onBrowse}
      />
      <section className="hero home-hero">
        <div className="home-copy">
          <span>Energy smart living</span>
          <h1>Smarter homes.<br />Lower bills. Better living.</h1>
          <p>Discover energy-efficient appliances, smart tech, and everyday essentials for your home.</p>
          <div className="hero-actions">
            <button className="dark-btn" onClick={() => onBrowse("Top Picks")}>Shop now <ArrowRight size={19} /></button>
            <button className="ghost-btn" onClick={() => onBrowse("Categories")}>Explore categories</button>
          </div>
          <div className="micro-row">
            <SmallPromise icon={ShieldCheck} title="1-year warranty" text="On eligible items" />
            <SmallPromise icon={Truck} title="Islandwide delivery" text="Fast & reliable" />
            <SmallPromise icon={Recycle} title="Easy returns" text="Hassle-free" />
          </div>
        </div>
        <img className="home-products" src={homeHero} alt="Home appliances and electronics" />
        <aside className="energy-card">
          <Leaf size={25} />
          <h3>Save energy.<br />Save money.</h3>
          <p>Smart picks for a sustainable home.</p>
          <button>Learn more <ArrowRight size={15} /></button>
        </aside>
      </section>
      <HomeCategories />
      <TrustBar
        compact
        items={[
          [Diamond, "Not sure what you need?", "Answer a few quick questions and we'll match you."],
          [ShieldCheck, "Trusted sellers", "Verified & reliable."],
          [Lock, "Secure payments", "Safe & encrypted."],
          [Headphones, "24/7 customer support", "We're here for you."],
          [MapPin, "Proudly for Ghana", "Supporting local."],
        ]}
      />
      <div className="home-rails">
        <ProductRail title="Energy Smart Picks" products={homeEnergy} onOpenProduct={onOpenProduct} onViewAll={() => onBrowse("Energy Smart")} />
        <ProductRail title="Top Picks" products={homeTop} onOpenProduct={onOpenProduct} onViewAll={() => onBrowse("Top Picks")} />
      </div>
    </>
  );
}

function CategoryNav({ items, active, note, home, onSelect }) {
  return (
    <div className={home ? "category-nav home-nav" : "category-nav"}>
      <nav>
        {items.map((item) => (
          <button className={item === active ? "active" : ""} key={item} onClick={() => onSelect?.(item)}>
            {home && item === "Home" ? <House size={19} /> : null}
            {home && item === "Find My Match" ? <Shuffle size={18} /> : null}
            {item}
            {(item === "Stock Drop" || item === "Stock Drops" || item === "Energy Smart") && (
              <span className="new-pill">New</span>
            )}
            {item === "Categories" ? <CaretDown size={13} /> : null}
          </button>
        ))}
      </nav>
      {note ? <p>{note}</p> : null}
    </div>
  );
}

function TrustBar({ items, compact = false }) {
  return (
    <section className={compact ? "trust-bar compact" : "trust-bar"}>
      {items.map(([Icon, title, body]) => (
        <div className="trust-item" key={title}>
          <Icon size={compact ? 24 : 34} />
          <div>
            <strong>{title}</strong>
            <span>{body}</span>
          </div>
        </div>
      ))}
    </section>
  );
}

function SectionTitle({ title, onAction }) {
  return (
    <div className="section-title">
      <h2>{title}</h2>
      <button onClick={onAction}>View all <ArrowRight size={14} /></button>
    </div>
  );
}

function ProductCard({ product, fashion, onOpen }) {
  return (
    <article
      className="product-card"
      onClick={() => onOpen?.(product)}
      onKeyDown={(event) => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault();
          onOpen?.(product);
        }
      }}
      role="button"
      tabIndex={0}
    >
      <div className="product-image">
        <img src={product.image} alt={product.name} />
      </div>
      <div className="product-meta">
        <div>
          <h3>{product.name}</h3>
          <strong>{product.price}</strong>
          {product.stockLabel ? <span className="stock-label">{product.stockLabel}</span> : null}
          {product.rating ? <span className="rating">★ {product.rating}</span> : null}
        </div>
        {fashion ? (
          <button aria-label={`Save ${product.name}`} onClick={(event) => event.stopPropagation()}><Heart size={20} /></button>
        ) : null}
      </div>
    </article>
  );
}

function HomeCategories() {
  const categories = [
    [CookingPot, "Kitchen"],
    [Fan, "Small Appliances"],
    [Fan, "Cooling & Fans"],
    [HandHeart, "Home Decor"],
    [WashingMachine, "Laundry"],
    [Desktop, "Entertainment"],
    [Lightning, "Power & Energy"],
    [House, "Smart Home"],
    [GridNine, "View all"],
  ];
  return (
    <section className="home-categories">
      {categories.map(([Icon, label]) => (
        <button key={label}>
          <Icon size={25} />
          <span>{label}</span>
        </button>
      ))}
    </section>
  );
}

function SmallPromise({ icon: Icon, title, text }) {
  return (
    <span>
      <Icon size={21} />
      <b>{title}</b>
      {text}
    </span>
  );
}

function ProductRail({ title, products, onOpenProduct, onViewAll }) {
  return (
    <section className="product-rail">
      <SectionTitle title={title} onAction={onViewAll} />
      <div className="rail-products">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} onOpen={onOpenProduct} />
        ))}
      </div>
      <button className="rail-next" aria-label={`Next ${title}`}>
        <ArrowRight size={18} />
      </button>
    </section>
  );
}

function BrowsePage({ active, category, onBrowse, onOpenProduct, clothingCatalog }) {
  const isHome = active === "home";
  const products = isHome ? allHomeProducts : clothingCatalog;
  const filtered = category && !["All Categories", "Categories", "Top Picks", "Home"].includes(category)
    ? products.filter((product) => product.category === category || category === "Stock Drop" || category === "Stock Drops" || category === "Dig the Pile")
    : products;
  const navItems = isHome
    ? ["Home", "Categories", "Energy Smart", "Stock Drops", "Find My Match"]
    : ["New Drop", "Women", "Men", "Children", "Shoes", "Bags & Accessories", "Dig the Pile", "Stock Drop"];

  return (
    <>
      <CategoryNav
        home={isHome}
        items={navItems}
        active={category || (isHome ? "Categories" : "New Drop")}
        note={!isHome ? "ONE MARKETPLACE. TWO SHOPPING WORLDS." : undefined}
        onSelect={onBrowse}
      />
      <section className="browse-shell">
        <div className="browse-heading">
          <span>{isHome ? "Home & Electronics" : "Clothing & Accessories"}</span>
          <h1>{category || "All Categories"}</h1>
          <p>{isHome ? "Energy-smart essentials, appliances, electronics, and home picks." : "Fresh thrift, accessories, shoes, drops, and curated pile finds."}</p>
        </div>
        <aside className="filter-panel">
          <strong>Filters</strong>
          <button className="selected">Verified sellers</button>
          <button>Ready to ship</button>
          <button>{isHome ? "Warranty eligible" : "One-of-ones"}</button>
          <button>{isHome ? "Energy smart" : "Fresh drop"}</button>
        </aside>
        <div className="browse-results">
          <div className="browse-toolbar">
            <span>{filtered.length} items</span>
            <button>Sort: Recommended <CaretDown size={14} /></button>
          </div>
          <div className="browse-grid">
            {filtered.map((product) => (
              <ProductCard key={product.id} product={product} fashion={!isHome} onOpen={onOpenProduct} />
            ))}
          </div>
        </div>
      </section>
    </>
  );
}

function ProductDetail({ active, product, onBack, onBrowse, onOpenProduct, clothingCatalog, onAddToBag, bagState }) {
  const isHome = active === "home";
  const related = (isHome ? allHomeProducts : clothingCatalog).filter((item) => item.id !== product.id).slice(0, 4);

  return (
    <>
      <CategoryNav
        home={isHome}
        items={isHome ? ["Home", "Categories", "Energy Smart", "Stock Drops", "Find My Match"] : ["New Drop", "Women", "Men", "Children", "Shoes", "Bags & Accessories", "Dig the Pile", "Stock Drop"]}
        active={product.category}
        note={!isHome ? "ONE MARKETPLACE. TWO SHOPPING WORLDS." : undefined}
        onSelect={onBrowse}
      />
      <section className="detail-shell">
        <button className="back-link" onClick={onBack}>Back to browsing</button>
        <div className="detail-media">
          <img src={product.image} alt={product.name} />
        </div>
        <article className="detail-copy">
          <span>{product.category} / {product.condition}</span>
          <h1>{product.name}</h1>
          <strong>{product.price}</strong>
          {product.rating ? <p className="rating">Star {product.rating}</p> : null}
          <p>{product.note}</p>
          {product.stockLabel || product.sizeLabel ? (
            <div className="detail-badges">
              {product.stockLabel ? <span>{product.stockLabel}</span> : null}
              {product.sizeLabel ? <span>Size {product.sizeLabel}</span> : null}
            </div>
          ) : null}
          <div className="detail-actions">
            <button className="dark-btn" disabled={bagState === "adding"} onClick={() => onAddToBag(product)}>
              {bagState === "adding" ? "Holding..." : "Add to bag"} <ArrowRight size={18} />
            </button>
            <button className="ghost-btn"><Heart size={18} /> Save</button>
          </div>
          {bagState === "held" ? (
            <p className="hold-message">Held for 10 minutes. Checkout before the timer runs out.</p>
          ) : null}
          {bagState === "conflict" ? (
            <p className="hold-message warning">Someone already has this in their bag. Save it or find similar.</p>
          ) : null}
        </article>
        <aside className="detail-trust">
          <div><ShieldCheck size={24} /><span>Seller verified</span><strong>{product.seller}</strong></div>
          <div><MapPin size={24} /><span>Ships from</span><strong>{product.location}, Ghana</strong></div>
          <div><Truck size={24} /><span>Delivery</span><strong>Fast local delivery</strong></div>
          <div><Lock size={24} /><span>Payment</span><strong>Secure checkout</strong></div>
        </aside>
      </section>
      <section className="related-shell">
        <SectionTitle title={isHome ? "Related picks" : "More from the pile"} onAction={() => onBrowse("All Categories")} />
        <div className="browse-grid compact-related">
          {related.map((item) => (
            <ProductCard key={item.id} product={item} fashion={!isHome} onOpen={onOpenProduct} />
          ))}
        </div>
      </section>
    </>
  );
}

function SellerConsolePage({ onCreated, refreshListings }) {
  const [form, setForm] = useState({
    title: "Fresh Y2K denim jacket",
    category: "New Drop",
    price: "120",
    sizeLabel: "M",
    colorLabel: "Blue",
    conditionPublic: "VERY_GOOD",
    imageUrl: "/assets/prod-leather.jpg",
    description: "Inspected seller item ready for YINILOW checkout.",
    chest: "54",
    length: "68",
  });
  const [status, setStatus] = useState("idle");
  const [created, setCreated] = useState(null);

  const updateField = (field) => (event) => {
    setForm((current) => ({ ...current, [field]: event.target.value }));
  };

  const submitListing = async (event) => {
    event.preventDefault();
    setStatus("saving");
    setCreated(null);
    try {
      const listing = await createAdminListing({
        title: form.title,
        category: form.category,
        price: form.price,
        sizeLabel: form.sizeLabel,
        colorLabel: form.colorLabel,
        conditionPublic: form.conditionPublic,
        imageUrl: form.imageUrl,
        description: form.description,
        pileEligible: true,
        measurements: {
          chest: form.chest,
          length: form.length,
        },
      });
      setCreated(listing);
      setStatus("saved");
      const products = await refreshListings();
      const product = products.find((item) => item.listingId === listing.listingId);
      if (product) {
        onCreated(product);
      }
    } catch (error) {
      setStatus("error");
    }
  };

  return (
    <>
      <CategoryNav
        items={["New Drop", "Women", "Men", "Children", "Shoes", "Bags & Accessories", "Dig the Pile", "Stock Drop"]}
        active="New Drop"
        note="SELLER INTAKE. PUBLIC CATALOG."
      />
      <section className="seller-shell">
        <div className="seller-heading">
          <span>Seller console</span>
          <h1>Add a live clothing listing</h1>
          <p>Create fresh stock for the public storefront, checkout, holds, and payment flow.</p>
        </div>
        <form className="seller-form" onSubmit={submitListing}>
          <label>
            Product title
            <input value={form.title} onChange={updateField("title")} placeholder="Vintage jacket" />
          </label>
          <label>
            Category
            <select value={form.category} onChange={updateField("category")}>
              {["New Drop", "Women", "Men", "Children", "Shoes", "Bags & Accessories"].map((category) => (
                <option key={category}>{category}</option>
              ))}
            </select>
          </label>
          <label>
            Price
            <input value={form.price} onChange={updateField("price")} inputMode="decimal" placeholder="120" />
          </label>
          <label>
            Size
            <input value={form.sizeLabel} onChange={updateField("sizeLabel")} placeholder="M" />
          </label>
          <label>
            Color
            <input value={form.colorLabel} onChange={updateField("colorLabel")} placeholder="Blue" />
          </label>
          <label>
            Condition
            <select value={form.conditionPublic} onChange={updateField("conditionPublic")}>
              <option value="LIKE_NEW">Like new</option>
              <option value="VERY_GOOD">Very good</option>
              <option value="GOOD">Good</option>
              <option value="FAIR">Fair</option>
            </select>
          </label>
          <label className="wide">
            Image path or URL
            <input value={form.imageUrl} onChange={updateField("imageUrl")} placeholder="/assets/prod-tee.jpg" />
          </label>
          <label className="wide">
            Description
            <textarea value={form.description} onChange={updateField("description")} placeholder="Describe fit, condition, and notable details" />
          </label>
          <label>
            Chest cm
            <input value={form.chest} onChange={updateField("chest")} inputMode="decimal" />
          </label>
          <label>
            Length cm
            <input value={form.length} onChange={updateField("length")} inputMode="decimal" />
          </label>
          <div className="seller-actions wide">
            <button className="dark-btn" disabled={status === "saving"}>
              {status === "saving" ? "Publishing..." : "Publish listing"} <ArrowRight size={17} />
            </button>
            {status === "saved" ? <span>Listing is live.</span> : null}
            {status === "error" ? <span className="warning">Listing could not be saved.</span> : null}
          </div>
        </form>
        <aside className="seller-preview">
          <span>Preview</span>
          <ProductCard
            product={{
              id: "seller-preview",
              name: form.title || "New listing",
              price: `GHS ${Number(form.price || 0).toFixed(2)}`,
              image: assetImageByUrl[form.imageUrl] ?? form.imageUrl ?? prodTee,
              category: form.category,
              condition: formatCondition(form.conditionPublic),
              stockLabel: "Only 1 left",
            }}
            fashion
          />
          {created ? (
            <button className="ghost-btn" onClick={() => refreshListings().then((products) => {
              const product = products.find((item) => item.listingId === created.listingId);
              if (product) onCreated(product);
            })}>
              Open live listing <ArrowRight size={17} />
            </button>
          ) : null}
        </aside>
      </section>
    </>
  );
}

function CartPage({ onBrowse, onOpenProduct, onCheckout, refreshCartCount }) {
  const [cart, setCart] = useState(null);
  const [quote, setQuote] = useState(null);
  const [status, setStatus] = useState("loading");
  const [tick, setTick] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setStatus("loading");
    Promise.allSettled([getCart(), checkoutQuote()])
      .then(([cartResult, quoteResult]) => {
        if (cancelled) return;
        if (cartResult.status === "fulfilled") {
          setCart(cartResult.value);
          refreshCartCount(cartResult.value.items?.length ?? 0);
        }
        if (quoteResult.status === "fulfilled") {
          setQuote(quoteResult.value);
        } else if (quoteResult.reason?.payload) {
          setQuote(quoteResult.reason.payload);
        }
        setStatus(cartResult.status === "fulfilled" ? "ready" : "error");
      });

    return () => {
      cancelled = true;
    };
  }, [refreshCartCount]);

  useEffect(() => {
    const timer = window.setInterval(() => setTick((value) => value + 1), 1000);
    return () => window.clearInterval(timer);
  }, []);

  const items = cart?.items ?? [];
  const expiredItems = new Set(quote?.expiredCartItems ?? []);
  const checkoutAllowed = Boolean(quote?.checkoutAllowed);

  return (
    <>
      <CategoryNav
        items={["New Drop", "Women", "Men", "Children", "Shoes", "Bags & Accessories", "Dig the Pile", "Stock Drop"]}
        active="New Drop"
        note="ONE BAG. ONE HOLD TIMER. ONE CHECKOUT."
        onSelect={onBrowse}
      />
      <section className="cart-shell">
        <div className="cart-heading">
          <span>Clothing & Accessories</span>
          <h1>Your Bag</h1>
          <p>Held thrift pieces stay reserved for a short time. Checkout before the timer runs out.</p>
        </div>
        <div className="cart-layout">
          <section className="bag-panel">
            <div className="bag-toolbar">
              <h2>Held items</h2>
              <button onClick={() => onBrowse("New Drop")}>Keep shopping <ArrowRight size={15} /></button>
            </div>
            {status === "loading" ? <p className="cart-empty">Loading your bag...</p> : null}
            {status === "error" ? <p className="cart-empty warning">We could not load your bag. Try again in a moment.</p> : null}
            {status === "ready" && items.length === 0 ? (
              <article className="empty-bag">
                <ShoppingCart size={34} />
                <h3>Your bag is empty</h3>
                <p>Find a one-of-one piece and hold it before checkout.</p>
                <button className="dark-btn" onClick={() => onBrowse("New Drop")}>Shop new drop <ArrowRight size={17} /></button>
              </article>
            ) : null}
            {items.map((item) => {
              const expired = expiredItems.has(item.cartItemId) || secondsUntil(item.holdExpiresAt) === 0;
              const product = {
                id: item.listingId,
                listingId: item.listingId,
                name: item.title,
                price: formatMoney(item.price, item.currency),
                image: assetImageByUrl[item.imageUrl] ?? prodTee,
                category: "New Drop",
                condition: formatCondition(item.conditionPublic),
                seller: "YINILOW Verified",
                location: "Ghana",
                note: "Held in your bag for checkout.",
                sizeLabel: item.sizeLabel,
              };
              return (
                <article className={expired ? "bag-item expired" : "bag-item"} key={item.cartItemId}>
                  <button className="bag-thumb" onClick={() => onOpenProduct(product)}>
                    <img src={product.image} alt={item.title} />
                  </button>
                  <div>
                    <span>{item.conditionPublic ? formatCondition(item.conditionPublic) : "Verified"} / Size {item.sizeLabel}</span>
                    <h3>{item.title}</h3>
                    <p>{expired ? "Hold expired" : `Hold expires in ${formatCountdown(item.holdExpiresAt)}`}</p>
                  </div>
                  <strong>{formatMoney(item.price, item.currency)}</strong>
                </article>
              );
            })}
          </section>
          <aside className="quote-panel">
            <h2>Checkout quote</h2>
            <dl>
              <div><dt>Subtotal</dt><dd>{formatMoney(quote?.subtotal, quote?.currency)}</dd></div>
              <div><dt>Service fee</dt><dd>{formatMoney(quote?.serviceFee, quote?.currency)}</dd></div>
              <div><dt>Delivery</dt><dd>{formatMoney(quote?.deliveryFee, quote?.currency)}</dd></div>
              <div className="quote-total"><dt>Total</dt><dd>{formatMoney(quote?.total, quote?.currency)}</dd></div>
            </dl>
            {checkoutAllowed ? (
              <p className="quote-ok"><Lock size={17} /> Ready for secure checkout.</p>
            ) : (
              <p className="quote-blocked"><Clock3 size={17} /> Add or refresh held items to checkout.</p>
            )}
            <button className="checkout-btn" disabled={!checkoutAllowed} onClick={onCheckout}>Proceed to checkout <ArrowRight size={17} /></button>
            <div className="quote-notes">
              <span><ShieldCheck size={18} /> One-off protection active</span>
              <span><Truck size={18} /> Delivery across Ghana</span>
              <span><Lock size={18} /> Secure payment step next</span>
            </div>
          </aside>
        </div>
      </section>
    </>
  );
}

function CheckoutPage({ onBrowse, onOrderCreated, refreshCartCount }) {
  const [quote, setQuote] = useState(null);
  const [cart, setCart] = useState(null);
  const [status, setStatus] = useState("loading");
  const [submitState, setSubmitState] = useState("idle");
  const [form, setForm] = useState({
    fullName: "",
    phone: "",
    city: "Accra",
    addressLine: "",
    notes: "",
  });

  useEffect(() => {
    let cancelled = false;
    Promise.allSettled([getCart(), checkoutQuote()]).then(([cartResult, quoteResult]) => {
      if (cancelled) return;
      if (cartResult.status === "fulfilled") {
        setCart(cartResult.value);
        refreshCartCount(cartResult.value.items?.length ?? 0);
      }
      if (quoteResult.status === "fulfilled") {
        setQuote(quoteResult.value);
      } else if (quoteResult.reason?.payload) {
        setQuote(quoteResult.reason.payload);
      }
      setStatus(cartResult.status === "fulfilled" ? "ready" : "error");
    });
    return () => {
      cancelled = true;
    };
  }, [refreshCartCount]);

  const updateField = (field) => (event) => {
    setForm((current) => ({ ...current, [field]: event.target.value }));
  };

  const canSubmit = Boolean(quote?.checkoutAllowed && form.fullName.trim() && form.phone.trim() && form.addressLine.trim());

  const submitOrder = async (event) => {
    event.preventDefault();
    if (!canSubmit || submitState === "submitting") return;
    setSubmitState("submitting");
    try {
      const order = await createOrder(form);
      refreshCartCount(0);
      setSubmitState("created");
      onOrderCreated(order);
    } catch (error) {
      setSubmitState(error.status === 409 ? "blocked" : "error");
    }
  };

  const items = cart?.items ?? [];

  return (
    <>
      <CategoryNav
        items={["New Drop", "Women", "Men", "Children", "Shoes", "Bags & Accessories", "Dig the Pile", "Stock Drop"]}
        active="New Drop"
        note="DELIVERY. PAYMENT. REVIEW."
        onSelect={onBrowse}
      />
      <section className="checkout-shell">
        <div className="cart-heading">
          <span>Secure checkout</span>
          <h1>Checkout</h1>
          <p>Confirm delivery details, review your held items, then create the order. Payment connection comes next.</p>
        </div>
        <form className="checkout-layout" onSubmit={submitOrder}>
          <section className="checkout-form-panel">
            <h2>Delivery details</h2>
            <div className="checkout-fields">
              <label>
                Full name
                <input value={form.fullName} onChange={updateField("fullName")} placeholder="Your name" />
              </label>
              <label>
                Phone
                <input value={form.phone} onChange={updateField("phone")} placeholder="024 000 0000" />
              </label>
              <label>
                City
                <input value={form.city} onChange={updateField("city")} placeholder="Accra" />
              </label>
              <label>
                Delivery address
                <input value={form.addressLine} onChange={updateField("addressLine")} placeholder="Street, area, landmark" />
              </label>
              <label className="wide">
                Delivery notes
                <textarea value={form.notes} onChange={updateField("notes")} placeholder="Optional notes for delivery" />
              </label>
            </div>
            <div className="payment-placeholder">
              <Lock size={21} />
              <div>
                <strong>Payment method</strong>
                <span>Mobile money/card provider will connect here next. This step creates a payment-pending order.</span>
              </div>
            </div>
            {submitState === "blocked" ? <p className="hold-message warning">Checkout is no longer available. Return to your bag and refresh held items.</p> : null}
            {submitState === "error" ? <p className="hold-message warning">Order could not be created. Please try again.</p> : null}
          </section>
          <aside className="quote-panel checkout-review">
            <h2>Order review</h2>
            {status === "loading" ? <p className="cart-empty">Loading order...</p> : null}
            {items.map((item) => (
              <div className="review-line" key={item.cartItemId}>
                <span>{item.title}</span>
                <strong>{formatMoney(item.price, item.currency)}</strong>
              </div>
            ))}
            <dl>
              <div><dt>Subtotal</dt><dd>{formatMoney(quote?.subtotal, quote?.currency)}</dd></div>
              <div><dt>Service fee</dt><dd>{formatMoney(quote?.serviceFee, quote?.currency)}</dd></div>
              <div><dt>Delivery</dt><dd>{formatMoney(quote?.deliveryFee, quote?.currency)}</dd></div>
              <div className="quote-total"><dt>Total</dt><dd>{formatMoney(quote?.total, quote?.currency)}</dd></div>
            </dl>
            <button className="checkout-btn" type="submit" disabled={!canSubmit || submitState === "submitting"}>
              {submitState === "submitting" ? "Creating order..." : "Create order"} <ArrowRight size={17} />
            </button>
          </aside>
        </form>
      </section>
    </>
  );
}

function OrderConfirmationPage({ order, onBrowse }) {
  const [payment, setPayment] = useState(null);
  const [paymentState, setPaymentState] = useState(order?.paymentStatus === "PAID" ? "paid" : "idle");

  const startPayment = async () => {
    if (!order?.orderId || paymentState === "starting") return;
    setPaymentState("starting");
    try {
      const attempt = await initializePayment(order.orderId);
      setPayment(attempt);
      setPaymentState("ready");
    } catch {
      setPaymentState("error");
    }
  };

  const confirmPayment = async () => {
    if (!payment?.providerReference || paymentState === "confirming") return;
    setPaymentState("confirming");
    try {
      const result = await confirmSandboxPayment(payment.providerReference);
      setPayment(result);
      setPaymentState("paid");
    } catch {
      setPaymentState("error");
    }
  };

  return (
    <>
      <CategoryNav
        items={["New Drop", "Women", "Men", "Children", "Shoes", "Bags & Accessories", "Dig the Pile", "Stock Drop"]}
        active="New Drop"
        note="ORDER CREATED. PAYMENT NEXT."
        onSelect={onBrowse}
      />
      <section className="confirmation-shell">
        <ShieldCheck size={46} />
        <span>Order created</span>
        <h1>{order?.orderNumber ?? "YINILOW ORDER"}</h1>
        <p>Your order is reserved and waiting for payment. This sandbox payment flow uses the same initialize and callback shape needed for the real provider.</p>
        <div>
          <strong>{formatMoney(order?.total, order?.currency)}</strong>
          <em>{paymentState === "paid" ? "PAID" : order?.status ?? "PAYMENT_PENDING"}</em>
        </div>
        <section className="payment-action-panel">
          <h2>Payment</h2>
          <p>{payment?.providerReference ? `Reference ${payment.providerReference}` : "Start a sandbox payment attempt for this order."}</p>
          {paymentState === "paid" ? (
            <span><ShieldCheck size={18} /> Payment confirmed</span>
          ) : (
            <div>
              <button className="dark-btn" onClick={startPayment} disabled={paymentState === "starting"}>
                {paymentState === "starting" ? "Starting..." : "Start payment"} <ArrowRight size={17} />
              </button>
              <button className="checkout-btn" onClick={confirmPayment} disabled={!payment || paymentState === "confirming"}>
                {paymentState === "confirming" ? "Confirming..." : "Confirm sandbox payment"} <ShieldCheck size={17} />
              </button>
            </div>
          )}
          {paymentState === "error" ? <p className="hold-message warning">Payment step failed. Please try again.</p> : null}
        </section>
        <button className="ghost-btn" onClick={() => onBrowse("New Drop")}>Continue shopping <ArrowRight size={17} /></button>
      </section>
    </>
  );
}

function DigPilePage({ onBrowse, onOpenProduct }) {
  const featured = clothingProducts[5];
  const tags = [
    ["GHC55", "pile-tag tag-hat"],
    ["GHC110", "pile-tag tag-jacket"],
    ["GHC95", "pile-tag tag-jeans"],
    ["GHC85", "pile-tag tag-tee"],
    ["GHC75", "pile-tag tag-bag"],
    ["GHC180", "pile-tag tag-shoe"],
    ["GHC150", "pile-tag tag-leather"],
    ["GHC45", "pile-tag tag-cap"],
  ];

  return (
    <>
      <CategoryNav
        items={["New Drop", "Clothes", "Shoes", "Sellers", "Stock Drop"]}
        active="Stock Drop"
        note="DIG DEEP. TAP ANYTHING."
        onSelect={onBrowse}
      />
      <section className="pile-page">
        <div className="pile-title">
          <h1>Dig the Pile</h1>
          <p>Dig deep. Tap anything. Save what you love.</p>
        </div>
        <div className="pile-actions">
          <button className="ghost-btn"><Shuffle size={17} /> Shuffle</button>
          <button className="ghost-btn"><SlidersHorizontal size={17} /> Filter</button>
        </div>
        <div className="pile-stage" aria-label="Interactive thrift pile">
          <img src={digPileReference} alt="YINILOW thrift pile with tagged clothing" />
          {tags.map(([label, className]) => (
            <button key={className} className={className}>{label}</button>
          ))}
          <article className="pile-quick-card">
            <h2>{featured.name}</h2>
            <strong>GHC85</strong>
            <span>Only 1 left</span>
            <div>
              <button aria-label={`Save ${featured.name}`}><Heart size={22} /></button>
              <button aria-label="More options">...</button>
            </div>
            <button className="dark-btn" onClick={() => onOpenProduct(featured)}>Quick view</button>
          </article>
        </div>
      </section>
    </>
  );
}

function StockDropPage({ onBrowse, onOpenProduct }) {
  const schedule = [
    ["Vintage Heatwave", "Streetwear + Vintage", "Ajourd'hui, 6:00 PM", "08:42:31", clothingProducts[0].image],
    ["90s Sports Classics", "Sportswear + Retro", "Ajourd'hui, 8:00 PM", "10:42:31", clothingProducts[2].image],
    ["Designer Grails", "Luxury + Premium", "Demain, 12:00 PM", "1D 02:42:31", clothingProducts[3].image],
    ["Y2K Essentials", "Y2K + Streetwear", "Demain, 3:00 PM", "1D 05:42:31", clothingProducts[6].image],
  ];
  const recap = [
    ["Urban Essentials", "May 25, 2025", [prodLeather, prodCargo, prodBag], "48/48"],
    ["Archive Picks", "May 23, 2025", [prodTee, prodJordan], "36/36"],
    ["Nike Heat Drop", "May 21, 2025", [prodJordan], "52/52"],
  ];

  return (
    <>
      <CategoryNav
        items={["New Drop", "Women", "Men", "Children", "Shoes", "Bags & Accessories", "Dig the Pile", "Stock Drop"]}
        active="Stock Drop"
        note="LIVE DROPS. FIRST COME, FIRST GRAB."
        onSelect={onBrowse}
      />
      <section className="drop-hero">
        <div>
          <h1>Stock Drop</h1>
          <p>Real drops. Real people. Real pieces. Join live drops for a chance to grab exclusive items before they are gone.</p>
        </div>
        <TrustBar
          compact
          items={[
            [ShieldCheck, "18+ only", "ID verified participants"],
            [Lock, "Secure & fair", "Transparent fees. No hidden cuts."],
            [Recycle, "Live drops", "Real-time stock. First come, first grab."],
          ]}
        />
        <button className="grab-badge">YINILOW<br />GRAB</button>
      </section>
      <section className="drop-dashboard">
        <div className="drop-main">
          <div className="drop-tabs">
            <button className="selected">Single pieces</button>
            <button>3+ deals</button>
            <button>6+ deals</button>
            <button className="timer"><Clock3 size={15} /> Ends in 08:42</button>
            <button><Bell size={15} /> Drop alerts on</button>
          </div>
          <div className="drop-grid">
            {dropProducts.map((product, index) => (
              <article className="drop-card" key={`${product.id}-${index}`}>
                <span>{index === 6 ? "Grabbed" : product.left}</span>
                <div className="drop-image"><img src={product.image} alt={product.name} /></div>
                <h3>{product.name}</h3>
                <dl>
                  <div><dt>Acq. price</dt><dd>{product.grabPrice}</dd></div>
                  <div><dt>Target retail</dt><dd>{product.retail}</dd></div>
                  <div><dt>Est. net</dt><dd>{product.net}</dd></div>
                </dl>
                <button className={index === 6 ? "grabbed" : ""} onClick={() => onOpenProduct(product)}>
                  {index === 6 ? "Grabbed" : "Grab"}
                </button>
              </article>
            ))}
          </div>
        </div>
        <aside className="grab-panel">
          <div className="grab-title"><h2>Your grab (3)</h2><span>Live</span></div>
          {dropProducts.slice(1, 4).map((product) => (
            <div className="grab-line" key={product.id}>
              <img src={product.image} alt={product.name} />
              <div><strong>{product.name}</strong><span>Qty: 1</span></div>
              <b>{product.grabPrice}</b>
            </div>
          ))}
          <button className="dark-btn">View grab bag (3) <ArrowRight size={17} /></button>
          <div className="metrics">
            <h3>Live metrics</h3>
            <p><span>Grabbers</span><strong>126</strong></p>
            <p><span>Items left</span><strong>48</strong></p>
            <p><span>Drop ends in</span><strong>08:42</strong></p>
          </div>
          <button className="checkout-btn">Proceed to checkout <ArrowRight size={17} /></button>
        </aside>
      </section>
      <section className="drop-lower">
        <div className="recap-panel">
          <SectionTitle title="Past drops recap" onAction={() => onBrowse("Stock Drop")} />
          <div className="recap-grid">
            {recap.map(([title, date, images, sold]) => (
              <article key={title}>
                <span>Sold out</span>
                <h3>{title}</h3>
                <p>{date}</p>
                <div>{images.map((image, index) => <img key={index} src={image} alt="" />)}</div>
                <strong>Items sold {sold}</strong>
              </article>
            ))}
          </div>
        </div>
        <div className="schedule-panel">
          <SectionTitle title="Upcoming drop schedule" onAction={() => onBrowse("Stock Drop")} />
          {schedule.map(([title, type, time, left, image], index) => (
            <article className="schedule-line" key={title}>
              <img src={image} alt="" />
              <div><strong>{title}</strong><span>{type}<br />{time}</span></div>
              <b>{left}</b>
              {index === 0 ? <em>Next</em> : null}
            </article>
          ))}
        </div>
      </section>
    </>
  );
}

function FindMatchPage({ onBrowse, onOpenProduct }) {
  const matchCategories = [
    [homeMicrowave, "Home Appliances", true],
    [homeAirfryer, "Kitchenware & Cookware", false],
    [homeLedBulbs, "Cleaning Equipment", false],
    [homeEarbuds, "Home Electronics", false],
    [homeSolarPanel, "Power & Energy", false],
    [homeHero, "Home Decor", false],
  ];
  const recommendations = [
    { ...homeTop[1], name: "6L Air Fryer", price: "GHS 450.00", tag: "Best for students" },
    { ...homeTop[0], id: "electric-oven", name: "13L Electric Oven", price: "GHS 280.00", image: homeMicrowave, tag: "Energy smart", note: "Bake, grill and toast with confidence." },
    { ...homeTop[2], name: "Mini Rechargeable Fan", price: "GHS 120.00", image: homeAc, tag: "Small-space friendly" },
    { ...homeTop[3], name: "Home Electronics Kit", price: "GHS 280.00", image: homeEarbuds, tag: "Hygiene essential" },
  ];

  return (
    <>
      <CategoryNav
        home
        items={["Home", "Categories", "Energy Smart", "Stock Drops", "Find My Match"]}
        active="Find My Match"
        onSelect={onBrowse}
      />
      <section className="match-page">
        <aside className="match-quiz">
          <span className="crumb">Home / Find My Match</span>
          <div className="match-intro">
            <div>
              <h1>Find My Match</h1>
              <p>Answer a few quick questions and we will recommend the perfect picks for your home.</p>
            </div>
            <Sparkle size={48} />
          </div>
          <div className="match-steps" aria-label="Quiz progress">
            {["Shopping for?", "Budget", "Priority", "Room", "Brands", "Results"].map((step, index) => (
              <div className={index === 0 ? "active" : ""} key={step}>
                <span>{index + 1}</span>
                <b>{step}</b>
              </div>
            ))}
          </div>
          <div className="question-card">
            <h2>1. What are you shopping for?</h2>
            <p>Select all that apply</p>
            <div className="choice-grid">
              {matchCategories.map(([image, label, selected]) => (
                <button className={selected ? "selected" : ""} key={label}>
                  <img src={image} alt="" />
                  <strong>{label}</strong>
                  <span>{selected ? "Check" : ""}</span>
                </button>
              ))}
            </div>
            <button className="checkout-btn">Next: Budget <ArrowRight size={17} /></button>
          </div>
        </aside>
        <section className="match-results">
          <div className="match-hero">
            <img src={findMatchReference} alt="Home appliance match recommendations" />
            <div>
              <h2>Your perfect match awaits</h2>
              <p>Based on your answers, we have handpicked the best appliances and essentials for your home.</p>
              <span>Smart Choices</span>
              <span>Energy Efficient</span>
              <span>Great Value</span>
            </div>
            <aside>
              <h3>Your selection summary</h3>
              <p><b>Shopping for</b> Home Appliances, Kitchenware...</p>
              <p><b>Budget</b> GHS 200 - GHS 800</p>
              <p><b>Top Priority</b> Energy Saving, Compact Size</p>
              <p><b>Room / Use Case</b> Kitchen, Living Room</p>
              <button>Edit answers</button>
            </aside>
          </div>
          <div className="match-toolbar">
            <h2>We recommend these for you</h2>
            <div>
              <button><Heart size={18} /> Save all</button>
              <button>Compare (0)</button>
            </div>
          </div>
          <div className="match-products">
            {recommendations.map((product) => (
              <article className="match-card" key={product.id}>
                <span>{product.tag}</span>
                <div><img src={product.image} alt={product.name} /></div>
                <h3>{product.name}</h3>
                <strong>{product.price}</strong>
                <p>{product.note}</p>
                <button onClick={() => onOpenProduct(product)}>View details</button>
                <footer>
                  <button><Heart size={17} /> Save</button>
                  <button><Shuffle size={16} /> Compare</button>
                </footer>
              </article>
            ))}
          </div>
          <TrustBar
            compact
            items={[
              [Sparkle, "Not sure yet?", "Our expert tool makes smart recommendations."],
              [ShieldCheck, "Tested for quality", "Every product is carefully tested."],
              [Leaf, "Energy smart picks", "Save more with efficient appliances."],
              [Truck, "Delivered in Ghana", "Fast & secure delivery across the country."],
            ]}
          />
          <button className="retake-btn"><RotateCcw size={17} /> Take the quiz again</button>
        </section>
      </section>
      <TrustBar
        compact
        items={[
          [ShieldCheck, "Verified quality", "All products are tested and verified."],
          [Lock, "Secure payments", "Pay safely with MoMo, cards and cash on delivery."],
          [Truck, "Delivers across Ghana", "From Accra to Tamale, we deliver to your doorstep."],
          [Headphones, "Dedicated support", "Our team is just a message away."],
        ]}
      />
    </>
  );
}

function Footer({ active }) {
  const isHome = active === "home";
  const shopLinks = isHome
    ? ["All Categories", "Energy Smart", "Stock Drops", "New Arrivals", "Top Picks"]
    : ["All Categories", "New Drop", "Trending Pieces", "Dig the Pile", "Stock Drop", "Gift Cards"];
  return (
    <footer className="footer">
      <div>
        <Logo />
        <p>{isHome ? "Ghana's home for quality home & electronics. Live smarter. Spend wiser." : "Ghana's home of style marketplace. One account. One cart. One checkout. One love."}</p>
        <div className="socials">
          <Circle size={17} />
          <Circle size={17} />
          <Circle size={17} />
          <Circle size={17} />
        </div>
      </div>
      <FooterColumn title="Shop" items={shopLinks} />
      <FooterColumn title="Help & Support" items={["Help Center", "Track My Order", "Shipping & Delivery", "Returns & Refunds", "Contact Us"]} />
      <FooterColumn title="About Yinilow" items={["About Us", "Careers", "News & Press", "Terms", "Privacy"]} />
      <div className="newsletter">
        <h4>Stay Connected</h4>
        <p>Get deals, drops & energy smart tips straight to your inbox.</p>
        <label>
          <input placeholder="Enter your email" />
          <button><ArrowRight size={18} /></button>
        </label>
        <div className="store-badges">
          <span>Download on the<br /><b>App Store</b></span>
          <span>Get it on<br /><b>Google Play</b></span>
        </div>
      </div>
      <div className="ghana-card">
        <h4>We deliver in Ghana</h4>
        <MapPin size={42} fill="currentColor" />
      </div>
    </footer>
  );
}

function FooterColumn({ title, items }) {
  return (
    <div>
      <h4>{title}</h4>
      <ul>
        {items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </div>
  );
}

export function App() {
  const [active, setActive] = useState("clothing");
  const [screen, setScreen] = useState({ type: "home", category: null, product: null });
  const [apiProducts, setApiProducts] = useState([]);
  const [apiStatus, setApiStatus] = useState("fallback");
  const [cartCount, setCartCount] = useState(0);
  const [bagState, setBagState] = useState("idle");
  const [createdOrder, setCreatedOrder] = useState(null);
  const clothingCatalog = apiProducts.length ? apiProducts : clothingProducts;

  const refreshListings = async () => {
    const listings = await getListings();
    const products = listings.map(normalizeApiProduct);
    setApiProducts(products);
    setApiStatus("connected");
    return products;
  };

  useEffect(() => {
    let cancelled = false;
    refreshListings()
      .then(() => {
        if (!cancelled) {
          setApiStatus("connected");
        }
      })
      .catch(() => {
        if (!cancelled) {
          setApiProducts([]);
          setApiStatus("fallback");
        }
      });

    getCart()
      .then((cart) => {
        if (!cancelled) {
          setCartCount(cart.items?.length ?? 0);
        }
      })
      .catch(() => {});

    return () => {
      cancelled = true;
    };
  }, []);

  const switchWorld = (world) => {
    setActive(world);
    setScreen({ type: "home", category: null, product: null });
    setBagState("idle");
  };
  const browse = (category) => {
    if (category === "Home") {
      setScreen({ type: "home", category: null, product: null });
      return;
    }
    if (active === "clothing" && category === "Dig the Pile") {
      setScreen({ type: "dig", category, product: null });
      return;
    }
    if (active === "clothing" && category === "Stock Drop") {
      setScreen({ type: "stockDrop", category, product: null });
      return;
    }
    if (active === "home" && category === "Find My Match") {
      setScreen({ type: "findMatch", category, product: null });
      return;
    }
    setScreen({ type: "browse", category, product: null });
    setBagState("idle");
  };
  const openProduct = (product) => {
    setBagState("idle");
    setScreen({ type: "product", category: product.category, product });
  };
  const openCart = () => {
    setActive("clothing");
    setBagState("idle");
    setScreen({ type: "cart", category: "Bag", product: null });
  };
  const openCheckout = () => {
    setActive("clothing");
    setBagState("idle");
    setScreen({ type: "checkout", category: "Checkout", product: null });
  };
  const openSeller = () => {
    setActive("clothing");
    setBagState("idle");
    setScreen({ type: "seller", category: "Seller", product: null });
  };
  const showOrderCreated = (order) => {
    setCreatedOrder(order);
    setScreen({ type: "orderCreated", category: "Order", product: null });
  };
  const holdProduct = async (product) => {
    if (active !== "clothing" || !product.listingId) {
      setBagState("held");
      return;
    }
    setBagState("adding");
    try {
      await addToBag(product.listingId);
      const cart = await getCart().catch(() => null);
      setCartCount(cart?.items?.length ?? cartCount + 1);
      setBagState("held");
    } catch (error) {
      setBagState(error.status === 409 ? "conflict" : "idle");
    }
  };
  const page = useMemo(() => {
    if (screen.type === "browse") {
      return <BrowsePage active={active} category={screen.category} onBrowse={browse} onOpenProduct={openProduct} clothingCatalog={clothingCatalog} />;
    }
    if (screen.type === "product" && screen.product) {
      return <ProductDetail active={active} product={screen.product} onBack={() => browse(screen.category)} onBrowse={browse} onOpenProduct={openProduct} clothingCatalog={clothingCatalog} onAddToBag={holdProduct} bagState={bagState} />;
    }
    if (screen.type === "cart") {
      return <CartPage onBrowse={browse} onOpenProduct={openProduct} onCheckout={openCheckout} refreshCartCount={setCartCount} />;
    }
    if (screen.type === "checkout") {
      return <CheckoutPage onBrowse={browse} onOrderCreated={showOrderCreated} refreshCartCount={setCartCount} />;
    }
    if (screen.type === "orderCreated") {
      return <OrderConfirmationPage order={createdOrder} onBrowse={browse} />;
    }
    if (screen.type === "seller") {
      return <SellerConsolePage onCreated={openProduct} refreshListings={refreshListings} />;
    }
    if (screen.type === "dig") {
      return <DigPilePage onBrowse={browse} onOpenProduct={openProduct} />;
    }
    if (screen.type === "stockDrop") {
      return <StockDropPage onBrowse={browse} onOpenProduct={openProduct} />;
    }
    if (screen.type === "findMatch") {
      return <FindMatchPage onBrowse={browse} onOpenProduct={openProduct} />;
    }
    return active === "home"
      ? <HomePage onBrowse={browse} onOpenProduct={openProduct} />
      : <ClothingPage onBrowse={browse} onOpenProduct={openProduct} products={clothingCatalog} />;
  }, [active, screen, clothingCatalog, bagState, createdOrder]);

  return (
    <main className={active === "home" ? "app home-mode" : "app fashion-mode"}>
      <Header active={active} setActive={switchWorld} cartCount={cartCount} onCart={openCart} onSeller={openSeller} />
      {active === "clothing" ? (
        <div className="api-status">
          {apiStatus === "connected" ? "Live catalog connected" : "Prototype catalog fallback"}
        </div>
      ) : null}
      {page}
      <Footer active={active} />
    </main>
  );
}
