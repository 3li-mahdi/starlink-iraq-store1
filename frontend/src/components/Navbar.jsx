import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { logoutUser } from "../features/auth/authSlice";
import { selectCartItemCount } from "../features/cart/cartSlice";
import "./Navbar.css";

/**
 * شريط التنقل العلوي: شعار المتجر، شريط بحث، روابط التصفح، أيقونة السلة مع عداد،
 * وقائمة حساب المستخدم. يبقى ثابتاً أعلى الصفحة عند التمرير (sticky)، ويتحوّل لقائمة
 * موبايل قابلة للطي على الشاشات الصغيرة.
 */
export default function Navbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector((state) => state.auth.user);
  const cartCount = useSelector(selectCartItemCount);

  const [mobileOpen, setMobileOpen] = useState(false);
  const [searchValue, setSearchValue] = useState("");

  function handleLogout() {
    dispatch(logoutUser());
    navigate("/");
    setMobileOpen(false);
  }

  function handleSearchSubmit(event) {
    event.preventDefault();
    navigate(`/products?search=${encodeURIComponent(searchValue.trim())}`);
    setMobileOpen(false);
  }

  function closeMobile() {
    setMobileOpen(false);
  }

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <NavLink to="/" end className="navbar-brand" onClick={closeMobile}>
          <span className="navbar-brand-badge">S</span>
          متجر Starlink العراق
        </NavLink>

        <form className="navbar-search" onSubmit={handleSearchSubmit} role="search">
          <input
            type="search"
            placeholder="ابحث عن منتج..."
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            aria-label="بحث عن منتج"
          />
          <button type="submit" className="icon-btn" aria-label="بحث">
            🔍
          </button>
        </form>

        <nav className="navbar-links">
          <NavLink to="/" end onClick={closeMobile}>
            الرئيسية
          </NavLink>
          <NavLink to="/products" onClick={closeMobile}>
            المتجر
          </NavLink>
          {user && (
            <NavLink to="/wishlist" onClick={closeMobile}>
              المفضلة
            </NavLink>
          )}
          {user && (
            <NavLink to="/orders" onClick={closeMobile}>
              طلباتي
            </NavLink>
          )}
          <NavLink to="/help" onClick={closeMobile}>
            الدعم
          </NavLink>
          {user?.role === "ADMIN" && (
            <NavLink to="/admin" onClick={closeMobile}>
              لوحة التحكم
            </NavLink>
          )}
        </nav>

        <div className="navbar-actions">
          <NavLink to="/cart" className="navbar-cart icon-btn" aria-label="سلة التسوق" onClick={closeMobile}>
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

          <button
            className="navbar-burger"
            aria-label="فتح القائمة"
            aria-expanded={mobileOpen}
            onClick={() => setMobileOpen((open) => !open)}
          >
            {mobileOpen ? "✕" : "☰"}
          </button>
        </div>
      </div>

      {mobileOpen && (
        <div className="navbar-mobile-panel">
          <form className="navbar-search navbar-search-mobile" onSubmit={handleSearchSubmit} role="search">
            <input
              type="search"
              placeholder="ابحث عن منتج..."
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              aria-label="بحث عن منتج"
            />
            <button type="submit" className="icon-btn" aria-label="بحث">
              🔍
            </button>
          </form>
          <NavLink to="/" end onClick={closeMobile}>
            الرئيسية
          </NavLink>
          <NavLink to="/products" onClick={closeMobile}>
            المتجر
          </NavLink>
          {user && (
            <NavLink to="/wishlist" onClick={closeMobile}>
              المفضلة
            </NavLink>
          )}
          {user && (
            <NavLink to="/orders" onClick={closeMobile}>
              طلباتي
            </NavLink>
          )}
          <NavLink to="/help" onClick={closeMobile}>
            الدعم
          </NavLink>
          <NavLink to="/contact" onClick={closeMobile}>
            تواصل معنا
          </NavLink>
          {user?.role === "ADMIN" && (
            <NavLink to="/admin" onClick={closeMobile}>
              لوحة التحكم
            </NavLink>
          )}
        </div>
      )}
    </header>
  );
}
