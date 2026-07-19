import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { checkout, validateCoupon } from "../api/ordersApi";
import { clearCartState } from "../features/cart/cartSlice";
import { showToast } from "../features/ui/uiSlice";
import { formatIqd } from "../utils/format";
import Breadcrumbs from "../components/Breadcrumbs";

/**
 * صفحة إتمام الشراء بخطوتين فقط لتقليل نسبة التسرّب: (1) عنوان الشحن (2) مراجعة الطلب والدفع.
 */
export default function CheckoutPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { items, totalAmount } = useSelector((state) => state.cart);

  const [step, setStep] = useState(1);
  const [shippingAddress, setShippingAddress] = useState("");
  const [couponCode, setCouponCode] = useState("");
  const [couponResult, setCouponResult] = useState(null);
  const [couponLoading, setCouponLoading] = useState(false);
  const [placing, setPlacing] = useState(false);

  const needsShipping = items.some((item) => item.availableStock !== null);

  function handleApplyCoupon() {
    if (!couponCode.trim()) return;
    setCouponLoading(true);
    validateCoupon(couponCode.trim(), totalAmount)
      .then(setCouponResult)
      .catch(() => setCouponResult({ valid: false, message: "تعذّر التحقق من الكوبون" }))
      .finally(() => setCouponLoading(false));
  }

  function handlePlaceOrder() {
    setPlacing(true);
    const idempotencyKey = crypto.randomUUID();
    checkout({
      idempotencyKey,
      shippingAddress: shippingAddress || undefined,
      couponCode: couponResult?.valid ? couponCode.trim() : undefined,
    })
      .then((order) => {
        dispatch(clearCartState());
        dispatch(showToast("تم تأكيد طلبك بنجاح!", "success"));
        navigate(`/orders/${order.id}`);
      })
      .catch((error) => dispatch(showToast(error.response?.data?.error || "تعذّر إتمام عملية الشراء", "error")))
      .finally(() => setPlacing(false));
  }

  const discount = couponResult?.valid ? Number(couponResult.discountAmount) : 0;
  const finalTotal = Math.max(0, Number(totalAmount) - discount);

  return (
    <div style={{ maxWidth: 640, margin: "24px auto", padding: "0 16px" }}>
      <Breadcrumbs items={[{ label: "سلة التسوق", to: "/cart" }, { label: "إتمام الشراء" }]} />
      <div style={{ display: "flex", gap: 8, marginBottom: 24 }}>
        <StepIndicator active={step === 1} label="1. عنوان الشحن" />
        <StepIndicator active={step === 2} label="2. المراجعة والدفع" />
      </div>

      {step === 1 && (
        <div className="card" style={{ padding: 24 }}>
          <h2 style={{ marginTop: 0 }}>عنوان الشحن</h2>
          {needsShipping ? (
            <div className="form-field">
              <label htmlFor="shippingAddress">العنوان بالتفصيل (المحافظة، المنطقة، أقرب نقطة دالة)</label>
              <textarea
                id="shippingAddress"
                rows={3}
                required
                value={shippingAddress}
                onChange={(e) => setShippingAddress(e.target.value)}
              />
            </div>
          ) : (
            <p style={{ color: "var(--color-text-muted)" }}>طلبك يحتوي منتجات رقمية فقط، لا حاجة لعنوان شحن.</p>
          )}
          <button
            className="btn btn-cta"
            style={{ width: "100%" }}
            onClick={() => setStep(2)}
            disabled={needsShipping && !shippingAddress.trim()}
          >
            التالي: المراجعة والدفع
          </button>
        </div>
      )}

      {step === 2 && (
        <div className="card" style={{ padding: 24 }}>
          <h2 style={{ marginTop: 0 }}>مراجعة الطلب</h2>
          {items.map((item) => (
            <div key={item.id} style={{ display: "flex", justifyContent: "space-between", fontSize: 14, marginBottom: 8 }}>
              <span>
                {item.productName} × {item.quantity}
              </span>
              <span>{formatIqd(item.subtotal)}</span>
            </div>
          ))}

          <div style={{ display: "flex", gap: 8, margin: "16px 0" }}>
            <input
              placeholder="كود الخصم (إن وجد)"
              value={couponCode}
              onChange={(e) => setCouponCode(e.target.value)}
              style={{ flex: 1, padding: "10px 14px", borderRadius: "var(--radius-md)", border: "1.5px solid var(--color-border-light)", backgroundColor: "var(--color-surface)", color: "var(--color-text)" }}
            />
            <button className="btn btn-outline" onClick={handleApplyCoupon} disabled={couponLoading}>
              تطبيق
            </button>
          </div>
          {couponResult && (
            <p className={couponResult.valid ? "" : "field-error"}>
              {couponResult.valid ? "✓ " : "✕ "}
              {couponResult.message}
            </p>
          )}

          <hr style={{ border: "none", borderTop: "1px solid var(--color-border)", margin: "16px 0" }} />
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 4 }}>
            <span>المجموع الفرعي</span>
            <span>{formatIqd(totalAmount)}</span>
          </div>
          {discount > 0 && (
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 4 }}>
              <span>الخصم</span>
              <span>- {formatIqd(discount)}</span>
            </div>
          )}
          <div style={{ display: "flex", justifyContent: "space-between", fontWeight: 800, fontSize: 18, marginTop: 8 }}>
            <span>الإجمالي</span>
            <span>{formatIqd(finalTotal)}</span>
          </div>

          <div style={{ display: "flex", gap: 10, marginTop: 20 }}>
            <button className="btn btn-outline" onClick={() => setStep(1)}>
              رجوع
            </button>
            <button className="btn btn-success" style={{ flex: 1, fontSize: 16 }} onClick={handlePlaceOrder} disabled={placing}>
              {placing ? "...جارٍ تأكيد الطلب" : "تأكيد الطلب والدفع"}
            </button>
          </div>
          <div style={{ display: "flex", gap: 10, flexWrap: "wrap", marginTop: 16, fontSize: 12, color: "var(--color-text-muted)" }}>
            <span>🛡️ ضمان</span>
            <span>🔒 دفع آمن</span>
            <span>↩️ إرجاع خلال 7 أيام</span>
          </div>
        </div>
      )}
    </div>
  );
}

function StepIndicator({ active, label }) {
  return (
    <div
      style={{
        flex: 1,
        padding: "10px 14px",
        borderRadius: "var(--radius-md)",
        textAlign: "center",
        fontWeight: 700,
        fontSize: 13,
        backgroundColor: active ? "var(--color-cta-bg)" : "var(--color-surface-alt)",
        color: active ? "var(--color-cta-text)" : "var(--color-text-muted)",
        transition: "background-color 0.2s ease, color 0.2s ease",
      }}
    >
      {label}
    </div>
  );
}
