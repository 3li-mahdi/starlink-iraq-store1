import { lazy, Suspense, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import ProductCard from "../components/ProductCard";
import SkeletonProductCard from "../components/SkeletonProductCard";
import CategoryTiltCard from "../components/CategoryTiltCard";
import { fetchProducts } from "../api/productsApi";

// يُحمَّل مشهد Three.js بشكل منفصل (code-split) حتى لا يثقّل الحزمة الرئيسية بمكتبة three.js
// إلا عند الحاجة الفعلية له بالصفحة الرئيسية فقط
const HeroGlobeScene = lazy(() => import("../components/three/HeroGlobeScene"));

const CATEGORIES = [
  { icon: "📡", title: "أطباق ومعدات", description: "أطباق الاستقبال والمعدات المادية", category: "أطباق ومعدات" },
  { icon: "🌐", title: "شبكات", description: "راوترات وكيبلات لتوصيل شبكتك", category: "شبكات" },
  { icon: "⚡", title: "اشتراكات", description: "اشتراكات رقمية وتفعيل فوري", category: "اشتراكات" },
];

/**
 * الصفحة الرئيسية: مشهد Hero ثلاثي الأبعاد (كرة أرضية بأقمار صناعية)، بطاقات الفئات الرئيسية،
 * وأشهر المنتجات القادمة فعلياً من الـ Backend لتشجيع الزائر على تصفح المتجر.
 */
export default function HomePage() {
  const [products, setProducts] = useState(null);

  useEffect(() => {
    fetchProducts({}, 0)
      .then((data) => setProducts(data.content))
      .catch(() => setProducts([]));
  }, []);

  return (
    <div>
      <motion.section
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="card"
        style={{
          position: "relative",
          overflow: "hidden",
          borderRadius: "var(--radius-lg)",
          color: "var(--color-text)",
          padding: "56px 32px",
          margin: "24px 0",
          minHeight: 380,
          display: "flex",
          alignItems: "center",
        }}
      >
        <div style={{ position: "absolute", inset: 0, opacity: 0.85 }} aria-hidden>
          <Suspense fallback={null}>
            <HeroGlobeScene />
          </Suspense>
        </div>
        <div style={{ position: "relative", zIndex: 1, maxWidth: 560 }}>
          <h1 style={{ fontSize: 34, margin: "0 0 12px" }}>إنترنت Starlink بلا حدود، بين يديك في العراق</h1>
          <p style={{ color: "var(--color-text-muted)", fontSize: 16, marginBottom: 24, maxWidth: 520 }}>
            أطباق، راوترات، كيبلات أصلية، واشتراكات رقمية فورية - بأمان وسرعة توصيل تناسبك.
          </p>
          <Link to="/products" className="btn btn-cta" style={{ fontSize: 16 }}>
            تسوّق الآن
          </Link>
        </div>
      </motion.section>

      <section style={{ margin: "48px 0" }}>
        <h2 style={{ marginBottom: 20 }}>تسوّق حسب الفئة</h2>
        <div className="grid-products">
          {CATEGORIES.map((cat) => (
            <CategoryTiltCard key={cat.category} {...cat} />
          ))}
        </div>
      </section>

      <section style={{ margin: "40px 0" }}>
        <h2 style={{ marginBottom: 20 }}>الأكثر طلباً</h2>
        <div className="grid-products">
          {products === null
            ? Array.from({ length: 8 }).map((_, i) => <SkeletonProductCard key={i} />)
            : products.map((product) => <ProductCard key={product.id} product={product} />)}
        </div>
      </section>
    </div>
  );
}
