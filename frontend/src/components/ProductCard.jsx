import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { useDispatch, useSelector } from "react-redux";
import PriceTag from "./PriceTag";
import StockBadge from "./StockBadge";
import StarRating from "./StarRating";
import { addItemToCart } from "../features/cart/cartSlice";
import { addWishlistItem, removeWishlistItem, selectIsInWishlist } from "../features/wishlist/wishlistSlice";
import { showToast } from "../features/ui/uiSlice";

/**
 * بطاقة عرض منتج بقوائم المنتجات: صورة، اسم، سعر، تقييم، وزر إضافة سريعة للسلة.
 */
export default function ProductCard({ product }) {
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);
  const isWishlisted = useSelector(selectIsInWishlist(product.id));
  const outOfStock = product.requiresShipping && product.stockQuantity === 0;

  function handleAddToCart(event) {
    event.preventDefault();
    dispatch(addItemToCart({ productId: product.id, quantity: 1 }))
      .unwrap()
      .then(() => dispatch(showToast(`تمت إضافة "${product.name}" للسلة`, "success")))
      .catch((message) => dispatch(showToast(message, "error")));
  }

  function handleToggleWishlist(event) {
    event.preventDefault();
    if (!user) {
      dispatch(showToast("سجّل الدخول لإضافة منتجات لقائمة رغباتك", "info"));
      return;
    }
    if (isWishlisted) {
      dispatch(removeWishlistItem(product.id));
    } else {
      dispatch(addWishlistItem(product.id));
    }
  }

  return (
    <motion.div whileHover={{ y: -4 }} transition={{ duration: 0.15 }} className="card" style={{ overflow: "hidden" }}>
      <Link to={`/products/${product.id}`}>
        <div style={{ position: "relative", aspectRatio: "1/1", backgroundColor: "var(--color-bg)" }}>
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} loading="lazy" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
          ) : (
            <div style={{ width: "100%", height: "100%", display: "flex", alignItems: "center", justifyContent: "center", color: "var(--color-text-muted)" }}>
              📡
            </div>
          )}
          {product.discountPrice != null && product.discountPrice < product.price && (
            <span className="badge badge-danger" style={{ position: "absolute", top: 10, insetInlineStart: 10 }}>
              خصم
            </span>
          )}
          <button
            onClick={handleToggleWishlist}
            aria-label="إضافة لقائمة الرغبات"
            style={{
              position: "absolute",
              top: 10,
              insetInlineEnd: 10,
              border: "none",
              background: "rgba(255,255,255,0.9)",
              borderRadius: "50%",
              width: 34,
              height: 34,
              fontSize: 16,
              color: isWishlisted ? "var(--color-danger)" : "var(--color-text-muted)",
            }}
          >
            {isWishlisted ? "♥" : "♡"}
          </button>
        </div>

        <div style={{ padding: 14 }}>
          <div style={{ fontSize: 12, color: "var(--color-text-muted)", marginBottom: 4 }}>{product.category}</div>
          <h3 style={{ fontSize: 15, margin: "0 0 8px", minHeight: 40 }}>{product.name}</h3>
          <div style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 8 }}>
            <StarRating value={product.averageRating} size={13} />
            <span style={{ fontSize: 12, color: "var(--color-text-muted)" }}>({product.averageRating?.toFixed?.(1) ?? "0.0"})</span>
          </div>
          <PriceTag price={product.price} discountPrice={product.discountPrice} />
          <div style={{ margin: "8px 0" }}>
            <StockBadge stockQuantity={product.stockQuantity} lowStock={product.lowStock} requiresShipping={product.requiresShipping} />
          </div>
        </div>
      </Link>
      <div style={{ padding: "0 14px 14px" }}>
        <button className="btn btn-cta" style={{ width: "100%" }} onClick={handleAddToCart} disabled={outOfStock}>
          {outOfStock ? "نفذت الكمية" : "أضف للسلة"}
        </button>
      </div>
    </motion.div>
  );
}
