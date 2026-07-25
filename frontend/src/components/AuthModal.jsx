import { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { useDispatch, useSelector } from "react-redux";
import { loginUser, registerUser } from "../features/auth/authSlice";
import { loadCart } from "../features/cart/cartSlice";
import { closeAuthModal } from "../features/ui/uiSlice";
import { clearGuestSessionId } from "../utils/guestSession";

const EMPTY_LOGIN = { email: "", password: "" };
const EMPTY_REGISTER = { fullName: "", email: "", password: "", phoneNumber: "" };

/**
 * مودال تسجيل الدخول/إنشاء حساب، يفتح تلقائياً عند محاولة مستخدم غير مسجَّل إتمام الشراء
 * (بدل التنقّل لصفحة منفصلة)، وينقله تلقائياً للوجهة المطلوبة (مثل /checkout) بعد نجاح الدخول.
 */
export default function AuthModal() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const isOpen = useSelector((state) => state.ui.isAuthModalOpen);
  const redirectTo = useSelector((state) => state.ui.authModalRedirectTo);
  const authStatus = useSelector((state) => state.auth.status);
  const authError = useSelector((state) => state.auth.error);

  const [tab, setTab] = useState("login");
  const [loginForm, setLoginForm] = useState(EMPTY_LOGIN);
  const [registerForm, setRegisterForm] = useState(EMPTY_REGISTER);
  const [localError, setLocalError] = useState(null);

  useEffect(() => {
    if (isOpen) {
      setLoginForm(EMPTY_LOGIN);
      setRegisterForm(EMPTY_REGISTER);
      setLocalError(null);
      setTab("login");
    }
  }, [isOpen]);

  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === "Escape" && isOpen) {
        dispatch(closeAuthModal());
      }
    }
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, dispatch]);

  if (!isOpen) {
    return null;
  }

  function handleSuccess() {
    clearGuestSessionId();
    dispatch(loadCart());
    dispatch(closeAuthModal());
    if (redirectTo) {
      navigate(redirectTo);
    }
  }

  function handleLoginSubmit(event) {
    event.preventDefault();
    dispatch(loginUser(loginForm)).unwrap().then(handleSuccess).catch(() => {});
  }

  function handleRegisterSubmit(event) {
    event.preventDefault();
    setLocalError(null);
    const { password } = registerForm;
    if (password.length < 8 || !/[A-Z]/.test(password) || !/[a-z]/.test(password) || !/\d/.test(password)) {
      setLocalError("كلمة المرور يجب أن تكون 8 أحرف على الأقل وتحتوي حرفاً كبيراً وصغيراً ورقماً");
      return;
    }
    dispatch(registerUser(registerForm)).unwrap().then(handleSuccess).catch(() => {});
  }

  return createPortal(
    <div
      role="dialog"
      aria-modal="true"
      aria-label="تسجيل الدخول أو إنشاء حساب"
      onClick={() => dispatch(closeAuthModal())}
      style={{
        position: "fixed",
        inset: 0,
        backgroundColor: "rgba(2, 4, 10, 0.75)",
        backdropFilter: "blur(4px)",
        zIndex: 300,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: 16,
      }}
    >
      <motion.div
        initial={{ opacity: 0, scale: 0.96, y: 12 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        transition={{ duration: 0.2 }}
        className="card"
        onClick={(event) => event.stopPropagation()}
        style={{ width: "100%", maxWidth: 420, padding: 28 }}
      >
        <button className="btn-ghost" onClick={() => dispatch(closeAuthModal())} aria-label="إغلاق" style={{ float: "left" }}>
          ✕
        </button>
        <h2 style={{ marginTop: 0 }}>{redirectTo ? "سجّل الدخول لإتمام الشراء" : "حسابك"}</h2>

        <div style={{ display: "flex", gap: 8, marginBottom: 20 }}>
          <button
            className={tab === "login" ? "btn btn-cta" : "btn btn-outline"}
            style={{ flex: 1 }}
            onClick={() => setTab("login")}
            type="button"
          >
            لديّ حساب
          </button>
          <button
            className={tab === "register" ? "btn btn-cta" : "btn btn-outline"}
            style={{ flex: 1 }}
            onClick={() => setTab("register")}
            type="button"
          >
            حساب جديد
          </button>
        </div>

        {tab === "login" ? (
          <form onSubmit={handleLoginSubmit}>
            <div className="form-field">
              <label htmlFor="modal-login-email">البريد الإلكتروني</label>
              <input
                id="modal-login-email"
                type="email"
                required
                value={loginForm.email}
                onChange={(e) => setLoginForm((f) => ({ ...f, email: e.target.value }))}
              />
            </div>
            <div className="form-field">
              <label htmlFor="modal-login-password">كلمة المرور</label>
              <input
                id="modal-login-password"
                type="password"
                required
                value={loginForm.password}
                onChange={(e) => setLoginForm((f) => ({ ...f, password: e.target.value }))}
              />
            </div>
            {authError && <p className="field-error">{authError}</p>}
            <button className="btn btn-cta" style={{ width: "100%" }} type="submit" disabled={authStatus === "loading"}>
              {authStatus === "loading" ? "...جارٍ الدخول" : "تسجيل الدخول"}
            </button>
          </form>
        ) : (
          <form onSubmit={handleRegisterSubmit}>
            <div className="form-field">
              <label htmlFor="modal-register-name">الاسم الكامل</label>
              <input
                id="modal-register-name"
                required
                minLength={2}
                value={registerForm.fullName}
                onChange={(e) => setRegisterForm((f) => ({ ...f, fullName: e.target.value }))}
              />
            </div>
            <div className="form-field">
              <label htmlFor="modal-register-email">البريد الإلكتروني</label>
              <input
                id="modal-register-email"
                type="email"
                required
                value={registerForm.email}
                onChange={(e) => setRegisterForm((f) => ({ ...f, email: e.target.value }))}
              />
            </div>
            <div className="form-field">
              <label htmlFor="modal-register-password">كلمة المرور</label>
              <input
                id="modal-register-password"
                type="password"
                required
                value={registerForm.password}
                onChange={(e) => setRegisterForm((f) => ({ ...f, password: e.target.value }))}
              />
            </div>
            {(localError || authError) && <p className="field-error">{localError || authError}</p>}
            <button className="btn btn-cta" style={{ width: "100%" }} type="submit" disabled={authStatus === "loading"}>
              {authStatus === "loading" ? "...جارٍ الإنشاء" : "إنشاء الحساب"}
            </button>
          </form>
        )}
      </motion.div>
    </div>,
    document.body
  );
}
