import { useEffect, useMemo, useRef, useState } from "react";
import {
  ArrowRight,
  Bell,
  Camera as CameraIcon,
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
import denimBlackWorkPants from "./assets/catalog/denim-black-work-pants.jpg";
import denimBlueLevisDenim from "./assets/catalog/denim-blue-levis-denim.jpg";
import denimGreyWorkPants from "./assets/catalog/denim-grey-work-pants.jpg";
import denimKhakiCargoPants from "./assets/catalog/denim-khaki-cargo-pants.jpg";
import denimLightBlueDenim from "./assets/catalog/denim-light-blue-denim.jpg";
import denimOliveCargoPants from "./assets/catalog/denim-olive-cargo-pants.jpg";
import denimStoneWorkPants from "./assets/catalog/denim-stone-work-pants.jpg";
import dropAdidasTrackJacket from "./assets/catalog/drop-adidas-track-jacket.jpg";
import dropBlackBomber from "./assets/catalog/drop-black-bomber.jpg";
import dropBlackRedAdidasShell from "./assets/catalog/drop-black-red-adidas-shell.jpg";
import dropBlackRedBomber from "./assets/catalog/drop-black-red-bomber.jpg";
import dropBlueVarsityJacket from "./assets/catalog/drop-blue-varsity-jacket.jpg";
import dropGreenMiamiWindbreaker from "./assets/catalog/drop-green-miami-windbreaker.jpg";
import dropGreyWorkJacket from "./assets/catalog/drop-grey-work-jacket.jpg";
import dropNavyHoodedJacket from "./assets/catalog/drop-navy-hooded-jacket.jpg";
import dropOliveUtilityVest from "./assets/catalog/drop-olive-utility-vest.jpg";
import dropRedOhioPullover from "./assets/catalog/drop-red-ohio-pullover.jpg";
import dropRedRainShell from "./assets/catalog/drop-red-rain-shell.jpg";
import dropTanWorkVest from "./assets/catalog/drop-tan-work-vest.jpg";

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
import { addToBag, checkoutQuote, clearSellerSession, confirmPayment as verifyPaymentReference, createAdminListing, createOrder, createSellerAccount, getAdminListings, getAdminOrders, getCart, getListings, getOrder, getStoredSellerSession, initializePayment, recordRealtimeTelemetry, sellerLogin, sellerLogout, updateAdminListingVisibility, updateAdminOrderStatus } from "./api";
import { createRealtimeDataChannel } from "./realtime";

const catalogProductImages = {
  "black-bomber-jacket": dropBlackBomber,
  "tan-work-vest": dropTanWorkVest,
  "black-red-bomber": dropBlackRedBomber,
  "olive-utility-vest": dropOliveUtilityVest,
  "navy-hooded-jacket": dropNavyHoodedJacket,
  "adidas-track-jacket": dropAdidasTrackJacket,
  "red-ohio-pullover": dropRedOhioPullover,
  "blue-varsity-windbreaker": dropBlueVarsityJacket,
  "green-miami-windbreaker": dropGreenMiamiWindbreaker,
  "grey-work-jacket": dropGreyWorkJacket,
  "red-rain-shell": dropRedRainShell,
  "black-red-adidas-shell": dropBlackRedAdidasShell,
  "stone-work-pants": denimStoneWorkPants,
  "blue-levis-denim": denimBlueLevisDenim,
  "grey-work-pants": denimGreyWorkPants,
  "khaki-cargo-pants": denimKhakiCargoPants,
  "light-blue-denim": denimLightBlueDenim,
  "black-work-pants": denimBlackWorkPants,
  "olive-cargo-pants": denimOliveCargoPants,
};

const freshPileProducts = [
  { id: "black-bomber-jacket", name: "Vintage Black Bomber Jacket", price: "GHC125", image: catalogProductImages["black-bomber-jacket"], category: "Jackets", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "3XL", note: "Freshly cropped from the YINILOW pile, checked and ready for local checkout." },
  { id: "tan-work-vest", name: "Tan Work Vest", price: "GHC95", image: catalogProductImages["tan-work-vest"], category: "Vests", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "2XL", note: "Rugged workwear vest with utility pockets and warm vintage texture." },
  { id: "black-red-bomber", name: "Black Red Bomber Jacket", price: "GHC110", image: catalogProductImages["black-red-bomber"], category: "Jackets", condition: "Very good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "XL", note: "Lightweight bomber with bold red trim and a clean streetwear profile." },
  { id: "olive-utility-vest", name: "Olive Utility Vest", price: "GHC85", image: catalogProductImages["olive-utility-vest"], category: "Vests", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "XL", note: "Sleeveless utility layer with a soft washed finish and easy styling." },
  { id: "navy-hooded-jacket", name: "Navy Hooded Jacket", price: "GHC140", image: catalogProductImages["navy-hooded-jacket"], category: "Jackets", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "XL", note: "Hooded everyday jacket with a strong oversized thrift feel." },
  { id: "adidas-track-jacket", name: "Adidas Track Jacket", price: "GHC120", image: catalogProductImages["adidas-track-jacket"], category: "Jackets", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "XL", note: "Classic black-and-white track jacket, inspected for zipper and cuff wear." },
  { id: "red-ohio-pullover", name: "Red Ohio Pullover", price: "GHC90", image: catalogProductImages["red-ohio-pullover"], category: "Jackets", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "L", note: "Bright collegiate pullover with a clean vintage sportswear mood." },
  { id: "blue-varsity-windbreaker", name: "Blue Varsity Windbreaker", price: "GHC115", image: catalogProductImages["blue-varsity-windbreaker"], category: "Jackets", condition: "Very good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "L", note: "Royal blue varsity shell with striped cuffs and strong color pop." },
  { id: "green-miami-windbreaker", name: "Green Miami Windbreaker", price: "GHC105", image: catalogProductImages["green-miami-windbreaker"], category: "Jackets", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "L", note: "Green and orange windbreaker with a bold retro campus look." },
  { id: "grey-work-jacket", name: "Grey Work Jacket", price: "GHC130", image: catalogProductImages["grey-work-jacket"], category: "Jackets", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "L", note: "Structured work jacket with neutral color and everyday layering weight." },
  { id: "red-rain-shell", name: "Red Rain Shell", price: "GHC80", image: catalogProductImages["red-rain-shell"], category: "Jackets", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "L", note: "Packable red shell jacket for light rain, errands, and street styling." },
  { id: "black-red-adidas-shell", name: "Black Red Adidas Shell", price: "GHC125", image: catalogProductImages["black-red-adidas-shell"], category: "Jackets", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "L", note: "Sport shell jacket with sharp black-and-red blocking and zip front." },
  { id: "stone-work-pants", name: "Stone Work Pants W46", price: "GHC110", image: catalogProductImages["stone-work-pants"], category: "Pants", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "W46", note: "Relaxed stone work pants with utility attitude and a roomy fit." },
  { id: "blue-levis-denim", name: "Blue Levi's Denim W44", price: "GHC125", image: catalogProductImages["blue-levis-denim"], category: "Pants", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "W44", note: "Blue Levi's denim with a straight-leg thrift profile and visible back patch." },
  { id: "grey-work-pants", name: "Grey Work Pants W44", price: "GHC105", image: catalogProductImages["grey-work-pants"], category: "Pants", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "W44", note: "Grey work pants with a softened wash and daily utility fit." },
  { id: "khaki-cargo-pants", name: "Khaki Cargo Pants W44", price: "GHC115", image: catalogProductImages["khaki-cargo-pants"], category: "Pants", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "W44", note: "Khaki cargo pants with roomy pockets and a relaxed secondhand feel." },
  { id: "light-blue-denim", name: "Light Blue Denim W42", price: "GHC100", image: catalogProductImages["light-blue-denim"], category: "Pants", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "W42", note: "Light blue denim with a classic worn-in wash and easy street fit." },
  { id: "black-work-pants", name: "Black Work Pants W40", price: "GHC110", image: catalogProductImages["black-work-pants"], category: "Pants", condition: "Very good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "W40", note: "Black work pants with a clean look, utility back patch, and solid structure." },
  { id: "olive-cargo-pants", name: "Olive Cargo Pants W40", price: "GHC105", image: catalogProductImages["olive-cargo-pants"], category: "Pants", condition: "Good", seller: "YINILOW Pile", location: "Accra", sizeLabel: "W40", note: "Olive cargo pants with soft fading and pocket-heavy thrift character." },
];

const clothingProducts = [
  ...freshPileProducts,
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
  lst_black_bomber_jacket: dropBlackBomber,
  lst_tan_work_vest: dropTanWorkVest,
  lst_black_red_bomber: dropBlackRedBomber,
  lst_olive_utility_vest: dropOliveUtilityVest,
  lst_navy_hooded_jacket: dropNavyHoodedJacket,
  lst_adidas_track_jacket: dropAdidasTrackJacket,
  lst_red_ohio_pullover: dropRedOhioPullover,
  lst_blue_varsity_windbreaker: dropBlueVarsityJacket,
  lst_green_miami_windbreaker: dropGreenMiamiWindbreaker,
  lst_grey_work_jacket: dropGreyWorkJacket,
  lst_red_rain_shell: dropRedRainShell,
  lst_black_red_adidas_shell: dropBlackRedAdidasShell,
  lst_stone_work_pants: denimStoneWorkPants,
  lst_blue_levis_denim: denimBlueLevisDenim,
  lst_grey_work_pants: denimGreyWorkPants,
  lst_khaki_cargo_pants: denimKhakiCargoPants,
  lst_light_blue_denim: denimLightBlueDenim,
  lst_black_work_pants: denimBlackWorkPants,
  lst_olive_cargo_pants: denimOliveCargoPants,
};

const assetImageByUrl = {
  "/assets/prod-rugby.jpg": prodRugby,
  "/assets/prod-leather.jpg": prodLeather,
  "/assets/prod-cargo.jpg": prodCargo,
  "/assets/catalog/drop-black-bomber.jpg": dropBlackBomber,
  "/assets/catalog/drop-tan-work-vest.jpg": dropTanWorkVest,
  "/assets/catalog/drop-black-red-bomber.jpg": dropBlackRedBomber,
  "/assets/catalog/drop-olive-utility-vest.jpg": dropOliveUtilityVest,
  "/assets/catalog/drop-navy-hooded-jacket.jpg": dropNavyHoodedJacket,
  "/assets/catalog/drop-adidas-track-jacket.jpg": dropAdidasTrackJacket,
  "/assets/catalog/drop-red-ohio-pullover.jpg": dropRedOhioPullover,
  "/assets/catalog/drop-blue-varsity-jacket.jpg": dropBlueVarsityJacket,
  "/assets/catalog/drop-green-miami-windbreaker.jpg": dropGreenMiamiWindbreaker,
  "/assets/catalog/drop-grey-work-jacket.jpg": dropGreyWorkJacket,
  "/assets/catalog/drop-red-rain-shell.jpg": dropRedRainShell,
  "/assets/catalog/drop-black-red-adidas-shell.jpg": dropBlackRedAdidasShell,
  "/assets/catalog/denim-stone-work-pants.jpg": denimStoneWorkPants,
  "/assets/catalog/denim-blue-levis-denim.jpg": denimBlueLevisDenim,
  "/assets/catalog/denim-grey-work-pants.jpg": denimGreyWorkPants,
  "/assets/catalog/denim-khaki-cargo-pants.jpg": denimKhakiCargoPants,
  "/assets/catalog/denim-light-blue-denim.jpg": denimLightBlueDenim,
  "/assets/catalog/denim-black-work-pants.jpg": denimBlackWorkPants,
  "/assets/catalog/denim-olive-cargo-pants.jpg": denimOliveCargoPants,
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

function formatStatus(value) {
  if (!value) return "Pending";
  return value.toLowerCase().replaceAll("_", " ").replace(/\b\w/g, (letter) => letter.toUpperCase());
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

function Header({ active, setActive, cartCount = 0, onCart, onSeller, onTrack }) {
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
        <button onClick={onTrack}>
          <Truck size={22} /> Track
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

function ClothingPage({ cardStates, onAddToBag, onBrowse, onOpenProduct, products }) {
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
          <ProductCard
            key={product.id}
            product={product}
            fashion
            onAddToBag={onAddToBag}
            onOpen={onOpenProduct}
            state={cardStates[product.listingId] ?? "idle"}
          />
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

function ToastNotice({ toast, onClose }) {
  return (
    <aside className={`toast-notice ${toast.type}`} role="status" aria-live="polite">
      <div>
        <strong>{toast.title}</strong>
        <span>{toast.message}</span>
      </div>
      <button aria-label="Close notice" onClick={onClose}>Close</button>
    </aside>
  );
}

function ProductCard({ product, fashion, onAddToBag, onOpen, state = "idle" }) {
  const canAdd = Boolean(onAddToBag && product.listingId);
  const actionLabel = state === "adding" ? "Adding..." : state === "added" ? "Added" : state === "failed" ? "Try again" : "Add to bag";
  const sizeText = product.sizeLabel ? `Size ${product.sizeLabel}` : product.category;

  function handleAdd(event) {
    event.stopPropagation();
    onAddToBag?.(product);
  }

  return (
    <article
      className={`product-card ${fashion ? "fashion-product-card" : "standard-product-card"} ${canAdd ? "has-card-action" : ""}`}
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
        {fashion ? (
          <div className="product-badges">
            {sizeText ? <span>{sizeText}</span> : null}
            {product.condition ? <span>{product.condition}</span> : null}
          </div>
        ) : null}
        <img src={product.image} alt={product.name} />
      </div>
      <div className="product-meta">
        <div>
          <h3>{product.name}</h3>
          <strong>{product.price}</strong>
          {product.rating ? <span className="rating">★ {product.rating}</span> : null}
        </div>
        {fashion ? (
          <button aria-label={`Save ${product.name}`} onClick={(event) => event.stopPropagation()}><Heart size={20} /></button>
        ) : null}
      </div>
      {product.stockLabel ? <span className="stock-label">{product.stockLabel}</span> : null}
      {canAdd ? (
        <button
          className={`card-add-btn ${state}`}
          aria-label={`${actionLabel} ${product.name} to bag`}
          disabled={state === "adding" || state === "added"}
          onClick={handleAdd}
        >
          {actionLabel}
        </button>
      ) : null}
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

function ProductRail({ title, products, onAddToBag, onOpenProduct, onViewAll, cardStates = {} }) {
  return (
    <section className="product-rail">
      <SectionTitle title={title} onAction={onViewAll} />
      <div className="rail-products">
        {products.map((product) => (
          <ProductCard
            key={product.id}
            product={product}
            onAddToBag={onAddToBag}
            onOpen={onOpenProduct}
            state={cardStates[product.listingId] ?? "idle"}
          />
        ))}
      </div>
      <button className="rail-next" aria-label={`Next ${title}`}>
        <ArrowRight size={18} />
      </button>
    </section>
  );
}

function BrowsePage({ active, cardStates, category, onAddToBag, onBrowse, onOpenProduct, clothingCatalog }) {
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
              <ProductCard
                key={product.id}
                product={product}
                fashion={!isHome}
                onAddToBag={!isHome ? onAddToBag : undefined}
                onOpen={onOpenProduct}
                state={cardStates[product.listingId] ?? "idle"}
              />
            ))}
          </div>
        </div>
      </section>
    </>
  );
}

function ProductDetail({ active, cardStates, product, onBack, onBrowse, onOpenProduct, clothingCatalog, onAddToBag, bagState }) {
  const isHome = active === "home";
  const canAdd = active === "clothing" && Boolean(product.listingId);
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
          {!isHome ? (
            <div className="detail-promise-row">
              <span><Clock3 size={16} /> 10 min hold</span>
              <span><ShieldCheck size={16} /> Seller checked</span>
              <span><Truck size={16} /> Ghana delivery</span>
            </div>
          ) : null}
          <div className="detail-actions">
            <button className="dark-btn" disabled={bagState === "adding" || bagState === "held"} onClick={() => onAddToBag(product)}>
              {bagState === "adding" ? "Holding..." : bagState === "held" ? "Added" : canAdd ? "Add to bag" : "Preview only"} <ArrowRight size={18} />
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
            <ProductCard
              key={item.id}
              product={item}
              fashion={!isHome}
              onAddToBag={!isHome ? onAddToBag : undefined}
              onOpen={onOpenProduct}
              state={cardStates[item.listingId] ?? "idle"}
            />
          ))}
        </div>
      </section>
    </>
  );
}

function SellerConsolePage({ onCreated, refreshListings }) {
  const [sellerSession, setSellerSession] = useState(() => getStoredSellerSession());
  const [authMode, setAuthMode] = useState("signin");
  const [authForm, setAuthForm] = useState({
    displayName: "YINILOW Seller",
    email: "seller@yinilow.local",
    password: "yinilow-demo",
  });
  const [loginStatus, setLoginStatus] = useState("idle");
  const [form, setForm] = useState({
    title: "Fresh Y2K denim jacket",
    category: "New Drop",
    price: "120",
    sizeLabel: "M",
    colorLabel: "Blue",
    conditionPublic: "VERY_GOOD",
    imageUrl: "/assets/prod-leather.jpg",
    imageFileName: "",
    description: "Inspected seller item ready for YINILOW checkout.",
    chest: "54",
    length: "68",
  });
  const [status, setStatus] = useState("idle");
  const [created, setCreated] = useState(null);
  const [inventory, setInventory] = useState([]);
  const [inventoryStatus, setInventoryStatus] = useState("loading");
  const [updatingListing, setUpdatingListing] = useState(null);
  const [orders, setOrders] = useState([]);
  const [ordersStatus, setOrdersStatus] = useState("loading");
  const [updatingOrder, setUpdatingOrder] = useState(null);

  const loadInventory = async () => {
    if (!getStoredSellerSession()) {
      setInventoryStatus("locked");
      return [];
    }
    setInventoryStatus("loading");
    try {
      const listings = await getAdminListings();
      setInventory(listings);
      setInventoryStatus("ready");
      return listings;
    } catch (error) {
      if (error.status === 401) {
        clearSellerSession();
        setSellerSession(null);
        setInventory([]);
        setInventoryStatus("locked");
        return [];
      }
      setInventoryStatus("error");
      return [];
    }
  };

  const loadOrders = async () => {
    if (!getStoredSellerSession()) {
      setOrdersStatus("locked");
      return [];
    }
    setOrdersStatus("loading");
    try {
      const sellerOrders = await getAdminOrders();
      setOrders(sellerOrders);
      setOrdersStatus("ready");
      return sellerOrders;
    } catch (error) {
      if (error.status === 401) {
        clearSellerSession();
        setSellerSession(null);
        setOrders([]);
        setOrdersStatus("locked");
        return [];
      }
      setOrdersStatus("error");
      return [];
    }
  };

  useEffect(() => {
    if (sellerSession) {
      loadInventory();
      loadOrders();
    } else {
      setInventoryStatus("locked");
      setOrdersStatus("locked");
    }
  }, [sellerSession]);

  const updateField = (field) => (event) => {
    setForm((current) => ({ ...current, [field]: event.target.value }));
  };

  const uploadProductImage = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      setStatus("image-error");
      return;
    }
    if (file.size > 1_500_000) {
      setStatus("image-large");
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      setForm((current) => ({
        ...current,
        imageUrl: String(reader.result),
        imageFileName: file.name,
      }));
      setStatus("image-ready");
    };
    reader.onerror = () => setStatus("image-error");
    reader.readAsDataURL(file);
  };

  const updateAuthField = (field) => (event) => {
    setAuthForm((current) => ({ ...current, [field]: event.target.value }));
  };

  const toggleVisibility = async (listing) => {
    const nextVisibility = listing.visibility === "PUBLIC" ? "HIDDEN" : "PUBLIC";
    setUpdatingListing(listing.listingId);
    try {
      await updateAdminListingVisibility(listing.listingId, nextVisibility);
      await Promise.all([loadInventory(), refreshListings()]);
    } finally {
      setUpdatingListing(null);
    }
  };

  const moveOrder = async (order, next) => {
    setUpdatingOrder(order.orderId);
    try {
      await updateAdminOrderStatus(order.orderId, next);
      await loadOrders();
    } finally {
      setUpdatingOrder(null);
    }
  };

  const submitLogin = async (event) => {
    event.preventDefault();
    setLoginStatus("checking");
    try {
      const session = authMode === "create"
        ? await createSellerAccount(authForm)
        : await sellerLogin({ email: authForm.email, password: authForm.password });
      setSellerSession(session);
      setLoginStatus("idle");
    } catch (error) {
      setLoginStatus("error");
    }
  };

  const logoutSeller = async () => {
    await sellerLogout();
    setSellerSession(null);
    setInventory([]);
    setOrders([]);
    setInventoryStatus("locked");
    setOrdersStatus("locked");
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
      await loadInventory();
      const products = await refreshListings();
      const product = products.find((item) => item.listingId === listing.listingId);
      if (product) {
        onCreated(product);
      }
    } catch (error) {
      setStatus("error");
    }
  };

  const sellerStats = {
    total: inventory.length,
    available: inventory.filter((listing) => listing.availabilityState === "AVAILABLE").length,
    hidden: inventory.filter((listing) => listing.visibility === "HIDDEN").length,
    sold: inventory.filter((listing) => listing.itemStatus === "SOLD").length,
    orders: orders.length,
  };

  return (
    <>
      <section className="seller-internal-shell">
        <aside className="seller-rail">
          <Logo />
          <nav>
            {[
              [GridNine, "Dashboard"],
              [Package, "Catalog"],
              [CameraIcon, "Photography"],
              [Truck, "Fulfillment"],
              [Headphones, "Support"],
            ].map(([Icon, label]) => (
              <button className={label === "Catalog" ? "selected" : ""} key={label}>
                <Icon size={19} /> {label}
              </button>
            ))}
          </nav>
          <div>
            <strong>YINILOW</strong>
            <span>INTERNAL</span>
          </div>
        </aside>
        <div className="seller-workspace">
          <div className="seller-heading">
            <div>
              <span>Seller catalog</span>
              <h1>Manage live clothing stock</h1>
              <p>{sellerSession ? `Signed in as ${sellerSession.displayName}` : "Sign in or create a seller account to publish and manage stock."}</p>
            </div>
            {sellerSession ? <button className="ghost-btn" onClick={logoutSeller}>Sign out</button> : null}
          </div>
        {!sellerSession ? (
          <form className="seller-login" onSubmit={submitLogin}>
            <div>
              <span>Protected access</span>
              <h2>{authMode === "create" ? "Create seller account" : "Seller sign in"}</h2>
              <p>Use the demo seller or create a new account for the console.</p>
            </div>
            <div className="seller-auth-tabs">
              <button type="button" className={authMode === "signin" ? "selected" : ""} onClick={() => setAuthMode("signin")}>Sign in</button>
              <button type="button" className={authMode === "create" ? "selected" : ""} onClick={() => setAuthMode("create")}>Create account</button>
            </div>
            {authMode === "create" ? (
              <label>
                Display name
                <input value={authForm.displayName} onChange={updateAuthField("displayName")} placeholder="Seller name" />
              </label>
            ) : null}
            <label>
              Email
              <input value={authForm.email} onChange={updateAuthField("email")} type="email" placeholder="seller@example.com" />
            </label>
            <label>
              Password
              <input value={authForm.password} onChange={updateAuthField("password")} type="password" placeholder="At least 6 characters" />
            </label>
            <button className="dark-btn" disabled={loginStatus === "checking"}>
              {loginStatus === "checking" ? "Checking..." : authMode === "create" ? "Create and enter" : "Enter console"} <ArrowRight size={17} />
            </button>
            {loginStatus === "error" ? <p className="hold-message warning">Those seller details did not work.</p> : null}
          </form>
        ) : (
          <>
        <section className="seller-stats">
          {[
            ["Live listings", sellerStats.total, "Owned by this seller"],
            ["Available", sellerStats.available, "Ready for shoppers"],
            ["Hidden", sellerStats.hidden, "Not public"],
            ["Sold", sellerStats.sold, "Converted orders"],
            ["Orders", sellerStats.orders, "Need fulfillment"],
          ].map(([label, value, hint]) => (
            <article key={label}>
              <strong>{value}</strong>
              <span>{label}</span>
              <small>{hint}</small>
            </article>
          ))}
        </section>
        <div className="seller-main-grid">
        <form className="seller-form" onSubmit={submitListing}>
          <div className="seller-panel-title wide">
            <span>Create listing</span>
            <h2>Item intake</h2>
          </div>
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
          <div className="seller-upload wide">
            <label>
              Product photo
              <input type="file" accept="image/*" onChange={uploadProductImage} />
            </label>
            <span>{form.imageFileName || "Upload a JPG, PNG, or WebP up to 1.5 MB."}</span>
          </div>
          <label className="wide">
            Image URL or stored photo data
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
            {status === "image-ready" ? <span>Photo ready.</span> : null}
            {status === "image-large" ? <span className="warning">Photo must be 1.5 MB or smaller.</span> : null}
            {status === "image-error" ? <span className="warning">Photo could not be read.</span> : null}
            {status === "error" ? <span className="warning">Listing could not be saved.</span> : null}
          </div>
        </form>
        <aside className="seller-preview">
          <div className="seller-panel-title">
            <span>Preview</span>
            <h2>Public card</h2>
          </div>
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
          <div className="seller-snapshot">
            <span>Seller snapshot</span>
            <strong>{sellerSession.displayName}</strong>
            <small>{sellerSession.email}</small>
            <p>Photos, measurements, pricing, and public availability are tied to this account.</p>
          </div>
        </aside>
        </div>
        <section className="seller-inventory">
          <div className="seller-inventory-head">
            <div>
              <span>Inventory</span>
              <h2>Current listings</h2>
            </div>
            <button className="ghost-btn" onClick={loadInventory}>Refresh</button>
          </div>
          {inventoryStatus === "error" ? (
            <p className="hold-message warning">Inventory could not be loaded.</p>
          ) : null}
          <div className="seller-table">
            {inventory.map((listing) => (
              <article className="seller-row" key={listing.listingId}>
                <img src={assetImageByUrl[listing.imageUrl] ?? listing.imageUrl ?? prodTee} alt={listing.title} />
                <div>
                  <h3>{listing.title}</h3>
                  <span>{listing.category} / Size {listing.sizeLabel || "One size"}</span>
                  <small>{listing.listingId}</small>
                </div>
                <strong>{formatMoney(listing.price, listing.currency)}</strong>
                <div className="seller-state">
                  <span className={listing.visibility === "PUBLIC" ? "state-public" : "state-hidden"}>{listing.visibility}</span>
                  <small>{listing.availabilityState}</small>
                </div>
                <button className="ghost-btn" onClick={() => toggleVisibility(listing)} disabled={updatingListing === listing.listingId}>
                  {listing.visibility === "PUBLIC" ? "Hide" : "Show"}
                </button>
              </article>
            ))}
            {inventoryStatus === "loading" ? <p className="seller-empty">Loading inventory...</p> : null}
            {inventoryStatus === "ready" && inventory.length === 0 ? <p className="seller-empty">No listings yet.</p> : null}
          </div>
        </section>
        <section className="seller-inventory seller-orders">
          <div className="seller-inventory-head">
            <div>
              <span>Orders</span>
              <h2>Fulfillment queue</h2>
            </div>
            <button className="ghost-btn" onClick={loadOrders}>Refresh</button>
          </div>
          {ordersStatus === "error" ? (
            <p className="hold-message warning">Orders could not be loaded.</p>
          ) : null}
          <div className="seller-table">
            {orders.map((order) => (
              <article className="seller-row order-row" key={order.orderId}>
                <div>
                  <h3>{order.orderNumber}</h3>
                  <span>{order.deliveryAddress?.fullName || "Customer"} / {order.deliveryAddress?.city || "Ghana"}</span>
                  <small>{order.deliveryAddress?.phone || "No phone"}</small>
                </div>
                <strong>{formatMoney(order.total, order.currency)}</strong>
                <div className="seller-state">
                  <span className={order.paymentStatus === "PAID" ? "state-public" : "state-hidden"}>{formatStatus(order.paymentStatus)}</span>
                  <small>{formatStatus(order.deliveryStatus)}</small>
                </div>
                <div className="order-actions">
                  <button className="ghost-btn" disabled={updatingOrder === order.orderId || order.paymentStatus === "PAID"} onClick={() => moveOrder(order, { status: "PAID", paymentStatus: "PAID" })}>Mark paid</button>
                  <button className="ghost-btn" disabled={updatingOrder === order.orderId} onClick={() => moveOrder(order, { status: "PACKED", deliveryStatus: "PACKED" })}>Packed</button>
                  <button className="ghost-btn" disabled={updatingOrder === order.orderId} onClick={() => moveOrder(order, { status: "OUT_FOR_DELIVERY", deliveryStatus: "OUT_FOR_DELIVERY" })}>Out</button>
                  <button className="ghost-btn" disabled={updatingOrder === order.orderId} onClick={() => moveOrder(order, { status: "DELIVERED", deliveryStatus: "DELIVERED" })}>Delivered</button>
                </div>
              </article>
            ))}
            {ordersStatus === "loading" ? <p className="seller-empty">Loading orders...</p> : null}
            {ordersStatus === "ready" && orders.length === 0 ? <p className="seller-empty">No orders yet.</p> : null}
          </div>
        </section>
          </>
        )}
        </div>
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
    email: "",
    city: "Accra",
    deliveryMethod: "Door delivery",
    paymentMethod: "MOBILE_MONEY",
    momoProvider: "mtn",
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

  const canSubmit = Boolean(quote?.checkoutAllowed && form.fullName.trim() && form.phone.trim() && form.email.trim() && form.addressLine.trim());

  const submitOrder = async (event) => {
    event.preventDefault();
    if (!canSubmit || submitState === "submitting") return;
    setSubmitState("submitting");
    try {
      const order = await createOrder(form, form.paymentMethod);
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
                Email
                <input value={form.email} onChange={updateField("email")} type="email" placeholder="you@example.com" />
              </label>
              <label>
                City
                <input value={form.city} onChange={updateField("city")} placeholder="Accra" />
              </label>
              <label>
                Delivery type
                <select value={form.deliveryMethod} onChange={updateField("deliveryMethod")}>
                  <option>Door delivery</option>
                  <option>Pickup point</option>
                  <option>Seller meetup</option>
                </select>
              </label>
              <label>
                MoMo network
                <select value={form.momoProvider} onChange={updateField("momoProvider")}>
                  <option value="mtn">MTN Mobile Money</option>
                  <option value="vod">Telecel Cash</option>
                  <option value="tgo">AirtelTigo Money</option>
                </select>
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
                <span>YINILOW sends a Paystack Mobile Money charge prompt directly to the customer phone.</span>
                <div className="payment-methods" aria-label="Payment methods">
                  <button type="button" className="selected"><Lightning size={15} /> Direct MoMo API</button>
                </div>
              </div>
            </div>
            {submitState === "blocked" ? <p className="hold-message warning">Checkout is no longer available. Return to your bag and refresh held items.</p> : null}
            {submitState === "error" ? <p className="hold-message warning">Order could not be created. Please try again.</p> : null}
          </section>
          <aside className="quote-panel checkout-review">
            <h2>Order review</h2>
            {status === "loading" ? <p className="cart-empty">Loading order...</p> : null}
            <div className="review-items">
              {items.map((item) => (
                <div className="review-line" key={item.cartItemId}>
                  <img src={assetImageByUrl[item.imageUrl] ?? prodTee} alt={item.title} />
                  <div>
                    <span>{item.title}</span>
                    <small>{item.sizeLabel ? `Size ${item.sizeLabel}` : "Verified item"}</small>
                  </div>
                  <strong>{formatMoney(item.price, item.currency)}</strong>
                </div>
              ))}
            </div>
            <dl>
              <div><dt>Subtotal</dt><dd>{formatMoney(quote?.subtotal, quote?.currency)}</dd></div>
              <div><dt>Service fee</dt><dd>{formatMoney(quote?.serviceFee, quote?.currency)}</dd></div>
              <div><dt>Delivery</dt><dd>{formatMoney(quote?.deliveryFee, quote?.currency)}</dd></div>
              <div className="quote-total"><dt>Total</dt><dd>{formatMoney(quote?.total, quote?.currency)}</dd></div>
            </dl>
            <button className="checkout-btn" type="submit" disabled={!canSubmit || submitState === "submitting"}>
              {submitState === "submitting" ? "Creating order..." : "Create order"} <ArrowRight size={17} />
            </button>
            {!canSubmit ? <p className="checkout-hint">Add name, phone, email, and delivery address to continue.</p> : null}
          </aside>
        </form>
      </section>
    </>
  );
}

function OrderConfirmationPage({ order, onBrowse, onTrack }) {
  const [payment, setPayment] = useState(null);
  const [paymentState, setPaymentState] = useState(order?.paymentStatus === "PAID" ? "paid" : "idle");

  const startPayment = async () => {
    if (!order?.orderId || paymentState === "starting") return;
    setPaymentState("starting");
    try {
      const attempt = await initializePayment(order.orderId);
      setPayment(attempt);
      setPaymentState(attempt.status === "FAILED" ? "error" : "ready");
    } catch {
      setPaymentState("error");
    }
  };

  const confirmPayment = async () => {
    if (!payment?.providerReference || paymentState === "confirming") return;
    setPaymentState("confirming");
    try {
      const result = await verifyPaymentReference(payment.providerReference, payment.provider ?? "PAYSTACK");
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
      <section className={paymentState === "paid" ? "confirmation-shell paid" : "confirmation-shell"}>
        <div className="confirmation-hero">
          <ShieldCheck size={46} />
          <span>{paymentState === "paid" ? "Payment confirmed" : "Order created"}</span>
          <h1>{order?.orderNumber ?? "YINILOW ORDER"}</h1>
          <p>Your order is reserved and waiting for payment. YINILOW sends a direct Mobile Money prompt through Paystack, then verifies the reference by API.</p>
        </div>
        <div className="receipt-card">
          <div>
            <span>Total</span>
            <strong>{formatMoney(order?.total, order?.currency)}</strong>
          </div>
          <div>
            <span>Status</span>
            <em>{paymentState === "paid" ? "PAID" : order?.status ?? "PAYMENT_PENDING"}</em>
          </div>
          <div>
            <span>Reference</span>
            <strong>{payment?.providerReference ?? "Pending"}</strong>
          </div>
        </div>
        <section className="payment-action-panel">
          <h2>Payment</h2>
          <p>{payment?.displayText ?? (payment?.providerReference ? `Reference ${payment.providerReference}` : "Send a Mobile Money prompt to the customer phone.")}</p>
          {paymentState === "paid" ? (
            <span><ShieldCheck size={18} /> Payment confirmed</span>
          ) : (
            <div>
              <button className="dark-btn" onClick={startPayment} disabled={paymentState === "starting"}>
                {paymentState === "starting" ? "Sending prompt..." : payment ? "Resend prompt" : "Send MoMo prompt"} <ArrowRight size={17} />
              </button>
              <button className="checkout-btn" onClick={confirmPayment} disabled={!payment || paymentState === "confirming"}>
                {paymentState === "confirming" ? "Checking..." : "Check payment"} <ShieldCheck size={17} />
              </button>
            </div>
          )}
          {paymentState === "error" ? <p className="hold-message warning">Payment step failed. Please try again.</p> : null}
        </section>
        <div className="confirmation-actions">
          <button className="dark-btn" onClick={() => onTrack(order)}>Track order <Truck size={17} /></button>
          <button className="ghost-btn" onClick={() => onBrowse("New Drop")}>Continue shopping <ArrowRight size={17} /></button>
        </div>
      </section>
    </>
  );
}

function OrderTrackingPage({ initialOrder, onBrowse }) {
  const [form, setForm] = useState({
    orderNumber: initialOrder?.orderNumber ?? "",
    phone: initialOrder?.deliveryAddress?.phone ?? "",
  });
  const [order, setOrder] = useState(initialOrder ?? null);
  const [status, setStatus] = useState(initialOrder ? "ready" : "idle");

  const paymentDone = order?.paymentStatus === "PAID" || order?.status === "PAID";
  const deliveryStatus = order?.deliveryStatus ?? "NOT_STARTED";
  const steps = [
    { label: "Order created", done: Boolean(order), detail: order?.orderNumber ?? "Waiting for lookup" },
    { label: "Payment", done: paymentDone, detail: formatStatus(order?.paymentStatus) },
    { label: "Packed", done: ["PACKED", "OUT_FOR_DELIVERY", "DELIVERED"].includes(deliveryStatus), detail: "Seller prepares item" },
    { label: "Out for delivery", done: ["OUT_FOR_DELIVERY", "DELIVERED"].includes(deliveryStatus), detail: "Rider assigned" },
    { label: "Delivered", done: deliveryStatus === "DELIVERED", detail: "Customer receives order" },
  ];

  const submitLookup = async (event) => {
    event.preventDefault();
    if (!form.orderNumber.trim() || !form.phone.trim() || status === "loading") return;
    setStatus("loading");
    try {
      const found = await getOrder(form.orderNumber.trim(), form.phone.trim());
      setOrder(found);
      setStatus("ready");
      recordRealtimeTelemetry({
        type: "order-tracking",
        roomId: "dig-pile-main",
        metadata: { label: "order lookup", state: found.status, at: Date.now() },
      });
    } catch {
      setOrder(null);
      setStatus("error");
    }
  };

  return (
    <>
      <CategoryNav
        items={["New Drop", "Women", "Men", "Children", "Shoes", "Bags & Accessories", "Dig the Pile", "Stock Drop"]}
        active="New Drop"
        note="REAL ORDER STATUS."
        onSelect={onBrowse}
      />
      <section className="tracking-shell">
        <div className="cart-heading">
          <span>Order tracking</span>
          <h1>Track your order</h1>
          <p>Use the order number and phone used at checkout to see payment, packing, and delivery progress.</p>
        </div>
        <div className="tracking-layout">
          <form className="tracking-form" onSubmit={submitLookup}>
            <h2>Find order</h2>
            <label>
              Order number
              <input value={form.orderNumber} onChange={(event) => setForm((current) => ({ ...current, orderNumber: event.target.value }))} placeholder="YLO-0000000000" />
            </label>
            <label>
              Phone
              <input value={form.phone} onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value }))} placeholder="024 000 0000" />
            </label>
            <button className="checkout-btn" type="submit" disabled={!form.orderNumber.trim() || !form.phone.trim() || status === "loading"}>
              {status === "loading" ? "Checking..." : "Check status"} <ArrowRight size={17} />
            </button>
            {status === "error" ? <p className="hold-message warning">No order matched those details. Check the order number and phone.</p> : null}
          </form>
          <section className="tracking-card">
            <div className="tracking-summary">
              <span>{order ? "Current order" : "Waiting for details"}</span>
              <h2>{order?.orderNumber ?? "YINILOW"}</h2>
              <strong>{formatMoney(order?.total, order?.currency)}</strong>
              <em>{formatStatus(order?.status)}</em>
            </div>
            <div className="tracking-steps">
              {steps.map((step) => (
                <div className={step.done ? "tracking-step done" : "tracking-step"} key={step.label}>
                  <span><ShieldCheck size={16} /></span>
                  <div>
                    <strong>{step.label}</strong>
                    <small>{step.detail}</small>
                  </div>
                </div>
              ))}
            </div>
            {order?.items?.length ? (
              <div className="tracking-items">
                {order.items.map((item) => (
                  <div className="review-line" key={`${item.listingId}-${item.title}`}>
                    <img src={assetImageByUrl[item.imageUrl] ?? listingImageById[item.listingId] ?? prodTee} alt={item.title} />
                    <div>
                      <span>{item.title}</span>
                      <small>{item.sizeLabel ? `Size ${item.sizeLabel}` : "Verified item"}</small>
                    </div>
                    <strong>{formatMoney(item.price, item.currency)}</strong>
                  </div>
                ))}
              </div>
            ) : null}
          </section>
        </div>
      </section>
    </>
  );
}

function useDigPileRealtime() {
  const channelRef = useRef(null);
  const recentTagsRef = useRef([]);
  const connectionStateRef = useRef("connecting");
  const viewerCountRef = useRef(1);
  const [connectionState, setConnectionState] = useState("connecting");
  const [viewerCount, setViewerCount] = useState(1);
  const [latencyMs, setLatencyMs] = useState(null);
  const [activity, setActivity] = useState("Live pile is warming up");

  useEffect(() => {
    let mounted = true;
    const roomId = "dig-pile-main";
    const pingPrefix = Math.random().toString(36).slice(2);

    try {
      channelRef.current = createRealtimeDataChannel({
        roomId,
        onPeerCount: (count) => {
          if (!mounted) return;
          const nextCount = Math.max(1, Number(count) || 1);
          viewerCountRef.current = nextCount;
          setViewerCount(nextCount);
        },
        onStateChange: (state) => {
          if (!mounted) return;
          connectionStateRef.current = state;
          setConnectionState(state);
          recordRealtimeTelemetry({
            type: "presence",
            roomId,
            metadata: { state, viewerCount: viewerCountRef.current, at: Date.now() },
          });
        },
        onMessage: (message) => {
          const payload = message?.payload;
          if (!payload) return;
          if (payload.kind === "ping") {
            channelRef.current?.sendTelemetry({ kind: "pong", id: payload.id, sentAt: payload.sentAt });
            return;
          }
          if (payload.kind === "pong" && payload.id?.startsWith(pingPrefix)) {
            const nextLatency = Math.max(1, Date.now() - payload.sentAt);
            if (mounted) setLatencyMs(nextLatency);
            recordRealtimeTelemetry({
              type: "latency",
              roomId,
              metadata: { latencyMs: nextLatency, viewerCount: viewerCountRef.current, at: Date.now() },
            });
            return;
          }
          if (payload.kind === "hover-tag" && mounted) {
            setActivity(`Someone is checking ${payload.label}`);
          }
          if (payload.kind === "active" && mounted) {
            setActivity("People are digging now");
          }
        },
      });
    } catch {
      if (mounted) setConnectionState("unavailable");
    }

    const heartbeat = window.setInterval(() => {
      channelRef.current?.sendTelemetry({ kind: "active", roomId, at: Date.now() });
      recordRealtimeTelemetry({
        type: "heartbeat",
        roomId,
        metadata: {
          state: connectionStateRef.current,
          viewerCount: viewerCountRef.current,
          recentTags: recentTagsRef.current,
          at: Date.now(),
        },
      });
    }, 4500);
    const latencyProbe = window.setInterval(() => {
      channelRef.current?.sendTelemetry({ kind: "ping", id: `${pingPrefix}-${Date.now()}`, sentAt: Date.now() });
    }, 7000);

    return () => {
      mounted = false;
      window.clearInterval(heartbeat);
      window.clearInterval(latencyProbe);
      channelRef.current?.close();
      channelRef.current = null;
    };
  }, []);

  function trackTag(label) {
    recentTagsRef.current = [label, ...recentTagsRef.current.filter((tag) => tag !== label)].slice(0, 5);
    setActivity(`You touched ${label}`);
    channelRef.current?.sendTelemetry({ kind: "hover-tag", label, at: Date.now() });
    recordRealtimeTelemetry({
      type: "tag-hover",
      roomId: "dig-pile-main",
      metadata: {
        label,
        viewerCount: viewerCountRef.current,
        recentTags: recentTagsRef.current,
        at: Date.now(),
      },
    });
  }

  return { activity, connectionState, latencyMs, trackTag, viewerCount };
}

function DigPilePage({ onBrowse, onOpenProduct }) {
  const realtime = useDigPileRealtime();
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
        <div className="pile-live-strip" aria-label="Dig the Pile live status">
          <span className={`live-dot ${realtime.connectionState}`}></span>
          <strong>{realtime.viewerCount} live</strong>
          <span>{realtime.activity}</span>
          <em>{realtime.latencyMs ? `${realtime.latencyMs}ms` : realtime.connectionState}</em>
        </div>
        <div className="pile-actions">
          <button className="ghost-btn"><Shuffle size={17} /> Shuffle</button>
          <button className="ghost-btn"><SlidersHorizontal size={17} /> Filter</button>
        </div>
        <div className="pile-stage" aria-label="Interactive thrift pile">
          <img src={digPileReference} alt="YINILOW thrift pile with tagged clothing" />
          {tags.map(([label, className]) => (
            <button
              key={className}
              className={className}
              onFocus={() => realtime.trackTag(label)}
              onMouseEnter={() => realtime.trackTag(label)}
              onClick={() => realtime.trackTag(label)}
            >
              {label}
            </button>
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
  const [cardStates, setCardStates] = useState({});
  const [createdOrder, setCreatedOrder] = useState(null);
  const [toast, setToast] = useState(null);
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

  useEffect(() => {
    if (!toast) return undefined;
    const timer = window.setTimeout(() => setToast(null), 3600);
    return () => window.clearTimeout(timer);
  }, [toast]);

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
  const openTracking = (order = null) => {
    setActive("clothing");
    setBagState("idle");
    setScreen({ type: "tracking", category: "Track", product: null, order });
  };
  const showOrderCreated = (order) => {
    setCreatedOrder(order);
    setScreen({ type: "orderCreated", category: "Order", product: null });
  };
  const holdProduct = async (product) => {
    if (active !== "clothing" || !product.listingId) {
      setBagState("idle");
      setToast({ type: "warning", title: "Preview only", message: "This item is not connected to live checkout yet." });
      return;
    }
    const cardKey = product.listingId;
    setBagState("held");
    setCardStates((states) => ({ ...states, [cardKey]: "added" }));
    setCartCount((count) => count + 1);
    setToast({ type: "success", title: "Added to bag", message: `${product.name} is being confirmed now.` });
    try {
      await addToBag(product.listingId);
      setToast({ type: "success", title: "Added to bag", message: `${product.name} is held for 10 minutes.` });
    } catch (error) {
      const conflict = error.status === 409;
      setBagState(conflict ? "conflict" : "idle");
      setCartCount((count) => Math.max(0, count - 1));
      setCardStates((states) => ({ ...states, [cardKey]: "failed" }));
      setToast({
        type: "warning",
        title: conflict ? "Already held" : "Could not add",
        message: conflict ? "Someone already has this item in their bag." : "Please try again in a moment.",
      });
      window.setTimeout(() => {
        setCardStates((states) => (states[cardKey] === "failed" ? { ...states, [cardKey]: "idle" } : states));
      }, 2800);
    }
  };
  const page = useMemo(() => {
    if (screen.type === "browse") {
      return <BrowsePage active={active} cardStates={cardStates} category={screen.category} onAddToBag={holdProduct} onBrowse={browse} onOpenProduct={openProduct} clothingCatalog={clothingCatalog} />;
    }
    if (screen.type === "product" && screen.product) {
      return <ProductDetail active={active} cardStates={cardStates} product={screen.product} onBack={() => browse(screen.category)} onBrowse={browse} onOpenProduct={openProduct} clothingCatalog={clothingCatalog} onAddToBag={holdProduct} bagState={bagState} />;
    }
    if (screen.type === "cart") {
      return <CartPage onBrowse={browse} onOpenProduct={openProduct} onCheckout={openCheckout} refreshCartCount={setCartCount} />;
    }
    if (screen.type === "checkout") {
      return <CheckoutPage onBrowse={browse} onOrderCreated={showOrderCreated} refreshCartCount={setCartCount} />;
    }
    if (screen.type === "orderCreated") {
      return <OrderConfirmationPage order={createdOrder} onBrowse={browse} onTrack={openTracking} />;
    }
    if (screen.type === "tracking") {
      return <OrderTrackingPage initialOrder={screen.order ?? createdOrder} onBrowse={browse} />;
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
      : <ClothingPage cardStates={cardStates} onAddToBag={holdProduct} onBrowse={browse} onOpenProduct={openProduct} products={clothingCatalog} />;
  }, [active, screen, clothingCatalog, bagState, createdOrder, cardStates]);

  return (
    <main className={active === "home" ? "app home-mode" : "app fashion-mode"}>
      <Header active={active} setActive={switchWorld} cartCount={cartCount} onCart={openCart} onSeller={openSeller} onTrack={() => openTracking()} />
      {active === "clothing" ? (
        <div className="api-status">
          {apiStatus === "connected" ? "Live catalog connected" : "Prototype catalog fallback"}
        </div>
      ) : null}
      {toast ? <ToastNotice toast={toast} onClose={() => setToast(null)} /> : null}
      {page}
      <Footer active={active} />
    </main>
  );
}
