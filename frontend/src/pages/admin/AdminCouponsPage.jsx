import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { createCoupon, fetchAllCoupons, setCouponActive } from "../../api/adminApi";
import { showToast } from "../../features/ui/uiSlice";

const EMPTY_FORM = { code: "", discountType: "PERCENTAGE", discountValue: "", minOrderAmount: "", maxUses: "", expiresAt: "" };

/**
 * صفحة إدارة كوبونات الخصم: إنشاء كوبونات جديدة وتفعيل/تعطيل الموجودة.
 */
export default function AdminCouponsPage() {
  const dispatch = useDispatch();
  const [coupons, setCoupons] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  function reload() {
    fetchAllCoupons().then(setCoupons);
  }

  useEffect(reload, []);

  function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    createCoupon({
      ...form,
      discountValue: Number(form.discountValue),
      minOrderAmount: form.minOrderAmount === "" ? 0 : Number(form.minOrderAmount),
      maxUses: form.maxUses === "" ? null : Number(form.maxUses),
      expiresAt: form.expiresAt ? new Date(form.expiresAt).toISOString() : null,
      isActive: true,
    })
      .then(() => {
        dispatch(showToast("تم إنشاء الكوبون", "success"));
        setForm(EMPTY_FORM);
        reload();
      })
      .catch((error) => dispatch(showToast(error.response?.data?.error || "حدث خطأ", "error")))
      .finally(() => setSaving(false));
  }

  function handleToggle(coupon) {
    setCouponActive(coupon.id, !coupon.isActive).then(() => reload());
  }

  return (
    <div>
      <h1>إدارة الكوبونات</h1>

      <form onSubmit={handleSubmit} className="card admin-form-grid" style={{ padding: 20, marginBottom: 24 }}>
        <div className="form-field">
          <label>الكود</label>
          <input required value={form.code} onChange={(e) => setForm((f) => ({ ...f, code: e.target.value.toUpperCase() }))} />
        </div>
        <div className="form-field">
          <label>نوع الخصم</label>
          <select value={form.discountType} onChange={(e) => setForm((f) => ({ ...f, discountType: e.target.value }))}>
            <option value="PERCENTAGE">نسبة مئوية %</option>
            <option value="FIXED">مبلغ ثابت</option>
          </select>
        </div>
        <div className="form-field">
          <label>قيمة الخصم</label>
          <input type="number" required min="0" step="0.01" value={form.discountValue} onChange={(e) => setForm((f) => ({ ...f, discountValue: e.target.value }))} />
        </div>
        <div className="form-field">
          <label>الحد الأدنى للطلب</label>
          <input type="number" min="0" value={form.minOrderAmount} onChange={(e) => setForm((f) => ({ ...f, minOrderAmount: e.target.value }))} />
        </div>
        <div className="form-field">
          <label>أقصى عدد استخدام (اختياري)</label>
          <input type="number" min="1" value={form.maxUses} onChange={(e) => setForm((f) => ({ ...f, maxUses: e.target.value }))} />
        </div>
        <div className="form-field">
          <label>تاريخ الانتهاء (اختياري)</label>
          <input type="date" value={form.expiresAt} onChange={(e) => setForm((f) => ({ ...f, expiresAt: e.target.value }))} />
        </div>
        <div style={{ gridColumn: "1 / -1" }}>
          <button className="btn btn-cta" type="submit" disabled={saving}>
            إنشاء الكوبون
          </button>
        </div>
      </form>

      <div className="admin-table-wrap">
      <table style={{ width: "100%", borderCollapse: "collapse" }}>
        <thead>
          <tr style={{ textAlign: "start", borderBottom: "2px solid var(--color-border)" }}>
            <th style={{ padding: 8 }}>الكود</th>
            <th style={{ padding: 8 }}>الخصم</th>
            <th style={{ padding: 8 }}>الاستخدام</th>
            <th style={{ padding: 8 }}>الحالة</th>
          </tr>
        </thead>
        <tbody>
          {coupons.map((coupon) => (
            <tr key={coupon.id} style={{ borderBottom: "1px solid var(--color-border)" }}>
              <td style={{ padding: 8 }}>{coupon.code}</td>
              <td style={{ padding: 8 }}>
                {coupon.discountValue} {coupon.discountType === "PERCENTAGE" ? "%" : "د.ع"}
              </td>
              <td style={{ padding: 8 }}>
                {coupon.currentUses} / {coupon.maxUses ?? "∞"}
              </td>
              <td style={{ padding: 8 }}>
                <button className={coupon.isActive ? "badge badge-success" : "badge badge-muted"} onClick={() => handleToggle(coupon)} style={{ border: "none" }}>
                  {coupon.isActive ? "مفعّل" : "معطّل"}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      </div>
    </div>
  );
}
