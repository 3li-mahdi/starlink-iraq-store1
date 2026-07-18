import { useState } from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { loginUser } from "../features/auth/authSlice";
import { loadCart } from "../features/cart/cartSlice";
import { clearGuestSessionId } from "../utils/guestSession";

/**
 * صفحة تسجيل الدخول.
 */
export default function LoginPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const status = useSelector((state) => state.auth.status);
  const error = useSelector((state) => state.auth.error);

  const [form, setForm] = useState({ email: "", password: "" });

  function handleSubmit(event) {
    event.preventDefault();
    dispatch(loginUser(form))
      .unwrap()
      .then(() => {
        clearGuestSessionId();
        dispatch(loadCart());
        const redirectTo = location.state?.from?.pathname || "/";
        navigate(redirectTo, { replace: true });
      })
      .catch(() => {});
  }

  return (
    <div style={{ maxWidth: 420, margin: "48px auto", padding: "0 16px" }}>
      <div className="card" style={{ padding: 28 }}>
        <h1 style={{ marginTop: 0 }}>تسجيل الدخول</h1>
        <form onSubmit={handleSubmit}>
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
            <label htmlFor="password">كلمة المرور</label>
            <input
              id="password"
              type="password"
              required
              value={form.password}
              onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
            />
          </div>
          {error && <p className="field-error">{error}</p>}
          <button className="btn btn-cta" style={{ width: "100%" }} type="submit" disabled={status === "loading"}>
            {status === "loading" ? "...جارٍ الدخول" : "تسجيل الدخول"}
          </button>
        </form>
        <p style={{ marginTop: 18, fontSize: 14, textAlign: "center" }}>
          ليس لديك حساب؟ <Link to="/register">أنشئ حساباً جديداً</Link>
        </p>
      </div>
    </div>
  );
}
