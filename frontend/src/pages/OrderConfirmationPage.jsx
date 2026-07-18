import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { fetchOrder } from "../api/ordersApi";
import { formatIqd } from "../utils/format";

const STATUS_LABELS = {
  PENDING: "قيد الانتظار",
  PAID: "تم الدفع",
  SHIPPED: "تم الشحن",
  DELIVERED: "تم التوصيل",
  CANCELLED: "ملغي",
  REFUNDED: "مسترجع",
};

/**
 * صفحة تأكيد الطلب: تعرض ملخص الطلب بعد إتمام الشراء مباشرة، وأي محتوى رقمي تم تسليمه.
 */
export default function OrderConfirmationPage() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    fetchOrder(id)
      .then(setOrder)
      .catch(() => setError(true));
  }, [id]);

  if (error) {
    return <p style={{ padding: "40px 0" }}>تعذّر العثور على هذا الطلب.</p>;
  }

  if (!order) {
    return <p style={{ padding: "40px 0" }}>...جاري التحميل</p>;
  }

  return (
    <div style={{ maxWidth: 640, margin: "32px auto", padding: "0 16px" }}>
      <div className="card" style={{ padding: 28, textAlign: "center", marginBottom: 24 }}>
        <div style={{ fontSize: 48 }}>✅</div>
        <h1>شكراً لطلبك!</h1>
        <p style={{ color: "var(--color-text-muted)" }}>
          رقم الطلب <strong>#{order.id}</strong> - الحالة: <span className="badge badge-success">{STATUS_LABELS[order.status]}</span>
        </p>
      </div>

      <div className="card" style={{ padding: 24 }}>
        <h2 style={{ marginTop: 0 }}>تفاصيل الطلب</h2>
        {order.items.map((item) => (
          <div key={item.id} style={{ marginBottom: 14 }}>
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <span>
                {item.productName} × {item.quantity}
              </span>
              <span>{formatIqd(item.priceAtPurchase * item.quantity)}</span>
            </div>
            {item.digitalContent && item.digitalContent.length > 0 && (
              <div className="card" style={{ padding: 12, marginTop: 8, backgroundColor: "var(--color-bg)" }}>
                <strong style={{ fontSize: 13 }}>محتواك الرقمي:</strong>
                <ul style={{ margin: "6px 0 0", paddingInlineStart: 18 }}>
                  {item.digitalContent.map((content, index) => (
                    <li key={index} style={{ fontFamily: "monospace", fontSize: 13 }}>
                      {content}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        ))}

        <hr style={{ border: "none", borderTop: "1px solid var(--color-border)", margin: "16px 0" }} />
        <div style={{ display: "flex", justifyContent: "space-between", fontWeight: 800, fontSize: 18 }}>
          <span>الإجمالي المدفوع</span>
          <span>{formatIqd(Number(order.totalAmount) - Number(order.discountAmount))}</span>
        </div>
      </div>

      <div style={{ textAlign: "center", marginTop: 24 }}>
        <Link to="/products" className="btn btn-outline">
          متابعة التسوق
        </Link>
      </div>
    </div>
  );
}
