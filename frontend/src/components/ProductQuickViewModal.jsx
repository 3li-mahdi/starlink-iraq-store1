import { lazy, Suspense, useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { Link, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { useDispatch } from "react-redux";
import PriceTag from "./PriceTag";
import StockBadge from "./StockBadge";
import { fetchProductVariants } from "../api/productsApi";
import { addItemToCart } from "../features/cart/cartSlice";
import { showToast } from "../features/ui/uiSlice";

const Product3DViewer = lazy(() => import("./three/Product3DViewer"));

const DIGITAL_DELIVERY_LABELS = {
  LICENSE_KEY: "كود تفعيل يُسلَّم فور إتمام الدفع",
  DOWNLOAD_LINK: "رابط تحميل يُسلَّم فور إتمام الدفع",
  ACCOUNT_CREDENTIALS: "بيانات حساب تُسلَّم فور إتمام الدفع",
};

/**
 * مودال معاينة سريعة لمنتج: نموذج 3D قابل للتدوير 360° بالسحب، السعر والمخزون الفعليَين،
 * آلية التسليم إذا كان المنتج رقمياً، واختيار الموديل إن وُجد أكثر من موديل لنفس المجموعة.
 * @param {{product: object, onClose: () => void}} props
 */
export default function ProductQuickViewModal({ product, onClose }) {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [quantity, setQuantity] = useState(1);
  const [variants, setVariants] = useState([]);

  useEffect(() => {
    document.body.style.overflow = "hidden";
    fetchProductVariants(product.id).then(setVariants);
    return () => {
      document.body.style.overflow = "";
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [product.id]);

  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === "Escape") {
        onClose();
      }
    }
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  const outOfStock = product.requiresShipping && product.stockQuantity === 0;
  const maxQuantity = product.requiresShipping && product.stockQuantity != null ? product.stockQuantity : 10;

  function handleAddToCart() {
    dispatch(addItemToCart({ productId: product.id, quantity }))
      .unwrap()
      .then(() => {
        dispatch(showToast(`تمت إضافة ${quantity} × "${product.name}" للسلة`, "success"));
        onClose();
      })
      .catch((message) => dispatch(showToast(message, "error")));
  }

  function handleVariantChange(event) {
    const variantId = event.target.value;
    if (variantId !== String(product.id)) {
      onClose();
      navigate(`/products/${variantId}`);
    }
  }

  return createPortal(
    <div
      role="dialog"
      aria-modal="true"
      aria-label={`معاينة سريعة: ${product.name}`}
      onClick={onClose}
      style={{
        position: "fixed",
        inset: 0,
        backgroundColor: "rgba(2, 4, 10, 0.75)",
        backdropFilter: "blur(4px)",
        zIndex: 200,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: 16,
      }}
    >
      <motion.div
        initial={{ opacity: 0, scale: 0.96, y: 12 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.96 }}
        transition={{ duration: 0.2 }}
        className="card"
        onClick={(event) => event.stopPropagation()}
        style={{ width: "100%", maxWidth: 760, maxHeight: "92vh", overflowY: "auto", padding: 0 }}
      >
        <div style={{ display: "flex", justifyContent: "flex-end", padding: "12px 12px 0" }}>
          <button className="btn-ghost" onClick={onClose} aria-label="إغلاق">
            ✕
          </button>
        </div>

        <div className="two-col-grid" style={{ padding: "0 28px 28px", gap: 28 }}>
          <div style={{ aspectRatio: "1/1", borderRadius: "var(--radius-md)", overflow: "hidden", backgroundColor: "var(--color-bg-elevated)" }}>
            <Suspense fallback={<div className="skeleton" style={{ width: "100%", height: "100%" }} />}>
              <Product3DViewer product={product} />
            </Suspense>
          </div>

          <div>
            <div style={{ color: "var(--color-text-muted)", fontSize: 13, marginBottom: 4 }}>{product.category}</div>
            <h2 style={{ margin: "0 0 10px" }}>{product.name}</h2>

            {variants.length > 1 && (
              <div className="form-field" style={{ maxWidth: 220 }}>
                <label htmlFor="quick-view-variant">الموديل</label>
                <select id="quick-view-variant" value={String(product.id)} onChange={handleVariantChange}>
                  {variants.map((variant) => (
                    <option key={variant.id} value={variant.id} disabled={!variant.isActive}>
                      {variant.variantLabel || `#${variant.id}`}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <PriceTag price={product.price} discountPrice={product.discountPrice} />

            <div style={{ margin: "12px 0" }}>
              {product.productType === "DIGITAL" ? (
                <span className="badge badge-gold">⚡ {DIGITAL_DELIVERY_LABELS[product.digitalDeliveryType] || "تسليم فوري بدون شحن"}</span>
              ) : (
                <StockBadge stockQuantity={product.stockQuantity} lowStock={product.lowStock} requiresShipping={product.requiresShipping} />
              )}
            </div>

            {product.description && (
              <p style={{ color: "var(--color-text-muted)", fontSize: 14, lineHeight: 1.8 }}>{product.description}</p>
            )}

            {!outOfStock && (
              <div style={{ display: "flex", alignItems: "center", gap: 12, margin: "14px 0" }}>
                <label htmlFor="quick-view-quantity" style={{ fontWeight: 600 }}>
                  الكمية:
                </label>
                <select
                  id="quick-view-quantity"
                  value={quantity}
                  onChange={(e) => setQuantity(Number(e.target.value))}
                  style={{ padding: "8px 12px", borderRadius: "var(--radius-md)", border: "1.5px solid var(--color-border-light)" }}
                >
                  {Array.from({ length: Math.min(maxQuantity, 10) }, (_, i) => i + 1).map((q) => (
                    <option key={q} value={q}>
                      {q}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <button className="btn btn-cta" style={{ width: "100%", fontSize: 16 }} onClick={handleAddToCart} disabled={outOfStock}>
              {outOfStock ? "نفذت الكمية" : "أضف للسلة"}
            </button>

            <Link to={`/products/${product.id}`} onClick={onClose} className="btn btn-outline" style={{ width: "100%", marginTop: 10 }}>
              عرض التفاصيل الكاملة والمراجعات
            </Link>
          </div>
        </div>
      </motion.div>
    </div>,
    document.body
  );
}
