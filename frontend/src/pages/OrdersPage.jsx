import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import { fetchUserOrders } from "../api/ordersApi";
import Pagination from "../components/Pagination";
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
 * صفحة "طلباتي": تعرض كل الطلبات السابقة للمستخدم الحالي.
 */
export default function OrdersPage() {
  const user = useSelector((state) => state.auth.user);
  const [result, setResult] = useState(null);
  const [page, setPage] = useState(0);

  useEffect(() => {
    if (user) {
      fetchUserOrders(user.id, page).then(setResult);
    }
  }, [user, page]);

  if (!result) {
    return <p style={{ padding: "40px 0" }}>...جاري التحميل</p>;
  }

  return (
    <div style={{ padding: "24px 0" }}>
      <h1>طلباتي</h1>
      {result.content.length === 0 ? (
        <p style={{ color: "var(--color-text-muted)" }}>لا توجد طلبات سابقة بعد.</p>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          {result.content.map((order) => (
            <Link key={order.id} to={`/orders/${order.id}`} className="card" style={{ padding: 16, display: "flex", justifyContent: "space-between" }}>
              <span>طلب #{order.id}</span>
              <span className="badge badge-muted">{STATUS_LABELS[order.status]}</span>
              <strong>{formatIqd(order.totalAmount)}</strong>
            </Link>
          ))}
        </div>
      )}
      <Pagination page={result.page} totalPages={result.totalPages} onChange={setPage} />
    </div>
  );
}
