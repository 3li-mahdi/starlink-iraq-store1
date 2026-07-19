import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import PriceTag from "../components/PriceTag";
import StockBadge from "../components/StockBadge";
import StarRating from "../components/StarRating";
import ProductCard from "../components/ProductCard";
import Breadcrumbs from "../components/Breadcrumbs";
import { fetchProduct, fetchRelatedProducts, fetchProductReviews, addProductReview } from "../api/productsApi";
import { addItemToCart } from "../features/cart/cartSlice";
import { showToast } from "../features/ui/uiSlice";

/**
 * صفحة تفاصيل منتج واحد: صورة كبيرة، سعر واضح، شارات ثقة، مراجعات، ومنتجات ذات صلة.
 */
export default function ProductDetailPage() {
  const { id } = useParams();
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);

  const [product, setProduct] = useState(null);
  const [related, setRelated] = useState([]);
  const [reviews, setReviews] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [reviewForm, setReviewForm] = useState({ rating: 5, comment: "" });
  const [reviewSubmitting, setReviewSubmitting] = useState(false);

  useEffect(() => {
    setProduct(null);
    fetchProduct(id).then(setProduct);
    fetchRelatedProducts(id).then(setRelated);
    fetchProductReviews(id).then((data) => setReviews(data.content));
  }, [id]);

  function handleAddToCart() {
    dispatch(addItemToCart({ productId: product.id, quantity }))
      .unwrap()
      .then(() => dispatch(showToast(`تمت إضافة ${quantity} × "${product.name}" للسلة`, "success")))
      .catch((message) => dispatch(showToast(message, "error")));
  }

  function handleSubmitReview(event) {
    event.preventDefault();
    setReviewSubmitting(true);
    addProductReview(id, reviewForm)
      .then(() => {
        dispatch(showToast("شكراً لمراجعتك! ستظهر بعد مراجعتها من فريقنا", "success"));
        setReviewForm({ rating: 5, comment: "" });
      })
      .catch((error) => dispatch(showToast(error.response?.data?.error || "تعذّر إرسال المراجعة", "error")))
      .finally(() => setReviewSubmitting(false));
  }

  if (!product) {
    return (
      <div style={{ padding: "40px 0", display: "grid", gridTemplateColumns: "1fr 1fr", gap: 32 }}>
        <div className="skeleton" style={{ aspectRatio: "1/1" }} />
        <div>
          <div className="skeleton" style={{ height: 30, width: "70%", marginBottom: 16 }} />
          <div className="skeleton" style={{ height: 20, width: "40%" }} />
        </div>
      </div>
    );
  }

  const outOfStock = product.requiresShipping && product.stockQuantity === 0;
  const maxQuantity = product.requiresShipping && product.stockQuantity != null ? product.stockQuantity : 99;

  return (
    <div style={{ padding: "24px 0" }}>
      <Breadcrumbs items={[{ label: "المتجر", to: "/products" }, { label: product.name }]} />
      <div className="two-col-grid" style={{ marginBottom: 48 }}>
        <div className="card" style={{ aspectRatio: "1/1", overflow: "hidden" }}>
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
          ) : (
            <div style={{ width: "100%", height: "100%", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 80 }}>📡</div>
          )}
        </div>

        <div>
          <div style={{ color: "var(--color-text-muted)", fontSize: 13, marginBottom: 6 }}>{product.category}</div>
          <h1 style={{ margin: "0 0 12px" }}>{product.name}</h1>
          <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 16 }}>
            <StarRating value={product.averageRating} />
            <span style={{ color: "var(--color-text-muted)", fontSize: 14 }}>({product.averageRating?.toFixed?.(1) ?? "0.0"})</span>
          </div>

          <PriceTag price={product.price} discountPrice={product.discountPrice} />

          <div style={{ margin: "14px 0" }}>
            <StockBadge stockQuantity={product.stockQuantity} lowStock={product.lowStock} requiresShipping={product.requiresShipping} />
          </div>

          <p style={{ color: "var(--color-text)", lineHeight: 1.8 }}>{product.description}</p>

          {!outOfStock && (
            <div style={{ display: "flex", alignItems: "center", gap: 12, margin: "18px 0" }}>
              <label htmlFor="quantity" style={{ fontWeight: 600 }}>
                الكمية:
              </label>
              <select
                id="quantity"
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value))}
                style={{ padding: "8px 12px", borderRadius: "var(--radius-md)", border: "1.5px solid var(--color-border)" }}
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

          <div style={{ display: "flex", gap: 16, flexWrap: "wrap", marginTop: 18, fontSize: 13, color: "var(--color-text-muted)" }}>
            <span>🛡️ ضمان على المنتج</span>
            <span>🔒 دفع آمن 100%</span>
            <span>↩️ إرجاع خلال 7 أيام</span>
          </div>
        </div>
      </div>

      <section style={{ marginBottom: 48 }}>
        <h2>المراجعات</h2>
        {reviews === null ? (
          <p>...جاري التحميل</p>
        ) : reviews.length === 0 ? (
          <p style={{ color: "var(--color-text-muted)" }}>لا توجد مراجعات بعد. كن أول من يراجع هذا المنتج!</p>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            {reviews.map((review) => (
              <div key={review.id} className="card" style={{ padding: 16 }}>
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                  <strong>{review.userFullName}</strong>
                  <StarRating value={review.rating} size={14} />
                </div>
                <p style={{ margin: 0, color: "var(--color-text-muted)" }}>{review.comment}</p>
              </div>
            ))}
          </div>
        )}

        {user ? (
          <form onSubmit={handleSubmitReview} className="card" style={{ padding: 16, marginTop: 20, maxWidth: 480 }}>
            <h3 style={{ marginTop: 0 }}>أضف مراجعتك</h3>
            <div className="form-field">
              <label>التقييم</label>
              <StarRating value={reviewForm.rating} onChange={(rating) => setReviewForm((f) => ({ ...f, rating }))} size={22} />
            </div>
            <div className="form-field">
              <label htmlFor="comment">تعليقك</label>
              <textarea
                id="comment"
                rows={3}
                value={reviewForm.comment}
                onChange={(e) => setReviewForm((f) => ({ ...f, comment: e.target.value }))}
              />
            </div>
            <button className="btn btn-outline" type="submit" disabled={reviewSubmitting}>
              إرسال المراجعة
            </button>
          </form>
        ) : (
          <p style={{ color: "var(--color-text-muted)", marginTop: 16 }}>سجّل الدخول لإضافة مراجعتك.</p>
        )}
      </section>

      {related.length > 0 && (
        <section>
          <h2>قد يعجبك أيضاً</h2>
          <div className="grid-products">
            {related.map((item) => (
              <ProductCard key={item.id} product={item} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
