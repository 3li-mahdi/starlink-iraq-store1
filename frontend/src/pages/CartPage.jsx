import { useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { motion } from "framer-motion";
import PriceTag from "../components/PriceTag";
import { loadCart, removeItemFromCart, updateCartItemQuantity } from "../features/cart/cartSlice";
import { showToast } from "../features/ui/uiSlice";
import { formatIqd } from "../utils/format";

/**
 * صفحة سلة التسوق: تحديث فوري للكمية والسعر الإجمالي، وحذف العناصر، مع الانتقال لإتمام الشراء.
 */
export default function CartPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { items, totalAmount, status } = useSelector((state) => state.cart);
  const user = useSelector((state) => state.auth.user);

  useEffect(() => {
    dispatch(loadCart());
  }, [dispatch]);

  function handleQuantityChange(itemId, quantity) {
    if (quantity < 1) return;
    dispatch(updateCartItemQuantity({ itemId, quantity }))
      .unwrap()
      .catch((message) => dispatch(showToast(message, "error")));
  }

  function handleRemove(itemId) {
    dispatch(removeItemFromCart(itemId));
  }

  function handleCheckoutClick() {
    if (!user) {
      dispatch(showToast("سجّل الدخول لإتمام عملية الشراء", "info"));
      navigate("/login", { state: { from: { pathname: "/checkout" } } });
      return;
    }
    navigate("/checkout");
  }

  if (status === "loading" && items.length === 0) {
    return <p style={{ padding: "40px 0" }}>...جاري تحميل السلة</p>;
  }

  if (items.length === 0) {
    return (
      <div style={{ textAlign: "center", padding: "60px 0" }}>
        <p style={{ fontSize: 40, marginBottom: 8 }}>🛒</p>
        <h2>سلتك فارغة</h2>
        <Link to="/products" className="btn btn-cta" style={{ marginTop: 16 }}>
          تصفّح المنتجات
        </Link>
      </div>
    );
  }

  return (
    <div style={{ padding: "24px 0", display: "grid", gridTemplateColumns: "2fr 1fr", gap: 32, alignItems: "start" }}>
      <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
        <h1>سلة التسوق</h1>
        {items.map((item) => (
          <motion.div
            key={item.id}
            layout
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="card"
            style={{ padding: 14, display: "flex", alignItems: "center", gap: 14 }}
          >
            <img
              src={item.productImageUrl || undefined}
              alt={item.productName}
              style={{ width: 72, height: 72, objectFit: "cover", borderRadius: "var(--radius-md)", backgroundColor: "var(--color-bg)" }}
            />
            <div style={{ flex: 1 }}>
              <strong>{item.productName}</strong>
              <div style={{ marginTop: 6 }}>
                <PriceTag price={item.unitPrice} />
              </div>
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <button className="btn btn-outline" style={{ padding: "4px 12px" }} onClick={() => handleQuantityChange(item.id, item.quantity - 1)}>
                −
              </button>
              <span style={{ minWidth: 20, textAlign: "center" }}>{item.quantity}</span>
              <button
                className="btn btn-outline"
                style={{ padding: "4px 12px" }}
                onClick={() => handleQuantityChange(item.id, item.quantity + 1)}
                disabled={item.availableStock != null && item.quantity >= item.availableStock}
              >
                +
              </button>
            </div>
            <strong style={{ minWidth: 100, textAlign: "left" }}>{formatIqd(item.subtotal)}</strong>
            <button className="btn-danger-text" onClick={() => handleRemove(item.id)} aria-label="حذف">
              ✕
            </button>
          </motion.div>
        ))}
      </div>

      <div className="card" style={{ padding: 20, position: "sticky", top: 88 }}>
        <h2 style={{ marginTop: 0 }}>ملخص الطلب</h2>
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
          <span>المجموع الفرعي</span>
          <strong>{formatIqd(totalAmount)}</strong>
        </div>
        <p style={{ fontSize: 13, color: "var(--color-text-muted)" }}>سيتم احتساب الشحن والخصومات بخطوة الدفع.</p>
        <button className="btn btn-success" style={{ width: "100%", fontSize: 16, marginTop: 12 }} onClick={handleCheckoutClick}>
          إتمام الشراء
        </button>
        <div style={{ display: "flex", gap: 10, flexWrap: "wrap", marginTop: 16, fontSize: 12, color: "var(--color-text-muted)" }}>
          <span>🛡️ ضمان</span>
          <span>🔒 دفع آمن</span>
          <span>↩️ إرجاع خلال 7 أيام</span>
        </div>
      </div>
    </div>
  );
}
