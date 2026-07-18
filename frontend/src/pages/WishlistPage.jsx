import { useEffect } from "react";
import { Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import PriceTag from "../components/PriceTag";
import { loadWishlist, removeWishlistItem } from "../features/wishlist/wishlistSlice";
import { addItemToCart } from "../features/cart/cartSlice";
import { showToast } from "../features/ui/uiSlice";

/**
 * صفحة قائمة رغبات المستخدم: عرض المنتجات المحفوظة مع إمكانية نقلها للسلة أو حذفها.
 */
export default function WishlistPage() {
  const dispatch = useDispatch();
  const { items, status } = useSelector((state) => state.wishlist);

  useEffect(() => {
    dispatch(loadWishlist());
  }, [dispatch]);

  function handleAddToCart(productId, name) {
    dispatch(addItemToCart({ productId, quantity: 1 }))
      .unwrap()
      .then(() => dispatch(showToast(`تمت إضافة "${name}" للسلة`, "success")));
  }

  if (status === "loading" && items.length === 0) {
    return <p style={{ padding: "40px 0" }}>...جاري التحميل</p>;
  }

  if (items.length === 0) {
    return (
      <div style={{ textAlign: "center", padding: "60px 0" }}>
        <p style={{ fontSize: 40 }}>♡</p>
        <h2>قائمة رغباتك فارغة</h2>
        <Link to="/products" className="btn btn-cta" style={{ marginTop: 16 }}>
          اكتشف منتجاتنا
        </Link>
      </div>
    );
  }

  return (
    <div style={{ padding: "24px 0" }}>
      <h1>قائمة رغباتي</h1>
      <div className="grid-products">
        {items.map(({ id, product }) => (
          <div key={id} className="card" style={{ padding: 14 }}>
            <Link to={`/products/${product.id}`}>
              <img
                src={product.imageUrl || undefined}
                alt={product.name}
                style={{ width: "100%", aspectRatio: "1/1", objectFit: "cover", borderRadius: "var(--radius-md)", backgroundColor: "var(--color-bg)" }}
              />
              <h3 style={{ fontSize: 15 }}>{product.name}</h3>
              <PriceTag price={product.price} discountPrice={product.discountPrice} />
            </Link>
            <div style={{ display: "flex", gap: 8, marginTop: 10 }}>
              <button className="btn btn-cta" style={{ flex: 1 }} onClick={() => handleAddToCart(product.id, product.name)}>
                أضف للسلة
              </button>
              <button className="btn-danger-text" onClick={() => dispatch(removeWishlistItem(product.id))}>
                حذف
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
