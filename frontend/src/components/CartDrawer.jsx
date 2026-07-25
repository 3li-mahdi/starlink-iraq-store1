import { useEffect } from "react";
import { createPortal } from "react-dom";
import { Link, useNavigate } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import { useDispatch, useSelector } from "react-redux";
import PriceTag from "./PriceTag";
import { loadCart, removeItemFromCart, updateCartItemQuantity } from "../features/cart/cartSlice";
import { closeCartDrawer, openAuthModal, showToast } from "../features/ui/uiSlice";
import { formatIqd } from "../utils/format";

/**
 * سلة تسوق جانبية تنزلق من الجانب، تعرض حالة السلة الفعلية القادمة من الـ Backend مباشرة
 * (وليس بيانات محلية وهمية)، مع تحديث فوري للكميات والسعر الإجمالي.
 * زر "إتمام الشراء": إذا كان المستخدم غير مسجَّل دخوله، يفتح مودال تسجيل الدخول أولاً
 * بدل الانتقال مباشرة لصفحة الدفع، تماشياً مع منطق المشروع.
 */
export default function CartDrawer() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const isOpen = useSelector((state) => state.ui.isCartDrawerOpen);
  const { items, totalAmount } = useSelector((state) => state.cart);
  const user = useSelector((state) => state.auth.user);

  useEffect(() => {
    if (isOpen) {
      dispatch(loadCart());
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
    return () => {
      document.body.style.overflow = "";
    };
  }, [isOpen, dispatch]);

  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === "Escape" && isOpen) {
        dispatch(closeCartDrawer());
      }
    }
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, dispatch]);

  function handleClose() {
    dispatch(closeCartDrawer());
  }

  function handleQuantityChange(itemId, quantity) {
    if (quantity < 1) return;
    dispatch(updateCartItemQuantity({ itemId, quantity }))
      .unwrap()
      .catch((message) => dispatch(showToast(message, "error")));
  }

  function handleCheckout() {
    dispatch(closeCartDrawer());
    if (!user) {
      dispatch(openAuthModal({ redirectTo: "/checkout" }));
      return;
    }
    navigate("/checkout");
  }

  return createPortal(
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div
            key="backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={handleClose}
            style={{ position: "fixed", inset: 0, backgroundColor: "rgba(2, 4, 10, 0.7)", zIndex: 200 }}
          />
          <motion.aside
            key="drawer"
            role="dialog"
            aria-modal="true"
            aria-label="سلة التسوق"
            initial={{ x: "-100%" }}
            animate={{ x: 0 }}
            exit={{ x: "-100%" }}
            transition={{ type: "tween", duration: 0.25 }}
            className="glass"
            style={{
              position: "fixed",
              top: 0,
              bottom: 0,
              left: 0,
              width: "min(400px, 100vw)",
              zIndex: 201,
              display: "flex",
              flexDirection: "column",
              borderInlineEnd: "1px solid var(--color-border)",
            }}
          >
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: 20 }}>
              <h2 style={{ margin: 0, fontSize: 18 }}>سلة التسوق</h2>
              <button className="btn-ghost" onClick={handleClose} aria-label="إغلاق السلة">
                ✕
              </button>
            </div>

            <div style={{ flex: 1, overflowY: "auto", padding: "0 20px", display: "flex", flexDirection: "column", gap: 12 }}>
              {items.length === 0 ? (
                <div style={{ textAlign: "center", padding: "60px 0", color: "var(--color-text-muted)" }}>
                  <p style={{ fontSize: 32 }}>🛒</p>
                  <p>سلتك فارغة</p>
                  <Link to="/products" onClick={handleClose} className="btn btn-cta" style={{ marginTop: 12 }}>
                    تصفّح المنتجات
                  </Link>
                </div>
              ) : (
                items.map((item) => (
                  <div key={item.id} className="card" style={{ padding: 12, display: "flex", alignItems: "center", gap: 10 }}>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <strong style={{ fontSize: 13, display: "block", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                        {item.productName}
                      </strong>
                      <PriceTag price={item.unitPrice} />
                      <div style={{ display: "flex", alignItems: "center", gap: 6, marginTop: 6 }}>
                        <button
                          className="btn btn-outline"
                          style={{ padding: "2px 10px" }}
                          onClick={() => handleQuantityChange(item.id, item.quantity - 1)}
                        >
                          −
                        </button>
                        <span>{item.quantity}</span>
                        <button
                          className="btn btn-outline"
                          style={{ padding: "2px 10px" }}
                          onClick={() => handleQuantityChange(item.id, item.quantity + 1)}
                          disabled={item.availableStock != null && item.quantity >= item.availableStock}
                        >
                          +
                        </button>
                        <button
                          className="btn-danger-text"
                          style={{ marginInlineStart: "auto" }}
                          onClick={() => dispatch(removeItemFromCart(item.id))}
                          aria-label="حذف"
                        >
                          ✕
                        </button>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>

            {items.length > 0 && (
              <div style={{ padding: 20, borderTop: "1px solid var(--color-border)" }}>
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 12, fontWeight: 700 }}>
                  <span>الإجمالي</span>
                  <span>{formatIqd(totalAmount)}</span>
                </div>
                <button className="btn btn-success" style={{ width: "100%", fontSize: 16 }} onClick={handleCheckout}>
                  إتمام الشراء
                </button>
                <Link to="/cart" onClick={handleClose} style={{ display: "block", textAlign: "center", marginTop: 10, fontSize: 13, color: "var(--color-text-muted)" }}>
                  عرض السلة كاملة
                </Link>
              </div>
            )}
          </motion.aside>
        </>
      )}
    </AnimatePresence>,
    document.body
  );
}
