import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { fetchAllOrders, updateOrderStatus } from "../../api/adminApi";
import { showToast } from "../../features/ui/uiSlice";
import { formatIqd } from "../../utils/format";

const STATUSES = ["PENDING", "PAID", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"];
const STATUS_LABELS = {
  PENDING: "قيد الانتظار",
  PAID: "تم الدفع",
  SHIPPED: "تم الشحن",
  DELIVERED: "تم التوصيل",
  CANCELLED: "ملغي",
  REFUNDED: "مسترجع",
};

/**
 * صفحة إدارة الطلبات: استعراض كل الطلبات وتحديث حالتها (شحن، توصيل، إلغاء...).
 */
export default function AdminOrdersPage() {
  const dispatch = useDispatch();
  const [orders, setOrders] = useState([]);

  function reload() {
    fetchAllOrders(0).then((data) => setOrders(data.content));
  }

  useEffect(reload, []);

  function handleStatusChange(orderId, status) {
    updateOrderStatus(orderId, status).then(() => {
      dispatch(showToast("تم تحديث حالة الطلب", "success"));
      reload();
    });
  }

  return (
    <div>
      <h1>إدارة الطلبات</h1>
      <table style={{ width: "100%", borderCollapse: "collapse" }}>
        <thead>
          <tr style={{ textAlign: "start", borderBottom: "2px solid var(--color-border)" }}>
            <th style={{ padding: 8 }}>رقم الطلب</th>
            <th style={{ padding: 8 }}>الزبون</th>
            <th style={{ padding: 8 }}>الإجمالي</th>
            <th style={{ padding: 8 }}>الحالة</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id} style={{ borderBottom: "1px solid var(--color-border)" }}>
              <td style={{ padding: 8 }}>#{order.id}</td>
              <td style={{ padding: 8 }}>{order.userEmail}</td>
              <td style={{ padding: 8 }}>{formatIqd(order.totalAmount)}</td>
              <td style={{ padding: 8 }}>
                <select value={order.status} onChange={(e) => handleStatusChange(order.id, e.target.value)}>
                  {STATUSES.map((s) => (
                    <option key={s} value={s}>
                      {STATUS_LABELS[s]}
                    </option>
                  ))}
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
