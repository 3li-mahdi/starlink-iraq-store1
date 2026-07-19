import { NavLink, Outlet } from "react-router-dom";

/**
 * الهيكل العام للوحة تحكم الأدمن: قائمة جانبية للتنقّل بين المنتجات والطلبات والكوبونات.
 */
export default function AdminLayout() {
  return (
    <div className="admin-grid" style={{ padding: "24px 0" }}>
      <aside className="card" style={{ padding: 16, height: "fit-content", position: "sticky", top: 88 }}>
        <h2 style={{ fontSize: 16, marginTop: 0 }}>لوحة التحكم</h2>
        <nav style={{ display: "flex", flexDirection: "column", gap: 6 }}>
          <AdminNavLink to="/admin" label="المنتجات" end />
          <AdminNavLink to="/admin/orders" label="الطلبات" />
          <AdminNavLink to="/admin/coupons" label="الكوبونات" />
        </nav>
      </aside>
      <div>
        <Outlet />
      </div>
    </div>
  );
}

function AdminNavLink({ to, label, end }) {
  return (
    <NavLink
      to={to}
      end={end}
      style={({ isActive }) => ({
        padding: "10px 12px",
        borderRadius: "var(--radius-md)",
        fontWeight: 600,
        backgroundColor: isActive ? "var(--color-cta-bg)" : "transparent",
        color: isActive ? "var(--color-cta-text)" : "var(--color-text)",
        transition: "background-color 0.2s ease, color 0.2s ease",
      })}
    >
      {label}
    </NavLink>
  );
}
