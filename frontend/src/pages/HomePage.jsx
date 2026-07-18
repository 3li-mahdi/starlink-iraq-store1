import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import ProductCard from "../components/ProductCard";
import SkeletonProductCard from "../components/SkeletonProductCard";
import { fetchProducts } from "../api/productsApi";

/**
 * الصفحة الرئيسية: بانر ترويجي وأشهر المنتجات لتشجيع الزائر على تصفح المتجر.
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
        style={{
          background: "linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%)",
          borderRadius: "var(--radius-lg)",
          color: "#fff",
          padding: "56px 32px",
          margin: "24px 0",
          display: "flex",
          flexWrap: "wrap",
          alignItems: "center",
          gap: 24,
        }}
      >
        <div style={{ flex: "1 1 320px" }}>
          <h1 style={{ fontSize: 34, margin: "0 0 12px" }}>إنترنت Starlink بلا حدود، بين يديك في العراق</h1>
          <p style={{ color: "rgba(255,255,255,0.85)", fontSize: 16, marginBottom: 24, maxWidth: 520 }}>
            أطباق، راوترات، كيبلات أصلية، واشتراكات رقمية فورية - بأمان وسرعة توصيل تناسبك.
          </p>
          <Link to="/products" className="btn btn-cta" style={{ fontSize: 16 }}>
            تسوّق الآن
          </Link>
        </div>
        <div style={{ fontSize: 120, opacity: 0.35 }} aria-hidden>
          🛰️
        </div>
      </motion.section>

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
