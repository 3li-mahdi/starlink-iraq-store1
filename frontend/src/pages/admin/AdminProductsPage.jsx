import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { fetchProducts } from "../../api/productsApi";
import { createProduct, updateProduct, deleteProduct } from "../../api/adminApi";
import { showToast } from "../../features/ui/uiSlice";

const EMPTY_FORM = {
  name: "",
  description: "",
  price: "",
  discountPrice: "",
  imageUrl: "",
  productType: "PHYSICAL",
  stockQuantity: "",
  requiresShipping: true,
  digitalDeliveryType: "",
  category: "",
  isActive: true,
};

/**
 * صفحة إدارة المنتجات: إضافة، تعديل، وحذف (تعطيل) المنتجات من لوحة تحكم الأدمن.
 */
export default function AdminProductsPage() {
  const dispatch = useDispatch();
  const [products, setProducts] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  function reload() {
    fetchProducts({}, 0).then((data) => setProducts(data.content));
  }

  useEffect(reload, []);

  function startEdit(product) {
    setEditingId(product.id);
    setForm({
      ...product,
      price: product.price,
      discountPrice: product.discountPrice ?? "",
      stockQuantity: product.stockQuantity ?? "",
      digitalDeliveryType: product.digitalDeliveryType ?? "",
    });
  }

  function resetForm() {
    setEditingId(null);
    setForm(EMPTY_FORM);
  }

  function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    const payload = {
      ...form,
      price: Number(form.price),
      discountPrice: form.discountPrice === "" ? null : Number(form.discountPrice),
      stockQuantity: form.stockQuantity === "" ? null : Number(form.stockQuantity),
      digitalDeliveryType: form.digitalDeliveryType || null,
    };

    const request = editingId ? updateProduct(editingId, payload) : createProduct(payload);
    request
      .then(() => {
        dispatch(showToast(editingId ? "تم تعديل المنتج" : "تم إضافة المنتج", "success"));
        resetForm();
        reload();
      })
      .catch((error) => dispatch(showToast(error.response?.data?.error || "حدث خطأ", "error")))
      .finally(() => setSaving(false));
  }

  function handleDelete(id) {
    deleteProduct(id).then(() => {
      dispatch(showToast("تم حذف المنتج", "success"));
      reload();
    });
  }

  return (
    <div>
      <h1>إدارة المنتجات</h1>

      <form onSubmit={handleSubmit} className="card admin-form-grid" style={{ padding: 20, marginBottom: 24 }}>
        <div className="form-field">
          <label>اسم المنتج</label>
          <input required value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} />
        </div>
        <div className="form-field">
          <label>الفئة</label>
          <input required value={form.category} onChange={(e) => setForm((f) => ({ ...f, category: e.target.value }))} />
        </div>
        <div className="form-field" style={{ gridColumn: "1 / -1" }}>
          <label>الوصف</label>
          <textarea rows={2} value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} />
        </div>
        <div className="form-field">
          <label>السعر</label>
          <input type="number" required min="0" step="0.01" value={form.price} onChange={(e) => setForm((f) => ({ ...f, price: e.target.value }))} />
        </div>
        <div className="form-field">
          <label>سعر الخصم (اختياري)</label>
          <input type="number" min="0" step="0.01" value={form.discountPrice} onChange={(e) => setForm((f) => ({ ...f, discountPrice: e.target.value }))} />
        </div>
        <div className="form-field">
          <label>رابط الصورة</label>
          <input value={form.imageUrl} onChange={(e) => setForm((f) => ({ ...f, imageUrl: e.target.value }))} />
        </div>
        <div className="form-field">
          <label>نوع المنتج</label>
          <select
            value={form.productType}
            onChange={(e) => {
              const productType = e.target.value;
              setForm((f) => ({ ...f, productType, requiresShipping: productType === "PHYSICAL" }));
            }}
          >
            <option value="PHYSICAL">مادي</option>
            <option value="DIGITAL">رقمي</option>
          </select>
        </div>
        {form.productType === "PHYSICAL" ? (
          <div className="form-field">
            <label>الكمية بالمخزون</label>
            <input type="number" min="0" value={form.stockQuantity} onChange={(e) => setForm((f) => ({ ...f, stockQuantity: e.target.value }))} />
          </div>
        ) : (
          <div className="form-field">
            <label>طريقة التسليم الرقمي</label>
            <select value={form.digitalDeliveryType} onChange={(e) => setForm((f) => ({ ...f, digitalDeliveryType: e.target.value }))}>
              <option value="">اختر</option>
              <option value="LICENSE_KEY">كود تفعيل</option>
              <option value="DOWNLOAD_LINK">رابط تحميل</option>
              <option value="ACCOUNT_CREDENTIALS">بيانات حساب</option>
            </select>
          </div>
        )}
        <div style={{ display: "flex", gap: 10, gridColumn: "1 / -1" }}>
          <button className="btn btn-cta" type="submit" disabled={saving}>
            {editingId ? "حفظ التعديلات" : "إضافة المنتج"}
          </button>
          {editingId && (
            <button type="button" className="btn btn-outline" onClick={resetForm}>
              إلغاء
            </button>
          )}
        </div>
      </form>

      <div className="admin-table-wrap">
      <table style={{ width: "100%", borderCollapse: "collapse" }}>
        <thead>
          <tr style={{ textAlign: "start", borderBottom: "2px solid var(--color-border)" }}>
            <th style={{ padding: 8 }}>الاسم</th>
            <th style={{ padding: 8 }}>الفئة</th>
            <th style={{ padding: 8 }}>السعر</th>
            <th style={{ padding: 8 }}>المخزون</th>
            <th style={{ padding: 8 }}></th>
          </tr>
        </thead>
        <tbody>
          {products.map((product) => (
            <tr key={product.id} style={{ borderBottom: "1px solid var(--color-border)" }}>
              <td style={{ padding: 8 }}>{product.name}</td>
              <td style={{ padding: 8 }}>{product.category}</td>
              <td style={{ padding: 8 }}>{product.price}</td>
              <td style={{ padding: 8 }}>{product.stockQuantity ?? "—"}</td>
              <td style={{ padding: 8, display: "flex", gap: 8 }}>
                <button className="btn btn-outline" style={{ padding: "4px 10px" }} onClick={() => startEdit(product)}>
                  تعديل
                </button>
                <button className="btn-danger-text" onClick={() => handleDelete(product.id)}>
                  حذف
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
