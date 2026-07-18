import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { registerUser } from "../features/auth/authSlice";
import { loadCart } from "../features/cart/cartSlice";
import { clearGuestSessionId } from "../utils/guestSession";

/**
 * صفحة إنشاء حساب جديد.
 */
export default function RegisterPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const status = useSelector((state) => state.auth.status);
  const error = useSelector((state) => state.auth.error);

  const [form, setForm] = useState({ fullName: "", email: "", password: "", phoneNumber: "" });
  const [localError, setLocalError] = useState(null);

  function handleSubmit(event) {
    event.preventDefault();
    setLocalError(null);

    if (form.password.length < 8 || !/[A-Z]/.test(form.password) || !/[a-z]/.test(form.password) || !/\d/.test(form.password)) {
      setLocalError("كلمة المرور يجب أن تكون 8 أحرف على الأقل وتحتوي حرفاً كبيراً وصغيراً ورقماً");
      return;
    }

    dispatch(registerUser(form))
      .unwrap()
      .then(() => {
        clearGuestSessionId();
        dispatch(loadCart());
        navigate("/");
      })
      .catch(() => {});
  }

  return (
    <div style={{ maxWidth: 460, margin: "48px auto", padding: "0 16px" }}>
      <div className="card" style={{ padding: 28 }}>
        <h1 style={{ marginTop: 0 }}>إنشاء حساب جديد</h1>
        <form onSubmit={handleSubmit}>
          <div className="form-field">
            <label htmlFor="fullName">الاسم الكامل</label>
            <input
              id="fullName"
              required
              minLength={2}
              value={form.fullName}
              onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))}
            />
          </div>
          <div className="form-field">
            <label htmlFor="email">البريد الإلكتروني</label>
            <input
              id="email"
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
            />
          </div>
          <div className="form-field">
            <label htmlFor="phoneNumber">رقم الهاتف (اختياري)</label>
            <input
              id="phoneNumber"
              value={form.phoneNumber}
              onChange={(e) => setForm((f) => ({ ...f, phoneNumber: e.target.value }))}
            />
          </div>
          <div className="form-field">
            <label htmlFor="password">كلمة المرور</label>
            <input
              id="password"
              type="password"
              required
              value={form.password}
              onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
            />
          </div>
          {(localError || error) && <p className="field-error">{localError || error}</p>}
          <button className="btn btn-cta" style={{ width: "100%" }} type="submit" disabled={status === "loading"}>
            {status === "loading" ? "...جارٍ الإنشاء" : "إنشاء الحساب"}
          </button>
        </form>
        <p style={{ marginTop: 18, fontSize: 14, textAlign: "center" }}>
          لديك حساب بالفعل؟ <Link to="/login">سجّل الدخول</Link>
        </p>
      </div>
    </div>
  );
}
