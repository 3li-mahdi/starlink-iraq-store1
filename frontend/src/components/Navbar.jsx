import { NavLink, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { logoutUser } from "../features/auth/authSlice";
import { selectCartItemCount } from "../features/cart/cartSlice";
import "./Navbar.css";

/**
 * شريط التنقل العلوي: شعار المتجر، روابط التصفح، أيقونة السلة مع عداد، وقائمة الحساب.
 */
export default function Navbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector((state) => state.auth.user);
  const cartCount = useSelector(selectCartItemCount);

  function handleLogout() {
    dispatch(logoutUser());
    navigate("/");
  }

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <NavLink to="/" className="navbar-brand">
          <span className="navbar-brand-badge">S</span>
          متجر Starlink العراق
        </NavLink>

        <nav className="navbar-links">
          <NavLink to="/" end>
            الرئيسية
          </NavLink>
          <NavLink to="/products">المتجر</NavLink>
          {user && <NavLink to="/wishlist">المفضلة</NavLink>}
          {user && <NavLink to="/orders">طلباتي</NavLink>}
          {user?.role === "ADMIN" && <NavLink to="/admin">لوحة التحكم</NavLink>}
        </nav>

        <div className="navbar-actions">
          <NavLink to="/cart" className="navbar-cart" aria-label="سلة التسوق">
            🛒
            {cartCount > 0 && <span className="navbar-cart-badge">{cartCount}</span>}
          </NavLink>

          {user ? (
            <div className="navbar-user">
              <span>مرحباً، {user.fullName.split(" ")[0]}</span>
              <button className="btn btn-ghost" onClick={handleLogout}>
                تسجيل الخروج
              </button>
            </div>
          ) : (
            <NavLink to="/login" className="btn btn-outline">
              تسجيل الدخول
            </NavLink>
          )}
        </div>
      </div>
    </header>
  );
}
